package com.example.cafefidelidaqrdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Canje;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.models.Producto;
import com.example.cafefidelidaqrdemo.models.Sucursal;
import com.example.cafefidelidaqrdemo.models.Visita;
import com.example.cafefidelidaqrdemo.models.ResenaProducto;
import com.example.cafefidelidaqrdemo.models.ResenaSucursal;
import com.example.cafefidelidaqrdemo.models.PromedioCalificacion;

import java.util.ArrayList;
import java.util.List;

public class CafeFidelidadDB extends SQLiteOpenHelper {
    private static final String TAG = "CafeFidelidadDB";
    
    // Información de la base de datos
    private static final String DATABASE_NAME = "cafe_fidelidad.db";
    private static final int DATABASE_VERSION = 5;
    
    // Nombres de las tablas
    private static final String TABLE_CLIENTES = "clientes";
    private static final String TABLE_PRODUCTOS = "productos";
    private static final String TABLE_SUCURSALES = "sucursales";
    private static final String TABLE_BENEFICIOS = "beneficios";
    private static final String TABLE_VISITAS = "visitas";
    private static final String TABLE_CANJES = "canjes";
    // Tablas de reseñas
    private static final String TABLE_RESENAS_PRODUCTOS = "resenas_productos";
    private static final String TABLE_RESENAS_SUCURSALES = "resenas_sucursales";
    
    // Columnas comunes
    private static final String COLUMN_ID = "id";
    
    // Columnas tabla clientes
    private static final String COLUMN_CLIENTE_NOMBRE = "nombre";
    private static final String COLUMN_CLIENTE_EMAIL = "email";
    private static final String COLUMN_CLIENTE_TELEFONO = "telefono";
    private static final String COLUMN_CLIENTE_PASSWORD = "password";
    private static final String COLUMN_CLIENTE_PUNTOS = "puntos_acumulados";
    private static final String COLUMN_CLIENTE_ACTIVO = "activo";
    
    // Columnas tabla productos
    private static final String COLUMN_PRODUCTO_NOMBRE = "nombre";
    private static final String COLUMN_PRODUCTO_DESCRIPCION = "descripcion";
    private static final String COLUMN_PRODUCTO_PRECIO = "precio";
    private static final String COLUMN_PRODUCTO_CATEGORIA = "categoria";
    private static final String COLUMN_PRODUCTO_DISPONIBLE = "disponible";
    private static final String COLUMN_PRODUCTO_IMAGEN_URL = "imagen_url";
    
    // Columnas tabla sucursales
    private static final String COLUMN_SUCURSAL_NOMBRE = "nombre";
    private static final String COLUMN_SUCURSAL_DIRECCION = "direccion";
    private static final String COLUMN_SUCURSAL_TELEFONO = "telefono";
    private static final String COLUMN_SUCURSAL_HORARIO_APERTURA = "horario_apertura";
    private static final String COLUMN_SUCURSAL_HORARIO_CIERRE = "horario_cierre";
    private static final String COLUMN_SUCURSAL_LATITUD = "latitud";
    private static final String COLUMN_SUCURSAL_LONGITUD = "longitud";
    private static final String COLUMN_SUCURSAL_ESTADO = "estado";
    private static final String COLUMN_SUCURSAL_ACTIVA = "activa";
    private static final String COLUMN_SUCURSAL_IMAGEN_URL = "imagen_url";
    
    // Columnas tabla beneficios
    private static final String COLUMN_BENEFICIO_NOMBRE = "nombre";
    private static final String COLUMN_BENEFICIO_DESCRIPCION = "descripcion";
    private static final String COLUMN_BENEFICIO_PUNTOS_REQUERIDOS = "puntos_requeridos";
    private static final String COLUMN_BENEFICIO_TIPO = "tipo";
    private static final String COLUMN_BENEFICIO_ACTIVO = "activo";
    
    // Columnas tabla visitas
    private static final String COLUMN_VISITA_CLIENTE_ID = "cliente_id";
    private static final String COLUMN_VISITA_SUCURSAL_ID = "sucursal_id";
    private static final String COLUMN_VISITA_FECHA = "fecha_visita";
    private static final String COLUMN_VISITA_PUNTOS_GANADOS = "puntos_ganados";
    
    // Columnas tabla canjes
    private static final String COLUMN_CANJE_CLIENTE_ID = "cliente_id";
    private static final String COLUMN_CANJE_BENEFICIO_ID = "beneficio_id";
    private static final String COLUMN_CANJE_FECHA = "fecha_canje";
    private static final String COLUMN_CANJE_PUNTOS_UTILIZADOS = "puntos_utilizados";
    private static final String COLUMN_CANJE_ESTADO = "estado";
    
    // Columnas tablas reseñas
    private static final String COLUMN_RESENA_PRODUCTO_ID = "producto_id";
    private static final String COLUMN_RESENA_SUCURSAL_ID = "sucursal_id";
    private static final String COLUMN_RESENA_USUARIO_ID = "usuario_id";
    private static final String COLUMN_RESENA_CALIFICACION = "calificacion";
    private static final String COLUMN_RESENA_COMENTARIO = "comentario";
    private static final String COLUMN_RESENA_FECHA_CREACION = "fecha_creacion";
    private static final String COLUMN_RESENA_FECHA_ACTUALIZACION = "fecha_actualizacion";
    
    // Sentencias SQL para crear las tablas
    private static final String CREATE_TABLE_CLIENTES = "CREATE TABLE " + TABLE_CLIENTES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CLIENTE_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_CLIENTE_EMAIL + " TEXT UNIQUE NOT NULL, " +
            COLUMN_CLIENTE_TELEFONO + " TEXT, " +
            COLUMN_CLIENTE_PASSWORD + " TEXT NOT NULL, " +
            COLUMN_CLIENTE_PUNTOS + " INTEGER DEFAULT 0, " +
            COLUMN_CLIENTE_ACTIVO + " INTEGER DEFAULT 1" +
            ");";
    
    private static final String CREATE_TABLE_PRODUCTOS = "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PRODUCTO_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_PRODUCTO_DESCRIPCION + " TEXT, " +
            COLUMN_PRODUCTO_PRECIO + " REAL NOT NULL, " +
            COLUMN_PRODUCTO_CATEGORIA + " TEXT, " +
            COLUMN_PRODUCTO_DISPONIBLE + " INTEGER DEFAULT 1, " +
            COLUMN_PRODUCTO_IMAGEN_URL + " TEXT" +
            ");";
    
    private static final String CREATE_TABLE_SUCURSALES = "CREATE TABLE " + TABLE_SUCURSALES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SUCURSAL_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_SUCURSAL_DIRECCION + " TEXT NOT NULL, " +
            COLUMN_SUCURSAL_TELEFONO + " TEXT, " +
            COLUMN_SUCURSAL_HORARIO_APERTURA + " TEXT, " +
            COLUMN_SUCURSAL_HORARIO_CIERRE + " TEXT, " +
            COLUMN_SUCURSAL_LATITUD + " REAL, " +
            COLUMN_SUCURSAL_LONGITUD + " REAL, " +
            COLUMN_SUCURSAL_ESTADO + " TEXT DEFAULT 'activo', " +
            COLUMN_SUCURSAL_IMAGEN_URL + " TEXT" +
            ");";
    
    private static final String CREATE_TABLE_BENEFICIOS = "CREATE TABLE " + TABLE_BENEFICIOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_BENEFICIO_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_BENEFICIO_DESCRIPCION + " TEXT, " +
            COLUMN_BENEFICIO_PUNTOS_REQUERIDOS + " INTEGER NOT NULL, " +
            COLUMN_BENEFICIO_TIPO + " TEXT, " +
            COLUMN_BENEFICIO_ACTIVO + " INTEGER DEFAULT 1" +
            ");";
    
    private static final String CREATE_TABLE_VISITAS = "CREATE TABLE " + TABLE_VISITAS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_VISITA_CLIENTE_ID + " INTEGER NOT NULL, " +
            COLUMN_VISITA_SUCURSAL_ID + " INTEGER NOT NULL, " +
            COLUMN_VISITA_FECHA + " TEXT NOT NULL, " +
            COLUMN_VISITA_PUNTOS_GANADOS + " INTEGER DEFAULT 0, " +
            "FOREIGN KEY(" + COLUMN_VISITA_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTES + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_VISITA_SUCURSAL_ID + ") REFERENCES " + TABLE_SUCURSALES + "(" + COLUMN_ID + ")" +
            ");";
    
    private static final String CREATE_TABLE_CANJES = "CREATE TABLE " + TABLE_CANJES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CANJE_CLIENTE_ID + " INTEGER NOT NULL, " +
            COLUMN_CANJE_BENEFICIO_ID + " INTEGER NOT NULL, " +
            COLUMN_CANJE_FECHA + " TEXT NOT NULL, " +
            COLUMN_CANJE_PUNTOS_UTILIZADOS + " INTEGER NOT NULL, " +
            COLUMN_CANJE_ESTADO + " TEXT DEFAULT 'activo', " +
            "FOREIGN KEY(" + COLUMN_CANJE_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTES + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_CANJE_BENEFICIO_ID + ") REFERENCES " + TABLE_BENEFICIOS + "(" + COLUMN_ID + ")" +
            ");";

    // Sentencias SQL para crear tablas de reseñas
    private static final String CREATE_TABLE_RESENAS_PRODUCTOS = "CREATE TABLE IF NOT EXISTS " + TABLE_RESENAS_PRODUCTOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_RESENA_PRODUCTO_ID + " INTEGER NOT NULL, " +
            COLUMN_RESENA_USUARIO_ID + " INTEGER NOT NULL, " +
            COLUMN_RESENA_CALIFICACION + " INTEGER NOT NULL CHECK(" + COLUMN_RESENA_CALIFICACION + " BETWEEN 1 AND 5), " +
            COLUMN_RESENA_COMENTARIO + " TEXT, " +
            COLUMN_RESENA_FECHA_CREACION + " INTEGER NOT NULL, " +
            COLUMN_RESENA_FECHA_ACTUALIZACION + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_RESENA_PRODUCTO_ID + ") REFERENCES " + TABLE_PRODUCTOS + "(" + COLUMN_ID + ")" +
            ");";

    private static final String CREATE_TABLE_RESENAS_SUCURSALES = "CREATE TABLE IF NOT EXISTS " + TABLE_RESENAS_SUCURSALES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_RESENA_SUCURSAL_ID + " INTEGER NOT NULL, " +
            COLUMN_RESENA_USUARIO_ID + " INTEGER NOT NULL, " +
            COLUMN_RESENA_CALIFICACION + " INTEGER NOT NULL CHECK(" + COLUMN_RESENA_CALIFICACION + " BETWEEN 1 AND 5), " +
            COLUMN_RESENA_COMENTARIO + " TEXT, " +
            COLUMN_RESENA_FECHA_CREACION + " INTEGER NOT NULL, " +
            COLUMN_RESENA_FECHA_ACTUALIZACION + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_RESENA_SUCURSAL_ID + ") REFERENCES " + TABLE_SUCURSALES + "(" + COLUMN_ID + ")" +
            ");";
    
    private static CafeFidelidadDB instance;
    
    public static synchronized CafeFidelidadDB getInstance(Context context) {
        if (instance == null) {
            instance = new CafeFidelidadDB(context.getApplicationContext());
        }
        return instance;
    }
    
    private CafeFidelidadDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creando base de datos...");
        
        db.execSQL(CREATE_TABLE_CLIENTES);
        db.execSQL(CREATE_TABLE_PRODUCTOS);
        db.execSQL(CREATE_TABLE_SUCURSALES);
        db.execSQL(CREATE_TABLE_BENEFICIOS);
        db.execSQL(CREATE_TABLE_VISITAS);
        db.execSQL(CREATE_TABLE_CANJES);
        // Crear tablas de reseñas
        db.execSQL(CREATE_TABLE_RESENAS_PRODUCTOS);
        db.execSQL(CREATE_TABLE_RESENAS_SUCURSALES);
        // Índices para reseñas
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_productos_producto ON " + TABLE_RESENAS_PRODUCTOS + "(" + COLUMN_RESENA_PRODUCTO_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_productos_usuario ON " + TABLE_RESENAS_PRODUCTOS + "(" + COLUMN_RESENA_USUARIO_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_sucursales_sucursal ON " + TABLE_RESENAS_SUCURSALES + "(" + COLUMN_RESENA_SUCURSAL_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_sucursales_usuario ON " + TABLE_RESENAS_SUCURSALES + "(" + COLUMN_RESENA_USUARIO_ID + ")");
        
        Log.d(TAG, "Base de datos creada exitosamente");
        
        // Insertar datos de ejemplo
        insertarDatosEjemplo(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Actualizando base de datos de versión " + oldVersion + " a " + newVersion);
        
        if (oldVersion < 2) {
            // Agregar campo activo a la tabla clientes
            try {
                db.execSQL("ALTER TABLE " + TABLE_CLIENTES + " ADD COLUMN " + COLUMN_CLIENTE_ACTIVO + " INTEGER DEFAULT 1");
                Log.d(TAG, "Campo 'activo' agregado a la tabla clientes");
            } catch (Exception e) {
                Log.e(TAG, "Error al agregar campo activo: " + e.getMessage());
                // Si falla, recrear la tabla
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CANJES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITAS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BENEFICIOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUCURSALES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                onCreate(db);
            }
        }
        if (oldVersion < 3) {
            // Agregar tablas de reseñas para productos y sucursales
            db.execSQL(CREATE_TABLE_RESENAS_PRODUCTOS);
            db.execSQL(CREATE_TABLE_RESENAS_SUCURSALES);
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_productos_producto ON " + TABLE_RESENAS_PRODUCTOS + "(" + COLUMN_RESENA_PRODUCTO_ID + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_productos_usuario ON " + TABLE_RESENAS_PRODUCTOS + "(" + COLUMN_RESENA_USUARIO_ID + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_sucursales_sucursal ON " + TABLE_RESENAS_SUCURSALES + "(" + COLUMN_RESENA_SUCURSAL_ID + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_resenas_sucursales_usuario ON " + TABLE_RESENAS_SUCURSALES + "(" + COLUMN_RESENA_USUARIO_ID + ")");
        }
        if (oldVersion < 4) {
            // Migración v4: Establecer imagen_url para productos que no la tienen
            try {
                String defaultImage = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop";
                db.execSQL("UPDATE " + TABLE_PRODUCTOS + " SET " + COLUMN_PRODUCTO_IMAGEN_URL + "='" + defaultImage + "' " +
                        "WHERE " + COLUMN_PRODUCTO_IMAGEN_URL + " IS NULL OR " + COLUMN_PRODUCTO_IMAGEN_URL + "='' ");
                Log.d(TAG, "Migración v4: imagen_url actualizada para productos sin URL");
            } catch (Exception e) {
                Log.e(TAG, "Error en migración v4 (imagen_url): " + e.getMessage());
            }
        }
        if (oldVersion < 5) {
            // Migración v5: Agregar imagen_url a sucursales y asignar un valor por defecto
            try {
                db.execSQL("ALTER TABLE " + TABLE_SUCURSALES + " ADD COLUMN " + COLUMN_SUCURSAL_IMAGEN_URL + " TEXT");
                String defaultSucursalImage = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&auto=format&fit=crop";
                db.execSQL("UPDATE " + TABLE_SUCURSALES + " SET " + COLUMN_SUCURSAL_IMAGEN_URL + "='" + defaultSucursalImage + "' " +
                        "WHERE " + COLUMN_SUCURSAL_IMAGEN_URL + " IS NULL OR " + COLUMN_SUCURSAL_IMAGEN_URL + "='' ");
                Log.d(TAG, "Migración v5: imagen_url agregada y establecida para sucursales");
            } catch (Exception e) {
                Log.e(TAG, "Error en migración v5 (sucursales.imagen_url): " + e.getMessage());
            }
        }
    }
    
    private void insertarDatosEjemplo(SQLiteDatabase db) {
        Log.d(TAG, "Insertando datos de ejemplo...");
        
        // Insertar clientes de ejemplo
        ContentValues clienteValues = new ContentValues();
        clienteValues.put(COLUMN_CLIENTE_NOMBRE, "Juan Pérez");
        clienteValues.put(COLUMN_CLIENTE_EMAIL, "juan@email.com");
        clienteValues.put(COLUMN_CLIENTE_TELEFONO, "123456789");
        clienteValues.put(COLUMN_CLIENTE_PASSWORD, "123456");
        clienteValues.put(COLUMN_CLIENTE_PUNTOS, 100);
        clienteValues.put(COLUMN_CLIENTE_ACTIVO, 1);
        db.insert(TABLE_CLIENTES, null, clienteValues);
        
        // Insertar productos de ejemplo
        ContentValues productoValues = new ContentValues();
        productoValues.put(COLUMN_PRODUCTO_NOMBRE, "Café Americano");
        productoValues.put(COLUMN_PRODUCTO_DESCRIPCION, "Café negro tradicional");
        productoValues.put(COLUMN_PRODUCTO_PRECIO, 2500.0);
        productoValues.put(COLUMN_PRODUCTO_CATEGORIA, "Bebidas Calientes");
        productoValues.put(COLUMN_PRODUCTO_DISPONIBLE, 1);
        // URL pública de imagen para validar carga en cliente y admin
        productoValues.put(COLUMN_PRODUCTO_IMAGEN_URL, "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop");
        db.insert(TABLE_PRODUCTOS, null, productoValues);
        
        // Insertar sucursales de ejemplo
        ContentValues sucursalValues = new ContentValues();
        sucursalValues.put(COLUMN_SUCURSAL_NOMBRE, "Sucursal Centro");
        sucursalValues.put(COLUMN_SUCURSAL_DIRECCION, "Calle Principal 123");
        sucursalValues.put(COLUMN_SUCURSAL_TELEFONO, "987654321");
        sucursalValues.put(COLUMN_SUCURSAL_HORARIO_APERTURA, "07:00");
        sucursalValues.put(COLUMN_SUCURSAL_HORARIO_CIERRE, "22:00");
        sucursalValues.put(COLUMN_SUCURSAL_LATITUD, -12.0464);
        sucursalValues.put(COLUMN_SUCURSAL_LONGITUD, -77.0428);
        sucursalValues.put(COLUMN_SUCURSAL_IMAGEN_URL, "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&auto=format&fit=crop");
        db.insert(TABLE_SUCURSALES, null, sucursalValues);
        
        // Insertar beneficios de ejemplo
        ContentValues beneficioValues = new ContentValues();
        beneficioValues.put(COLUMN_BENEFICIO_NOMBRE, "Café Gratis");
        beneficioValues.put(COLUMN_BENEFICIO_DESCRIPCION, "Un café americano gratis");
        beneficioValues.put(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS, 50);
        beneficioValues.put(COLUMN_BENEFICIO_TIPO, "Producto");
        beneficioValues.put(COLUMN_BENEFICIO_ACTIVO, 1);
        db.insert(TABLE_BENEFICIOS, null, beneficioValues);
        
        Log.d(TAG, "Datos de ejemplo insertados");
    }
    
    // MÉTODOS CRUD PARA CLIENTES
    
    public long insertarCliente(Cliente cliente) {
        Log.d(TAG, "Iniciando inserción de cliente: " + cliente.getNombre());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_CLIENTE_NOMBRE, cliente.getNombre());
        values.put(COLUMN_CLIENTE_EMAIL, cliente.getEmail());
        values.put(COLUMN_CLIENTE_TELEFONO, cliente.getTelefono());
        values.put(COLUMN_CLIENTE_PASSWORD, cliente.getPassword());
        values.put(COLUMN_CLIENTE_PUNTOS, cliente.getPuntosAcumulados());
        values.put(COLUMN_CLIENTE_ACTIVO, cliente.isActivo() ? 1 : 0);
        
        Log.d(TAG, "Datos del cliente a insertar:");
        Log.d(TAG, "Nombre: " + cliente.getNombre());
        Log.d(TAG, "Email: " + cliente.getEmail());
        Log.d(TAG, "Teléfono: " + cliente.getTelefono());
        Log.d(TAG, "Puntos: " + cliente.getPuntosAcumulados());
        Log.d(TAG, "Activo: " + cliente.isActivo());
        
        long id = db.insert(TABLE_CLIENTES, null, values);
        
        if (id == -1) {
            Log.e(TAG, "Error al insertar cliente en la base de datos");
        } else {
            Log.d(TAG, "Cliente insertado exitosamente con ID: " + id);
        }
        
        db.close();
        
        return id;
    }
    
    public Cliente obtenerClientePorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cliente cliente = null;
        
        Cursor cursor = db.query(TABLE_CLIENTES, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            cliente = new Cliente();
            cliente.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            cliente.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_NOMBRE)));
            cliente.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_EMAIL)));
            cliente.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_TELEFONO)));
            cliente.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PASSWORD)));
            cliente.setPuntosAcumulados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PUNTOS)));
            cliente.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_ACTIVO)) == 1);
            cursor.close();
        }
        
        db.close();
        return cliente;
    }
    
    public Cliente obtenerClientePorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cliente cliente = null;
        
        Cursor cursor = db.query(TABLE_CLIENTES, null, COLUMN_CLIENTE_EMAIL + "=?", 
                new String[]{email}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            cliente = new Cliente();
            cliente.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            cliente.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_NOMBRE)));
            cliente.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_EMAIL)));
            cliente.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_TELEFONO)));
            cliente.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PASSWORD)));
            cliente.setPuntosAcumulados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PUNTOS)));
            cliente.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_ACTIVO)) == 1);
            cursor.close();
        }
        
        db.close();
        return cliente;
    }
    
    public List<Cliente> obtenerTodosLosClientes() {
        List<Cliente> clientes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CLIENTES, null, null, null, null, null, COLUMN_CLIENTE_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Cliente cliente = new Cliente();
                cliente.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                cliente.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_NOMBRE)));
                cliente.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_EMAIL)));
                cliente.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_TELEFONO)));
                cliente.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PASSWORD)));
                cliente.setPuntosAcumulados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PUNTOS)));
                cliente.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_ACTIVO)) == 1);
                clientes.add(cliente);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return clientes;
    }

    public List<Cliente> obtenerClientesActivos() {
        List<Cliente> clientes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CLIENTES, null, COLUMN_CLIENTE_ACTIVO + "=?", 
                new String[]{"1"}, null, null, COLUMN_CLIENTE_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Cliente cliente = new Cliente();
                cliente.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                cliente.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_NOMBRE)));
                cliente.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_EMAIL)));
                cliente.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_TELEFONO)));
                cliente.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PASSWORD)));
                cliente.setPuntosAcumulados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_PUNTOS)));
                cliente.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLIENTE_ACTIVO)) == 1);
                clientes.add(cliente);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return clientes;
    }

    public int actualizarCliente(Cliente cliente) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_CLIENTE_NOMBRE, cliente.getNombre());
        values.put(COLUMN_CLIENTE_EMAIL, cliente.getEmail());
        values.put(COLUMN_CLIENTE_TELEFONO, cliente.getTelefono());
        values.put(COLUMN_CLIENTE_PASSWORD, cliente.getPassword());
        values.put(COLUMN_CLIENTE_PUNTOS, cliente.getPuntosAcumulados());
        values.put(COLUMN_CLIENTE_ACTIVO, cliente.isActivo() ? 1 : 0);
        
        int rowsAffected = db.update(TABLE_CLIENTES, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(cliente.getId())});
        db.close();
        
        Log.d(TAG, "Cliente actualizado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarCliente(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_CLIENTES, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Cliente eliminado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoClientes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CLIENTES, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
    
    //  MÉTODOS CRUD PARA BENEFICIOS
    
    public long insertarBeneficio(Beneficio beneficio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_BENEFICIO_NOMBRE, beneficio.getNombre());
        values.put(COLUMN_BENEFICIO_DESCRIPCION, beneficio.getDescripcion());
        values.put(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS, beneficio.getVisitasRequeridas());
        values.put(COLUMN_BENEFICIO_TIPO, beneficio.getTipo());
        values.put(COLUMN_BENEFICIO_ACTIVO, beneficio.isActivo() ? 1 : 0);
        
        long id = db.insert(TABLE_BENEFICIOS, null, values);
        db.close();
        
        Log.d(TAG, "Beneficio insertado con ID: " + id);
        return id;
    }
    
    public Beneficio obtenerBeneficioPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Beneficio beneficio = null;
        
        Cursor cursor = db.query(TABLE_BENEFICIOS, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            beneficio = new Beneficio();
            beneficio.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            beneficio.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_NOMBRE)));
            beneficio.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_DESCRIPCION)));
            beneficio.setVisitasRequeridas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS)));
            beneficio.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_TIPO)));
            beneficio.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_ACTIVO)) == 1);
            cursor.close();
        }
        
        db.close();
        return beneficio;
    }
    
    public List<Beneficio> obtenerTodosLosBeneficios() {
        List<Beneficio> beneficios = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_BENEFICIOS, null, null, null, null, null, COLUMN_BENEFICIO_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Beneficio beneficio = new Beneficio();
                beneficio.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                beneficio.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_NOMBRE)));
                beneficio.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_DESCRIPCION)));
                beneficio.setVisitasRequeridas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS)));
                beneficio.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_TIPO)));
                beneficio.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_ACTIVO)) == 1);
                beneficios.add(beneficio);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return beneficios;
    }
    
    public List<Beneficio> obtenerBeneficiosActivos() {
        List<Beneficio> beneficios = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_BENEFICIOS, null, COLUMN_BENEFICIO_ACTIVO + "=?", 
                new String[]{"1"}, null, null, COLUMN_BENEFICIO_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Beneficio beneficio = new Beneficio();
                beneficio.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                beneficio.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_NOMBRE)));
                beneficio.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_DESCRIPCION)));
                beneficio.setVisitasRequeridas(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS)));
                beneficio.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_TIPO)));
                beneficio.setActivo(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BENEFICIO_ACTIVO)) == 1);
                beneficios.add(beneficio);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return beneficios;
    }
    
    public int actualizarBeneficio(Beneficio beneficio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_BENEFICIO_NOMBRE, beneficio.getNombre());
        values.put(COLUMN_BENEFICIO_DESCRIPCION, beneficio.getDescripcion());
        values.put(COLUMN_BENEFICIO_PUNTOS_REQUERIDOS, beneficio.getVisitasRequeridas());
        values.put(COLUMN_BENEFICIO_TIPO, beneficio.getTipo());
        values.put(COLUMN_BENEFICIO_ACTIVO, beneficio.isActivo() ? 1 : 0);
        
        int rowsAffected = db.update(TABLE_BENEFICIOS, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(beneficio.getId())});
        db.close();
        
        Log.d(TAG, "Beneficio actualizado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarBeneficio(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_BENEFICIOS, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Beneficio eliminado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoBeneficios() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BENEFICIOS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
    
    // MÉTODOS CRUD PARA VISITAS
    
    public long insertarVisita(Visita visita) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_VISITA_CLIENTE_ID, visita.getUserId());
        values.put(COLUMN_VISITA_SUCURSAL_ID, visita.getSucursal());
        values.put(COLUMN_VISITA_FECHA, visita.getFechaVisita());
        values.put(COLUMN_VISITA_PUNTOS_GANADOS, visita.getPuntosGanados());
        
        long id = db.insert(TABLE_VISITAS, null, values);
        db.close();
        
        Log.d(TAG, "Visita insertada con ID: " + id);
        return id;
    }
    
    public Visita obtenerVisitaPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Visita visita = null;
        
        Cursor cursor = db.query(TABLE_VISITAS, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            visita = new Visita();
            visita.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            visita.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_CLIENTE_ID)));
            visita.setSucursal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_SUCURSAL_ID)));
            visita.setFechaVisita(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_VISITA_FECHA)));
            visita.setPuntosGanados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VISITA_PUNTOS_GANADOS)));
            cursor.close();
        }
        
        db.close();
        return visita;
    }
    
    public List<Visita> obtenerTodasLasVisitas() {
        List<Visita> visitas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_VISITAS, null, null, null, null, null, COLUMN_VISITA_FECHA + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Visita visita = new Visita();
                visita.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                visita.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_CLIENTE_ID)));
                visita.setSucursal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_SUCURSAL_ID)));
                visita.setFechaVisita(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_VISITA_FECHA)));
                visita.setPuntosGanados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VISITA_PUNTOS_GANADOS)));
                visitas.add(visita);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return visitas;
    }
    
    public List<Visita> obtenerVisitasPorCliente(int clienteId) {
        List<Visita> visitas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_VISITAS, null, COLUMN_VISITA_CLIENTE_ID + "=?", 
                new String[]{String.valueOf(clienteId)}, null, null, COLUMN_VISITA_FECHA + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Visita visita = new Visita();
                visita.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                visita.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_CLIENTE_ID)));
                visita.setSucursal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VISITA_SUCURSAL_ID)));
                visita.setFechaVisita(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_VISITA_FECHA)));
                visita.setPuntosGanados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VISITA_PUNTOS_GANADOS)));
                visitas.add(visita);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return visitas;
    }
    
    public int actualizarVisita(Visita visita) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_VISITA_CLIENTE_ID, visita.getUserId());
        values.put(COLUMN_VISITA_SUCURSAL_ID, visita.getSucursal());
        values.put(COLUMN_VISITA_FECHA, visita.getFechaVisita());
        values.put(COLUMN_VISITA_PUNTOS_GANADOS, visita.getPuntosGanados());
        
        int rowsAffected = db.update(TABLE_VISITAS, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(visita.getId())});
        db.close();
        
        Log.d(TAG, "Visita actualizada. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarVisita(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_VISITAS, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Visita eliminada. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoVisitas() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VISITAS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
    
    // MÉTODOS CRUD PARA CANJES
    
    public long insertarCanje(Canje canje) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_CANJE_CLIENTE_ID, canje.getClienteId());
        values.put(COLUMN_CANJE_BENEFICIO_ID, canje.getBeneficioId());
        values.put(COLUMN_CANJE_FECHA, canje.getFechaCanje());
        values.put(COLUMN_CANJE_PUNTOS_UTILIZADOS, canje.getPuntosUtilizados());
        values.put(COLUMN_CANJE_ESTADO, canje.getEstado());
        
        long id = db.insert(TABLE_CANJES, null, values);
        db.close();
        
        Log.d(TAG, "Canje insertado con ID: " + id);
        return id;
    }
    
    public Canje obtenerCanjePorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Canje canje = null;
        
        Cursor cursor = db.query(TABLE_CANJES, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            canje = new Canje();
            canje.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            canje.setClienteId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_CLIENTE_ID)));
            canje.setBeneficioId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_BENEFICIO_ID)));
            canje.setFechaCanje(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CANJE_FECHA)));
            canje.setPuntosUtilizados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_PUNTOS_UTILIZADOS)));
            canje.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CANJE_ESTADO)));
            cursor.close();
        }
        
        db.close();
        return canje;
    }
    
    public List<Canje> obtenerTodosLosCanjes() {
        List<Canje> canjes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CANJES, null, null, null, null, null, COLUMN_CANJE_FECHA + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Canje canje = new Canje();
                canje.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                canje.setClienteId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_CLIENTE_ID)));
                canje.setBeneficioId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_BENEFICIO_ID)));
                canje.setFechaCanje(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CANJE_FECHA)));
                canje.setPuntosUtilizados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_PUNTOS_UTILIZADOS)));
                canje.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CANJE_ESTADO)));
                canjes.add(canje);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return canjes;
    }
    
    public List<Canje> obtenerCanjesPorCliente(int clienteId) {
        List<Canje> canjes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CANJES, null, COLUMN_CANJE_CLIENTE_ID + "=?", 
                new String[]{String.valueOf(clienteId)}, null, null, COLUMN_CANJE_FECHA + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Canje canje = new Canje();
                canje.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                canje.setClienteId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_CLIENTE_ID)));
                canje.setBeneficioId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_BENEFICIO_ID)));
                canje.setFechaCanje(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CANJE_FECHA)));
                canje.setPuntosUtilizados(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CANJE_PUNTOS_UTILIZADOS)));
                canje.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CANJE_ESTADO)));
                canjes.add(canje);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return canjes;
    }
    
    public int actualizarCanje(Canje canje) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_CANJE_CLIENTE_ID, canje.getClienteId());
        values.put(COLUMN_CANJE_BENEFICIO_ID, canje.getBeneficioId());
        values.put(COLUMN_CANJE_FECHA, canje.getFechaCanje());
        values.put(COLUMN_CANJE_PUNTOS_UTILIZADOS, canje.getPuntosUtilizados());
        values.put(COLUMN_CANJE_ESTADO, canje.getEstado());
        
        int rowsAffected = db.update(TABLE_CANJES, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(canje.getId())});
        db.close();
        
        Log.d(TAG, "Canje actualizado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarCanje(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_CANJES, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Canje eliminado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoCanjes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CANJES, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
    
    // MÉTODOS CRUD PARA SUCURSALES
    
    public long insertarSucursal(Sucursal sucursal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SUCURSAL_NOMBRE, sucursal.getNombre());
        values.put(COLUMN_SUCURSAL_DIRECCION, sucursal.getDireccion());
        values.put(COLUMN_SUCURSAL_TELEFONO, sucursal.getTelefono());
        values.put(COLUMN_SUCURSAL_HORARIO_APERTURA, sucursal.getHorarioApertura());
        values.put(COLUMN_SUCURSAL_HORARIO_CIERRE, sucursal.getHorarioCierre());
        values.put(COLUMN_SUCURSAL_LATITUD, sucursal.getLatitud());
        values.put(COLUMN_SUCURSAL_LONGITUD, sucursal.getLongitud());
        values.put(COLUMN_SUCURSAL_IMAGEN_URL, sucursal.getImagenUrl());
        // Alinear estado con flag de activa del modelo
        values.put(COLUMN_SUCURSAL_ESTADO, sucursal.isActiva() ? "activo" : "inactivo");
        
        long id = db.insert(TABLE_SUCURSALES, null, values);
        db.close();
        
        Log.d(TAG, "Sucursal insertada con ID: " + id);
        return id;
    }
    
    public Sucursal obtenerSucursalPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Sucursal sucursal = null;
        
        Cursor cursor = db.query(TABLE_SUCURSALES, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            sucursal = new Sucursal();
            sucursal.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            sucursal.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_NOMBRE)));
            sucursal.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_DIRECCION)));
            sucursal.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_TELEFONO)));
            sucursal.setHorarioApertura(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_APERTURA)));
            sucursal.setHorarioCierre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_CIERRE)));
            sucursal.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LATITUD)));
            sucursal.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LONGITUD)));
            sucursal.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_IMAGEN_URL)));
            // Mapear estado a bandera activa
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_ESTADO));
            sucursal.setActiva("activo".equalsIgnoreCase(estado));
            cursor.close();
        }
        
        db.close();
        return sucursal;
    }
    
    public List<Sucursal> obtenerTodasLasSucursales() {
        List<Sucursal> sucursales = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_SUCURSALES, null, null, null, null, null, COLUMN_SUCURSAL_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Sucursal sucursal = new Sucursal();
                sucursal.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                sucursal.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_NOMBRE)));
                sucursal.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_DIRECCION)));
                sucursal.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_TELEFONO)));
                sucursal.setHorarioApertura(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_APERTURA)));
                sucursal.setHorarioCierre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_CIERRE)));
                sucursal.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LATITUD)));
                sucursal.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LONGITUD)));
                sucursal.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_IMAGEN_URL)));
                // Mapear estado a bandera activa
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_ESTADO));
                sucursal.setActiva("activo".equalsIgnoreCase(estado));
                sucursales.add(sucursal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return sucursales;
    }
    
    public List<Sucursal> obtenerSucursalesActivas() {
        List<Sucursal> sucursales = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Seleccionar por estado 'activo' para evitar columnas inexistentes
        String selection = COLUMN_SUCURSAL_ESTADO + " = ?";
        String[] selectionArgs = {"activo"};
        
        Cursor cursor = db.query(TABLE_SUCURSALES, null, selection, selectionArgs, null, null, COLUMN_SUCURSAL_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Sucursal sucursal = new Sucursal();
                sucursal.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                sucursal.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_NOMBRE)));
                sucursal.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_DIRECCION)));
                sucursal.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_TELEFONO)));
                sucursal.setHorarioApertura(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_APERTURA)));
                sucursal.setHorarioCierre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_HORARIO_CIERRE)));
                sucursal.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LATITUD)));
                sucursal.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_LONGITUD)));
                sucursal.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_IMAGEN_URL)));
                // Mapear estado a bandera activa
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUCURSAL_ESTADO));
                sucursal.setActiva("activo".equalsIgnoreCase(estado));
                sucursales.add(sucursal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return sucursales;
    }
    
    public int actualizarSucursal(Sucursal sucursal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SUCURSAL_NOMBRE, sucursal.getNombre());
        values.put(COLUMN_SUCURSAL_DIRECCION, sucursal.getDireccion());
        values.put(COLUMN_SUCURSAL_TELEFONO, sucursal.getTelefono());
        values.put(COLUMN_SUCURSAL_HORARIO_APERTURA, sucursal.getHorarioApertura());
        values.put(COLUMN_SUCURSAL_HORARIO_CIERRE, sucursal.getHorarioCierre());
        values.put(COLUMN_SUCURSAL_LATITUD, sucursal.getLatitud());
        values.put(COLUMN_SUCURSAL_LONGITUD, sucursal.getLongitud());
        values.put(COLUMN_SUCURSAL_IMAGEN_URL, sucursal.getImagenUrl());
        
        int rowsAffected = db.update(TABLE_SUCURSALES, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(sucursal.getId())});
        db.close();
        
        Log.d(TAG, "Sucursal actualizada. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarSucursal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_SUCURSALES, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Sucursal eliminada. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoSucursales() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SUCURSALES, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }
    
    // MÉTODOS CRUD PARA PRODUCTOS
    
    public long insertarProducto(Producto producto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PRODUCTO_NOMBRE, producto.getNombre());
        values.put(COLUMN_PRODUCTO_DESCRIPCION, producto.getDescripcion());
        values.put(COLUMN_PRODUCTO_PRECIO, producto.getPrecio());
        values.put(COLUMN_PRODUCTO_CATEGORIA, producto.getCategoria());
        values.put(COLUMN_PRODUCTO_DISPONIBLE, producto.isDisponible() ? 1 : 0);
        values.put(COLUMN_PRODUCTO_IMAGEN_URL, producto.getImagenUrl());
        
        long id = db.insert(TABLE_PRODUCTOS, null, values);
        db.close();
        
        Log.d(TAG, "Producto insertado con ID: " + id);
        return id;
    }
    
    public Producto obtenerProductoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Producto producto = null;
        
        Cursor cursor = db.query(TABLE_PRODUCTOS, null, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            producto = new Producto();
            producto.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
            producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_NOMBRE)));
            producto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DESCRIPCION)));
            producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_PRECIO)));
            producto.setCategoria(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_CATEGORIA)));
            producto.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DISPONIBLE)) == 1);
            producto.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_IMAGEN_URL)));
            cursor.close();
        }
        
        db.close();
        return producto;
    }
    
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PRODUCTOS, null, null, null, null, null, COLUMN_PRODUCTO_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Producto producto = new Producto();
                producto.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_NOMBRE)));
                producto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DESCRIPCION)));
                producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_PRECIO)));
                producto.setCategoria(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_CATEGORIA)));
                producto.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DISPONIBLE)) == 1);
                producto.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_IMAGEN_URL)));
                productos.add(producto);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return productos;
    }
    
    public List<Producto> obtenerProductosDisponibles() {
        List<Producto> productos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = COLUMN_PRODUCTO_DISPONIBLE + " = ?";
        String[] selectionArgs = {"1"};
        
        Cursor cursor = db.query(TABLE_PRODUCTOS, null, selection, selectionArgs, null, null, COLUMN_PRODUCTO_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Producto producto = new Producto();
                producto.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_NOMBRE)));
                producto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DESCRIPCION)));
                producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_PRECIO)));
                producto.setCategoria(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_CATEGORIA)));
                producto.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DISPONIBLE)) == 1);
                producto.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_IMAGEN_URL)));
                productos.add(producto);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return productos;
    }
    
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        List<Producto> productos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PRODUCTOS, null, COLUMN_PRODUCTO_CATEGORIA + "=?", 
                new String[]{categoria}, null, null, COLUMN_PRODUCTO_NOMBRE);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Producto producto = new Producto();
                producto.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                producto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_NOMBRE)));
                producto.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DESCRIPCION)));
                producto.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_PRECIO)));
                producto.setCategoria(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_CATEGORIA)));
                producto.setDisponible(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_DISPONIBLE)) == 1);
                producto.setImagenUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCTO_IMAGEN_URL)));
                productos.add(producto);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        db.close();
        return productos;
    }
    
    public int actualizarProducto(Producto producto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PRODUCTO_NOMBRE, producto.getNombre());
        values.put(COLUMN_PRODUCTO_DESCRIPCION, producto.getDescripcion());
        values.put(COLUMN_PRODUCTO_PRECIO, producto.getPrecio());
        values.put(COLUMN_PRODUCTO_CATEGORIA, producto.getCategoria());
        values.put(COLUMN_PRODUCTO_DISPONIBLE, producto.isDisponible() ? 1 : 0);
        values.put(COLUMN_PRODUCTO_IMAGEN_URL, producto.getImagenUrl());
        
        int rowsAffected = db.update(TABLE_PRODUCTOS, values, COLUMN_ID + "=?", 
                new String[]{String.valueOf(producto.getId())});
        db.close();
        
        Log.d(TAG, "Producto actualizado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int eliminarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTOS, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
        db.close();
        
        Log.d(TAG, "Producto eliminado. Filas afectadas: " + rowsAffected);
        return rowsAffected;
    }
    
    public int obtenerConteoProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTOS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    // =====================
    // Reseñas de Productos
    // =====================
    public long insertarResenaProducto(ResenaProducto resena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESENA_PRODUCTO_ID, resena.getProductoId());
        values.put(COLUMN_RESENA_USUARIO_ID, resena.getUsuarioId());
        values.put(COLUMN_RESENA_CALIFICACION, resena.getCalificacion());
        values.put(COLUMN_RESENA_COMENTARIO, resena.getComentario());
        values.put(COLUMN_RESENA_FECHA_CREACION, resena.getFechaCreacion());
        values.put(COLUMN_RESENA_FECHA_ACTUALIZACION, resena.getFechaActualizacion());
        long id = db.insert(TABLE_RESENAS_PRODUCTOS, null, values);
        db.close();
        return id;
    }

    public List<ResenaProducto> obtenerResenasProducto(int productoId, int limit, int offset) {
        List<ResenaProducto> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Nota: En algunas versiones de SQLite en Android, los parámetros enlazados no se 
        // manejan bien en LIMIT/OFFSET. Para asegurar compatibilidad, incrustamos los
        // valores de paginación directamente en la consulta.
        String sql = "SELECT * FROM " + TABLE_RESENAS_PRODUCTOS +
                " WHERE " + COLUMN_RESENA_PRODUCTO_ID + " = ?" +
                " ORDER BY " + COLUMN_RESENA_FECHA_CREACION + " DESC" +
                " LIMIT " + Math.max(0, limit) +
                " OFFSET " + Math.max(0, offset);
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(productoId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ResenaProducto r = new ResenaProducto();
                r.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                r.setProductoId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_PRODUCTO_ID)));
                r.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_USUARIO_ID)));
                r.setCalificacion(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_CALIFICACION)));
                r.setComentario(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESENA_COMENTARIO)));
                r.setFechaCreacion(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESENA_FECHA_CREACION)));
                r.setFechaActualizacion(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESENA_FECHA_ACTUALIZACION)));
                lista.add(r);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return lista;
    }

    public int actualizarResenaProducto(ResenaProducto resena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESENA_CALIFICACION, resena.getCalificacion());
        values.put(COLUMN_RESENA_COMENTARIO, resena.getComentario());
        values.put(COLUMN_RESENA_FECHA_ACTUALIZACION, resena.getFechaActualizacion());
        int rows = db.update(TABLE_RESENAS_PRODUCTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(resena.getId())});
        db.close();
        return rows;
    }

    public int eliminarResenaProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RESENAS_PRODUCTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public PromedioCalificacion obtenerPromedioCalificacionProducto(int productoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(" + COLUMN_RESENA_CALIFICACION + ") AS promedio, COUNT(*) AS cantidad FROM " + TABLE_RESENAS_PRODUCTOS + " WHERE " + COLUMN_RESENA_PRODUCTO_ID + " = ?",
                new String[]{String.valueOf(productoId)}
        );
        double promedio = 0.0;
        int cantidad = 0;
        if (cursor != null && cursor.moveToFirst()) {
            promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio"));
            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"));
            cursor.close();
        }
        db.close();
        return new PromedioCalificacion(promedio, cantidad);
    }

    // =====================
    // Reseñas de Sucursales
    // =====================
    public long insertarResenaSucursal(ResenaSucursal resena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESENA_SUCURSAL_ID, resena.getSucursalId());
        values.put(COLUMN_RESENA_USUARIO_ID, resena.getUsuarioId());
        values.put(COLUMN_RESENA_CALIFICACION, resena.getCalificacion());
        values.put(COLUMN_RESENA_COMENTARIO, resena.getComentario());
        values.put(COLUMN_RESENA_FECHA_CREACION, resena.getFechaCreacion());
        values.put(COLUMN_RESENA_FECHA_ACTUALIZACION, resena.getFechaActualizacion());
        long id = db.insert(TABLE_RESENAS_SUCURSALES, null, values);
        db.close();
        return id;
    }

    public List<ResenaSucursal> obtenerResenasSucursal(int sucursalId, int limit, int offset) {
        List<ResenaSucursal> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Igual que en productos, evitamos parámetros enlazados para LIMIT/OFFSET
        String sql = "SELECT * FROM " + TABLE_RESENAS_SUCURSALES +
                " WHERE " + COLUMN_RESENA_SUCURSAL_ID + " = ?" +
                " ORDER BY " + COLUMN_RESENA_FECHA_CREACION + " DESC" +
                " LIMIT " + Math.max(0, limit) +
                " OFFSET " + Math.max(0, offset);
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(sucursalId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ResenaSucursal r = new ResenaSucursal();
                r.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                r.setSucursalId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_SUCURSAL_ID)));
                r.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_USUARIO_ID)));
                r.setCalificacion(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESENA_CALIFICACION)));
                r.setComentario(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESENA_COMENTARIO)));
                r.setFechaCreacion(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESENA_FECHA_CREACION)));
                r.setFechaActualizacion(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESENA_FECHA_ACTUALIZACION)));
                lista.add(r);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return lista;
    }

    public int actualizarResenaSucursal(ResenaSucursal resena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESENA_CALIFICACION, resena.getCalificacion());
        values.put(COLUMN_RESENA_COMENTARIO, resena.getComentario());
        values.put(COLUMN_RESENA_FECHA_ACTUALIZACION, resena.getFechaActualizacion());
        int rows = db.update(TABLE_RESENAS_SUCURSALES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(resena.getId())});
        db.close();
        return rows;
    }

    public int eliminarResenaSucursal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RESENAS_SUCURSALES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public PromedioCalificacion obtenerPromedioCalificacionSucursal(int sucursalId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(" + COLUMN_RESENA_CALIFICACION + ") AS promedio, COUNT(*) AS cantidad FROM " + TABLE_RESENAS_SUCURSALES + " WHERE " + COLUMN_RESENA_SUCURSAL_ID + " = ?",
                new String[]{String.valueOf(sucursalId)}
        );
        double promedio = 0.0;
        int cantidad = 0;
        if (cursor != null && cursor.moveToFirst()) {
            promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio"));
            cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"));
            cursor.close();
        }
        db.close();
        return new PromedioCalificacion(promedio, cantidad);
    }

    // ===============================
// 🔹 REINICIAR VISITAS DEL CLIENTE
// ===============================
    /**
     * Reinicia (elimina) todas las visitas registradas de un cliente,
     * utilizado después de realizar un canje de beneficio por sellos.
     */
    public void reiniciarVisitasCliente(int clienteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int filas = db.delete("visitas", "cliente_id = ?", new String[]{String.valueOf(clienteId)});
            Log.d(TAG, "✅ Visitas reiniciadas para cliente ID: " + clienteId + ". Filas eliminadas: " + filas);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al reiniciar visitas del cliente", e);
        } finally {
            db.close();
        }
    }
}
