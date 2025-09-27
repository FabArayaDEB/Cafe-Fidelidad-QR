package com.example.cafefidelidaqrdemo.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Modelo simplificado para Beneficio
 */
public class Beneficio implements Serializable {
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo; // "descuento", "producto_gratis", "puntos_extra"
    private String estado; // "disponible", "usado", "expirado"
    private int visitasRequeridas;
    private double valorDescuento; // Porcentaje o valor fijo
    private String productoId; // Para productos gratis
    private long fechaCreacion;
    private long fechaVencimiento;
    private boolean activo;
    private String clienteId;
    
    // Nuevos campos para compatibilidad
    private Date fechaInicioVigencia;
    private Date fechaFinVigencia;
    private int vecesCanjeado;
    private double valorDescuentoPorcentaje;
    private double valorDescuentoFijo;
    private Map<String, Object> condicionesEspeciales;
    private int cantidadMaximaUsos;
    private int cantidadUsosActuales;

    // Constructor vacío
    public Beneficio() {}

    // Constructor principal
    public Beneficio(String id, String nombre, String descripcion, String tipo, 
                    int visitasRequeridas, double valorDescuento) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.visitasRequeridas = visitasRequeridas;
        this.valorDescuento = valorDescuento;
        this.fechaCreacion = System.currentTimeMillis();
        this.estado = "disponible";
        this.activo = true;
        this.vecesCanjeado = 0;
        this.valorDescuentoPorcentaje = 0.0;
        this.valorDescuentoFijo = 0.0;
    }

    // Getters y Setters existentes
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getVisitasRequeridas() { return visitasRequeridas; }
    public void setVisitasRequeridas(int visitasRequeridas) { this.visitasRequeridas = visitasRequeridas; }

    public double getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(double valorDescuento) { this.valorDescuento = valorDescuento; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(long fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    // Nuevos getters y setters
    public Date getFechaInicioVigencia() { return fechaInicioVigencia; }
    public void setFechaInicioVigencia(Date fechaInicioVigencia) { this.fechaInicioVigencia = fechaInicioVigencia; }

    public Date getFechaFinVigencia() { return fechaFinVigencia; }
    public void setFechaFinVigencia(Date fechaFinVigencia) { this.fechaFinVigencia = fechaFinVigencia; }

    public int getVecesCanjeado() { return vecesCanjeado; }
    public void setVecesCanjeado(int vecesCanjeado) { this.vecesCanjeado = vecesCanjeado; }

    public double getValorDescuentoPorcentaje() { return valorDescuentoPorcentaje; }
    public void setValorDescuentoPorcentaje(double valorDescuentoPorcentaje) { this.valorDescuentoPorcentaje = valorDescuentoPorcentaje; }

    public double getValorDescuentoFijo() { return valorDescuentoFijo; }
    public void setValorDescuentoFijo(double valorDescuentoFijo) { this.valorDescuentoFijo = valorDescuentoFijo; }

    public Map<String, Object> getCondicionesEspeciales() { return condicionesEspeciales; }
    public void setCondicionesEspeciales(Map<String, Object> condicionesEspeciales) { this.condicionesEspeciales = condicionesEspeciales; }

    public int getCantidadMaximaUsos() { return cantidadMaximaUsos; }
    public void setCantidadMaximaUsos(int cantidadMaximaUsos) { this.cantidadMaximaUsos = cantidadMaximaUsos; }

    public int getCantidadUsosActuales() { return cantidadUsosActuales; }
    public void setCantidadUsosActuales(int cantidadUsosActuales) { this.cantidadUsosActuales = cantidadUsosActuales; }

    // Métodos de utilidad
    public boolean esValido() {
        long ahora = System.currentTimeMillis();
        return activo && 
               "disponible".equals(estado) &&
               (fechaVencimiento == 0 || ahora < fechaVencimiento);
    }

    public boolean estaVencido() {
        return fechaVencimiento > 0 && System.currentTimeMillis() > fechaVencimiento;
    }

    public void marcarComoUsado() {
        this.estado = "usado";
        this.vecesCanjeado++;
    }

    @Override
    public String toString() {
        return "Beneficio{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", visitasRequeridas=" + visitasRequeridas +
                ", valorDescuento=" + valorDescuento +
                ", activo=" + activo +
                ", vecesCanjeado=" + vecesCanjeado +
                '}';
    }
}