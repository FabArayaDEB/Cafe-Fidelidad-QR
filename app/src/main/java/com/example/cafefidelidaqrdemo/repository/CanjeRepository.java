package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.database.models.Canje;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CanjeRepository {
    
    private static final String TAG = "CanjeRepository";
    private final CafeFidelidadDB database;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Canje>> canjesLiveData = new MutableLiveData<>();
    
    public CanjeRepository(Context context) {
        this.database = new CafeFidelidadDB(context);
        this.executor = Executors.newFixedThreadPool(4);
        loadCanjes();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<List<Canje>> getAllCanjes() {
        return canjesLiveData;
    }
    
    public LiveData<List<Canje>> getCanjesByCliente(int clienteId) {
        MutableLiveData<List<Canje>> canjesClienteLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Canje> canjes = database.obtenerCanjesPorCliente(clienteId);
                canjesClienteLiveData.postValue(canjes);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener canjes por cliente", e);
                errorLiveData.postValue("Error al obtener canjes: " + e.getMessage());
            }
        });
        return canjesClienteLiveData;
    }
    
    public LiveData<List<Canje>> getHistorialCanjesCliente(String clienteId) {
        MutableLiveData<List<Canje>> historialCanjesLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                if (clienteId == null || clienteId.isEmpty()) {
                    historialCanjesLiveData.postValue(null);
                    return;
                }
                int clienteIdInt = Integer.parseInt(clienteId);
                List<Canje> canjes = database.obtenerCanjesPorCliente(clienteIdInt);
                historialCanjesLiveData.postValue(canjes);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al convertir clienteId a entero: " + clienteId, e);
                errorLiveData.postValue("ID de cliente inválido");
                historialCanjesLiveData.postValue(null);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener historial de canjes por cliente", e);
                errorLiveData.postValue("Error al obtener historial de canjes: " + e.getMessage());
                historialCanjesLiveData.postValue(null);
            }
        });
        return historialCanjesLiveData;
    }
    
    public LiveData<Canje> getCanjeById(int id) {
        MutableLiveData<Canje> canjeLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Canje canje = database.obtenerCanjePorId(id);
                canjeLiveData.postValue(canje);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener canje por ID", e);
                errorLiveData.postValue("Error al obtener canje: " + e.getMessage());
            }
        });
        return canjeLiveData;
    }
    
    // Métodos CRUD
    public void insertCanje(Canje canje, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (canje == null) {
                    callback.onResult(false);
                    errorLiveData.postValue("Canje no puede ser nulo");
                    return;
                }
                
                if (canje.getClienteId() <= 0 || canje.getBeneficioId() <= 0) {
                    callback.onResult(false);
                    errorLiveData.postValue("Cliente y beneficio son requeridos");
                    return;
                }
                
                long result = database.insertarCanje(canje);
                boolean success = result != -1;
                
                if (success) {
                    loadCanjes();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al insertar canje");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar canje", e);
                errorLiveData.postValue("Error al insertar canje: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void updateCanje(Canje canje, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (canje == null || canje.getId() <= 0) {
                    callback.onResult(false);
                    errorLiveData.postValue("Canje inválido");
                    return;
                }
                
                int result = database.actualizarCanje(canje);
                boolean success = result > 0;
                
                if (success) {
                    loadCanjes();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al actualizar canje");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar canje", e);
                errorLiveData.postValue("Error al actualizar canje: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void deleteCanje(int canjeId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int result = database.deleteCanje(canjeId);
                boolean success = result > 0;
                
                if (success) {
                    loadCanjes();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al eliminar canje");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar canje", e);
                errorLiveData.postValue("Error al eliminar canje: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // Métodos privados
    private void loadCanjes() {
        executor.execute(() -> {
            try {
                List<Canje> canjes = database.obtenerTodosLosCanjes();
                canjesLiveData.postValue(canjes);
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar canjes", e);
                errorLiveData.postValue("Error al cargar canjes: " + e.getMessage());
            }
        });
    }
    
    // Interface para callbacks
    public interface OnResultCallback<T> {
        void onResult(T result);
    }
    
    // Métodos de sincronización (simplificados)
    public void refreshCanjes(OnResultCallback<Boolean> callback) {
        loadCanjes();
        callback.onResult(true);
    }
}