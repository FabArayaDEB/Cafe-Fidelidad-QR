package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.models.PromedioCalificacion;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResenasSucursalRepository {

    private static ResenasSucursalRepository instance;
    private final CafeFidelidadDB database;
    private final ExecutorService executor;

    // LiveData
    private final MutableLiveData<List<ResenaSucursal>> resenasLiveData = new MutableLiveData<>();
    private final MutableLiveData<PromedioCalificacion> promedioLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public static synchronized ResenasSucursalRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ResenasSucursalRepository(context.getApplicationContext());
        }
        return instance;
    }

    public ResenasSucursalRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.executor = Executors.newFixedThreadPool(3);
    }

    public LiveData<List<ResenaSucursal>> getResenasLiveData() { return resenasLiveData; }
    public LiveData<PromedioCalificacion> getPromedioLiveData() { return promedioLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<String> getSuccessLiveData() { return successLiveData; }

    public void crearResena(ResenaSucursal resena) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                long id = database.insertarResenaSucursal(resena);
                if (id > 0) {
                    successLiveData.postValue("Reseña creada");
                } else {
                    errorLiveData.postValue("No se pudo crear la reseña");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void listarResenasPorSucursal(int sucursalId, int limit, int offset) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                List<ResenaSucursal> lista = database.obtenerResenasSucursal(sucursalId, limit, offset);
                resenasLiveData.postValue(lista);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public LiveData<List<ResenaSucursal>> obtenerResenas(int sucursalId) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Cargar 50 reseñas por defecto
                List<ResenaSucursal> lista = database.obtenerResenasSucursal(sucursalId, 50, 0);
                resenasLiveData.postValue(lista);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
        return resenasLiveData;
    }

    public void actualizarResena(ResenaSucursal resena) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rows = database.actualizarResenaSucursal(resena);
                if (rows > 0) {
                    successLiveData.postValue("Reseña actualizada");
                } else {
                    errorLiveData.postValue("No se actualizó ninguna reseña");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void eliminarResena(int id) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rows = database.eliminarResenaSucursal(id);
                if (rows > 0) {
                    successLiveData.postValue("Reseña eliminada");
                } else {
                    errorLiveData.postValue("No se eliminó ninguna reseña");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void obtenerPromedio(int sucursalId) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                PromedioCalificacion promedio = database.obtenerPromedioCalificacionSucursal(sucursalId);
                promedioLiveData.postValue(promedio);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
}