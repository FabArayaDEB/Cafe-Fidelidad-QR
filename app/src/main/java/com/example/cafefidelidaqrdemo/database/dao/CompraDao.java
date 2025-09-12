package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.cafefidelidaqrdemo.database.entities.CompraEntity;

import java.util.Date;
import java.util.List;

/**
 * DAO para manejar operaciones de base de datos relacionadas con las compras.
 */
@Dao
public interface CompraDao {

    /**
     * Inserta una nueva compra en la base de datos.
     * @param compra La compra a insertar.
     * @return El ID generado para la compra.
     */
    @Insert
    long insert(CompraEntity compra);

    /**
     * Obtiene todas las compras de un cliente específico.
     * @param clienteId El ID del cliente.
     * @return Lista de compras del cliente.
     */
    @Query("SELECT * FROM compras WHERE clienteId = :clienteId ORDER BY fecha DESC")
    LiveData<List<CompraEntity>> getComprasByCliente(long clienteId);

    /**
     * Obtiene todas las compras de un cliente por su McID.
     * @param mcId El McID único del cliente.
     * @return Lista de compras del cliente.
     */
    @Query("SELECT * FROM compras WHERE mcId = :mcId ORDER BY fecha DESC")
    LiveData<List<CompraEntity>> getComprasByMcId(String mcId);

    /**
     * Obtiene el total gastado por un cliente.
     * @param clienteId El ID del cliente.
     * @return El monto total gastado.
     */
    @Query("SELECT SUM(monto) FROM compras WHERE clienteId = :clienteId")
    LiveData<Double> getTotalGastadoByCliente(long clienteId);

    /**
     * Obtiene las compras realizadas en un rango de fechas.
     * @param fechaInicio Fecha de inicio.
     * @param fechaFin Fecha de fin.
     * @return Lista de compras en el rango de fechas.
     */
    @Query("SELECT * FROM compras WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha DESC")
    LiveData<List<CompraEntity>> getComprasByFechas(Date fechaInicio, Date fechaFin);

    /**
     * Obtiene todas las compras ordenadas por fecha.
     * @return Lista de todas las compras.
     */
    @Query("SELECT * FROM compras ORDER BY fecha DESC")
    LiveData<List<CompraEntity>> getAllCompras();
}