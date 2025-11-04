package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.ResenaProducto;
import com.example.cafefidelidaqrdemo.models.PromedioCalificacion;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository local para gestionar reseñas de productos (SQLite)
 */
public class ResenasProductoRepository {

    private static ResenasProductoRepository instance;
    private final CafeFidelidadDB database;
    private final ExecutorService executor;

    // LiveData
    private final MutableLiveData<List<ResenaProducto>> resenasLiveData = new MutableLiveData<>();
    private final MutableLiveData<PromedioCalificacion> promedioLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();

    public static synchronized ResenasProductoRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ResenasProductoRepository(context.getApplicationContext());
        }
        return instance;
    }

    private ResenasProductoRepository(Context context) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.executor = Executors.newFixedThreadPool(3);
    }

    public LiveData<List<ResenaProducto>> getResenasLiveData() { return resenasLiveData; }
    public LiveData<PromedioCalificacion> getPromedioLiveData() { return promedioLiveData; }
    public LiveData<Boolean> getIsLoadingLiveData() { return isLoadingLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<String> getSuccessLiveData() { return successLiveData; }

    // Crear reseña
    public void crearResena(ResenaProducto resena) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                long id = database.insertarResenaProducto(resena);
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

    // Listar reseñas por producto con paginación simple
    public void listarResenasPorProducto(int productoId, int limit, int offset) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                List<ResenaProducto> lista = database.obtenerResenasProducto(productoId, limit, offset);
                resenasLiveData.postValue(lista);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    // Actualizar reseña
    public void actualizarResena(ResenaProducto resena) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rows = database.actualizarResenaProducto(resena);
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

    // Eliminar reseña
    public void eliminarResena(int id) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rows = database.eliminarResenaProducto(id);
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

    // Obtener promedio
    public void obtenerPromedio(int productoId) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                PromedioCalificacion promedio = database.obtenerPromedioCalificacionProducto(productoId);
                promedioLiveData.postValue(promedio);
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
}