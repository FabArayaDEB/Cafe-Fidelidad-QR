package com.example.cafefidelidaqrdemo.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.cafefidelidaqrdemo.database.dao.UsuarioDao;
import com.example.cafefidelidaqrdemo.database.dao.TransaccionDao;
import com.example.cafefidelidaqrdemo.database.dao.ClienteDao;
import com.example.cafefidelidaqrdemo.database.dao.SucursalDao;
import com.example.cafefidelidaqrdemo.database.dao.ProductoDao;
import com.example.cafefidelidaqrdemo.database.dao.BeneficioDao;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.dao.CanjeDao;
import com.example.cafefidelidaqrdemo.database.dao.ReglaDao;
import com.example.cafefidelidaqrdemo.database.dao.ReporteDao;
import com.example.cafefidelidaqrdemo.database.dao.CompraDao;
import com.example.cafefidelidaqrdemo.database.dao.UbicacionDao;
import com.example.cafefidelidaqrdemo.database.dao.TableroDao;
import com.example.cafefidelidaqrdemo.database.Converters;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.database.entities.SucursalEntity;
import com.example.cafefidelidaqrdemo.database.entities.ProductoEntity;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.example.cafefidelidaqrdemo.database.entities.ReglaEntity;
import com.example.cafefidelidaqrdemo.database.entities.ReporteEntity;
import com.example.cafefidelidaqrdemo.database.entities.CompraEntity;
import com.example.cafefidelidaqrdemo.database.entities.UbicacionEntity;
import com.example.cafefidelidaqrdemo.database.entities.TableroEntity;

/**
 * Base de datos Room principal para cache offline
 */
@Database(
    entities = {
        UsuarioEntity.class, 
        TransaccionEntity.class,
        ClienteEntity.class,
        SucursalEntity.class,
        ProductoEntity.class,
        BeneficioEntity.class,
        VisitaEntity.class,
        CanjeEntity.class,
        ReglaEntity.class,
        ReporteEntity.class,
        CompraEntity.class,
        UbicacionEntity.class,
        TableroEntity.class
    },
    version = 1,
    exportSchema = false
)
@androidx.room.TypeConverters({Converters.class})
public abstract class CafeFidelidadDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "cafe_fidelidad_db";
    private static volatile CafeFidelidadDatabase INSTANCE;
    
    // DAOs abstractos
    public abstract UsuarioDao usuarioDao();
    public abstract TransaccionDao transaccionDao();
    public abstract ClienteDao clienteDao();
    public abstract SucursalDao sucursalDao();
    public abstract ProductoDao productoDao();
    public abstract BeneficioDao beneficioDao();
    public abstract VisitaDao visitaDao();
    public abstract CanjeDao canjeDao();
    public abstract ReglaDao reglaDao();
    public abstract ReporteDao reporteDao();
    public abstract CompraDao compraDao();
    public abstract UbicacionDao ubicacionDao();
    public abstract TableroDao tableroDao();
    
    /**
     * Singleton pattern para obtener instancia de la base de datos
     */
    public static CafeFidelidadDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CafeFidelidadDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CafeFidelidadDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // Permite recrear la BD si hay cambios de esquema
                    .addCallback(roomCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Callback para inicialización de la base de datos
     */
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Aquí se pueden ejecutar scripts de inicialización si es necesario
        }
        
        @Override
        public void onOpen(SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Configuraciones que se ejecutan cada vez que se abre la DB
        }
     };
    
    /**
     * Cierra la base de datos (útil para testing)
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    
    /**
     * Limpia el cache de la base de datos
     */
    @Override
    public void clearAllTables() {
        // Implementación personalizada para limpiar todas las tablas
        usuarioDao().deleteAll();
        transaccionDao().deleteAll();
        clienteDao().deleteAll();
        sucursalDao().deleteAll();
        productoDao().deleteAll();
        beneficioDao().deleteAllBeneficios();
        visitaDao().deleteAll();
        canjeDao().deleteAll();
        reglaDao().deleteAll();
        // reporteDao().deleteAll(); // Comentado hasta implementar método deleteAll
    }
}