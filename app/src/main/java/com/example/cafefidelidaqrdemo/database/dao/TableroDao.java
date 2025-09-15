package com.example.cafefidelidaqrdemo.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.cafefidelidaqrdemo.database.entities.TableroEntity;
import java.util.Date;
import java.util.List;

/**
 * DAO para operaciones de tablero personal del cliente
 * Maneja KPIs, métricas y datos del dashboard
 */
@Dao
public interface TableroDao {
    
    // ==================== OPERACIONES BÁSICAS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(TableroEntity tablero);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<TableroEntity> tableros);
    
    @Update
    void actualizar(TableroEntity tablero);
    
    @Delete
    void eliminar(TableroEntity tablero);
    
    @Query("DELETE FROM tablero_cliente WHERE cliente_id = :clienteId")
    void eliminarPorCliente(String clienteId);
    
    @Query("DELETE FROM tablero_cliente")
    void eliminarTodos();
    
    // ==================== CONSULTAS BÁSICAS ====================
    
    @Query("SELECT * FROM tablero_cliente WHERE cliente_id = :clienteId")
    TableroEntity obtenerPorCliente(String clienteId);
    
    @Query("SELECT * FROM tablero_cliente WHERE cliente_id = :clienteId")
    LiveData<TableroEntity> obtenerPorClienteLiveData(String clienteId);
    
    @Query("SELECT * FROM tablero_cliente")
    List<TableroEntity> obtenerTodos();
    
    @Query("SELECT * FROM tablero_cliente")
    LiveData<List<TableroEntity>> obtenerTodosLiveData();
    
    @Query("SELECT COUNT(*) FROM tablero_cliente")
    int contarTableros();
    
    @Query("SELECT COUNT(*) FROM tablero_cliente")
    LiveData<Integer> contarTablerosLiveData();
    
    // ==================== SINCRONIZACIÓN Y CACHE ====================
    
    @Query("SELECT * FROM tablero_cliente WHERE sincronizado = 0")
    List<TableroEntity> obtenerNoSincronizados();
    
    @Query("SELECT * FROM tablero_cliente WHERE cache_valido = 1 AND fecha_expiracion_cache > :fechaActual")
    List<TableroEntity> obtenerCacheValido(Date fechaActual);
    
    @Query("SELECT * FROM tablero_cliente WHERE cache_valido = 0 OR fecha_expiracion_cache <= :fechaActual")
    List<TableroEntity> obtenerCacheExpirado(Date fechaActual);
    
    @Query("UPDATE tablero_cliente SET sincronizado = 1 WHERE cliente_id = :clienteId")
    void marcarComoSincronizado(String clienteId);
    
    @Query("UPDATE tablero_cliente SET cache_valido = 0, fecha_expiracion_cache = :fechaActual WHERE cliente_id = :clienteId")
    void invalidarCache(String clienteId, Date fechaActual);
    
    @Query("UPDATE tablero_cliente SET cache_valido = 1, fecha_expiracion_cache = :fechaExpiracion WHERE cliente_id = :clienteId")
    void actualizarCache(String clienteId, Date fechaExpiracion);
    
    // ==================== MÉTRICAS DE VISITAS ====================
    
    @Query("SELECT SUM(total_visitas) FROM tablero_cliente")
    int obtenerTotalVisitasGlobal();
    
    @Query("SELECT SUM(visitas_mes_actual) FROM tablero_cliente")
    int obtenerVisitasMesActualGlobal();
    
    @Query("SELECT AVG(total_visitas) FROM tablero_cliente")
    double obtenerPromedioVisitas();
    
    @Query("SELECT * FROM tablero_cliente ORDER BY total_visitas DESC LIMIT :limite")
    List<TableroEntity> obtenerTopClientesPorVisitas(int limite);
    
    @Query("SELECT * FROM tablero_cliente WHERE total_visitas >= :minimoVisitas ORDER BY total_visitas DESC")
    List<TableroEntity> obtenerClientesActivosPorVisitas(int minimoVisitas);
    
    @Query("SELECT * FROM tablero_cliente WHERE ultima_visita >= :fechaDesde ORDER BY ultima_visita DESC")
    List<TableroEntity> obtenerClientesConVisitasRecientes(Date fechaDesde);
    
    // ==================== MÉTRICAS DE PUNTOS ====================
    
    @Query("SELECT SUM(puntos_totales) FROM tablero_cliente")
    int obtenerTotalPuntosGlobal();
    
    @Query("SELECT SUM(puntos_disponibles) FROM tablero_cliente")
    int obtenerPuntosDisponiblesGlobal();
    
    @Query("SELECT SUM(puntos_canjeados) FROM tablero_cliente")
    int obtenerPuntosCanjeadosGlobal();
    
    @Query("SELECT AVG(puntos_disponibles) FROM tablero_cliente")
    double obtenerPromedioPuntosDisponibles();
    
    @Query("SELECT * FROM tablero_cliente ORDER BY puntos_disponibles DESC LIMIT :limite")
    List<TableroEntity> obtenerTopClientesPorPuntos(int limite);
    
    @Query("SELECT * FROM tablero_cliente WHERE puntos_disponibles >= :minimoPuntos ORDER BY puntos_disponibles DESC")
    List<TableroEntity> obtenerClientesConPuntosSuficientes(int minimoPuntos);
    
    // ==================== MÉTRICAS DE CANJES ====================
    
    @Query("SELECT SUM(total_canjes) FROM tablero_cliente")
    int obtenerTotalCanjesGlobal();
    
    @Query("SELECT SUM(canjes_mes_actual) FROM tablero_cliente")
    int obtenerCanjesMesActualGlobal();
    
    @Query("SELECT SUM(valor_total_canjes) FROM tablero_cliente")
    double obtenerValorTotalCanjesGlobal();
    
    @Query("SELECT AVG(total_canjes) FROM tablero_cliente")
    double obtenerPromedioCanjes();
    
    @Query("SELECT * FROM tablero_cliente ORDER BY total_canjes DESC LIMIT :limite")
    List<TableroEntity> obtenerTopClientesPorCanjes(int limite);
    
    @Query("SELECT * FROM tablero_cliente WHERE ultimo_canje_fecha >= :fechaDesde ORDER BY ultimo_canje_fecha DESC")
    List<TableroEntity> obtenerClientesConCanjesRecientes(Date fechaDesde);
    
    // ==================== GAMIFICACIÓN Y NIVELES ====================
    
    @Query("SELECT COUNT(*) FROM tablero_cliente WHERE nivel_fidelidad = :nivel")
    int contarClientesPorNivel(String nivel);
    
    @Query("SELECT nivel_fidelidad, COUNT(*) as cantidad FROM tablero_cliente GROUP BY nivel_fidelidad")
    List<NivelFidelidadCount> obtenerDistribucionNiveles();
    
    @Query("SELECT * FROM tablero_cliente WHERE nivel_fidelidad = :nivel ORDER BY puntos_disponibles DESC")
    List<TableroEntity> obtenerClientesPorNivel(String nivel);
    
    @Query("SELECT * FROM tablero_cliente WHERE progreso_nivel >= :progresoMinimo ORDER BY progreso_nivel DESC")
    List<TableroEntity> obtenerClientesCercaDelSiguienteNivel(double progresoMinimo);
    
    // ==================== SUCURSALES ====================
    
    @Query("SELECT sucursal_favorita_id, COUNT(*) as cantidad FROM tablero_cliente WHERE sucursal_favorita_id IS NOT NULL GROUP BY sucursal_favorita_id ORDER BY cantidad DESC")
    List<SucursalFavoritaCount> obtenerSucursalesFavoritas();
    
    @Query("SELECT * FROM tablero_cliente WHERE sucursal_favorita_id = :sucursalId")
    List<TableroEntity> obtenerClientesPorSucursalFavorita(Long sucursalId);
    
    @Query("SELECT * FROM tablero_cliente WHERE sucursal_recomendada_id = :sucursalId")
    List<TableroEntity> obtenerClientesConSucursalRecomendada(Long sucursalId);
    
    // ==================== BENEFICIOS Y RECOMENDACIONES ====================
    
    @Query("SELECT * FROM tablero_cliente WHERE beneficios_disponibles > 0 ORDER BY beneficios_disponibles DESC")
    List<TableroEntity> obtenerClientesConBeneficiosDisponibles();
    
    @Query("SELECT * FROM tablero_cliente WHERE beneficios_canjeables > 0 ORDER BY beneficios_canjeables DESC")
    List<TableroEntity> obtenerClientesConBeneficiosCanjeables();
    
    @Query("SELECT beneficio_recomendado_id, COUNT(*) as cantidad FROM tablero_cliente WHERE beneficio_recomendado_id IS NOT NULL GROUP BY beneficio_recomendado_id ORDER BY cantidad DESC")
    List<BeneficioRecomendadoCount> obtenerBeneficiosMasRecomendados();
    
    @Query("SELECT * FROM tablero_cliente WHERE beneficio_recomendado_id = :beneficioId")
    List<TableroEntity> obtenerClientesConBeneficioRecomendado(String beneficioId);
    
    @Query("SELECT * FROM tablero_cliente WHERE puntos_disponibles >= beneficio_recomendado_puntos AND beneficio_recomendado_puntos > 0")
    List<TableroEntity> obtenerClientesQuePuedenCanjearRecomendacion();
    
    // ==================== ANÁLISIS DE COMPORTAMIENTO ====================
    
    @Query("SELECT * FROM tablero_cliente WHERE racha_visitas >= :rachaMinima ORDER BY racha_visitas DESC")
    List<TableroEntity> obtenerClientesConRachaVisitas(int rachaMinima);
    
    @Query("SELECT AVG(racha_visitas) FROM tablero_cliente")
    double obtenerPromedioRachaVisitas();
    
    @Query("SELECT * FROM tablero_cliente WHERE progreso_meta_visitas >= :progresoMinimo ORDER BY progreso_meta_visitas DESC")
    List<TableroEntity> obtenerClientesCercaDeMeta(double progresoMinimo);
    
    @Query("SELECT * FROM tablero_cliente WHERE (julianday('now') - julianday(ultima_visita)) <= 30 ORDER BY ultima_visita DESC")
    List<TableroEntity> obtenerClientesActivos();
    
    @Query("SELECT * FROM tablero_cliente WHERE (julianday('now') - julianday(ultima_visita)) > 30 ORDER BY ultima_visita ASC")
    List<TableroEntity> obtenerClientesInactivos();
    
    // ==================== AUDITORÍA Y CONTROL ====================
    
    @Query("SELECT * FROM tablero_cliente WHERE fecha_actualizacion >= :fechaDesde ORDER BY fecha_actualizacion DESC")
    List<TableroEntity> obtenerActualizacionesRecientes(Date fechaDesde);
    
    @Query("SELECT * FROM tablero_cliente WHERE fecha_actualizacion BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha_actualizacion DESC")
    List<TableroEntity> obtenerActualizacionesEnRango(Date fechaInicio, Date fechaFin);
    
    @Query("SELECT DATE(fecha_actualizacion) as fecha, COUNT(*) as cantidad FROM tablero_cliente WHERE fecha_actualizacion >= :fechaDesde GROUP BY DATE(fecha_actualizacion) ORDER BY fecha DESC")
    List<ActualizacionesPorDia> obtenerActualizacionesPorDia(Date fechaDesde);
    
    // ==================== MANTENIMIENTO ====================
    
    @Query("DELETE FROM tablero_cliente WHERE fecha_actualizacion < :fechaLimite")
    void limpiarDatosAntiguos(Date fechaLimite);
    
    @Query("DELETE FROM tablero_cliente WHERE cache_valido = 0 AND fecha_expiracion_cache < :fechaActual")
    void limpiarCacheExpirado(Date fechaActual);
    
    @Query("UPDATE tablero_cliente SET version = version + 1, sincronizado = 0, fecha_actualizacion = :fechaActual WHERE cliente_id = :clienteId")
    void incrementarVersion(String clienteId, Date fechaActual);
    
    @Query("SELECT MAX(version) FROM tablero_cliente WHERE cliente_id = :clienteId")
    Long obtenerUltimaVersion(String clienteId);
    
    // ==================== CLASES AUXILIARES ====================
    
    class NivelFidelidadCount {
        public String nivel_fidelidad;
        public int cantidad;
    }
    
    class SucursalFavoritaCount {
        public Long sucursal_favorita_id;
        public int cantidad;
    }
    
    class BeneficioRecomendadoCount {
        public String beneficio_recomendado_id;
        public int cantidad;
    }
    
    class ActualizacionesPorDia {
        public String fecha;
        public int cantidad;
    }
}