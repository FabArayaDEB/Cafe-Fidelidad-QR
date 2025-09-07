package com.example.cafefidelidaqrdemo.ui.admin.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.repository.BeneficioRepository;

import java.util.List;

/**
 * ViewModel para la gestión de beneficios por administradores
 * Maneja la lógica de negocio para CU-04.1
 */
public class BeneficiosAdminViewModel extends AndroidViewModel {
    
    private final BeneficioRepository beneficioRepository;
    
    // LiveData para la UI
    private final MutableLiveData<List<BeneficioEntity>> beneficiosLiveData = new MutableLiveData<>();
    private final MutableLiveData<OperationResult> operationResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>();
    
    // LiveData derivados del repository
    private final LiveData<Boolean> isLoadingLiveData;
    private final LiveData<String> errorLiveData;
    private final LiveData<Boolean> syncStatusLiveData;
    
    public BeneficiosAdminViewModel(@NonNull Application application) {
        super(application);
        this.beneficioRepository = new BeneficioRepository(application);
        
        // Configurar LiveData del repository
        this.isLoadingLiveData = beneficioRepository.getIsLoading();
        this.errorLiveData = beneficioRepository.getError();
        this.syncStatusLiveData = beneficioRepository.getSyncStatus();
        
        // Configurar observación automática de beneficios
        setupBeneficiosObserver();
    }
    
    private void setupBeneficiosObserver() {
        // Funcionalidad temporalmente deshabilitada debido a incompatibilidad de tipos
        // TODO: Implementar conversión entre BeneficioEntity y Beneficio
    }
    
    // Getters para LiveData
    public LiveData<List<BeneficioEntity>> getBeneficios() {
        return beneficiosLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    public LiveData<OperationResult> getOperationResult() {
        return operationResultLiveData;
    }
    
    // Métodos de carga
    public void loadBeneficios() {
        refreshTrigger.setValue(true);
    }
    
    public void refreshBeneficios() {
        refreshTrigger.setValue(true);
    }
    
    // Operaciones CRUD - Temporalmente deshabilitadas
    public void createBeneficio(BeneficioEntity beneficio) {
        // TODO: Implementar conversión entre tipos de Beneficio
        operationResultLiveData.postValue(
            new OperationResult(false, "Funcionalidad en desarrollo")
        );
    }
    
    public void updateBeneficio(BeneficioEntity beneficio) {
        // TODO: Implementar conversión entre tipos de Beneficio
        operationResultLiveData.postValue(
            new OperationResult(false, "Funcionalidad en desarrollo")
        );
    }
    
    public void deleteBeneficio(String beneficioId) {
        beneficioRepository.deleteBeneficio(beneficioId, success -> {
            if (success) {
                operationResultLiveData.postValue(
                    new OperationResult(true, "Beneficio eliminado exitosamente")
                );
                refreshBeneficios();
            } else {
                operationResultLiveData.postValue(
                    new OperationResult(false, "Error al eliminar el beneficio")
                );
            }
        });
    }
    
    public void toggleBeneficioActive(BeneficioEntity beneficio) {
        if (beneficio.isActivo()) {
            // Desactivar beneficio
            beneficioRepository.desactivarBeneficio(beneficio.getId_beneficio(), success -> {
                if (success) {
                    operationResultLiveData.postValue(
                        new OperationResult(true, "Beneficio desactivado exitosamente")
                    );
                    refreshBeneficios();
                } else {
                    operationResultLiveData.postValue(
                        new OperationResult(false, "Error al desactivar el beneficio")
                    );
                }
            });
        } else {
            // Activar beneficio (actualizar con activo = true)
            beneficio.activar();
            // updateBeneficio(beneficio); // TODO: Implementar método para BeneficioEntity
        }
    }
    
    // Métodos de validación
    public BeneficioRepository.ValidationResult validateBeneficioRules(String reglasJson) {
        return beneficioRepository.validateReglasJson(reglasJson);
    }
    
    public boolean validateBeneficioData(BeneficioEntity beneficio) {
        if (beneficio == null) {
            operationResultLiveData.postValue(
                new OperationResult(false, "Datos del beneficio no válidos")
            );
            return false;
        }
        
        if (beneficio.getNombre() == null || beneficio.getNombre().trim().isEmpty()) {
            operationResultLiveData.postValue(
                new OperationResult(false, "El nombre del beneficio es obligatorio")
            );
            return false;
        }
        
        if (beneficio.getTipo() == null) {
            operationResultLiveData.postValue(
                new OperationResult(false, "El tipo de beneficio es obligatorio")
            );
            return false;
        }
        
        if (beneficio.getDescuento_pct() <= 0 && beneficio.getDescuento_monto() <= 0) {
            operationResultLiveData.postValue(
                new OperationResult(false, "El beneficio debe tener un descuento válido")
            );
            return false;
        }
        
        if (beneficio.getVigencia_ini() > 0 && beneficio.getVigencia_fin() > 0) {
            if (beneficio.getVigencia_ini() > beneficio.getVigencia_fin()) {
                operationResultLiveData.postValue(
                    new OperationResult(false, "La fecha de inicio no puede ser posterior a la fecha de fin")
                );
                return false;
            }
        }
        
        // Validar reglas JSON
        BeneficioRepository.ValidationResult validationResult = validateBeneficioRules(beneficio.getRegla());
        if (!validationResult.isValid) {
            operationResultLiveData.postValue(
                new OperationResult(false, validationResult.errorMessage)
            );
            return false;
        }
        
        return true;
    }
    
    // Métodos de filtrado y búsqueda
    public void filterBeneficiosByType(String tipo) {
        List<BeneficioEntity> currentBeneficios = beneficiosLiveData.getValue();
        if (currentBeneficios != null) {
            List<BeneficioEntity> filtered = currentBeneficios.stream()
                .filter(b -> tipo.equals(b.getTipo()))
                .collect(java.util.stream.Collectors.toList());
            beneficiosLiveData.setValue(filtered);
        }
    }
    
    public void filterBeneficiosByStatus(boolean activo) {
        List<BeneficioEntity> currentBeneficios = beneficiosLiveData.getValue();
        if (currentBeneficios != null) {
            List<BeneficioEntity> filtered = currentBeneficios.stream()
                .filter(b -> b.isActivo() == activo)
                .collect(java.util.stream.Collectors.toList());
            beneficiosLiveData.setValue(filtered);
        }
    }
    
    public void searchBeneficios(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshBeneficios();
            return;
        }
        
        List<BeneficioEntity> currentBeneficios = beneficiosLiveData.getValue();
        if (currentBeneficios != null) {
            String lowerQuery = query.toLowerCase();
            List<BeneficioEntity> filtered = currentBeneficios.stream()
                .filter(b -> 
                    b.getNombre().toLowerCase().contains(lowerQuery) ||
                    (b.getTipo() != null && b.getTipo().toLowerCase().contains(lowerQuery))
                )
                .collect(java.util.stream.Collectors.toList());
            beneficiosLiveData.setValue(filtered);
        }
    }
    
    public void clearFilters() {
        refreshBeneficios();
    }
    
    // Métodos de estadísticas
    public LiveData<Integer> getTotalBeneficiosCount() {
        return beneficioRepository.getCountBeneficiosActivos();
    }
    
    public LiveData<Integer> getBeneficiosVigentesCount() {
        return beneficioRepository.getCountBeneficiosVigentes();
    }
    
    // Clase para resultados de operaciones
    public static class OperationResult {
        private final boolean success;
        private final String message;
        
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}