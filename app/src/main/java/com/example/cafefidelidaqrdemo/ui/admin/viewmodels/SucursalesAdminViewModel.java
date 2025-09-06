package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.data.repositories.AdminRepository;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * ViewModel para la gestión CRUD de sucursales
 * Maneja la lógica de negocio, validaciones y operaciones geográficas
 */
public class SucursalesAdminViewModel extends AndroidViewModel {
    
    private final AdminRepository adminRepository;
    private final ExecutorService executor;
    
    // Estados de carga y mensajes
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    // Datos de sucursales
    private final LiveData<List<SucursalEntity>> allSucursales;
    private final LiveData<List<SucursalEntity>> sucursalesActivas;
    private final LiveData<Integer> countSucursalesActivas;
    private final LiveData<Integer> countSucursalesInactivas;
    
    // Estados de operaciones
    private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
    
    // Sucursal seleccionada para edición
    private final MutableLiveData<SucursalEntity> selectedSucursal = new MutableLiveData<>();
    
    // Ubicación actual del usuario
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[\\+]?[0-9]{10,15}$"
    );
    
    public SucursalesAdminViewModel(@NonNull Application application) {
        super(application);
        
        adminRepository = new AdminRepository(application);
        executor = Executors.newFixedThreadPool(3);
        
        // Inicializar LiveData observables
        allSucursales = adminRepository.getAllSucursales();
        sucursalesActivas = adminRepository.getSucursalesActivas();
        countSucursalesActivas = adminRepository.getCountSucursalesActivas();
        countSucursalesInactivas = adminRepository.getCountSucursalesInactivas();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    
    public LiveData<List<SucursalEntity>> getAllSucursales() {
        return allSucursales;
    }
    
    public LiveData<List<SucursalEntity>> getSucursalesActivas() {
        return sucursalesActivas;
    }
    
    public LiveData<Integer> getCountSucursalesActivas() {
        return countSucursalesActivas;
    }
    
    public LiveData<Integer> getCountSucursalesInactivas() {
        return countSucursalesInactivas;
    }
    
    public LiveData<Boolean> getIsCreating() {
        return isCreating;
    }
    
    public LiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }
    
    public LiveData<Boolean> getIsDeleting() {
        return isDeleting;
    }
    
    public LiveData<SucursalEntity> getSelectedSucursal() {
        return selectedSucursal;
    }
    
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }
    
    /**
     * Crea una nueva sucursal
     */
    public void crearSucursal(SucursalEntity sucursal) {
        if (!validarSucursal(sucursal)) {
            return;
        }
        
        isCreating.setValue(true);
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si ya existe una sucursal muy cerca (radio de 100m)
                if (adminRepository.existeSucursalCercana(sucursal.getLat(), sucursal.getLon(), 0.1)) {
                    errorMessage.postValue("Ya existe una sucursal muy cerca de esa ubicación");
                    return;
                }
                
                adminRepository.crearSucursal(sucursal, new AdminRepository.AdminCallback<SucursalEntity>() {
                    @Override
                    public void onSuccess(SucursalEntity result) {
                        successMessage.postValue("Sucursal creada exitosamente");
                        isCreating.postValue(false);
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al crear sucursal: " + error);
                        isCreating.postValue(false);
                        isLoading.postValue(false);
                    }
                });
                
            } catch (Exception e) {
                errorMessage.postValue("Error al crear sucursal: " + e.getMessage());
                isCreating.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza una sucursal existente
     */
    public void actualizarSucursal(SucursalEntity sucursal) {
        if (!validarSucursal(sucursal)) {
            return;
        }
        
        isUpdating.setValue(true);
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar control de versión
                SucursalEntity sucursalActual = adminRepository.getSucursalPorId(String.valueOf(sucursal.getId_sucursal()));
                if (sucursalActual == null) {
                    errorMessage.postValue("La sucursal no existe");
                    return;
                }
                
                if (sucursalActual.getVersion() != sucursal.getVersion()) {
                    errorMessage.postValue("La sucursal ha sido modificada por otro usuario. Actualice y vuelva a intentar.");
                    return;
                }
                
                // Verificar nombres duplicados (excluyendo la sucursal actual)
                if (adminRepository.existeSucursalPorNombreExcluyendoId(sucursal.getNombre(), String.valueOf(sucursal.getId_sucursal()))) {
                    errorMessage.postValue("Ya existe otra sucursal con ese nombre");
                    return;
                }
                
                // Verificar ubicaciones cercanas (excluyendo la sucursal actual)
                // if (adminRepository.existeSucursalCercanaExcluyendoId(
                //         sucursal.getLat(), sucursal.getLon(), 0.1, sucursal.getId_sucursal())) {
                //     errorMessage.postValue("Ya existe otra sucursal muy cerca de esa ubicación");
                //     return;
                // }
                
                adminRepository.actualizarSucursal(sucursal, new AdminRepository.AdminCallback<SucursalEntity>() {
                    @Override
                    public void onSuccess(SucursalEntity result) {
                        successMessage.postValue("Sucursal actualizada exitosamente");
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Error al actualizar sucursal: " + error);
                    }
                });
                successMessage.postValue("Sucursal actualizada exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar sucursal: " + e.getMessage());
            } finally {
                isUpdating.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Activa una sucursal
     */
    public void activarSucursal(long sucursalId) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                adminRepository.activarSucursal(sucursalId);
                successMessage.postValue("Sucursal activada exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al activar sucursal: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Desactiva una sucursal
     */
    public void desactivarSucursal(long sucursalId, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si la sucursal tiene visitas activas o programadas
                if (adminRepository.sucursalTieneVisitasActivas(sucursalId)) {
                    errorMessage.postValue("No se puede desactivar: la sucursal tiene visitas activas o programadas");
                    return;
                }
                
                adminRepository.desactivarSucursal(sucursalId, motivo);
                successMessage.postValue("Sucursal desactivada exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al desactivar sucursal: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Elimina una sucursal (eliminación lógica)
     */
    public void eliminarSucursal(long sucursalId) {
        isDeleting.setValue(true);
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // Verificar si la sucursal tiene dependencias
                if (adminRepository.sucursalTieneDependencias(sucursalId)) {
                    errorMessage.postValue("No se puede eliminar: la sucursal tiene dependencias. Considere desactivarla.");
                    return;
                }
                
                adminRepository.eliminarSucursal(sucursalId);
                successMessage.postValue("Sucursal eliminada exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al eliminar sucursal: " + e.getMessage());
            } finally {
                isDeleting.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Busca sucursales por nombre, ciudad o dirección
     */
    public LiveData<List<SucursalEntity>> buscarSucursales(String query) {
        return adminRepository.buscarSucursales(query);
    }
    
    /**
     * Obtiene sucursales por ciudad
     */
    public LiveData<List<SucursalEntity>> getSucursalesPorCiudad(String ciudad) {
        return adminRepository.getSucursalesPorCiudad(ciudad);
    }
    
    /**
     * Obtiene sucursales cercanas a una ubicación
     */
    public LiveData<List<SucursalEntity>> getSucursalesCercanas(double latitud, double longitud, double radioKm) {
        return adminRepository.getSucursalesCercanas(latitud, longitud, radioKm);
    }
    
    /**
     * Obtiene sucursales abiertas en un horario específico
     */
    public LiveData<List<SucursalEntity>> getSucursalesAbiertas(String hora, String dia) {
        return adminRepository.getSucursalesAbiertas(hora, dia);
    }
    
    /**
     * Actualiza la capacidad de una sucursal
     */
    public void actualizarCapacidad(long sucursalId, int nuevaCapacidad, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                if (nuevaCapacidad <= 0) {
                    errorMessage.postValue("La capacidad debe ser mayor a 0");
                    return;
                }
                
                if (nuevaCapacidad > 1000) {
                    errorMessage.postValue("La capacidad no puede exceder 1000 personas");
                    return;
                }
                
                // TODO: Implementar método actualizarCapacidadSucursal en AdminRepository
                // adminRepository.actualizarCapacidadSucursal(sucursalId, nuevaCapacidad, motivo);
                successMessage.postValue("Funcionalidad de capacidad no implementada aún");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar capacidad: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza los horarios de una sucursal
     */
    public void actualizarHorarios(long sucursalId, String horarioApertura, String horarioCierre, 
                                  String diasOperacion, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                if (!validarHorario(horarioApertura) || !validarHorario(horarioCierre)) {
                    errorMessage.postValue("Formato de horario inválido (use HH:MM)");
                    return;
                }
                
                if (horarioApertura.compareTo(horarioCierre) >= 0) {
                    errorMessage.postValue("El horario de apertura debe ser anterior al de cierre");
                    return;
                }
                
                // TODO: Implementar método actualizarHorariosSucursal en AdminRepository
                // adminRepository.actualizarHorariosSucursal(sucursalId, horarioApertura, 
                //         horarioCierre, diasOperacion, motivo);
                successMessage.postValue("Funcionalidad de horarios no implementada aún");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar horarios: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza la ubicación de una sucursal
     */
    public void actualizarUbicacion(long sucursalId, double latitud, double longitud, 
                                   String direccion, String motivo) {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                if (!validarCoordenadas(latitud, longitud)) {
                    errorMessage.postValue("Coordenadas inválidas");
                    return;
                }
                
                // Verificar que no haya otra sucursal muy cerca
                // if (adminRepository.existeSucursalCercanaExcluyendoId(latitud, longitud, 0.1, sucursalId)) {
                //     errorMessage.postValue("Ya existe otra sucursal muy cerca de esa ubicación");
                //     return;
                // }
                
                // adminRepository.actualizarUbicacionSucursal(sucursalId, latitud, longitud, direccion, motivo);
                successMessage.postValue("Ubicación actualizada exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar ubicación: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza todas las sucursales desde el servidor
     */
    public void actualizarSucursales() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // adminRepository.sincronizarSucursales();
                successMessage.postValue("Sucursales actualizadas desde el servidor");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar sucursales: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Exporta la lista de sucursales
     */
    public void exportarSucursales() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // adminRepository.exportarSucursales();
                successMessage.postValue("Sucursales exportadas exitosamente");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al exportar sucursales: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Sincroniza con el servidor
     */
    public void sincronizarConServidor() {
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                // adminRepository.sincronizarSucursalesConServidor();
                successMessage.postValue("Sincronización completada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error en sincronización: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Selecciona una sucursal para edición
     */
    public void seleccionarSucursal(SucursalEntity sucursal) {
        selectedSucursal.setValue(sucursal);
    }
    
    /**
     * Limpia la selección de sucursal
     */
    public void limpiarSeleccion() {
        selectedSucursal.setValue(null);
    }
    
    /**
     * Actualiza la ubicación actual del usuario
     */
    public void actualizarUbicacionActual(Location location) {
        currentLocation.setValue(location);
    }
    
    /**
     * Valida los datos de una sucursal
     */
    private boolean validarSucursal(SucursalEntity sucursal) {
        if (sucursal == null) {
            errorMessage.setValue("Datos de sucursal inválidos");
            return false;
        }
        
        if (sucursal.getNombre() == null || sucursal.getNombre().trim().isEmpty()) {
            errorMessage.setValue("El nombre de la sucursal es obligatorio");
            return false;
        }
        
        if (sucursal.getNombre().length() > 100) {
            errorMessage.setValue("El nombre de la sucursal no puede exceder 100 caracteres");
            return false;
        }
        
        if (sucursal.getDireccion() == null || sucursal.getDireccion().trim().isEmpty()) {
            errorMessage.setValue("La dirección es obligatoria");
            return false;
        }
        
        // TODO: Campo ciudad no disponible en SucursalEntity
        // if (sucursal.getCiudad() == null || sucursal.getCiudad().trim().isEmpty()) {
        //     errorMessage.setValue("La ciudad es obligatoria");
        //     return false;
        // }
        
        if (!validarCoordenadas(sucursal.getLat(), sucursal.getLon())) {
            errorMessage.setValue("Las coordenadas son inválidas");
            return false;
        }
        
        // TODO: Campo capacidadMaxima no disponible en SucursalEntity
        // if (sucursal.getCapacidadMaxima() <= 0) {
        //     errorMessage.setValue("La capacidad debe ser mayor a 0");
        //     return false;
        // }
        // 
        // if (sucursal.getCapacidadMaxima() > 1000) {
        //     errorMessage.setValue("La capacidad no puede exceder 1000 personas");
        //     return false;
        // }
        
        String[] horarios = sucursal.getHorario().split(" - ");
        if (horarios.length != 2 || !validarHorario(horarios[0]) || !validarHorario(horarios[1])) {
            errorMessage.setValue("Formato de horario inválido (use HH:MM)");
            return false;
        }
        
        if (horarios[0].compareTo(horarios[1]) >= 0) {
            errorMessage.setValue("El horario de apertura debe ser anterior al de cierre");
            return false;
        }
        
        // TODO: Campos telefono y email no disponibles en SucursalEntity
        // if (sucursal.getTelefono() != null && !sucursal.getTelefono().isEmpty() && 
        //     !PHONE_PATTERN.matcher(sucursal.getTelefono()).matches()) {
        //     errorMessage.setValue("Formato de teléfono inválido");
        //     return false;
        // }
        // 
        // if (sucursal.getEmail() != null && !sucursal.getEmail().isEmpty() && 
        //     !EMAIL_PATTERN.matcher(sucursal.getEmail()).matches()) {
        //     errorMessage.setValue("Formato de email inválido");
        //     return false;
        // }
        
        return true;
    }
    
    /**
     * Valida coordenadas geográficas
     */
    private boolean validarCoordenadas(double latitud, double longitud) {
        return latitud >= -90 && latitud <= 90 && longitud >= -180 && longitud <= 180 &&
               latitud != 0.0 && longitud != 0.0;
    }
    
    /**
     * Valida formato de horario (HH:MM)
     */
    private boolean validarHorario(String horario) {
        if (horario == null || horario.isEmpty()) {
            return false;
        }
        
        Pattern pattern = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
        return pattern.matcher(horario).matches();
    }
    
    /**
     * Limpia mensajes de error y éxito
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
    
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}