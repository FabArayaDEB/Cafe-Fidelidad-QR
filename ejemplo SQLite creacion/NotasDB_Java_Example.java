package com.example.notas_app_sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class NotasDB extends SQLiteOpenHelper {
    
    // Constantes de la base de datos
    private static final String DATABASE_NAME = "notas.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "notas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITULO = "titulo";
    private static final String COLUMN_DESCRIPCION = "descripcion";

    // Constructor
    public NotasDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TITULO + " TEXT, " +
                COLUMN_DESCRIPCION + " TEXT)";
        
        // Ejecución de la consulta
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    // Método para insertar una nota
    public void insertNota(Nota nota) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITULO, nota.getTitulo());
        values.put(COLUMN_DESCRIPCION, nota.getDescripcion());
        
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Método para obtener todas las notas
    public List<Nota> getAllNotas() {
        List<Nota> notasList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION));
                
                Nota nota = new Nota(id, titulo, descripcion);
                notasList.add(nota);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return notasList;
    }

    // Método para obtener una nota por ID
    public Nota getIdNota(int idNota) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + idNota;
        Cursor cursor = db.rawQuery(query, null);
        
        Nota nota = null;
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO));
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION));
            
            nota = new Nota(id, titulo, descripcion);
        }
        
        cursor.close();
        db.close();
        return nota;
    }

    // Método para actualizar una nota
    public void updateNota(Nota nota) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITULO, nota.getTitulo());
        values.put(COLUMN_DESCRIPCION, nota.getDescripcion());
        
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(nota.getId())};
        
        db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();
    }

    // Método para eliminar una nota
    public void deleteNota(Nota nota) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(nota.getId())};
        
        db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    // Método para eliminar una nota por ID
    public void deleteNotaById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        
        db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
    }

    // Método para contar el número total de notas
    public int getNotasCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
}