package com.example.cafefidelidaqrdemo.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Beneficio implements Serializable {
    private String id;
    private String nombre; // Antes 'titulo'
    private String descripcion;
    private int puntosRequeridos;
    private String tipo;
    private double valor;
    private int productoId;
    private boolean activo;

    // --- CAMPOS EXTENDIDOS PARA COMPATIBILIDAD ---
    private String estado; // "disponible", "usado", "expirado"
    private int cantidadMaximaUsos;
    private int cantidadUsosActuales;
    private String clienteId;
    private int visitasRequeridas;
    private double valorDescuentoPorcentaje;
    private double valorDescuentoFijo;
    private int vecesCanjeado;

    // Fechas como objetos Date (requerido por los adaptadores)
    private Date fechaInicioVigencia;
    private Date fechaFinVigencia;

    private long fechaCreacion;
    private long fechaActualizacion;

    // Campo auxiliar para lógica de negocio (no necesariamente en BD)
    private Map<String, Object> condicionesEspeciales;

    // Constructor vacío
    public Beneficio() {
        this.estado = "disponible";
        this.cantidadMaximaUsos = 0;
        this.cantidadUsosActuales = 0;
    }

    // Constructor compatible con BeneficioManager
    public Beneficio(String id, String nombre, String descripcion, String tipo, int puntosRequeridos, double valor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.puntosRequeridos = puntosRequeridos;
        this.valor = valor;
        this.estado = "disponible";
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
    }

    // --- GETTERS Y SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getPuntosRequeridos() { return puntosRequeridos; }
    public void setPuntosRequeridos(int puntosRequeridos) { this.puntosRequeridos = puntosRequeridos; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    // Sobrecarga para manejar String si viene del Manager
    public void setProductoId(String productoId) {
        try {
            this.productoId = Integer.parseInt(productoId);
        } catch (NumberFormatException e) {
            this.productoId = 0;
        }
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getCantidadMaximaUsos() { return cantidadMaximaUsos; }
    public void setCantidadMaximaUsos(int cantidadMaximaUsos) { this.cantidadMaximaUsos = cantidadMaximaUsos; }

    public int getCantidadUsosActuales() { return cantidadUsosActuales; }
    public void setCantidadUsosActuales(int cantidadUsosActuales) { this.cantidadUsosActuales = cantidadUsosActuales; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public int getVisitasRequeridas() { return visitasRequeridas; }
    public void setVisitasRequeridas(int visitasRequeridas) { this.visitasRequeridas = visitasRequeridas; }

    public double getValorDescuentoPorcentaje() { return valorDescuentoPorcentaje; }
    public void setValorDescuentoPorcentaje(double valorDescuentoPorcentaje) { this.valorDescuentoPorcentaje = valorDescuentoPorcentaje; }

    public double getValorDescuentoFijo() { return valorDescuentoFijo; }
    public void setValorDescuentoFijo(double valorDescuentoFijo) { this.valorDescuentoFijo = valorDescuentoFijo; }

    public int getVecesCanjeado() { return vecesCanjeado; }
    public void setVecesCanjeado(int vecesCanjeado) { this.vecesCanjeado = vecesCanjeado; }

    public Date getFechaInicioVigencia() { return fechaInicioVigencia; }
    public void setFechaInicioVigencia(Date fechaInicioVigencia) { this.fechaInicioVigencia = fechaInicioVigencia; }

    public Date getFechaFinVigencia() { return fechaFinVigencia; }
    public void setFechaFinVigencia(Date fechaFinVigencia) { this.fechaFinVigencia = fechaFinVigencia; }

    // Métodos de compatibilidad para Base de Datos (long vs Date)
    public long getFechaVencimiento() {
        return fechaFinVigencia != null ? fechaFinVigencia.getTime() : 0;
    }
    public void setFechaVencimiento(long millis) {
        if (millis > 0) this.fechaFinVigencia = new Date(millis);
    }

    public Map<String, Object> getCondicionesEspeciales() { return condicionesEspeciales; }
    public void setCondicionesEspeciales(Map<String, Object> condicionesEspeciales) { this.condicionesEspeciales = condicionesEspeciales; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    public double getValorDescuento() {
        if (valor > 0) return valor;
        if (valorDescuentoFijo > 0) return valorDescuentoFijo;
        if (valorDescuentoPorcentaje > 0) return valorDescuentoPorcentaje;
        return 0.0;
    }

    public boolean esValido() {
        long now = System.currentTimeMillis();
        boolean fechasValidas = true;
        if (fechaInicioVigencia != null && now < fechaInicioVigencia.getTime()) fechasValidas = false;
        if (fechaFinVigencia != null && now > fechaFinVigencia.getTime()) fechasValidas = false;

        boolean estadoValido = "disponible".equals(estado);
        boolean usosValidos = cantidadMaximaUsos == 0 || cantidadUsosActuales < cantidadMaximaUsos;

        return estadoValido && fechasValidas && usosValidos;
    }

    public boolean estaVencido() {
        return fechaFinVigencia != null && System.currentTimeMillis() > fechaFinVigencia.getTime();
    }

    public void marcarComoUsado() {
        this.cantidadUsosActuales++;
        if (cantidadMaximaUsos > 0 && cantidadUsosActuales >= cantidadMaximaUsos) {
            this.estado = "usado";
        }
    }

    public boolean isVigente() {
        return esValido();
    }
}