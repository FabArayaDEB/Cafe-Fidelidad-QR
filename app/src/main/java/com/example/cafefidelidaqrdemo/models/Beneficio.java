package com.example.cafefidelidaqrdemo.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Beneficio {
    private String id;
    private String nombre;
    private String descripcion;
    private TipoBeneficio tipo;
    private EstadoBeneficio estado;
    
    // Reglas de activación
    private int visitasRequeridas;
    private double montoMinimoCompra;
    private List<String> sucursalesAplicables;
    private List<String> categoriasProductos;
    private List<String> diasSemanaValidos;
    private String horaInicioValido;
    private String horaFinValido;
    
    // Valores del beneficio
    private double valorDescuentoPorcentaje;
    private double valorDescuentoFijo;
    private String productoGratisId;
    private String productoDescuentoId;
    private int cantidadMaximaUsos;
    private int cantidadUsosActuales;
    
    // Fechas y vigencia
    private Date fechaCreacion;
    private Date fechaInicioVigencia;
    private Date fechaFinVigencia;
    private Date fechaUltimoUso;
    private boolean activo;
    
    // Configuración avanzada
    private boolean esAcumulable;
    private boolean requiereCodigoPromocional;
    private String codigoPromocional;
    private int prioridadAplicacion;
    private double montoMaximoDescuento;
    private boolean aplicaSoloUnaVez;
    private Map<String, Object> condicionesEspeciales;
    
    // Información del cliente
    private String clienteId;
    private boolean esPersonalizado;
    private String motivoOtorgamiento;
    
    // Estadísticas
    private int vecesCanjeado;
    private double montoTotalAhorrado;
    private Date fechaUltimoCanje;

    public enum TipoBeneficio {
        DESCUENTO_PORCENTAJE,
        DESCUENTO_FIJO,
        PRODUCTO_GRATIS,
        DOS_POR_UNO,
        PUNTOS_EXTRA,
        ENVIO_GRATIS,
        UPGRADE_PRODUCTO
    }
    
    public enum EstadoBeneficio {
        DISPONIBLE,
        USADO,
        EXPIRADO,
        BLOQUEADO,
        PENDIENTE_ACTIVACION
    }
    
    // Constructor vacío
    public Beneficio() {
        this.fechaCreacion = new Date();
        this.estado = EstadoBeneficio.PENDIENTE_ACTIVACION;
        this.activo = true;
        this.cantidadUsosActuales = 0;
        this.vecesCanjeado = 0;
        this.montoTotalAhorrado = 0.0;
    }
    
    // Constructor completo
    public Beneficio(String nombre, String descripcion, TipoBeneficio tipo, int visitasRequeridas) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.visitasRequeridas = visitasRequeridas;
    }
    
    // Métodos de validación
    public boolean esValido() {
        Date ahora = new Date();
        return activo && 
               estado == EstadoBeneficio.DISPONIBLE &&
               (fechaInicioVigencia == null || ahora.after(fechaInicioVigencia)) &&
               (fechaFinVigencia == null || ahora.before(fechaFinVigencia)) &&
               (cantidadMaximaUsos == 0 || cantidadUsosActuales < cantidadMaximaUsos);
    }
    
    public boolean puedeAplicarseEn(String sucursalId) {
        return sucursalesAplicables == null || sucursalesAplicables.isEmpty() || 
               sucursalesAplicables.contains(sucursalId);
    }
    
    public boolean puedeAplicarseAProducto(String categoriaProducto) {
        return categoriasProductos == null || categoriasProductos.isEmpty() || 
               categoriasProductos.contains(categoriaProducto);
    }
    
    public boolean esValidoEnHorario() {
        if (horaInicioValido == null || horaFinValido == null) {
            return true;
        }
        // Implementar lógica de validación de horario
        return true;
    }
    
    public double calcularDescuento(double montoCompra) {
        switch (tipo) {
            case DESCUENTO_PORCENTAJE:
                double descuento = montoCompra * (valorDescuentoPorcentaje / 100.0);
                return montoMaximoDescuento > 0 ? Math.min(descuento, montoMaximoDescuento) : descuento;
            case DESCUENTO_FIJO:
                return Math.min(valorDescuentoFijo, montoCompra);
            default:
                return 0.0;
        }
    }
    
    public void marcarComoUsado() {
        this.cantidadUsosActuales++;
        this.vecesCanjeado++;
        this.fechaUltimoCanje = new Date();
        this.fechaUltimoUso = new Date();
        
        if (aplicaSoloUnaVez || (cantidadMaximaUsos > 0 && cantidadUsosActuales >= cantidadMaximaUsos)) {
            this.estado = EstadoBeneficio.USADO;
        }
    }
    
    public void activar() {
        this.estado = EstadoBeneficio.DISPONIBLE;
    }
    
    public void expirar() {
        this.estado = EstadoBeneficio.EXPIRADO;
    }
    
    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder(descripcion);
        
        if (fechaFinVigencia != null) {
            desc.append(" (Válido hasta: ").append(fechaFinVigencia.toString()).append(")");
        }
        
        if (cantidadMaximaUsos > 0) {
            int usosRestantes = cantidadMaximaUsos - cantidadUsosActuales;
            desc.append(" (Usos restantes: ").append(usosRestantes).append(")");
        }
        
        return desc.toString();
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public TipoBeneficio getTipo() { return tipo; }
    public void setTipo(TipoBeneficio tipo) { this.tipo = tipo; }
    
    public EstadoBeneficio getEstado() { return estado; }
    public void setEstado(EstadoBeneficio estado) { this.estado = estado; }
    
    public int getVisitasRequeridas() { return visitasRequeridas; }
    public void setVisitasRequeridas(int visitasRequeridas) { this.visitasRequeridas = visitasRequeridas; }
    
    public double getMontoMinimoCompra() { return montoMinimoCompra; }
    public void setMontoMinimoCompra(double montoMinimoCompra) { this.montoMinimoCompra = montoMinimoCompra; }
    
    public List<String> getSucursalesAplicables() { return sucursalesAplicables; }
    public void setSucursalesAplicables(List<String> sucursalesAplicables) { this.sucursalesAplicables = sucursalesAplicables; }
    
    public List<String> getCategoriasProductos() { return categoriasProductos; }
    public void setCategoriasProductos(List<String> categoriasProductos) { this.categoriasProductos = categoriasProductos; }
    
    public double getValorDescuentoPorcentaje() { return valorDescuentoPorcentaje; }
    public void setValorDescuentoPorcentaje(double valorDescuentoPorcentaje) { this.valorDescuentoPorcentaje = valorDescuentoPorcentaje; }
    
    public double getValorDescuentoFijo() { return valorDescuentoFijo; }
    public void setValorDescuentoFijo(double valorDescuentoFijo) { this.valorDescuentoFijo = valorDescuentoFijo; }
    
    public String getProductoGratisId() { return productoGratisId; }
    public void setProductoGratisId(String productoGratisId) { this.productoGratisId = productoGratisId; }
    
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Date getFechaInicioVigencia() { return fechaInicioVigencia; }
    public void setFechaInicioVigencia(Date fechaInicioVigencia) { this.fechaInicioVigencia = fechaInicioVigencia; }
    
    public Date getFechaFinVigencia() { return fechaFinVigencia; }
    public void setFechaFinVigencia(Date fechaFinVigencia) { this.fechaFinVigencia = fechaFinVigencia; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public int getCantidadMaximaUsos() { return cantidadMaximaUsos; }
    public void setCantidadMaximaUsos(int cantidadMaximaUsos) { this.cantidadMaximaUsos = cantidadMaximaUsos; }
    
    public int getCantidadUsosActuales() { return cantidadUsosActuales; }
    public void setCantidadUsosActuales(int cantidadUsosActuales) { this.cantidadUsosActuales = cantidadUsosActuales; }
    
    public boolean isEsAcumulable() { return esAcumulable; }
    public void setEsAcumulable(boolean esAcumulable) { this.esAcumulable = esAcumulable; }
    
    public boolean isRequiereCodigoPromocional() { return requiereCodigoPromocional; }
    public void setRequiereCodigoPromocional(boolean requiereCodigoPromocional) { this.requiereCodigoPromocional = requiereCodigoPromocional; }
    
    public String getCodigoPromocional() { return codigoPromocional; }
    public void setCodigoPromocional(String codigoPromocional) { this.codigoPromocional = codigoPromocional; }
    
    public int getVecesCanjeado() { return vecesCanjeado; }
    public void setVecesCanjeado(int vecesCanjeado) { this.vecesCanjeado = vecesCanjeado; }
    
    public double getMontoTotalAhorrado() { return montoTotalAhorrado; }
    public void setMontoTotalAhorrado(double montoTotalAhorrado) { this.montoTotalAhorrado = montoTotalAhorrado; }
    
    public Date getFechaUltimoCanje() { return fechaUltimoCanje; }
    public void setFechaUltimoCanje(Date fechaUltimoCanje) { this.fechaUltimoCanje = fechaUltimoCanje; }
    
    public List<String> getDiasSemanaValidos() { return diasSemanaValidos; }
    public void setDiasSemanaValidos(List<String> diasSemanaValidos) { this.diasSemanaValidos = diasSemanaValidos; }
    
    public String getHoraInicioValido() { return horaInicioValido; }
    public void setHoraInicioValido(String horaInicioValido) { this.horaInicioValido = horaInicioValido; }
    
    public String getHoraFinValido() { return horaFinValido; }
    public void setHoraFinValido(String horaFinValido) { this.horaFinValido = horaFinValido; }
    
    public String getProductoDescuentoId() { return productoDescuentoId; }
    public void setProductoDescuentoId(String productoDescuentoId) { this.productoDescuentoId = productoDescuentoId; }
    
    public int getPrioridadAplicacion() { return prioridadAplicacion; }
    public void setPrioridadAplicacion(int prioridadAplicacion) { this.prioridadAplicacion = prioridadAplicacion; }
    
    public double getMontoMaximoDescuento() { return montoMaximoDescuento; }
    public void setMontoMaximoDescuento(double montoMaximoDescuento) { this.montoMaximoDescuento = montoMaximoDescuento; }
    
    public boolean isAplicaSoloUnaVez() { return aplicaSoloUnaVez; }
    public void setAplicaSoloUnaVez(boolean aplicaSoloUnaVez) { this.aplicaSoloUnaVez = aplicaSoloUnaVez; }
    
    public Map<String, Object> getCondicionesEspeciales() { return condicionesEspeciales; }
    public void setCondicionesEspeciales(Map<String, Object> condicionesEspeciales) { this.condicionesEspeciales = condicionesEspeciales; }
    
    public boolean isEsPersonalizado() { return esPersonalizado; }
    public void setEsPersonalizado(boolean esPersonalizado) { this.esPersonalizado = esPersonalizado; }
    
    public String getMotivoOtorgamiento() { return motivoOtorgamiento; }
    public void setMotivoOtorgamiento(String motivoOtorgamiento) { this.motivoOtorgamiento = motivoOtorgamiento; }
    
    public Date getFechaUltimoUso() { return fechaUltimoUso; }
    public void setFechaUltimoUso(Date fechaUltimoUso) { this.fechaUltimoUso = fechaUltimoUso; }
}