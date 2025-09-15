package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.UbicacionDao;
import com.example.cafefidelidaqrdemo.database.entities.UbicacionEntity;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.utils.LocationManager;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UbicacionRepository extends BaseRepository {
    private UbicacionDao ubicacionDao;
    private LocationManager locationManager;
    private ExecutorService executor;
    private MutableLiveData<String> operationStatus = new MutableLiveData<>();

    public UbicacionRepository(Context context) {
        super(2); // Pool de 2 threads para operaciones de ubicación
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        ubicacionDao = database.ubicacionDao();
        locationManager = new LocationManager(context);
        executor = Executors.newFixedThreadPool(2);
    }

    /**
     * Obtiene el LocationManager para acceso directo
     */
    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * Guarda una nueva ubicación en la base de datos
     */
    public void guardarUbicacion(int usuarioId, Location location) {
        executor.execute(() -> {
            try {
                UbicacionEntity ubicacion = new UbicacionEntity(
                        usuarioId,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy()
                );
                
                long id = ubicacionDao.insertUbicacion(ubicacion);
                if (id > 0) {
                    operationStatus.postValue("Ubicación guardada exitosamente");
                    // Verificar si está cerca de alguna sucursal
                    verificarSucursalesCercanas(usuarioId, location);
                } else {
                    operationStatus.postValue("Error al guardar ubicación");
                }
            } catch (Exception e) {
                operationStatus.postValue("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Guarda ubicación con información adicional
     */
    public void guardarUbicacionCompleta(int usuarioId, Location location, String direccion, String ciudad) {
        executor.execute(() -> {
            try {
                UbicacionEntity ubicacion = new UbicacionEntity(
                        usuarioId,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy()
                );
                ubicacion.setDireccion(direccion);
                ubicacion.setCiudad(ciudad);
                
                long id = ubicacionDao.insertUbicacion(ubicacion);
                if (id > 0) {
                    operationStatus.postValue("Ubicación completa guardada exitosamente");
                    verificarSucursalesCercanas(usuarioId, location);
                } else {
                    operationStatus.postValue("Error al guardar ubicación completa");
                }
            } catch (Exception e) {
                operationStatus.postValue("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene todas las ubicaciones de un usuario
     */
    public LiveData<List<UbicacionEntity>> getUbicacionesByUsuario(int usuarioId) {
        return ubicacionDao.getUbicacionesByUsuario(usuarioId);
    }

    /**
     * Obtiene la última ubicación de un usuario
     */
    public LiveData<UbicacionEntity> getUltimaUbicacionUsuario(int usuarioId) {
        return ubicacionDao.getUltimaUbicacionUsuario(usuarioId);
    }

    /**
     * Obtiene ubicaciones cercanas a sucursales
     */
    public LiveData<List<UbicacionEntity>> getUbicacionesCercanasSucursales(int usuarioId) {
        return ubicacionDao.getUbicacionesCercanasSucursales(usuarioId);
    }

    /**
     * Obtiene ubicaciones por rango de fechas
     */
    public LiveData<List<UbicacionEntity>> getUbicacionesPorRangoFecha(int usuarioId, Date fechaInicio, Date fechaFin) {
        return ubicacionDao.getUbicacionesPorRangoFecha(usuarioId, fechaInicio, fechaFin);
    }

    /**
     * Obtiene ubicaciones recientes (últimas 24 horas)
     */
    public LiveData<List<UbicacionEntity>> getUbicacionesRecientes(int usuarioId) {
        return ubicacionDao.getUbicacionesRecientes(usuarioId);
    }

    /**
     * Obtiene conteo de ubicaciones del usuario
     */
    public LiveData<Integer> getConteoUbicacionesUsuario(int usuarioId) {
        return ubicacionDao.getConteoUbicacionesUsuario(usuarioId);
    }

    /**
     * Obtiene conteo de visitas a sucursales
     */
    public LiveData<Integer> getConteoVisitasSucursales(int usuarioId) {
        return ubicacionDao.getConteoVisitasSucursales(usuarioId);
    }

    /**
     * Obtiene ciudades visitadas por el usuario
     */
    public LiveData<List<String>> getCiudadesVisitadas(int usuarioId) {
        return ubicacionDao.getCiudadesVisitadas(usuarioId);
    }

    /**
     * Obtiene ubicaciones en un área específica
     */
    public LiveData<List<UbicacionEntity>> getUbicacionesEnArea(int usuarioId, double latMin, double latMax, double lngMin, double lngMax) {
        return ubicacionDao.getUbicacionesEnArea(usuarioId, latMin, latMax, lngMin, lngMax);
    }

    /**
     * Sincroniza ubicaciones no sincronizadas con el servidor
     */
    public void sincronizarUbicaciones() {
        executor.execute(() -> {
            try {
                List<UbicacionEntity> ubicacionesNoSincronizadas = ubicacionDao.getUbicacionesNoSincronizadas();
                
                if (ubicacionesNoSincronizadas.isEmpty()) {
                    operationStatus.postValue("No hay ubicaciones para sincronizar");
                    return;
                }

                // TODO: Implementar sincronización con API
                // Por ahora, marcar como sincronizadas localmente
                for (UbicacionEntity ubicacion : ubicacionesNoSincronizadas) {
                    ubicacionDao.marcarComoSincronizada(ubicacion.getId());
                }
                
                operationStatus.postValue("Sincronización completada: " + ubicacionesNoSincronizadas.size() + " ubicaciones");
            } catch (Exception e) {
                operationStatus.postValue("Error en sincronización: " + e.getMessage());
            }
        });
    }

    /**
     * Verifica si hay sucursales cercanas a la ubicación actual
     */
    private void verificarSucursalesCercanas(int usuarioId, Location location) {
        // TODO: Implementar lógica para verificar sucursales cercanas
        // Esto requeriría obtener las sucursales de la base de datos
        // y calcular distancias
        
        // Por ahora, ejemplo básico:
        // Si la precisión es buena (< 50 metros), marcar como potencial visita a sucursal
        if (location.getAccuracy() < 50) {
            // Lógica para determinar si está cerca de una sucursal
            // Esto se implementaría con las coordenadas de las sucursales
        }
    }

    /**
     * Actualiza información de sucursal para una ubicación
     */
    public void actualizarInfoSucursal(int ubicacionId, boolean esCercana, Integer sucursalId, Float distancia) {
        executor.execute(() -> {
            try {
                int updated = ubicacionDao.actualizarInfoSucursal(ubicacionId, esCercana, sucursalId, distancia);
                if (updated > 0) {
                    operationStatus.postValue("Información de sucursal actualizada");
                } else {
                    operationStatus.postValue("Error al actualizar información de sucursal");
                }
            } catch (Exception e) {
                operationStatus.postValue("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Actualiza dirección de una ubicación
     */
    public void actualizarDireccion(int ubicacionId, String direccion, String ciudad) {
        executor.execute(() -> {
            try {
                int updated = ubicacionDao.actualizarDireccion(ubicacionId, direccion, ciudad);
                if (updated > 0) {
                    operationStatus.postValue("Dirección actualizada");
                } else {
                    operationStatus.postValue("Error al actualizar dirección");
                }
            } catch (Exception e) {
                operationStatus.postValue("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina ubicaciones antiguas (más de X días)
     */
    public void limpiarUbicacionesAntiguas(int diasAntiguedad) {
        executor.execute(() -> {
            try {
                Date fechaLimite = new Date(System.currentTimeMillis() - (diasAntiguedad * 24L * 60L * 60L * 1000L));
                int deleted = ubicacionDao.deleteUbicacionesSincronizadasAntiguas(fechaLimite);
                operationStatus.postValue("Ubicaciones antiguas eliminadas: " + deleted);
            } catch (Exception e) {
                operationStatus.postValue("Error al limpiar ubicaciones: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina todas las ubicaciones de un usuario
     */
    public void eliminarUbicacionesUsuario(int usuarioId) {
        executor.execute(() -> {
            try {
                int deleted = ubicacionDao.deleteUbicacionesByUsuario(usuarioId);
                operationStatus.postValue("Ubicaciones del usuario eliminadas: " + deleted);
            } catch (Exception e) {
                operationStatus.postValue("Error al eliminar ubicaciones: " + e.getMessage());
            }
        });
    }

    /**
     * Obtiene el estado de las operaciones
     */
    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    /**
     * Inicia el seguimiento de ubicación
     */
    public void iniciarSeguimientoUbicacion() {
        if (locationManager.hasLocationPermissions()) {
            locationManager.startLocationUpdates();
            operationStatus.setValue("Seguimiento de ubicación iniciado");
        } else {
            operationStatus.setValue("Permisos de ubicación requeridos");
        }
    }

    /**
     * Detiene el seguimiento de ubicación
     */
    public void detenerSeguimientoUbicacion() {
        locationManager.stopLocationUpdates();
        operationStatus.setValue("Seguimiento de ubicación detenido");
    }

    /**
     * Limpia recursos
     */
    public void cleanup() {
        if (locationManager != null) {
            locationManager.cleanup();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}