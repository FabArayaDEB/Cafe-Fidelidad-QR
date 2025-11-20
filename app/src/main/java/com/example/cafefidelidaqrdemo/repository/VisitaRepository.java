package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Visita;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisitaRepository {
    
    private static final String TAG = "VisitaRepository";
    private final CafeFidelidadDB database;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Visita>> visitasLiveData = new MutableLiveData<>();
    
    public VisitaRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.executor = Executors.newFixedThreadPool(4);
        loadVisitas();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<List<Visita>> getAllVisitas() {
        return visitasLiveData;
    }
    
    public LiveData<List<Visita>> getVisitasByCliente(int clienteId) {
        MutableLiveData<List<Visita>> visitasClienteLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Visita> visitas = database.obtenerVisitasPorCliente(clienteId);
                visitasClienteLiveData.postValue(visitas);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener visitas por cliente", e);
                errorLiveData.postValue("Error al obtener visitas: " + e.getMessage());
            }
        });
        return visitasClienteLiveData;
    }
    
    public LiveData<Visita> getVisitaById(int id) {
        MutableLiveData<Visita> visitaLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Visita visita = database.obtenerVisitaPorId(id);
                visitaLiveData.postValue(visita);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener visita por ID", e);
                errorLiveData.postValue("Error al obtener visita: " + e.getMessage());
            }
        });
        return visitaLiveData;
    }
    
    // Métodos CRUD
    public void insertVisita(Visita visita, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (visita == null) {
                    callback.onResult(false);
                    errorLiveData.postValue("Visita no puede ser nula");
                    return;
                }
                
                if (visita.getUserId() == null || visita.getUserId().isEmpty() || 
                    visita.getSucursal() == null || visita.getSucursal().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("Cliente y sucursal son requeridos");
                    return;
                }
                
                long result = database.insertarVisita(visita);
                boolean success = result != -1;
                
                if (success) {
                    loadVisitas();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al insertar visita");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar visita", e);
                errorLiveData.postValue("Error al insertar visita: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void updateVisita(Visita visita, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (visita == null || visita.getId() == null || visita.getId().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("Visita inválida");
                    return;
                }
                
                int result = database.actualizarVisita(visita);
                boolean success = result > 0;
                
                if (success) {
                    loadVisitas();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al actualizar visita");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar visita", e);
                errorLiveData.postValue("Error al actualizar visita: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void deleteVisita(int visitaId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int result = database.eliminarVisita(visitaId);
                boolean success = result > 0;
                
                if (success) {
                    loadVisitas();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al eliminar visita");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar visita", e);
                errorLiveData.postValue("Error al eliminar visita: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // Métodos privados
    private void loadVisitas() {
        executor.execute(() -> {
            try {
                List<Visita> visitas = database.obtenerTodasLasVisitas();
                visitasLiveData.postValue(visitas);
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar visitas", e);
                errorLiveData.postValue("Error al cargar visitas: " + e.getMessage());
            }
        });
    }

    /**
     * Registra un sello (visita) para un cliente identificado por su QR personal.
     * Este método crea una visita con fecha actual y marca un sello ganado (compatibilidad: puntosGanados=1).
     */
    public void registrarSello(String clienteId, String sucursalId, OnResultCallback<Boolean> callback) {
        if (clienteId == null || clienteId.isEmpty() || sucursalId == null || sucursalId.isEmpty()) {
            if (callback != null) callback.onResult(false);
            errorLiveData.postValue("Cliente y sucursal son requeridos para registrar sello");
            return;
        }

        Visita visita = new Visita();
        visita.setUserId(clienteId);
        visita.setSucursal(sucursalId);
        visita.setFechaVisita(System.currentTimeMillis());
        // Compatibilidad con esquema actual: usar puntosGanados=1 como un sello
        try {
            visita.setPuntosGanados(1);
        } catch (Exception ignored) {}

        insertVisita(visita, result -> {
            if (callback != null) callback.onResult(result);
        });
    }

    // Interface para callbacks
    public interface OnResultCallback<T> {
        void onResult(T result);
    }
    
    // Métodos de sincronización (simplificados)
    public void refreshVisitas(OnResultCallback<Boolean> callback) {
        loadVisitas();
        callback.onResult(true);
    }
}