package com.example.cafefidelidaqrdemo.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
// Removed import for models.Beneficio as we now use String for tipo in BeneficioEntity

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * Converters para Room Database
 * Maneja la conversión de tipos complejos a tipos primitivos para almacenamiento
 */
public class Converters {
    
    private static final Gson gson = new Gson();
    
    // Conversores para Date
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    // Conversores para List<String>
    @TypeConverter
    public static List<String> fromStringList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromListString(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
    
    // Conversores para TipoBeneficio enum
    // TipoBeneficio converters removed as BeneficioEntity now uses String directly for tipo field
}