package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.database.models.MetricasVisitas;
import com.example.cafefidelidaqrdemo.database.models.MetricasCanjes;
import com.example.cafefidelidaqrdemo.database.models.TopCliente;

import java.util.List;

@Dao
public interface ReporteDao {
    
    @Insert
    void insertar(ReporteEntity reporte);
    
    @Update
    void actualizar(ReporteEntity reporte);
    
    @Delete
    void eliminar(ReporteEntity reporte);
    
    @Query("SELECT * FROM reportes")
    List<ReporteEntity> obtenerTodos();
    
    @Query("SELECT * FROM reportes")
    LiveData<List<ReporteEntity>> obtenerTodosLiveData();
    
    @Query("SELECT * FROM reportes WHERE id = :reporteId")
    ReporteEntity obtenerPorId(String reporteId);
    
    @Query("SELECT * FROM reportes WHERE id = :reporteId")
    LiveData<ReporteEntity> obtenerPorIdLiveData(String reporteId);
    
    @Query("SELECT * FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    List<ReporteEntity> obtenerPorRangoFechas(String fechaInicio, String fechaFin);
    
    @Query("SELECT * FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    LiveData<List<ReporteEntity>> obtenerPorRangoFechasLiveData(java.util.Date fechaInicio, java.util.Date fechaFin);
    
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo")
    List<ReporteEntity> obtenerPorTipo(String tipo);
    
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo")
    LiveData<List<ReporteEntity>> obtenerPorTipoLiveData(String tipo);
    
    @Query("SELECT * FROM reportes WHERE sucursal_id = :sucursalId")
    List<ReporteEntity> obtenerPorSucursal(String sucursalId);
    
    @Query("SELECT * FROM reportes WHERE sucursal_id = :sucursalId")
    LiveData<List<ReporteEntity>> obtenerPorSucursalLiveData(Long sucursalId);
    
    @Query("SELECT * FROM reportes WHERE beneficio_id = :beneficioId")
    LiveData<List<ReporteEntity>> obtenerPorBeneficioLiveData(String beneficioId);
    
    @Query("SELECT * FROM reportes WHERE (:tipo IS NULL OR tipo_reporte = :tipo) AND (:fechaInicio IS NULL OR fecha_inicio >= :fechaInicio) AND (:fechaFin IS NULL OR fecha_fin <= :fechaFin) AND (:sucursalId IS NULL OR sucursal_id = :sucursalId) AND (:beneficioId IS NULL OR beneficio_id = :beneficioId)")
    LiveData<List<ReporteEntity>> obtenerConFiltrosLiveData(String tipo, java.util.Date fechaInicio, java.util.Date fechaFin, Long sucursalId, String beneficioId);
    
    @Query("SELECT SUM(total_visitas) as totalVisitas, SUM(nuevos_clientes) as nuevosClientes, SUM(total_clientes_activos - nuevos_clientes) as clientesRecurrentes, AVG(CAST(total_visitas AS REAL) / NULLIF(total_clientes_activos, 0)) as promedioVisitasPorCliente FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin AND (:sucursalId IS NULL OR sucursal_id = :sucursalId)")
    MetricasVisitas obtenerMetricasVisitas(java.util.Date fechaInicio, java.util.Date fechaFin, Long sucursalId);
    
    @Query("SELECT SUM(total_canjes) as totalCanjes, SUM(total_canjes) as puntosCanjeados, COUNT(DISTINCT beneficio_id) as beneficiosUtilizados, AVG(CAST(total_canjes AS REAL) / NULLIF(total_clientes_activos, 0)) as promedioCanjesPorCliente FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin AND (:beneficioId IS NULL OR beneficio_id = :beneficioId)")
    MetricasCanjes obtenerMetricasCanjes(java.util.Date fechaInicio, java.util.Date fechaFin, String beneficioId);
    
    @Query("SELECT cliente_top_id as clienteId, cliente_top_nombre as nombre, cliente_top_visitas as totalVisitas, total_canjes as totalCanjes, 0 as totalPuntos, valor_total_canjes as valorTotalCanjes, sucursal_nombre as sucursalFavorita FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY cliente_top_visitas DESC LIMIT :limite")
    List<TopCliente> obtenerTopClientes(int limite, java.util.Date fechaInicio, java.util.Date fechaFin);
    
    @Query("SELECT * FROM reportes WHERE sincronizado = 0")
    List<ReporteEntity> obtenerNoSincronizados();
    
    @Query("DELETE FROM reportes")
    void eliminarTodos();
}