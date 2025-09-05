package com.example.cafefidelidaqrdemo.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Modelo para Regla de fidelización
 */
public class Regla {
    private String id;
    private String descripcion;
    private String expresion; // JSON string con la lógica de la regla
    private String tipo; // "cadaNVisitas", "montoMinimo", "diasConsecutivos", etc.
    private boolean activa;
    private long fechaCreacion;
    private long fechaActualizacion;
    private String creadoPor;
    private String sucursalesAplicables; // JSON array de IDs de sucursales
    private String categoriasProductos; // JSON array de categorías aplicables
    private int prioridad; // Para ordenar reglas cuando hay múltiples
    private String condicionesAdicionales; // JSON con condiciones extra
    
    // Constructor vacío
    public Regla() {
        this.activa = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaActualizacion = System.currentTimeMillis();
        this.prioridad = 1;
    }
    
    // Constructor con parámetros básicos
    public Regla(String id, String descripcion, String expresion) {
        this();
        this.id = id;
        this.descripcion = descripcion;
        this.expresion = expresion;
        this.tipo = extraerTipoDeExpresion(expresion);
    }
    
    // Constructor completo
    public Regla(String id, String descripcion, String expresion, String tipo, 
                boolean activa, String creadoPor) {
        this(id, descripcion, expresion);
        this.tipo = tipo;
        this.activa = activa;
        this.creadoPor = creadoPor;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion;
        this.fechaActualizacion = System.currentTimeMillis();
    }
    
    public String getExpresion() { return expresion; }
    public void setExpresion(String expresion) { 
        this.expresion = expresion;
        this.tipo = extraerTipoDeExpresion(expresion);
        this.fechaActualizacion = System.currentTimeMillis();
    }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { 
        this.activa = activa;
        this.fechaActualizacion = System.currentTimeMillis();
    }
    
    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    
    public String getSucursalesAplicables() { return sucursalesAplicables; }
    public void setSucursalesAplicables(String sucursalesAplicables) { this.sucursalesAplicables = sucursalesAplicables; }
    
    public String getCategoriasProductos() { return categoriasProductos; }
    public void setCategoriasProductos(String categoriasProductos) { this.categoriasProductos = categoriasProductos; }
    
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { 
        this.prioridad = prioridad;
        this.fechaActualizacion = System.currentTimeMillis();
    }
    
    public String getCondicionesAdicionales() { return condicionesAdicionales; }
    public void setCondicionesAdicionales(String condicionesAdicionales) { this.condicionesAdicionales = condicionesAdicionales; }
    
    // Métodos de utilidad
    
    /**
     * Extrae el tipo de regla desde la expresión JSON
     */
    private String extraerTipoDeExpresion(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            return "desconocido";
        }
        
        try {
            JsonObject json = JsonParser.parseString(expresion).getAsJsonObject();
            
            if (json.has("cadaNVisitas")) {
                return "cadaNVisitas";
            } else if (json.has("montoMinimo")) {
                return "montoMinimo";
            } else if (json.has("diasConsecutivos")) {
                return "diasConsecutivos";
            } else if (json.has("horariosEspecificos")) {
                return "horariosEspecificos";
            } else if (json.has("fechaEspecial")) {
                return "fechaEspecial";
            } else if (json.has("categoriaProducto")) {
                return "categoriaProducto";
            } else {
                return "personalizada";
            }
        } catch (JsonSyntaxException e) {
            return "invalida";
        }
    }
    
    /**
     * Verifica si la expresión JSON es válida
     */
    public boolean tieneExpresionValida() {
        if (expresion == null || expresion.trim().isEmpty()) {
            return false;
        }
        
        try {
            JsonParser.parseString(expresion);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * Obtiene la expresión como JsonObject
     */
    public JsonObject getExpresionAsJson() {
        if (!tieneExpresionValida()) {
            return new JsonObject();
        }
        
        try {
            return JsonParser.parseString(expresion).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }
    
    /**
     * Verifica si la regla se aplica a una sucursal específica
     */
    public boolean seAplicaASucursal(String sucursalId) {
        if (sucursalesAplicables == null || sucursalesAplicables.trim().isEmpty()) {
            return true; // Se aplica a todas las sucursales
        }
        
        try {
            return sucursalesAplicables.contains(sucursalId);
        } catch (Exception e) {
            return true; // En caso de error, asumir que se aplica
        }
    }
    
    /**
     * Verifica si la regla se aplica a una categoría de producto específica
     */
    public boolean seAplicaACategoria(String categoria) {
        if (categoriasProductos == null || categoriasProductos.trim().isEmpty()) {
            return true; // Se aplica a todas las categorías
        }
        
        try {
            return categoriasProductos.contains(categoria);
        } catch (Exception e) {
            return true; // En caso de error, asumir que se aplica
        }
    }
    
    /**
     * Obtiene el valor numérico principal de la regla (visitas, monto, días, etc.)
     */
    public int getValorPrincipal() {
        JsonObject json = getExpresionAsJson();
        
        if (json.has("cadaNVisitas")) {
            return json.get("cadaNVisitas").getAsInt();
        } else if (json.has("diasConsecutivos")) {
            return json.get("diasConsecutivos").getAsInt();
        } else if (json.has("montoMinimo")) {
            return json.get("montoMinimo").getAsInt();
        }
        
        return 0;
    }
    
    /**
     * Crea una copia de la regla
     */
    public Regla clonar() {
        Regla copia = new Regla();
        copia.setId(this.id + "_copy");
        copia.setDescripcion(this.descripcion + " (Copia)");
        copia.setExpresion(this.expresion);
        copia.setTipo(this.tipo);
        copia.setActiva(false); // Las copias inician inactivas
        copia.setCreadoPor(this.creadoPor);
        copia.setSucursalesAplicables(this.sucursalesAplicables);
        copia.setCategoriasProductos(this.categoriasProductos);
        copia.setPrioridad(this.prioridad);
        copia.setCondicionesAdicionales(this.condicionesAdicionales);
        return copia;
    }
    
    /**
     * Actualiza la regla con nuevos valores
     */
    public void actualizar(String nuevaDescripcion, String nuevaExpresion) {
        setDescripcion(nuevaDescripcion);
        setExpresion(nuevaExpresion);
    }
    
    @Override
    public String toString() {
        return "Regla{" +
                "id='" + id + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipo='" + tipo + '\'' +
                ", activa=" + activa +
                ", prioridad=" + prioridad +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Regla regla = (Regla) o;
        return id != null ? id.equals(regla.id) : regla.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}