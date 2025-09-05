package com.example.cafefidelidaqrdemo.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.dao.SucursalDao;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.sync.SyncManager;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.utils.SearchManager;
import com.example.cafefidelidaqrdemo.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SucursalRepository {
    private final SucursalDao sucursalDao;
    private final ApiService apiService;
    private final SyncManager syncManager;
    private final SearchManager searchManager;
    private final Executor executor;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> locationPermissionDenied = new MutableLiveData<>(false);
    
    public SucursalRepository(SucursalDao sucursalDao, ApiService apiService, SyncManager syncManager) {
        this.sucursalDao = sucursalDao;
        this.apiService = apiService;
        this.syncManager = syncManager;
        this.searchManager = new SearchManager();
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // LiveData getters
    public LiveData<List<SucursalEntity>> getAllSucursales() {
        return sucursalDao.getAllSucursales();
    }
    
    public LiveData<List<SucursalEntity>> getSucursalesActivas() {
        return sucursalDao.getSucursalesActivas();
    }
    
    public LiveData<List<SucursalEntity>> getActiveSucursales() {
        return sucursalDao.getSucursalesActivas();
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
    
    public LiveData<Boolean> getLocationPermissionDenied() {
        return locationPermissionDenied;
    }
    
    // Métodos principales
    public void refreshSucursales() {
        if (!NetworkUtils.isNetworkAvailable()) {
            isOffline.postValue(true);
            return;
        }
        
        isLoading.postValue(true);
        isOffline.postValue(false);
        error.postValue(null);
        
        apiService.getSucursales().enqueue(new Callback<List<Sucursal>>() {
            @Override
            public void onResponse(Call<List<Sucursal>> call, Response<List<Sucursal>> response) {
                isLoading.postValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Sucursal> sucursales = response.body();
                    executor.execute(() -> {
                        // Convertir y guardar en cache local
                        List<SucursalEntity> entities = new ArrayList<>();
                        for (Sucursal sucursal : sucursales) {
                            entities.add(convertToEntity(sucursal));
                        }
                        
                        // Limpiar cache anterior y insertar nuevos datos
                        sucursalDao.deleteAll();
                        sucursalDao.insertAll(entities);
                    });
                } else {
                    error.postValue("Error al cargar sucursales: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Sucursal>> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    public void getSucursalById(Long idSucursal, SucursalCallback callback) {
        executor.execute(() -> {
            SucursalEntity entity = sucursalDao.getSucursalById(idSucursal);
            if (entity != null) {
                callback.onSuccess(convertToModel(entity));
            } else {
                // Si no está en cache, intentar obtener de API
                if (NetworkUtils.isNetworkAvailable()) {
                    fetchSucursalFromApi(idSucursal, callback);
                } else {
                    callback.onError("Sucursal no encontrada y sin conexión");
                }
            }
        });
    }
    
    private void fetchSucursalFromApi(Long idSucursal, SucursalCallback callback) {
        apiService.getSucursalById(idSucursal).enqueue(new Callback<Sucursal>() {
            @Override
            public void onResponse(Call<Sucursal> call, Response<Sucursal> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Sucursal sucursal = response.body();
                    executor.execute(() -> {
                        // Guardar en cache
                        SucursalEntity entity = convertToEntity(sucursal);
                        sucursalDao.insert(entity);
                    });
                    callback.onSuccess(sucursal);
                } else {
                    callback.onError("Error al obtener sucursal: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Sucursal> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    public void getSucursalesWithDistance(double userLat, double userLon, DistanceCallback callback) {
        executor.execute(() -> {
            List<SucursalEntity> entities = sucursalDao.getAllSucursalesSync();
            List<SucursalWithDistance> sucursalesWithDistance = new ArrayList<>();
            
            for (SucursalEntity entity : entities) {
                if (entity.getLat() != null && entity.getLon() != null) {
                    double distance = calculateDistance(userLat, userLon, entity.getLat(), entity.getLon());
                    sucursalesWithDistance.add(new SucursalWithDistance(convertToModel(entity), distance));
                }
            }
            
            // Ordenar por distancia
            sucursalesWithDistance.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
            
            callback.onResults(sucursalesWithDistance);
        });
    }
    
    public void searchSucursales(String query, SearchCallback callback) {
        executor.execute(() -> {
            List<SucursalEntity> allSucursales = sucursalDao.getAllSucursalesSync();
            List<SearchManager.SucursalWithDistance> results = searchManager.searchSucursalesLocal(
                allSucursales, query, null, null, null, null
            );
            List<Sucursal> sucursales = new ArrayList<>();
            for (SearchManager.SucursalWithDistance item : results) {
                sucursales.add(convertToModel(item.getSucursal()));
            }
            callback.onResults(sucursales);
        });
    }
    
    public void searchSucursalesWithLocation(String query, Double userLat, Double userLon, 
                                           Boolean activasOnly, Double maxDistance, SearchWithLocationCallback callback) {
        executor.execute(() -> {
            List<SucursalEntity> allSucursales = sucursalDao.getAllSucursalesSync();
            LocationUtils.GeoPoint userLocation = (userLat != null && userLon != null) ? 
                new LocationUtils.GeoPoint(userLat, userLon) : null;
            
            List<SearchManager.SucursalWithDistance> results = searchManager.searchSucursalesLocal(
                allSucursales, query, userLocation, activasOnly, maxDistance, null
            );
            
            callback.onResults(results);
        });
    }
    
    public void searchSucursalesRemote(String query, Double userLat, Double userLon, RemoteSearchCallback callback) {
        if (!NetworkUtils.isNetworkAvailable()) {
            callback.onError("Sin conexión a internet");
            return;
        }
        
        // Implementar búsqueda remota cuando esté disponible en la API
        // Por ahora, usar búsqueda local como fallback
        searchSucursalesWithLocation(query, userLat, userLon, null, null, new SearchWithLocationCallback() {
            @Override
            public void onResults(List<SearchManager.SucursalWithDistance> sucursales) {
                callback.onResults(sucursales, false); // false = no hay más resultados remotos
            }
        });
    }
    
    public void forceSyncSucursales() {
        syncManager.syncSucursales();
    }
    
    public void setLocationPermissionDenied(boolean denied) {
        locationPermissionDenied.postValue(denied);
    }
    
    public void clearError() {
        error.postValue(null);
    }
    
    // Método para calcular distancia usando fórmula de Haversine
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distancia en kilómetros
    }
    
    // Métodos de conversión
    private SucursalEntity convertToEntity(Sucursal sucursal) {
        SucursalEntity entity = new SucursalEntity();
        entity.setIdSucursal(sucursal.getIdSucursal());
        entity.setNombre(sucursal.getNombre());
        entity.setDireccion(sucursal.getDireccion());
        entity.setLat(sucursal.getLat());
        entity.setLon(sucursal.getLon());
        entity.setHorario(sucursal.getHorario());
        entity.setEstado(sucursal.getEstado());
        return entity;
    }
    
    private Sucursal convertToModel(SucursalEntity entity) {
        Sucursal sucursal = new Sucursal();
        sucursal.setIdSucursal(entity.getIdSucursal());
        sucursal.setNombre(entity.getNombre());
        sucursal.setDireccion(entity.getDireccion());
        sucursal.setLat(entity.getLat());
        sucursal.setLon(entity.getLon());
        sucursal.setHorario(entity.getHorario());
        sucursal.setEstado(entity.getEstado());
        return sucursal;
    }
    
    // Clases auxiliares
    public static class SucursalWithDistance {
        private final Sucursal sucursal;
        private final double distance;
        
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
        
        public String getFormattedDistance() {
            if (distance < 1) {
                return String.format("%.0f m", distance * 1000);
            } else {
                return String.format("%.1f km", distance);
            }
        }
    }
    
    // Interfaces de callback
    public interface SucursalCallback {
        void onSuccess(Sucursal sucursal);
        void onError(String error);
    }
    
    public interface SearchCallback {
        void onResults(List<Sucursal> sucursales);
    }
    
    public interface SearchWithLocationCallback {
        void onResults(List<SearchManager.SucursalWithDistance> sucursales);
    }
    
    public interface RemoteSearchCallback {
        void onResults(List<SearchManager.SucursalWithDistance> sucursales, boolean hasMore);
        void onError(String error);
    }
    
    public interface DistanceCallback {
        void onResults(List<SucursalWithDistance> sucursales);
    }
}