package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Sucursal;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SucursalRepository {
    
    private static final String TAG = "SucursalRepository";
    private final CafeFidelidadDB database;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Sucursal>> sucursalesLiveData = new MutableLiveData<>();
    
    public SucursalRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.executor = Executors.newFixedThreadPool(4);
        loadSucursales();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<List<Sucursal>> getAllSucursales() {
        return sucursalesLiveData;
    }
    
    public LiveData<Sucursal> getSucursalById(int id) {
        MutableLiveData<Sucursal> sucursalLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Sucursal sucursal = database.obtenerSucursalPorId(id);
                sucursalLiveData.postValue(sucursal);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener sucursal por ID", e);
                errorLiveData.postValue("Error al obtener sucursal: " + e.getMessage());
            }
        });
        return sucursalLiveData;
    }
    
    // Métodos CRUD
    public void insertSucursal(Sucursal sucursal, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (sucursal == null) {
                    callback.onResult(false);
                    errorLiveData.postValue("Sucursal no puede ser nula");
                    return;
                }
                
                if (sucursal.getNombre() == null || sucursal.getNombre().trim().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("El nombre de la sucursal es requerido");
                    return;
                }
                
                long result = database.insertarSucursal(sucursal);
                boolean success = result != -1;
                
                if (success) {
                    loadSucursales();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al insertar sucursal");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar sucursal", e);
                errorLiveData.postValue("Error al insertar sucursal: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void updateSucursal(Sucursal sucursal, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (sucursal == null || sucursal.getId() == null || sucursal.getId().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("Sucursal inválida");
                    return;
                }
                
                // Validar que el ID sea un número válido
                try {
                    int idValue = Integer.parseInt(sucursal.getId());
                    if (idValue <= 0) {
                        callback.onResult(false);
                        errorLiveData.postValue("ID de sucursal inválido");
                        return;
                    }
                } catch (NumberFormatException e) {
                    callback.onResult(false);
                    errorLiveData.postValue("ID de sucursal debe ser un número válido");
                    return;
                }
                
                int result = database.actualizarSucursal(sucursal);
                boolean success = result > 0;
                
                if (success) {
                    loadSucursales();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al actualizar sucursal");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar sucursal", e);
                errorLiveData.postValue("Error al actualizar sucursal: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void deleteSucursal(int sucursalId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int result = database.eliminarSucursal(sucursalId);
                boolean success = result > 0;
                
                if (success) {
                    loadSucursales();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al eliminar sucursal");
                }
                
                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar sucursal", e);
                errorLiveData.postValue("Error al eliminar sucursal: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void getSucursalById(long id, SucursalCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                Sucursal sucursal = database.obtenerSucursalPorId((int) id);
                if (sucursal != null) {
                    callback.onSuccess(sucursal);
                } else {
                    callback.onError("Sucursal no encontrada");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener sucursal por ID", e);
                callback.onError("Error al obtener sucursal: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // Métodos privados
    private void loadSucursales() {
        executor.execute(() -> {
            try {
                List<Sucursal> sucursales = database.obtenerTodasLasSucursales();
                sucursalesLiveData.postValue(sucursales);
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar sucursales", e);
                errorLiveData.postValue("Error al cargar sucursales: " + e.getMessage());
            }
        });
    }
    
    // Interface para callbacks
    public interface OnResultCallback<T> {
        void onResult(T result);
    }
    
    public interface SucursalCallback {
        void onSuccess(Sucursal sucursal);
        void onError(String error);
    }
    
    // Métodos de sincronización (simplificados)
    public void refreshSucursales(OnResultCallback<Boolean> callback) {
        loadSucursales();
        callback.onResult(true);
    }
    
    public void forceSyncSucursales() {
        loadSucursales();
    }
    
    public void clearError() {
        errorLiveData.postValue(null);
    }
    
    /**
     * Clase para representar sucursal con distancia
     */
    public static class SucursalWithDistance {
        private Sucursal sucursal;
        private double distance;
        
        public SucursalWithDistance(Sucursal sucursal, double distance) {
            this.sucursal = sucursal;
            this.distance = distance;
        }
        
        public Sucursal getSucursal() {
            return sucursal;
        }
        
        public double getDistance() {
            return distance;
        }
    }

}