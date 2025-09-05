package com.example.cafefidelidaqrdemo.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilidades para validación de datos del cliente
 */
public class ValidationUtils {
    
    // Patrones de validación
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{2,50}$");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    /**
     * Valida formato de email
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "El email es obligatorio");
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "Formato de email inválido");
        }
        
        if (email.length() > 100) {
            return new ValidationResult(false, "El email no puede exceder 100 caracteres");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Valida formato de teléfono
     */
    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return new ValidationResult(false, "El teléfono es obligatorio");
        }
        
        // Remover espacios y guiones
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return new ValidationResult(false, "Formato de teléfono inválido (8-15 dígitos)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Valida nombre del cliente
     */
    public static ValidationResult validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return new ValidationResult(false, "El nombre es obligatorio");
        }
        
        String trimmedName = name.trim();
        
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return new ValidationResult(false, "El nombre debe contener solo letras y espacios (2-50 caracteres)");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Valida fecha de nacimiento
     */
    public static ValidationResult validateBirthDate(String dateString) {
        if (TextUtils.isEmpty(dateString)) {
            return new ValidationResult(false, "La fecha de nacimiento es obligatoria");
        }
        
        try {
            Date birthDate = DATE_FORMAT.parse(dateString);
            Date currentDate = new Date();
            
            if (birthDate == null) {
                return new ValidationResult(false, "Formato de fecha inválido (dd/MM/yyyy)");
            }
            
            if (birthDate.after(currentDate)) {
                return new ValidationResult(false, "La fecha de nacimiento no puede ser futura");
            }
            
            // Verificar edad mínima (13 años)
            long ageInMillis = currentDate.getTime() - birthDate.getTime();
            long ageInYears = ageInMillis / (365L * 24 * 60 * 60 * 1000);
            
            if (ageInYears < 13) {
                return new ValidationResult(false, "Debe ser mayor de 13 años");
            }
            
            if (ageInYears > 120) {
                return new ValidationResult(false, "Fecha de nacimiento inválida");
            }
            
            return new ValidationResult(true, null);
            
        } catch (ParseException e) {
            return new ValidationResult(false, "Formato de fecha inválido (dd/MM/yyyy)");
        }
    }
    
    /**
     * Valida todos los campos del perfil del cliente
     */
    public static ValidationResult validateClientProfile(String name, String email, String phone, String birthDate) {
        ValidationResult nameResult = validateName(name);
        if (!nameResult.isValid()) {
            return nameResult;
        }
        
        ValidationResult emailResult = validateEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }
        
        ValidationResult phoneResult = validatePhone(phone);
        if (!phoneResult.isValid()) {
            return phoneResult;
        }
        
        ValidationResult birthDateResult = validateBirthDate(birthDate);
        if (!birthDateResult.isValid()) {
            return birthDateResult;
        }
        
        return new ValidationResult(true, "Todos los campos son válidos");
    }
    
    /**
     * Formatea el teléfono removiendo caracteres especiales
     */
    public static String formatPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return "";
        }
        return phone.replaceAll("[\\s-()]", "");
    }
    
    /**
     * Formatea el nombre capitalizando cada palabra
     */
    public static String formatName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                formatted.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    formatted.append(words[i].substring(1));
                }
                if (i < words.length - 1) {
                    formatted.append(" ");
                }
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Convierte fecha de String a Date
     */
    public static Date parseDate(String dateString) {
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Convierte fecha de Date a String
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Clase para encapsular resultado de validación
     */
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
}