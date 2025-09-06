package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.location.Location;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SucursalRepository {
    private final SucursalDao sucursalDao;
    private final ApiService apiService;
    private final SyncManager syncManager;
    private final SearchManager searchManager;
    private final ExecutorService executor;
    private final Context context;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> locationPermissionDenied = new MutableLiveData<>(false);
    
    public SucursalRepository(SucursalDao sucursalDao, ApiService apiService, SyncManager syncManager, Context context) {
        this.sucursalDao = sucursalDao;
        this.apiService = apiService;
        this.syncManager = syncManager;
        this.searchManager = new SearchManager();
        this.executor = Executors.newFixedThreadPool(2);
        this.context = context;
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
            SucursalEntity entity = sucursalDao.getById(String.valueOf(idSucursal));
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
                if (entity.getLat() != 0.0 && entity.getLon() != 0.0) {
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
            List<Sucursal> sucursales = new ArrayList<>();
            
            // Filtrado simple por nombre y dirección
            String normalizedQuery = query != null ? query.toLowerCase().trim() : "";
            
            for (SucursalEntity entity : allSucursales) {
                if (normalizedQuery.isEmpty() || 
                    entity.getNombre().toLowerCase().contains(normalizedQuery) ||
                    entity.getDireccion().toLowerCase().contains(normalizedQuery)) {
                    sucursales.add(convertToModel(entity));
                }
            }
            
            callback.onResults(sucursales);
        });
    }
    
    public void searchSucursalesWithLocation(String query, Double userLat, Double userLon, 
                                           Boolean activasOnly, Double maxDistance, SearchWithLocationCallback callback) {
        executor.execute(() -> {
            List<SucursalEntity> allSucursales = sucursalDao.getAllSucursalesSync();
            Location userLocation = null;
            if (userLat != null && userLon != null) {
                userLocation = new Location("");
                userLocation.setLatitude(userLat);
                userLocation.setLongitude(userLon);
            }
            
            LiveData<List<SearchManager.SucursalWithDistance>> resultsLiveData = searchManager.searchSucursalesLocal(
                allSucursales, query, userLocation, activasOnly, maxDistance
            );
            
            // Como necesitamos el resultado de forma síncrona, implementamos filtrado directo
            List<SearchManager.SucursalWithDistance> results = new ArrayList<>();
            // Implementar filtrado simple aquí
            for (SucursalEntity entity : allSucursales) {
                // Filtro básico por query
                String normalizedQuery = query != null ? query.toLowerCase().trim() : "";
                if (!normalizedQuery.isEmpty()) {
                    if (!entity.getNombre().toLowerCase().contains(normalizedQuery) &&
                        !entity.getDireccion().toLowerCase().contains(normalizedQuery)) {
                        continue;
                    }
                }
                
                // Filtro por estado activo
                if (activasOnly != null && activasOnly && !entity.isActiva()) {
                    continue;
                }
                
                // Calcular distancia si hay ubicación
                Double distance = null;
                if (userLocation != null && entity.getLat() != 0.0 && entity.getLon() != 0.0) {
                    distance = calculateDistance(userLat, userLon, entity.getLat(), entity.getLon());
                    
                    // Filtro por distancia máxima
                    if (maxDistance != null && distance > maxDistance) {
                        continue;
                    }
                }
                
                results.add(new SearchManager.SucursalWithDistance(entity, distance));
            }
            
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
        SyncManager.forceSyncAll(context);
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
        entity.setId_sucursal(sucursal.getId());
        entity.setNombre(sucursal.getNombre());
        entity.setDireccion(sucursal.getDireccion());
        entity.setLat(sucursal.getLatitud());
        entity.setLon(sucursal.getLongitud());
        entity.setHorario(sucursal.getHorarioApertura() + " - " + sucursal.getHorarioCierre());
        entity.setEstado(sucursal.isActiva() ? "activo" : "inactivo");
        return entity;
    }
    
    private Sucursal convertToModel(SucursalEntity entity) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(entity.getId_sucursal());
        sucursal.setNombre(entity.getNombre());
        sucursal.setDireccion(entity.getDireccion());
        sucursal.setLatitud(entity.getLat());
        sucursal.setLongitud(entity.getLon());
        // Separar horario si es necesario
        String horario = entity.getHorario();
        if (horario != null && horario.contains(" - ")) {
            String[] horarios = horario.split(" - ");
            sucursal.setHorarioApertura(horarios[0]);
            sucursal.setHorarioCierre(horarios[1]);
        }
        sucursal.setActiva(entity.isActiva());
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