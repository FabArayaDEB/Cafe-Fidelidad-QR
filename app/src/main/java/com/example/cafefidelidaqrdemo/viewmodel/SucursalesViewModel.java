package com.example.cafefidelidaqrdemo.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.repository.SucursalRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SucursalesViewModel extends AndroidViewModel {
    
    private final SucursalRepository repository;
    private final ExecutorService executor;
    
    // LiveData para la UI
    private final MutableLiveData<List<SucursalEntity>> sucursales = new MutableLiveData<>();
    private final MutableLiveData<List<SucursalEntity>> sucursalesActivas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> locationPermissionDenied = new MutableLiveData<>(false);
    
    // Estado de ubicación del usuario
    private final MutableLiveData<Double> userLatitude = new MutableLiveData<>(); 
    private final MutableLiveData<Double> userLongitude = new MutableLiveData<>();
    
    // Estado de filtros
    private String filtroEstado = "";
    private String ordenSeleccionado = "nombre";
    
    public SucursalesViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SucursalRepository(application);
        this.executor = Executors.newFixedThreadPool(3);
        
        // Observar datos del repositorio
        observeRepositoryData();
        
        // Verificar estado de red inicial
        checkNetworkStatus();
    }
    
    private void observeRepositoryData() {
        // Observar todas las sucursales
        repository.getAllSucursales().observeForever(sucursalesList -> {
            if (sucursalesList != null) {
                sucursales.setValue(sucursalesList);
            }
        });
        
        // Observar sucursales activas
        repository.getActiveSucursales().observeForever(sucursalesActivasList -> {
            if (sucursalesActivasList != null) {
                sucursalesActivas.setValue(sucursalesActivasList);
            }
        });
        
        // Observar estado de carga
        repository.getIsLoading().observeForever(loading -> {
            if (loading != null) {
                isLoading.setValue(loading);
            }
        });
        
        // Observar errores
        repository.getError().observeForever(errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                error.setValue(errorMsg);
            }
        });
        
        // Observar estado offline
        repository.getIsOffline().observeForever(offline -> {
            if (offline != null) {
                isOffline.setValue(offline);
            }
        });
    }
    
    // Getters para LiveData
    public LiveData<List<SucursalEntity>> getSucursales() {
        return sucursales;
    }
    
    public LiveData<List<SucursalEntity>> getSucursalesActivas() {
        return sucursalesActivas;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsOffline() {
        return isOffline;
    }
    
    public LiveData<Boolean> getIsNetworkAvailable() {
        return isNetworkAvailable;
    }
    
    public LiveData<Boolean> getLocationPermissionDenied() {
        return locationPermissionDenied;
    }
    
    public LiveData<Double> getUserLatitude() {
        return userLatitude;
    }
    
    public LiveData<Double> getUserLongitude() {
        return userLongitude;
    }
    
    // Métodos de carga de datos
    public void loadSucursales() {
        executor.execute(() -> {
            try {
                repository.refreshSucursales();
            } catch (Exception e) {
                error.postValue("Error al cargar sucursales: " + e.getMessage());
            }
        });
    }
    
    public void refreshSucursales() {
        executor.execute(() -> {
            try {
                repository.refreshSucursales();
            } catch (Exception e) {
                error.postValue("Error al actualizar sucursales: " + e.getMessage());
            }
        });
    }
    
    public void getSucursalById(int id, SucursalRepository.SucursalCallback callback) {
        executor.execute(() -> {
            repository.getSucursalById(id, callback);
        });
    }
    
    public void searchSucursales(String query, SucursalRepository.SearchCallback callback) {
        executor.execute(() -> {
            repository.searchSucursales(query, callback);
        });
    }
    
    // Métodos de geolocalización
    public void updateUserLocation(double latitude, double longitude) {
        userLatitude.setValue(latitude);
        userLongitude.setValue(longitude);
    }
    
    public void getSucursalesWithDistance(double userLat, double userLon, 
                                        SucursalRepository.DistanceCallback callback) {
        executor.execute(() -> {
            repository.getSucursalesWithDistance(userLat, userLon, callback);
        });
    }
    
    public void setLocationPermissionDenied(boolean denied) {
        locationPermissionDenied.setValue(denied);
    }
    
    // Métodos de filtrado y ordenamiento
    public void setFiltroEstado(String estado) {
        this.filtroEstado = estado;
        applyFilters();
    }
    
    public void setOrdenSeleccionado(String orden) {
        this.ordenSeleccionado = orden;
        applyFilters();
    }
    
    public String getFiltroEstado() {
        return filtroEstado;
    }
    
    public String getOrdenSeleccionado() {
        return ordenSeleccionado;
    }
    
    private void applyFilters() {
        // Los filtros se aplicarán en el Fragment
        // Aquí solo notificamos que los filtros han cambiado
        List<SucursalEntity> currentList = sucursales.getValue();
        if (currentList != null) {
            sucursales.setValue(currentList);
        }
    }
    
    // Métodos de sincronización
    public void forceSyncSucursales() {
        executor.execute(() -> {
            try {
                repository.forceSyncSucursales();
            } catch (Exception e) {
                error.postValue("Error en sincronización forzada: " + e.getMessage());
            }
        });
    }
    
    // Métodos de utilidad
    public void clearError() {
        error.setValue(null);
        repository.clearError();
    }
    
    public void retryLastOperation() {
        refreshSucursales();
    }
    
    public boolean isDataEmpty() {
        List<SucursalEntity> currentList = sucursales.getValue();
        return currentList == null || currentList.isEmpty();
    }
    
    public boolean hasActiveFilters() {
        return !filtroEstado.isEmpty();
    }
    
    public void clearFilters() {
        filtroEstado = "";
        ordenSeleccionado = "nombre";
        applyFilters();
    }
    
    // Verificación de estado de red
    private void checkNetworkStatus() {
        executor.execute(() -> {
            try {
                ConnectivityManager cm = (ConnectivityManager) 
                    getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                
                if (cm != null) {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                    isNetworkAvailable.postValue(isConnected);
                } else {
                    isNetworkAvailable.postValue(false);
                }
            } catch (Exception e) {
                isNetworkAvailable.postValue(false);
            }
        });
    }
    
    public void refreshNetworkStatus() {
        checkNetworkStatus();
    }
    
    // Métodos de validación
    public boolean isLocationDataAvailable() {
        Double lat = userLatitude.getValue();
        Double lon = userLongitude.getValue();
        return lat != null && lon != null;
    }
    
    public boolean canShowDistances() {
        Boolean permissionDenied = locationPermissionDenied.getValue();
        return permissionDenied != null && !permissionDenied && isLocationDataAvailable();
    }
    
    // Métodos de estadísticas
    public int getTotalSucursales() {
        List<SucursalEntity> currentList = sucursales.getValue();
        return currentList != null ? currentList.size() : 0;
    }
    
    public int getSucursalesActivasCount() {
        List<SucursalEntity> activasList = sucursalesActivas.getValue();
        return activasList != null ? activasList.size() : 0;
    }
    
    // Callback interfaces para operaciones asíncronas
    public interface SucursalDetailCallback {
        void onSuccess(SucursalEntity sucursal);
        void onError(String error);
    }
    
    public interface SucursalListCallback {
        void onSuccess(List<SucursalEntity> sucursales);
        void onError(String error);
    }
    
    public interface LocationUpdateCallback {
        void onLocationUpdated(double latitude, double longitude);
        void onLocationError(String error);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}