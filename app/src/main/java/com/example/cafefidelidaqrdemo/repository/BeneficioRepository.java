package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de Beneficio con validación de reglas
 * y sincronización con API
 */
public class BeneficioRepository {

    private static final String TAG = "BeneficioRepository";
    private final CafeFidelidadDB database;
    private final ExecutorService executor;
    private final Gson gson;

    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<List<Beneficio>> beneficiosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Beneficio>> beneficiosActivosLiveData = new MutableLiveData<>();

    public BeneficioRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.executor = Executors.newFixedThreadPool(4);
        this.gson = new Gson();
        loadBeneficios();
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

    public LiveData<Boolean> getIsOffline() {
        MutableLiveData<Boolean> offlineLiveData = new MutableLiveData<>(false);
        return offlineLiveData;
    }

    public LiveData<List<Beneficio>> getAllBeneficios() {
        return beneficiosLiveData;
    }

    public LiveData<List<Beneficio>> getBeneficiosActivos() {
        return beneficiosActivosLiveData;
    }

    public LiveData<Beneficio> getBeneficioById(int id) {
        MutableLiveData<Beneficio> beneficioLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Beneficio beneficio = database.obtenerBeneficioPorId(id);
                beneficioLiveData.postValue(beneficio);
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener beneficio por ID", e);
                errorLiveData.postValue("Error al obtener beneficio: " + e.getMessage());
            }
        });
        return beneficioLiveData;
    }

    public LiveData<List<Beneficio>> getBeneficiosVigentes() {
        return getBeneficiosActivos();
    }

    public LiveData<List<Beneficio>> getBeneficiosPorSucursal(String sucursalId) {
        return getBeneficiosActivos(); // Simplificado para la nueva estructura
    }

    public LiveData<Integer> getCountBeneficiosActivos() {
        MutableLiveData<Integer> countLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoBeneficios();
                countLiveData.postValue(count);
            } catch (Exception e) {
                Log.e(TAG, "Error al contar beneficios", e);
                countLiveData.postValue(0);
            }
        });
        return countLiveData;
    }

    public LiveData<Integer> getCountBeneficiosVigentes() {
        return getCountBeneficiosActivos();
    }

    // Métodos CRUD
    public void insertBeneficio(Beneficio beneficio, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (beneficio == null) {
                    callback.onResult(false);
                    errorLiveData.postValue("Beneficio no puede ser nulo");
                    return;
                }

                if (beneficio.getNombre() == null || beneficio.getNombre().trim().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("El nombre del beneficio es requerido");
                    return;
                }

                if (beneficio.getVisitasRequeridas() <= 0) {
                    callback.onResult(false);
                    errorLiveData.postValue("Las visitas requeridas deben ser mayor a 0");
                    return;
                }

                long result = database.insertarBeneficio(convertToDBBeneficio(beneficio));
                boolean success = result != -1;

                if (success) {
                    loadBeneficios();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al insertar beneficio");
                }

                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al insertar beneficio", e);
                errorLiveData.postValue("Error al insertar beneficio: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateBeneficio(Beneficio beneficio, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (beneficio == null || beneficio.getId() == null || beneficio.getId().isEmpty()) {
                    callback.onResult(false);
                    errorLiveData.postValue("Beneficio inválido");
                    return;
                }

                int result = database.actualizarBeneficio(convertToDBBeneficio(beneficio));
                boolean success = result > 0;

                if (success) {
                    loadBeneficios();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al actualizar beneficio");
                }

                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar beneficio", e);
                errorLiveData.postValue("Error al actualizar beneficio: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void deleteBeneficio(int beneficioId, OnResultCallback<Boolean> callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int result = database.eliminarBeneficio(beneficioId);
                boolean success = result > 0;

                if (success) {
                    loadBeneficios();
                    errorLiveData.postValue(null);
                } else {
                    errorLiveData.postValue("Error al eliminar beneficio");
                }

                callback.onResult(success);
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar beneficio", e);
                errorLiveData.postValue("Error al eliminar beneficio: " + e.getMessage());
                callback.onResult(false);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void desactivarBeneficio(int beneficioId, OnResultCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Beneficio beneficio = database.obtenerBeneficioPorId(beneficioId);
                if (beneficio != null) {
                    beneficio.setActivo(false);
                    updateBeneficio(beneficio, callback);
                } else {
                    callback.onResult(false);
                    errorLiveData.postValue("Beneficio no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al desactivar beneficio", e);
                errorLiveData.postValue("Error al desactivar beneficio: " + e.getMessage());
                callback.onResult(false);
            }
        });
    }

    public void activarBeneficio(int beneficioId, OnResultCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Beneficio beneficio = database.obtenerBeneficioPorId(beneficioId);
                if (beneficio != null) {
                    beneficio.setActivo(true);
                    updateBeneficio(beneficio, callback);
                } else {
                    callback.onResult(false);
                    errorLiveData.postValue("Beneficio no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al activar beneficio", e);
                errorLiveData.postValue("Error al activar beneficio: " + e.getMessage());
                callback.onResult(false);
            }
        });
    }

    // Métodos de validación
    public ValidationResult validateReglasJson(String reglasJson) {
        if (reglasJson == null || reglasJson.trim().isEmpty()) {
            return new ValidationResult(true, null);
        }

        try {
            JsonObject jsonObject = gson.fromJson(reglasJson, JsonObject.class);
            return new ValidationResult(true, null);
        } catch (JsonSyntaxException e) {
            return new ValidationResult(false, "JSON inválido: " + e.getMessage());
        }
    }

    // Métodos privados
    private void loadBeneficios() {
        executor.execute(() -> {
            try {
                List<Beneficio> beneficios = database.obtenerTodosLosBeneficios();
                List<Beneficio> beneficiosActivos = database.obtenerBeneficiosActivos();

                beneficiosLiveData.postValue(beneficios);
                beneficiosActivosLiveData.postValue(beneficiosActivos);
            } catch (Exception e) {
                Log.e(TAG, "Error al cargar beneficios", e);
                errorLiveData.postValue("Error al cargar beneficios: " + e.getMessage());
            }
        });
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

    // Métodos de sincronización (simplificados)
    public void forceSyncBeneficios(OnResultCallback<Boolean> callback) {
        loadBeneficios();
        callback.onResult(true);
    }

    public LiveData<List<Beneficio>> getBeneficiosDisponiblesParaCliente() {
        return getBeneficiosActivos();
    }

    public void refreshBeneficios(OnResultCallback<Boolean> callback) {
        loadBeneficios();
        callback.onResult(true);
    }

    /**
     * Convierte de modelo de dominio a modelo de base de datos
     */
    private com.example.cafefidelidaqrdemo.models.Beneficio convertToDBBeneficio(Beneficio beneficio) {
        return beneficio;
    }

    /**
     * 🔹 Nuevo método: Obtiene beneficios según la cantidad de sellos acumulados
     */
    public LiveData<List<Beneficio>> getBeneficiosPorSellos(String clienteId, int sellosRequeridos) {
        MutableLiveData<List<Beneficio>> beneficiosPorSellosLiveData = new MutableLiveData<>();

        executor.execute(() -> {
            try {
                List<Beneficio> beneficios = database.obtenerTodosLosBeneficios();
                int totalVisitas = database.obtenerVisitasPorCliente(Integer.parseInt(clienteId)).size();

                for (Beneficio beneficio : beneficios) {
                    if (totalVisitas >= sellosRequeridos) {
                        beneficio.setActivo(true);
                        beneficio.setEstado("disponible");
                    } else {
                        beneficio.setActivo(false);
                        beneficio.setEstado("pendiente");
                    }
                }

                beneficiosPorSellosLiveData.postValue(beneficios);
            } catch (Exception e) {
                Log.e(TAG, "Error al calcular beneficios por sellos", e);
                errorLiveData.postValue("Error al calcular beneficios: " + e.getMessage());
            }
        });

        return beneficiosPorSellosLiveData;
    }
}
