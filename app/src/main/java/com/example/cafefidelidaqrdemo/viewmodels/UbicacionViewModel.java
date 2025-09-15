package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.entities.UbicacionEntity;
import com.example.cafefidelidaqrdemo.repository.UbicacionRepository;
import com.example.cafefidelidaqrdemo.utils.LocationManager;

import java.util.Date;
import java.util.List;

public class UbicacionViewModel extends AndroidViewModel {
    private UbicacionRepository ubicacionRepository;
    private LocationManager locationManager;
    
    // LiveData para la UI
    private MutableLiveData<Boolean> isLocationEnabled = new MutableLiveData<>();
    private MutableLiveData<Boolean> hasLocationPermissions = new MutableLiveData<>();
    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private MutableLiveData<String> locationStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);
    
    // Datos del usuario actual
    private int currentUserId = -1;

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        ubicacionRepository = new UbicacionRepository(application);
        locationManager = ubicacionRepository.getLocationManager();
        
        // Configurar observadores
        setupLocationObservers();
        
        // Verificar estado inicial
        checkInitialLocationState();
    }

    /**
     * Configura los observadores de ubicación
     */
    private void setupLocationObservers() {
        // Observar cambios en la ubicación actual
        locationManager.getCurrentLocation().observeForever(location -> {
            currentLocation.setValue(location);
            if (location != null && currentUserId != -1) {
                // Guardar automáticamente la nueva ubicación
                ubicacionRepository.guardarUbicacion(currentUserId, location);
            }
        });
        
        // Observar estado de operaciones del repositorio
        ubicacionRepository.getOperationStatus().observeForever(status -> {
            locationStatus.setValue(status);
        });
    }

    /**
     * Verifica el estado inicial de ubicación
     */
    private void checkInitialLocationState() {
        hasLocationPermissions.setValue(locationManager.hasLocationPermissions());
        isLocationEnabled.setValue(locationManager.isLocationEnabled());
    }

    /**
     * Establece el ID del usuario actual
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    /**
     * Obtiene el ID del usuario actual
     */
    public int getCurrentUserId() {
        return currentUserId;
    }

    // Getters para LiveData
    public LiveData<Boolean> getIsLocationEnabled() {
        return isLocationEnabled;
    }

    public LiveData<Boolean> getHasLocationPermissions() {
        return hasLocationPermissions;
    }

    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<String> getLocationStatus() {
        return locationStatus;
    }

    public LiveData<Boolean> getIsTracking() {
        return isTracking;
    }

    /**
     * Solicita permisos de ubicación
     */
    public void requestLocationPermissions() {
        // Este método debe ser llamado desde la Activity
        // que manejará el resultado de los permisos
        locationStatus.setValue("Solicitando permisos de ubicación...");
    }

    /**
     * Maneja el resultado de la solicitud de permisos
     */
    public void onPermissionResult(boolean granted) {
        hasLocationPermissions.setValue(granted);
        if (granted) {
            locationStatus.setValue("Permisos de ubicación concedidos");
            checkLocationEnabled();
        } else {
            locationStatus.setValue("Permisos de ubicación denegados");
        }
    }

    /**
     * Verifica si la ubicación está habilitada
     */
    public void checkLocationEnabled() {
        boolean enabled = locationManager.isLocationEnabled();
        isLocationEnabled.setValue(enabled);
        if (!enabled) {
            locationStatus.setValue("La ubicación está deshabilitada en el dispositivo");
        }
    }

    /**
     * Inicia el seguimiento de ubicación
     */
    public void startLocationTracking() {
        if (!hasLocationPermissions.getValue()) {
            locationStatus.setValue("Se requieren permisos de ubicación");
            return;
        }
        
        if (!isLocationEnabled.getValue()) {
            locationStatus.setValue("La ubicación debe estar habilitada");
            return;
        }
        
        if (currentUserId == -1) {
            locationStatus.setValue("Usuario no establecido");
            return;
        }
        
        ubicacionRepository.iniciarSeguimientoUbicacion();
        isTracking.setValue(true);
        locationStatus.setValue("Seguimiento de ubicación iniciado");
    }

    /**
     * Detiene el seguimiento de ubicación
     */
    public void stopLocationTracking() {
        ubicacionRepository.detenerSeguimientoUbicacion();
        isTracking.setValue(false);
        locationStatus.setValue("Seguimiento de ubicación detenido");
    }

    /**
     * Obtiene la ubicación actual una sola vez
     */
    public void getCurrentLocationOnce() {
        if (!hasLocationPermissions.getValue()) {
            locationStatus.setValue("Se requieren permisos de ubicación");
            return;
        }
        
        Location location = locationManager.getLastKnownLocation();
        currentLocation.setValue(location);
        if (location != null && currentUserId != -1) {
            ubicacionRepository.guardarUbicacion(currentUserId, location);
            locationStatus.setValue("Ubicación obtenida y guardada");
        } else {
            locationStatus.setValue("No se pudo obtener la ubicación");
        }
    }

    /**
     * Guarda una ubicación manualmente
     */
    public void saveLocation(Location location) {
        if (currentUserId == -1) {
            locationStatus.setValue("Usuario no establecido");
            return;
        }
        
        ubicacionRepository.guardarUbicacion(currentUserId, location);
    }

    /**
     * Guarda una ubicación con información adicional
     */
    public void saveLocationWithDetails(Location location, String direccion, String ciudad) {
        if (currentUserId == -1) {
            locationStatus.setValue("Usuario no establecido");
            return;
        }
        
        ubicacionRepository.guardarUbicacionCompleta(currentUserId, location, direccion, ciudad);
    }

    // Métodos para obtener datos de ubicación
    
    /**
     * Obtiene todas las ubicaciones del usuario actual
     */
    public LiveData<List<UbicacionEntity>> getUserLocations() {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUbicacionesByUsuario(currentUserId);
    }

    /**
     * Obtiene la última ubicación del usuario actual
     */
    public LiveData<UbicacionEntity> getLastUserLocation() {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUltimaUbicacionUsuario(currentUserId);
    }

    /**
     * Obtiene ubicaciones cercanas a sucursales
     */
    public LiveData<List<UbicacionEntity>> getNearBranchLocations() {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUbicacionesCercanasSucursales(currentUserId);
    }

    /**
     * Obtiene ubicaciones por rango de fechas
     */
    public LiveData<List<UbicacionEntity>> getLocationsByDateRange(Date startDate, Date endDate) {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUbicacionesPorRangoFecha(currentUserId, startDate, endDate);
    }

    /**
     * Obtiene ubicaciones recientes (últimas 24 horas)
     */
    public LiveData<List<UbicacionEntity>> getRecentLocations() {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUbicacionesRecientes(currentUserId);
    }

    /**
     * Obtiene el conteo de ubicaciones del usuario
     */
    public LiveData<Integer> getLocationCount() {
        if (currentUserId == -1) {
            return new MutableLiveData<>(0);
        }
        return ubicacionRepository.getConteoUbicacionesUsuario(currentUserId);
    }

    /**
     * Obtiene el conteo de visitas a sucursales
     */
    public LiveData<Integer> getBranchVisitCount() {
        if (currentUserId == -1) {
            return new MutableLiveData<>(0);
        }
        return ubicacionRepository.getConteoVisitasSucursales(currentUserId);
    }

    /**
     * Obtiene las ciudades visitadas
     */
    public LiveData<List<String>> getVisitedCities() {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getCiudadesVisitadas(currentUserId);
    }

    /**
     * Obtiene ubicaciones en un área específica
     */
    public LiveData<List<UbicacionEntity>> getLocationsInArea(double latMin, double latMax, double lngMin, double lngMax) {
        if (currentUserId == -1) {
            return new MutableLiveData<>();
        }
        return ubicacionRepository.getUbicacionesEnArea(currentUserId, latMin, latMax, lngMin, lngMax);
    }

    // Métodos de utilidad
    
    /**
     * Sincroniza ubicaciones con el servidor
     */
    public void syncLocations() {
        ubicacionRepository.sincronizarUbicaciones();
    }

    /**
     * Limpia ubicaciones antiguas
     */
    public void cleanOldLocations(int daysOld) {
        ubicacionRepository.limpiarUbicacionesAntiguas(daysOld);
    }

    /**
     * Elimina todas las ubicaciones del usuario actual
     */
    public void deleteAllUserLocations() {
        if (currentUserId != -1) {
            ubicacionRepository.eliminarUbicacionesUsuario(currentUserId);
        }
    }

    /**
     * Actualiza información de sucursal para una ubicación
     */
    public void updateBranchInfo(int locationId, boolean isNearBranch, Integer branchId, Float distance) {
        ubicacionRepository.actualizarInfoSucursal(locationId, isNearBranch, branchId, distance);
    }

    /**
     * Actualiza la dirección de una ubicación
     */
    public void updateLocationAddress(int locationId, String address, String city) {
        ubicacionRepository.actualizarDireccion(locationId, address, city);
    }

    /**
     * Calcula la distancia entre dos ubicaciones
     */
    public float calculateDistance(Location location1, Location location2) {
        return locationManager.calculateDistance(location1, location2);
    }

    /**
     * Verifica si una ubicación está cerca de otra
     */
    public boolean isLocationWithinRadius(Location targetLocation, float radiusInMeters) {
        return locationManager.isNearLocation(targetLocation, radiusInMeters);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar recursos
        ubicacionRepository.cleanup();
    }
}