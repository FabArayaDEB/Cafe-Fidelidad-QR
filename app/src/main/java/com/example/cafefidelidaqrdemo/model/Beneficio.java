package com.example.cafefidelidaqrdemo.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.cafefidelidaqrdemo.database.Converters;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

@Entity(tableName = "beneficios")
@TypeConverters(Converters.class)
public class Beneficio {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("tipo")
    private TipoBeneficio tipo;

    @SerializedName("valor")
    private double valor; // Para descuento% o monto

    @SerializedName("reglas")
    private String reglasJson; // JSON con reglas como {"cadaNVisitas": 5}

    @SerializedName("fechaInicio")
    private Date fechaInicio;

    @SerializedName("fechaFin")
    private Date fechaFin;

    @SerializedName("sucursalesAplicables")
    private List<String> sucursalesAplicables; // IDs de sucursales

    @SerializedName("activo")
    private boolean activo;

    @SerializedName("fechaCreacion")
    private Date fechaCreacion;

    @SerializedName("fechaModificacion")
    private Date fechaModificacion;

    // Enum para tipos de beneficio
    public enum TipoBeneficio {
        DESCUENTO_PORCENTAJE("descuento_porcentaje"),
        DESCUENTO_MONTO("descuento_monto"),
        DOS_POR_UNO("2x1"),
        PREMIO("premio");

        private final String valor;

        TipoBeneficio(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public static TipoBeneficio fromString(String valor) {
            for (TipoBeneficio tipo : TipoBeneficio.values()) {
                if (tipo.valor.equals(valor)) {
                    return tipo;
                }
            }
            return DESCUENTO_PORCENTAJE; // Default
        }
    }

    // Constructores
    public Beneficio() {
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
        this.activo = true;
    }

    public Beneficio(String id, String nombre, String descripcion, TipoBeneficio tipo, 
                    double valor, String reglasJson, Date fechaInicio, Date fechaFin, 
                    List<String> sucursalesAplicables) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.valor = valor;
        this.reglasJson = reglasJson;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.sucursalesAplicables = sucursalesAplicables;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public void setTipo(TipoBeneficio tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getReglasJson() {
        return reglasJson;
    }

    public void setReglasJson(String reglasJson) {
        this.reglasJson = reglasJson;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public List<String> getSucursalesAplicables() {
        return sucursalesAplicables;
    }

    public void setSucursalesAplicables(List<String> sucursalesAplicables) {
        this.sucursalesAplicables = sucursalesAplicables;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    // Métodos de utilidad
    public boolean isVigente() {
        Date ahora = new Date();
        return activo && 
               (fechaInicio == null || !ahora.before(fechaInicio)) &&
               (fechaFin == null || !ahora.after(fechaFin));
    }

    public String getTipoDisplayName() {
        switch (tipo) {
            case DESCUENTO_PORCENTAJE:
                return (int)valor + "% de descuento";
            case DESCUENTO_MONTO:
                return "$" + (int)valor + " de descuento";
            case DOS_POR_UNO:
                return "2x1";
            case PREMIO:
                return "Premio especial";
            default:
                return "Beneficio";
        }
    }

    @Override
    public String toString() {
        return "Beneficio{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", valor=" + valor +
                ", activo=" + activo +
                '}';
    }
}