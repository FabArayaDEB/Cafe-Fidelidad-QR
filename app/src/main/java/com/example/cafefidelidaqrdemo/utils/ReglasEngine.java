package com.example.cafefidelidaqrdemo.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Motor de reglas para validar esquemas JSON y calcular progreso de fidelización
 * Maneja diferentes tipos de reglas como cadaNVisitas, montoMinimo, etc.
 */
public class ReglasEngine {
    
    private static final String TAG = "ReglasEngine";
    private final Gson gson;
    
    public ReglasEngine() {
        this.gson = new Gson();
    }
    
    /**
     * Valida si un JSON de reglas tiene el esquema correcto
     */
    public ValidationResult validarEsquemaReglas(String reglasJson) {
        if (reglasJson == null || reglasJson.trim().isEmpty()) {
            return new ValidationResult(false, "Las reglas no pueden estar vacías");
        }
        
        try {
            JsonObject reglas = JsonParser.parseString(reglasJson).getAsJsonObject();
            
            // Validar que tenga al menos una regla válida
            boolean tieneReglaValida = false;
            StringBuilder errores = new StringBuilder();
            
            // Validar regla cadaNVisitas
            if (reglas.has("cadaNVisitas")) {
                ValidationResult result = validarCadaNVisitas(reglas);
                if (result.isValid()) {
                    tieneReglaValida = true;
                } else {
                    errores.append(result.getErrorMessage()).append("; ");
                }
            }
            
            // Validar regla montoMinimo
            if (reglas.has("montoMinimo")) {
                ValidationResult result = validarMontoMinimo(reglas);
                if (result.isValid()) {
                    tieneReglaValida = true;
                } else {
                    errores.append(result.getErrorMessage()).append("; ");
                }
            }
            
            // Validar regla diasConsecutivos
            if (reglas.has("diasConsecutivos")) {
                ValidationResult result = validarDiasConsecutivos(reglas);
                if (result.isValid()) {
                    tieneReglaValida = true;
                } else {
                    errores.append(result.getErrorMessage()).append("; ");
                }
            }
            
            // Validar regla horariosEspecificos
            if (reglas.has("horariosEspecificos")) {
                ValidationResult result = validarHorariosEspecificos(reglas);
                if (result.isValid()) {
                    tieneReglaValida = true;
                } else {
                    errores.append(result.getErrorMessage()).append("; ");
                }
            }
            
            if (!tieneReglaValida) {
                return new ValidationResult(false, 
                    "No se encontraron reglas válidas. Errores: " + errores.toString());
            }
            
            return new ValidationResult(true, "Esquema de reglas válido");
            
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing JSON reglas", e);
            return new ValidationResult(false, "JSON inválido: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error validando reglas", e);
            return new ValidationResult(false, "Error validando reglas: " + e.getMessage());
        }
    }
    
    /**
     * Calcula el progreso hacia un beneficio basado en las reglas
     */
    public ProgresoResult calcularProgreso(String reglasJson, int visitasActuales, 
                                          double montoAcumulado, Date ultimaVisita) {
        try {
            JsonObject reglas = JsonParser.parseString(reglasJson).getAsJsonObject();
            
            // Calcular progreso para cadaNVisitas
            if (reglas.has("cadaNVisitas")) {
                return calcularProgresoCadaNVisitas(reglas, visitasActuales);
            }
            
            // Calcular progreso para montoMinimo
            if (reglas.has("montoMinimo")) {
                return calcularProgresoMontoMinimo(reglas, montoAcumulado);
            }
            
            // Calcular progreso para diasConsecutivos
            if (reglas.has("diasConsecutivos")) {
                return calcularProgresoDiasConsecutivos(reglas, ultimaVisita);
            }
            
            return new ProgresoResult(0, 1, false, "No se pudo calcular progreso");
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculando progreso", e);
            return new ProgresoResult(0, 1, false, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si un beneficio está habilitado según las reglas
     */
    public boolean esBeneficioHabilitado(String reglasJson, int visitasActuales, 
                                       double montoAcumulado, Date ultimaVisita) {
        ProgresoResult progreso = calcularProgreso(reglasJson, visitasActuales, montoAcumulado, ultimaVisita);
        return progreso.isCompleto();
    }
    
    // Métodos de validación específicos
    
    private ValidationResult validarCadaNVisitas(JsonObject reglas) {
        try {
            int cadaNVisitas = reglas.get("cadaNVisitas").getAsInt();
            if (cadaNVisitas <= 0) {
                return new ValidationResult(false, "cadaNVisitas debe ser mayor a 0");
            }
            if (cadaNVisitas > 100) {
                return new ValidationResult(false, "cadaNVisitas no puede ser mayor a 100");
            }
            return new ValidationResult(true, "cadaNVisitas válido");
        } catch (Exception e) {
            return new ValidationResult(false, "cadaNVisitas debe ser un número entero");
        }
    }
    
    private ValidationResult validarMontoMinimo(JsonObject reglas) {
        try {
            double montoMinimo = reglas.get("montoMinimo").getAsDouble();
            if (montoMinimo <= 0) {
                return new ValidationResult(false, "montoMinimo debe ser mayor a 0");
            }
            return new ValidationResult(true, "montoMinimo válido");
        } catch (Exception e) {
            return new ValidationResult(false, "montoMinimo debe ser un número");
        }
    }
    
    private ValidationResult validarDiasConsecutivos(JsonObject reglas) {
        try {
            int diasConsecutivos = reglas.get("diasConsecutivos").getAsInt();
            if (diasConsecutivos <= 0) {
                return new ValidationResult(false, "diasConsecutivos debe ser mayor a 0");
            }
            if (diasConsecutivos > 365) {
                return new ValidationResult(false, "diasConsecutivos no puede ser mayor a 365");
            }
            return new ValidationResult(true, "diasConsecutivos válido");
        } catch (Exception e) {
            return new ValidationResult(false, "diasConsecutivos debe ser un número entero");
        }
    }
    
    private ValidationResult validarHorariosEspecificos(JsonObject reglas) {
        try {
            String horarios = reglas.get("horariosEspecificos").getAsString();
            if (horarios == null || horarios.trim().isEmpty()) {
                return new ValidationResult(false, "horariosEspecificos no puede estar vacío");
            }
            // Validar formato básico (ej: "09:00-12:00,14:00-18:00")
            String[] rangos = horarios.split(",");
            for (String rango : rangos) {
                if (!rango.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                    return new ValidationResult(false, "Formato de horarios inválido. Use HH:MM-HH:MM");
                }
            }
            return new ValidationResult(true, "horariosEspecificos válido");
        } catch (Exception e) {
            return new ValidationResult(false, "horariosEspecificos debe ser una cadena");
        }
    }
    
    // Métodos de cálculo de progreso específicos
    
    private ProgresoResult calcularProgresoCadaNVisitas(JsonObject reglas, int visitasActuales) {
        int cadaNVisitas = reglas.get("cadaNVisitas").getAsInt();
        int progreso = visitasActuales % cadaNVisitas;
        boolean completo = progreso == 0 && visitasActuales > 0;
        
        String mensaje = String.format("%d/%d visitas", progreso, cadaNVisitas);
        if (completo) {
            mensaje = "¡Beneficio disponible!";
        }
        
        return new ProgresoResult(progreso, cadaNVisitas, completo, mensaje);
    }
    
    private ProgresoResult calcularProgresoMontoMinimo(JsonObject reglas, double montoAcumulado) {
        double montoMinimo = reglas.get("montoMinimo").getAsDouble();
        boolean completo = montoAcumulado >= montoMinimo;
        
        String mensaje = String.format("$%.2f/$%.2f", 
            Math.min(montoAcumulado, montoMinimo), montoMinimo);
        if (completo) {
            mensaje = "¡Beneficio disponible!";
        }
        
        return new ProgresoResult(
            (int) Math.min(montoAcumulado, montoMinimo),
            (int) montoMinimo,
            completo,
            mensaje
        );
    }
    
    private ProgresoResult calcularProgresoDiasConsecutivos(JsonObject reglas, Date ultimaVisita) {
        int diasConsecutivos = reglas.get("diasConsecutivos").getAsInt();
        
        if (ultimaVisita == null) {
            return new ProgresoResult(0, diasConsecutivos, false, "0/" + diasConsecutivos + " días");
        }
        
        Date ahora = new Date();
        long diffInMillies = ahora.getTime() - ultimaVisita.getTime();
        long diasTranscurridos = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        
        // Si han pasado más de 2 días, se reinicia el contador
        if (diasTranscurridos > 2) {
            return new ProgresoResult(0, diasConsecutivos, false, "0/" + diasConsecutivos + " días");
        }
        
        // Calcular días consecutivos (esto requeriría más lógica con historial de visitas)
        // Por simplicidad, asumimos que si la última visita fue ayer o hoy, cuenta como 1 día
        int diasActuales = diasTranscurridos <= 1 ? 1 : 0;
        boolean completo = diasActuales >= diasConsecutivos;
        
        String mensaje = String.format("%d/%d días consecutivos", diasActuales, diasConsecutivos);
        if (completo) {
            mensaje = "¡Beneficio disponible!";
        }
        
        return new ProgresoResult(diasActuales, diasConsecutivos, completo, mensaje);
    }
    
    // Clases de resultado
    
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    public static class ProgresoResult {
        private final int progreso;
        private final int total;
        private final boolean completo;
        private final String mensaje;
        
        public ProgresoResult(int progreso, int total, boolean completo, String mensaje) {
            this.progreso = progreso;
            this.total = total;
            this.completo = completo;
            this.mensaje = mensaje;
        }
        
        public int getProgreso() {
            return progreso;
        }
        
        public int getTotal() {
            return total;
        }
        
        public boolean isCompleto() {
            return completo;
        }
        
        public String getMensaje() {
            return mensaje;
        }
        
        public float getProgresoPercentage() {
            return total > 0 ? (float) progreso / total : 0f;
        }
    }
}