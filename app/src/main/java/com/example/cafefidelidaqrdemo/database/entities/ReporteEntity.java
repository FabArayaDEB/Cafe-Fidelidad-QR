package com.example.cafefidelidaqrdemo.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import java.util.Date;

/**
 * Entidad para almacenar datos de reportes administrativos
 * Contiene métricas agregadas de visitas, canjes y clientes
 */
@Entity(tableName = "reportes")
public class ReporteEntity {
    
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    
    @ColumnInfo(name = "tipo_reporte")
    private String tipoReporte; // "visitas", "canjes", "top_clientes"
    
    @ColumnInfo(name = "fecha_inicio")
    private Date fechaInicio;
    
    @ColumnInfo(name = "fecha_fin")
    private Date fechaFin;
    
    @ColumnInfo(name = "sucursal_id")
    private Long sucursalId;
    
    @ColumnInfo(name = "sucursal_nombre")
    private String sucursalNombre;
    
    @ColumnInfo(name = "beneficio_id")
    private String beneficioId;
    
    @ColumnInfo(name = "beneficio_nombre")
    private String beneficioNombre;
    
    // Métricas de visitas
    @ColumnInfo(name = "total_visitas")
    private int totalVisitas;
    
    @ColumnInfo(name = "visitas_unicas")
    private int visitasUnicas;
    
    @ColumnInfo(name = "promedio_visitas_dia")
    private double promedioVisitasDia;
    
    // Métricas de canjes
    @ColumnInfo(name = "total_canjes")
    private int totalCanjes;
    
    @ColumnInfo(name = "valor_total_canjes")
    private double valorTotalCanjes;
    
    @ColumnInfo(name = "promedio_canjes_dia")
    private double promedioCanjesDia;
    
    // Métricas de clientes
    @ColumnInfo(name = "total_clientes_activos")
    private int totalClientesActivos;
    
    @ColumnInfo(name = "nuevos_clientes")
    private int nuevosClientes;
    
    @ColumnInfo(name = "cliente_top_id")
    private String clienteTopId;
    
    @ColumnInfo(name = "cliente_top_nombre")
    private String clienteTopNombre;
    
    @ColumnInfo(name = "cliente_top_visitas")
    private int clienteTopVisitas;
    
    // Metadatos
    @ColumnInfo(name = "fecha_generacion")
    private Date fechaGeneracion;
    
    @ColumnInfo(name = "version")
    private long version;
    
    @ColumnInfo(name = "sincronizado")
    private boolean sincronizado;
    
    @ColumnInfo(name = "estado_sincronizacion")
    private String estadoSincronizacion;
    
    // Constructor
    public ReporteEntity() {
        this.fechaGeneracion = new Date();
        this.version = 1;
        this.sincronizado = false;
    }
    
    public ReporteEntity(String id, String tipoReporte, Date fechaInicio, Date fechaFin) {
        this();
        this.id = id;
        this.tipoReporte = tipoReporte;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    
    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
    
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
    
    public String getBeneficioId() { return beneficioId; }
    public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
    
    public String getBeneficioNombre() { return beneficioNombre; }
    public void setBeneficioNombre(String beneficioNombre) { this.beneficioNombre = beneficioNombre; }
    
    public int getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
    
    public int getVisitasUnicas() { return visitasUnicas; }
    public void setVisitasUnicas(int visitasUnicas) { this.visitasUnicas = visitasUnicas; }
    
    public double getPromedioVisitasDia() { return promedioVisitasDia; }
    public void setPromedioVisitasDia(double promedioVisitasDia) { this.promedioVisitasDia = promedioVisitasDia; }
    
    public int getTotalCanjes() { return totalCanjes; }
    public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
    
    public double getValorTotalCanjes() { return valorTotalCanjes; }
    public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
    
    public double getPromedioCanjesDia() { return promedioCanjesDia; }
    public void setPromedioCanjesDia(double promedioCanjesDia) { this.promedioCanjesDia = promedioCanjesDia; }
    
    public int getTotalClientesActivos() { return totalClientesActivos; }
    public void setTotalClientesActivos(int totalClientesActivos) { this.totalClientesActivos = totalClientesActivos; }
    
    public int getNuevosClientes() { return nuevosClientes; }
    public void setNuevosClientes(int nuevosClientes) { this.nuevosClientes = nuevosClientes; }
    
    public String getClienteTopId() { return clienteTopId; }
    public void setClienteTopId(String clienteTopId) { this.clienteTopId = clienteTopId; }
    
    public String getClienteTopNombre() { return clienteTopNombre; }
    public void setClienteTopNombre(String clienteTopNombre) { this.clienteTopNombre = clienteTopNombre; }
    
    public int getClienteTopVisitas() { return clienteTopVisitas; }
    public void setClienteTopVisitas(int clienteTopVisitas) { this.clienteTopVisitas = clienteTopVisitas; }
    
    public Date getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(Date fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    
    public String getEstadoSincronizacion() { return estadoSincronizacion; }
    public void setEstadoSincronizacion(String estadoSincronizacion) { this.estadoSincronizacion = estadoSincronizacion; }
    
    // Métodos de utilidad
    public void incrementarVersion() {
        this.version++;
        this.sincronizado = false;
        this.fechaGeneracion = new Date();
    }
    
    public void marcarComoSincronizado() {
        this.sincronizado = true;
    }
    
    public boolean esReporteReciente() {
        long tiempoTranscurrido = new Date().getTime() - fechaGeneracion.getTime();
        return tiempoTranscurrido < (24 * 60 * 60 * 1000); // 24 horas
    }
    
    public String getResumenMetricas() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Visitas: ").append(totalVisitas);
        resumen.append(", Canjes: ").append(totalCanjes);
        resumen.append(", Clientes: ").append(totalClientesActivos);
        return resumen.toString();
    }
    
    public double getTasaConversion() {
        if (totalVisitas == 0) return 0.0;
        return (double) totalCanjes / totalVisitas * 100;
    }
    
    @Override
    public String toString() {
        return "ReporteEntity{" +
                "id='" + id + '\'' +
                ", tipoReporte='" + tipoReporte + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", totalVisitas=" + totalVisitas +
                ", totalCanjes=" + totalCanjes +
                '}';
    }
}