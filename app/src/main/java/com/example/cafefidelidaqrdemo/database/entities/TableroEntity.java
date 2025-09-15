package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import java.util.Date;

/**
 * Entidad para almacenar KPIs del tablero personal del cliente
 * Contiene métricas personalizadas y estadísticas de actividad
 */
@Entity(tableName = "tablero_cliente")
public class TableroEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "cliente_id")
    private String clienteId;
    
    @ColumnInfo(name = "nombre_cliente")
    private String nombreCliente;
    
    // Métricas de visitas
    @ColumnInfo(name = "total_visitas")
    private int totalVisitas;
    
    @ColumnInfo(name = "visitas_mes_actual")
    private int visitasMesActual;
    
    @ColumnInfo(name = "visitas_semana_actual")
    private int visitasSemanaActual;
    
    @ColumnInfo(name = "ultima_visita")
    private Date ultimaVisita;
    
    @ColumnInfo(name = "sucursal_favorita_id")
    private Long sucursalFavoritaId;
    
    @ColumnInfo(name = "sucursal_favorita_nombre")
    private String sucursalFavoritaNombre;
    
    // Métricas de puntos y beneficios
    @ColumnInfo(name = "puntos_totales")
    private int puntosTotales;
    
    @ColumnInfo(name = "puntos_disponibles")
    private int puntosDisponibles;
    
    @ColumnInfo(name = "puntos_canjeados")
    private int puntosCanjeados;
    
    @ColumnInfo(name = "beneficios_disponibles")
    private int beneficiosDisponibles;
    
    @ColumnInfo(name = "beneficios_canjeables")
    private int beneficiosCanjeables;
    
    // Métricas de canjes
    @ColumnInfo(name = "total_canjes")
    private int totalCanjes;
    
    @ColumnInfo(name = "canjes_mes_actual")
    private int canjesMesActual;
    
    @ColumnInfo(name = "ultimo_canje_fecha")
    private Date ultimoCanjeFecha;
    
    @ColumnInfo(name = "ultimo_canje_beneficio")
    private String ultimoCanjeBeneficio;
    
    @ColumnInfo(name = "valor_total_canjes")
    private double valorTotalCanjes;
    
    // Gamificación
    @ColumnInfo(name = "nivel_fidelidad")
    private String nivelFidelidad; // "Bronce", "Plata", "Oro", "Platino"
    
    @ColumnInfo(name = "puntos_siguiente_nivel")
    private int puntosSiguienteNivel;
    
    @ColumnInfo(name = "progreso_nivel")
    private double progresoNivel; // Porcentaje 0-100
    
    @ColumnInfo(name = "racha_visitas")
    private int rachaVisitas; // Días consecutivos con visitas
    
    @ColumnInfo(name = "meta_visitas_mes")
    private int metaVisitasMes;
    
    @ColumnInfo(name = "progreso_meta_visitas")
    private double progresoMetaVisitas;
    
    // Recomendaciones
    @ColumnInfo(name = "beneficio_recomendado_id")
    private String beneficioRecomendadoId;
    
    @ColumnInfo(name = "beneficio_recomendado_nombre")
    private String beneficioRecomendadoNombre;
    
    @ColumnInfo(name = "beneficio_recomendado_puntos")
    private int beneficioRecomendadoPuntos;
    
    @ColumnInfo(name = "sucursal_recomendada_id")
    private Long sucursalRecomendadaId;
    
    @ColumnInfo(name = "sucursal_recomendada_nombre")
    private String sucursalRecomendadaNombre;
    
    // Control de versión y sincronización
    @ColumnInfo(name = "fecha_actualizacion")
    private Date fechaActualizacion;
    
    @ColumnInfo(name = "version")
    private long version;
    
    @ColumnInfo(name = "sincronizado")
    private boolean sincronizado;
    
    @ColumnInfo(name = "cache_valido")
    private boolean cacheValido;
    
    @ColumnInfo(name = "fecha_expiracion_cache")
    private Date fechaExpiracionCache;
    
    // Constructores
    public TableroEntity() {
        this.fechaActualizacion = new Date();
        this.version = 1;
        this.sincronizado = false;
        this.cacheValido = true;
        // Cache válido por 1 hora
        this.fechaExpiracionCache = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
    }
    
    public TableroEntity(String clienteId, String nombreCliente) {
        this();
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
    }
    
    // Getters y Setters
    @NonNull
    public String getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(@NonNull String clienteId) {
        this.clienteId = clienteId;
    }
    
    public String getNombreCliente() {
        return nombreCliente;
    }
    
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    public int getTotalVisitas() {
        return totalVisitas;
    }
    
    public void setTotalVisitas(int totalVisitas) {
        this.totalVisitas = totalVisitas;
    }
    
    public int getVisitasMesActual() {
        return visitasMesActual;
    }
    
    public void setVisitasMesActual(int visitasMesActual) {
        this.visitasMesActual = visitasMesActual;
    }
    
    public int getVisitasSemanaActual() {
        return visitasSemanaActual;
    }
    
    public void setVisitasSemanaActual(int visitasSemanaActual) {
        this.visitasSemanaActual = visitasSemanaActual;
    }
    
    public Date getUltimaVisita() {
        return ultimaVisita;
    }
    
    public void setUltimaVisita(Date ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }
    
    public Long getSucursalFavoritaId() {
        return sucursalFavoritaId;
    }
    
    public void setSucursalFavoritaId(Long sucursalFavoritaId) {
        this.sucursalFavoritaId = sucursalFavoritaId;
    }
    
    public String getSucursalFavoritaNombre() {
        return sucursalFavoritaNombre;
    }
    
    public void setSucursalFavoritaNombre(String sucursalFavoritaNombre) {
        this.sucursalFavoritaNombre = sucursalFavoritaNombre;
    }
    
    public int getPuntosTotales() {
        return puntosTotales;
    }
    
    public void setPuntosTotales(int puntosTotales) {
        this.puntosTotales = puntosTotales;
    }
    
    public int getPuntosDisponibles() {
        return puntosDisponibles;
    }
    
    public void setPuntosDisponibles(int puntosDisponibles) {
        this.puntosDisponibles = puntosDisponibles;
    }
    
    public int getPuntosCanjeados() {
        return puntosCanjeados;
    }
    
    public void setPuntosCanjeados(int puntosCanjeados) {
        this.puntosCanjeados = puntosCanjeados;
    }
    
    public int getBeneficiosDisponibles() {
        return beneficiosDisponibles;
    }
    
    public void setBeneficiosDisponibles(int beneficiosDisponibles) {
        this.beneficiosDisponibles = beneficiosDisponibles;
    }
    
    public int getBeneficiosCanjeables() {
        return beneficiosCanjeables;
    }
    
    public void setBeneficiosCanjeables(int beneficiosCanjeables) {
        this.beneficiosCanjeables = beneficiosCanjeables;
    }
    
    public int getTotalCanjes() {
        return totalCanjes;
    }
    
    public void setTotalCanjes(int totalCanjes) {
        this.totalCanjes = totalCanjes;
    }
    
    public int getCanjesMesActual() {
        return canjesMesActual;
    }
    
    public void setCanjesMesActual(int canjesMesActual) {
        this.canjesMesActual = canjesMesActual;
    }
    
    public Date getUltimoCanjeFecha() {
        return ultimoCanjeFecha;
    }
    
    public void setUltimoCanjeFecha(Date ultimoCanjeFecha) {
        this.ultimoCanjeFecha = ultimoCanjeFecha;
    }
    
    public String getUltimoCanjeBeneficio() {
        return ultimoCanjeBeneficio;
    }
    
    public void setUltimoCanjeBeneficio(String ultimoCanjeBeneficio) {
        this.ultimoCanjeBeneficio = ultimoCanjeBeneficio;
    }
    
    public double getValorTotalCanjes() {
        return valorTotalCanjes;
    }
    
    public void setValorTotalCanjes(double valorTotalCanjes) {
        this.valorTotalCanjes = valorTotalCanjes;
    }
    
    public String getNivelFidelidad() {
        return nivelFidelidad;
    }
    
    public void setNivelFidelidad(String nivelFidelidad) {
        this.nivelFidelidad = nivelFidelidad;
    }
    
    public int getPuntosSiguienteNivel() {
        return puntosSiguienteNivel;
    }
    
    public void setPuntosSiguienteNivel(int puntosSiguienteNivel) {
        this.puntosSiguienteNivel = puntosSiguienteNivel;
    }
    
    public double getProgresoNivel() {
        return progresoNivel;
    }
    
    public void setProgresoNivel(double progresoNivel) {
        this.progresoNivel = progresoNivel;
    }
    
    public int getRachaVisitas() {
        return rachaVisitas;
    }
    
    public void setRachaVisitas(int rachaVisitas) {
        this.rachaVisitas = rachaVisitas;
    }
    
    public int getMetaVisitasMes() {
        return metaVisitasMes;
    }
    
    public void setMetaVisitasMes(int metaVisitasMes) {
        this.metaVisitasMes = metaVisitasMes;
    }
    
    public double getProgresoMetaVisitas() {
        return progresoMetaVisitas;
    }
    
    public void setProgresoMetaVisitas(double progresoMetaVisitas) {
        this.progresoMetaVisitas = progresoMetaVisitas;
    }
    
    public String getBeneficioRecomendadoId() {
        return beneficioRecomendadoId;
    }
    
    public void setBeneficioRecomendadoId(String beneficioRecomendadoId) {
        this.beneficioRecomendadoId = beneficioRecomendadoId;
    }
    
    public String getBeneficioRecomendadoNombre() {
        return beneficioRecomendadoNombre;
    }
    
    public void setBeneficioRecomendadoNombre(String beneficioRecomendadoNombre) {
        this.beneficioRecomendadoNombre = beneficioRecomendadoNombre;
    }
    
    public int getBeneficioRecomendadoPuntos() {
        return beneficioRecomendadoPuntos;
    }
    
    public void setBeneficioRecomendadoPuntos(int beneficioRecomendadoPuntos) {
        this.beneficioRecomendadoPuntos = beneficioRecomendadoPuntos;
    }
    
    public Long getSucursalRecomendadaId() {
        return sucursalRecomendadaId;
    }
    
    public void setSucursalRecomendadaId(Long sucursalRecomendadaId) {
        this.sucursalRecomendadaId = sucursalRecomendadaId;
    }
    
    public String getSucursalRecomendadaNombre() {
        return sucursalRecomendadaNombre;
    }
    
    public void setSucursalRecomendadaNombre(String sucursalRecomendadaNombre) {
        this.sucursalRecomendadaNombre = sucursalRecomendadaNombre;
    }
    
    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    public boolean isSincronizado() {
        return sincronizado;
    }
    
    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
    
    public boolean isCacheValido() {
        return cacheValido;
    }
    
    public void setCacheValido(boolean cacheValido) {
        this.cacheValido = cacheValido;
    }
    
    public Date getFechaExpiracionCache() {
        return fechaExpiracionCache;
    }
    
    public void setFechaExpiracionCache(Date fechaExpiracionCache) {
        this.fechaExpiracionCache = fechaExpiracionCache;
    }
    
    // Métodos de utilidad
    public void incrementarVersion() {
        this.version++;
        this.fechaActualizacion = new Date();
        this.sincronizado = false;
    }
    
    public void marcarComoSincronizado() {
        this.sincronizado = true;
    }
    
    public void actualizarCache() {
        this.cacheValido = true;
        this.fechaExpiracionCache = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
    }
    
    public boolean esCacheExpirado() {
        return new Date().after(fechaExpiracionCache);
    }
    
    public void invalidarCache() {
        this.cacheValido = false;
        this.fechaExpiracionCache = new Date();
    }
    
    public boolean puedeCanjeaBeneficioRecomendado() {
        return puntosDisponibles >= beneficioRecomendadoPuntos;
    }
    
    public int getDiasDesdeUltimaVisita() {
        if (ultimaVisita == null) return -1;
        long diff = new Date().getTime() - ultimaVisita.getTime();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }
    
    public boolean esClienteActivo() {
        return getDiasDesdeUltimaVisita() <= 30;
    }
    
    public String getEstadoProgreso() {
        if (progresoNivel >= 80) return "Cerca del siguiente nivel";
        if (progresoNivel >= 50) return "Progreso medio";
        return "Inicio de nivel";
    }
    
    public String getMensajeMotivacional() {
        if (rachaVisitas >= 7) return "¡Excelente racha de visitas!";
        if (puntosDisponibles >= beneficioRecomendadoPuntos) return "¡Tienes puntos para canjear!";
        return "¡Sigue visitando para ganar más puntos!";
    }
    
    @Override
    public String toString() {
        return "TableroEntity{" +
                "clienteId='" + clienteId + '\'' +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", totalVisitas=" + totalVisitas +
                ", puntosTotales=" + puntosTotales +
                ", nivelFidelidad='" + nivelFidelidad + '\'' +
                '}';
    }
}