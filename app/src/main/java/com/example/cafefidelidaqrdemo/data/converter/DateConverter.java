package com.example.cafefidelidaqrdemo.data.converter;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Conversor de tipos para Room Database
 * Convierte Date a Long y viceversa para almacenamiento en SQLite
 */
public class DateConverter {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}