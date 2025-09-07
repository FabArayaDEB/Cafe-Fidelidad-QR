package com.example.cafefidelidaqrdemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;
import java.util.Date;
import java.util.List;

/**
 * DAO para operaciones de reportes administrativos
 * Incluye consultas agregadas para métricas y estadísticas
 */
@Dao
public interface ReporteAdminDao {
    
    // Operaciones básicas CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReporte(ReporteEntity reporte);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReportes(List<ReporteEntity> reportes);
    
    @Update
    void updateReporte(ReporteEntity reporte);
    
    @Delete
    void deleteReporte(ReporteEntity reporte);
    
    @Query("DELETE FROM reportes WHERE id = :reporteId")
    void deleteReporteById(String reporteId);
    
    // Consultas básicas
    @Query("SELECT * FROM reportes WHERE id = :reporteId")
    LiveData<ReporteEntity> getReporteById(String reporteId);
    
    @Query("SELECT * FROM reportes WHERE id = :reporteId")
    ReporteEntity getReporteByIdSync(String reporteId);
    
    @Query("SELECT * FROM reportes ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getAllReportes();
    
    @Query("SELECT * FROM reportes ORDER BY fecha_generacion DESC")
    List<ReporteEntity> getAllReportesSync();
    
    // Consultas por tipo de reporte
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getReportesByTipo(String tipo);
    
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo ORDER BY fecha_generacion DESC LIMIT :limit")
    List<ReporteEntity> getReportesByTipoSync(String tipo, int limit);
    
    // Consultas por rango de fechas
    @Query("SELECT * FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getReportesByRangoFechas(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT * FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY fecha_generacion DESC")
    List<ReporteEntity> getReportesByRangoFechasSync(Date fechaInicio, Date fechaFin);
    
    // Consultas por sucursal
    @Query("SELECT * FROM reportes WHERE sucursal_id = :sucursalId ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getReportesBySucursal(Long sucursalId);
    
    @Query("SELECT * FROM reportes WHERE sucursal_id = :sucursalId ORDER BY fecha_generacion DESC")
    List<ReporteEntity> getReportesBySucursalSync(Long sucursalId);
    
    // Consultas por beneficio
    @Query("SELECT * FROM reportes WHERE beneficio_id = :beneficioId ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getReportesByBeneficio(String beneficioId);
    
    // Consultas con filtros combinados
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo AND sucursal_id = :sucursalId AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY fecha_generacion DESC")
    LiveData<List<ReporteEntity>> getReportesFiltrados(String tipo, Long sucursalId, Date fechaInicio, Date fechaFin);
    
    @Query("SELECT * FROM reportes WHERE tipo_reporte = :tipo AND sucursal_id = :sucursalId AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY fecha_generacion DESC")
    List<ReporteEntity> getReportesFiltradosSync(String tipo, Long sucursalId, Date fechaInicio, Date fechaFin);
    
    // Consultas agregadas para métricas generales
    @Query("SELECT SUM(total_visitas) FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    int getTotalVisitasPeriodo(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT SUM(total_canjes) FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    int getTotalCanjesPeriodo(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT SUM(valor_total_canjes) FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    double getValorTotalCanjesPeriodo(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT AVG(promedio_visitas_dia) FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    double getPromedioVisitasDiaPeriodo(Date fechaInicio, Date fechaFin);
    
    // Consultas para top clientes
    @Query("SELECT * FROM reportes WHERE tipo_reporte = 'top_clientes' ORDER BY cliente_top_visitas DESC LIMIT :limit")
    List<ReporteEntity> getTopClientesPorVisitas(int limit);
    
    @Query("SELECT DISTINCT cliente_top_nombre, cliente_top_visitas FROM reportes WHERE cliente_top_nombre IS NOT NULL ORDER BY cliente_top_visitas DESC LIMIT :limit")
    List<ReporteEntity> getClientesMasActivos(int limit);
    
    // Consultas por sucursal específica
    @Query("SELECT SUM(total_visitas) FROM reportes WHERE sucursal_id = :sucursalId AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    int getTotalVisitasSucursal(Long sucursalId, Date fechaInicio, Date fechaFin);
    
    @Query("SELECT SUM(total_canjes) FROM reportes WHERE sucursal_id = :sucursalId AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin")
    int getTotalCanjesSucursal(Long sucursalId, Date fechaInicio, Date fechaFin);
    
    // Consultas de rendimiento por sucursal
    @Query("SELECT sucursal_nombre, SUM(total_visitas) as visitas, SUM(total_canjes) as canjes FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin GROUP BY sucursal_id ORDER BY visitas DESC")
    List<ReporteEntity> getRankingSucursalesPorVisitas(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT sucursal_nombre, SUM(total_canjes) as canjes, SUM(valor_total_canjes) as valor FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin GROUP BY sucursal_id ORDER BY valor DESC")
    List<ReporteEntity> getRankingSucursalesPorValor(Date fechaInicio, Date fechaFin);
    
    // Consultas de tendencias temporales
    @Query("SELECT fecha_inicio, SUM(total_visitas) as visitas FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin GROUP BY fecha_inicio ORDER BY fecha_inicio ASC")
    List<ReporteEntity> getTendenciaVisitas(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT fecha_inicio, SUM(total_canjes) as canjes FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin GROUP BY fecha_inicio ORDER BY fecha_inicio ASC")
    List<ReporteEntity> getTendenciaCanjes(Date fechaInicio, Date fechaFin);
    
    // Consultas de sincronización
    @Query("SELECT * FROM reportes WHERE sincronizado = 0 ORDER BY fecha_generacion ASC")
    List<ReporteEntity> getReportesNoSincronizados();
    
    @Query("UPDATE reportes SET sincronizado = 1 WHERE id = :reporteId")
    void marcarComoSincronizado(String reporteId);
    
    @Query("UPDATE reportes SET sincronizado = 1 WHERE id IN (:reporteIds)")
    void marcarComoSincronizados(List<String> reporteIds);
    
    // Consultas de limpieza
    @Query("DELETE FROM reportes WHERE fecha_generacion < :fechaLimite")
    void eliminarReportesAntiguos(Date fechaLimite);
    
    @Query("DELETE FROM reportes WHERE sincronizado = 1 AND fecha_generacion < :fechaLimite")
    void eliminarReportesSincronizadosAntiguos(Date fechaLimite);
    
    // Consultas de estadísticas
    @Query("SELECT COUNT(*) FROM reportes")
    int getConteoTotalReportes();
    
    @Query("SELECT COUNT(*) FROM reportes WHERE sincronizado = 0")
    int getConteoReportesNoSincronizados();
    
    @Query("SELECT COUNT(*) FROM reportes WHERE tipo_reporte = :tipo")
    int getConteoReportesPorTipo(String tipo);
    
    @Query("SELECT MAX(fecha_generacion) FROM reportes WHERE tipo_reporte = :tipo")
    Date getUltimaFechaGeneracionPorTipo(String tipo);
    
    // Consulta para verificar existencia
    @Query("SELECT COUNT(*) > 0 FROM reportes WHERE id = :reporteId")
    boolean existeReporte(String reporteId);
    
    // Consultas para exportación
    @Query("SELECT * FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY tipo_reporte, sucursal_nombre, fecha_inicio")
    List<ReporteEntity> getReportesParaExportacion(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT * FROM reportes WHERE sucursal_id = :sucursalId AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin ORDER BY fecha_inicio")
    List<ReporteEntity> getReportesSucursalParaExportacion(Long sucursalId, Date fechaInicio, Date fechaFin);
    
    // Consulta para obtener top clientes
    @Query("SELECT cliente_top_nombre as nombre, cliente_top_visitas as totalVisitas, 0 as totalCanjes, 0.0 as valorTotalCanjes, sucursal_nombre as sucursalFavorita FROM reportes WHERE cliente_top_nombre IS NOT NULL AND fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin GROUP BY cliente_top_nombre ORDER BY cliente_top_visitas DESC LIMIT :limite")
    List<TopCliente> obtenerTopClientes(int limite, Date fechaInicio, Date fechaFin);
    
    // Consulta para obtener métricas de canjes
    @Query("SELECT SUM(total_canjes) as totalCanjes, SUM(valor_total_canjes) as valorTotal, AVG(valor_total_canjes) as valorPromedio, COUNT(DISTINCT sucursal_id) as sucursalesActivas FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin AND (:beneficioId IS NULL OR beneficio_id = :beneficioId)")
    MetricasCanjes obtenerMetricasCanjes(Date fechaInicio, Date fechaFin, String beneficioId);
    
    // Consulta para obtener métricas de visitas
    @Query("SELECT SUM(total_visitas) as totalVisitas, AVG(promedio_visitas_dia) as promedioVisitasDia, COUNT(DISTINCT cliente_top_nombre) as clientesUnicos, MAX(total_visitas) as maxVisitasDia FROM reportes WHERE fecha_inicio >= :fechaInicio AND fecha_fin <= :fechaFin AND (:sucursalId IS NULL OR sucursal_id = :sucursalId)")
    MetricasVisitas obtenerMetricasVisitas(Date fechaInicio, Date fechaFin, Long sucursalId);
    
    /**
     * Clase para representar información de top clientes
     */
    class TopCliente {
        public String nombre;
        public int totalVisitas;
        public int totalCanjes;
        public double valorTotalCanjes;
        public String sucursalFavorita;
        
        public TopCliente(String nombre, int totalVisitas, int totalCanjes, double valorTotalCanjes, String sucursalFavorita) {
            this.nombre = nombre;
            this.totalVisitas = totalVisitas;
            this.totalCanjes = totalCanjes;
            this.valorTotalCanjes = valorTotalCanjes;
            this.sucursalFavorita = sucursalFavorita;
        }
    }
    
    /**
     * Clase para representar métricas de canjes
     */
    class MetricasCanjes {
        public int totalCanjes;
        public double valorTotal;
        public double valorPromedio;
        public int sucursalesActivas;
        
        public MetricasCanjes(int totalCanjes, double valorTotal, double valorPromedio, int sucursalesActivas) {
            this.totalCanjes = totalCanjes;
            this.valorTotal = valorTotal;
            this.valorPromedio = valorPromedio;
            this.sucursalesActivas = sucursalesActivas;
        }
    }
    
    /**
     * Clase para representar métricas de visitas
     */
    class MetricasVisitas {
        public int totalVisitas;
        public double promedioVisitasDia;
        public int clientesUnicos;
        public int maxVisitasDia;
        
        public MetricasVisitas(int totalVisitas, double promedioVisitasDia, int clientesUnicos, int maxVisitasDia) {
            this.totalVisitas = totalVisitas;
            this.promedioVisitasDia = promedioVisitasDia;
            this.clientesUnicos = clientesUnicos;
            this.maxVisitasDia = maxVisitasDia;
        }
    }
}