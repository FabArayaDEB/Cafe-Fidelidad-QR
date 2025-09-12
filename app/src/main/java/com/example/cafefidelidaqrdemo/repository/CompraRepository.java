package com.example.cafefidelidaqrdemo.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.CompraDao;
import com.example.cafefidelidaqrdemo.database.entities.CompraEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio para manejar operaciones relacionadas con las compras.
 */
public class CompraRepository {

    private final CompraDao compraDao;
    private final ExecutorService executorService;

    public CompraRepository(Application application) {
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        compraDao = database.compraDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Registra una nueva compra en la base de datos.
     * @param compra La compra a registrar.
     * @param callback Callback para notificar cuando se complete la operación.
     */
    public void insertCompra(CompraEntity compra, OnCompraInsertedCallback callback) {
        executorService.execute(() -> {
            try {
                long id = compraDao.insert(compra);
                compra.setId(id);
                callback.onCompraInserted(compra);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Obtiene todas las compras de un cliente específico.
     * @param clienteId El ID del cliente.
     * @return LiveData con la lista de compras del cliente.
     */
    public LiveData<List<CompraEntity>> getComprasByCliente(long clienteId) {
        return compraDao.getComprasByCliente(clienteId);
    }

    /**
     * Obtiene todas las compras de un cliente por su McID.
     * @param mcId El McID único del cliente.
     * @return LiveData con la lista de compras del cliente.
     */
    public LiveData<List<CompraEntity>> getComprasByMcId(String mcId) {
        return compraDao.getComprasByMcId(mcId);
    }

    /**
     * Obtiene el total gastado por un cliente.
     * @param clienteId El ID del cliente.
     * @return LiveData con el monto total gastado.
     */
    public LiveData<Double> getTotalGastadoByCliente(long clienteId) {
        return compraDao.getTotalGastadoByCliente(clienteId);
    }

    /**
     * Obtiene las compras realizadas en un rango de fechas.
     * @param fechaInicio Fecha de inicio.
     * @param fechaFin Fecha de fin.
     * @return LiveData con la lista de compras en el rango de fechas.
     */
    public LiveData<List<CompraEntity>> getComprasByFechas(Date fechaInicio, Date fechaFin) {
        return compraDao.getComprasByFechas(fechaInicio, fechaFin);
    }

    /**
     * Obtiene todas las compras ordenadas por fecha.
     * @return LiveData con la lista de todas las compras.
     */
    public LiveData<List<CompraEntity>> getAllCompras() {
        return compraDao.getAllCompras();
    }
    
    /**
     * Registra una nueva compra de forma síncrona.
     * @param compra La compra a registrar.
     * @return El ID de la compra insertada.
     */
    public long insertCompraSync(CompraEntity compra) {
        return compraDao.insert(compra);
    }

    /**
     * Interfaz para manejar callbacks de inserción de compras.
     */
    public interface OnCompraInsertedCallback {
        void onCompraInserted(CompraEntity compra);
        void onError(String error);
    }
}