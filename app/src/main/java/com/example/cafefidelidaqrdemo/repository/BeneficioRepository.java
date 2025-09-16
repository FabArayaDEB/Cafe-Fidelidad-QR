package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.dao.BeneficioDao;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de Beneficio con validación de reglas
 * y sincronización con API
 */
public class BeneficioRepository {
    
    private final BeneficioDao beneficioDao;
    private final ExecutorService executor;
    private final Gson gson;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    public BeneficioRepository(Context context) {
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.beneficioDao = database.beneficioDao();
        this.executor = Executors.newFixedThreadPool(4);
        this.gson = new Gson();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    /**
     * Obtiene el estado offline del repositorio
     * @return LiveData con el estado offline
     */
    public LiveData<Boolean> getIsOffline() {
        MutableLiveData<Boolean> offlineLiveData = new MutableLiveData<>();
        Boolean syncStatus = syncStatusLiveData.getValue();
        offlineLiveData.setValue(syncStatus == null || !syncStatus);
        return offlineLiveData;
    }
    
    // Operaciones de lectura
    public LiveData<List<BeneficioEntity>> getAllBeneficios() {
        return beneficioDao.getAllBeneficios();
    }
    
    public LiveData<List<BeneficioEntity>> getBeneficiosActivos() {
        return beneficioDao.getBeneficiosActivos();
    }
    
    public LiveData<BeneficioEntity> getBeneficioById(String id) {
        return beneficioDao.getBeneficioById(id);
    }
    
    public LiveData<List<BeneficioEntity>> getBeneficiosVigentes() {
        return beneficioDao.getBeneficiosVigentes(System.currentTimeMillis());
    }
    
    public LiveData<List<BeneficioEntity>> getBeneficiosPorSucursal(String sucursalId) {
        return beneficioDao.getBeneficiosPorSucursal(sucursalId, System.currentTimeMillis());
    }
    
    public LiveData<Integer> getCountBeneficiosActivos() {
        return beneficioDao.getCountBeneficiosActivos();
    }
    
    public LiveData<Integer> getCountBeneficiosVigentes() {
        return beneficioDao.getCountBeneficiosVigentes(System.currentTimeMillis());
    }
    
    // Operaciones de escritura
    public void insertBeneficio(BeneficioEntity beneficio, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Validar reglas JSON antes de insertar
                ValidationResult validation = validateReglasJson(beneficio.getRegla());
                if (!validation.isValid) {
                    errorLiveData.postValue("Regla inválida: " + validation.errorMessage);
                    isLoadingLiveData.postValue(false);
                    if (callback != null) callback.onResult(false);
                    return;
                }
                
                // Verificar conflictos de vigencia
                if (hasConflictingVigencia(beneficio)) {
                    errorLiveData.postValue("Conflicto de vigencias detectado. Revise las fechas.");
                    isLoadingLiveData.postValue(false);
                    if (callback != null) callback.onResult(false);
                    return;
                }
                
                // Generar ID si no existe
                if (beneficio.getId_beneficio() == null || beneficio.getId_beneficio().isEmpty()) {
                    beneficio.setId_beneficio(generateBeneficioId());
                }
                
                // Establecer fechas de auditoría
                beneficio.setLastSync(System.currentTimeMillis());
                
                beneficioDao.insertBeneficio(beneficio);
                
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(null);
                if (callback != null) callback.onResult(true);
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al crear beneficio: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                if (callback != null) callback.onResult(false);
            }
        });
    }
    
    public void updateBeneficio(BeneficioEntity beneficio, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Validar reglas JSON
                ValidationResult validation = validateReglasJson(beneficio.getRegla());
                if (!validation.isValid) {
                    errorLiveData.postValue("Regla inválida: " + validation.errorMessage);
                    isLoadingLiveData.postValue(false);
                    if (callback != null) callback.onResult(false);
                    return;
                }
                
                // Verificar conflictos de vigencia (excluyendo el beneficio actual)
                if (hasConflictingVigencia(beneficio)) {
                    errorLiveData.postValue("Conflicto de vigencias detectado. Revise las fechas.");
                    isLoadingLiveData.postValue(false);
                    if (callback != null) callback.onResult(false);
                    return;
                }
                
                // Actualizar fecha de modificación
                beneficio.setLastSync(System.currentTimeMillis());
                
                beneficioDao.updateBeneficio(beneficio);
                
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(null);
                if (callback != null) callback.onResult(true);
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al actualizar beneficio: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                if (callback != null) callback.onResult(false);
            }
        });
    }
    
    public void deleteBeneficio(String beneficioId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                beneficioDao.deleteBeneficioById(beneficioId);
                
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(null);
                if (callback != null) callback.onResult(true);
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al eliminar beneficio: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                if (callback != null) callback.onResult(false);
            }
        });
    }
    
    public void desactivarBeneficio(String beneficioId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                beneficioDao.desactivarBeneficio(beneficioId);
                // Actualizar fecha de sincronización se maneja en el DAO
                
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue(null);
                if (callback != null) callback.onResult(true);
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al desactivar beneficio: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                if (callback != null) callback.onResult(false);
            }
        });
    }
    
    // Validación simplificada de reglas JSON
    public ValidationResult validateReglasJson(String reglasJson) {
        // Permitir reglas vacías o nulas para CRUD básico
        if (reglasJson == null || reglasJson.trim().isEmpty()) {
            return new ValidationResult(true, null);
        }
        
        try {
            // Solo validar que sea JSON válido
            gson.fromJson(reglasJson, JsonObject.class);
            return new ValidationResult(true, null);
            
        } catch (JsonSyntaxException e) {
            return new ValidationResult(false, "JSON inválido: " + e.getMessage());
        } catch (Exception e) {
            return new ValidationResult(false, "Error en validación: " + e.getMessage());
        }
    }
    
    // Verificar conflictos de vigencia
    private boolean hasConflictingVigencia(BeneficioEntity beneficio) {
        if (beneficio.getVigencia_ini() == 0 || beneficio.getVigencia_fin() == 0) {
            return false; // Sin fechas definidas, no hay conflicto
        }
        
        try {
            int conflictos = beneficioDao.countBeneficiosConflictivos(
                beneficio.getVigencia_ini(),
                beneficio.getVigencia_fin(),
                beneficio.getId_beneficio() != null ? beneficio.getId_beneficio() : ""
            );
            return conflictos > 0;
        } catch (Exception e) {
            return false; // En caso de error, permitir la operación
        }
    }
    
    // Generar ID único para beneficio
    private String generateBeneficioId() {
        return "BEN_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Clases auxiliares
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }
    
    public interface OnResultCallback<T> {
        void onResult(T result);
    }
    
    /**
     * Fuerza la sincronización de beneficios con el servidor
     */
    public void forceSyncBeneficios(com.example.cafefidelidaqrdemo.repository.base.BaseRepository.SimpleCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Aquí iría la lógica de sincronización con la API
                // Por ahora simulamos una sincronización exitosa
                Thread.sleep(1000); // Simular tiempo de red
                
                isLoadingLiveData.postValue(false);
                syncStatusLiveData.postValue(true);
                if (callback != null) {
                    callback.onSuccess();
                }
                
            } catch (Exception e) {
                isLoadingLiveData.postValue(false);
                syncStatusLiveData.postValue(false);
                errorLiveData.postValue("Error al sincronizar beneficios: " + e.getMessage());
                if (callback != null) {
                    callback.onError("Error al sincronizar beneficios: " + e.getMessage());
                }
            }
        });
     }
     
     /**
       * Obtiene beneficios disponibles activos y vigentes
       */
      public LiveData<List<BeneficioEntity>> getBeneficiosDisponiblesParaCliente() {
          return beneficioDao.getBeneficiosDisponiblesParaCliente();
      }
      
      /**
       * Refresca los beneficios desde el servidor
       */
      public void refreshBeneficios(com.example.cafefidelidaqrdemo.repository.base.BaseRepository.SimpleCallback callback) {
          isLoadingLiveData.postValue(true);
          executor.execute(() -> {
              try {
                  // Aquí iría la lógica de sincronización con la API
                  // Por ahora simulamos una sincronización exitosa
                  Thread.sleep(1000); // Simular tiempo de red
                  
                  isLoadingLiveData.postValue(false);
                  syncStatusLiveData.postValue(true);
                  if (callback != null) {
                      callback.onSuccess();
                  }
                  
              } catch (Exception e) {
                  isLoadingLiveData.postValue(false);
                  syncStatusLiveData.postValue(false);
                  errorLiveData.postValue("Error al refrescar beneficios: " + e.getMessage());
                  if (callback != null) {
                      callback.onError("Error al refrescar beneficios: " + e.getMessage());
                  }
              }
          });
      }
}