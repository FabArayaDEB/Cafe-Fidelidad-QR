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
import com.example.cafefidelidaqrdemo.data.entities.ReporteEntity;

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
        ReporteEntity.class
    },
    version = 4,
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
                    .addCallback(roomCallback)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Migraciones para nuevas entidades
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
     * Migración de versión 1 a 2 - Agregar nuevas entidades del modelo ER
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Las nuevas tablas se crearán automáticamente por Room
            // cuando se detecten las nuevas entidades
        }
    };
    
    /**
     * Migración de versión 2 a 3 - Agregar tabla beneficios mejorada
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Crear nueva tabla beneficios con estructura mejorada
            database.execSQL("CREATE TABLE IF NOT EXISTS `beneficios_new` (" +
                    "`id` TEXT NOT NULL, " +
                    "`nombre` TEXT, " +
                    "`descripcion` TEXT, " +
                    "`tipo` TEXT, " +
                    "`valor` REAL NOT NULL, " +
                    "`reglasJson` TEXT, " +
                    "`fechaInicio` INTEGER, " +
                    "`fechaFin` INTEGER, " +
                    "`sucursalesAplicables` TEXT, " +
                    "`activo` INTEGER NOT NULL, " +
                    "`fechaCreacion` INTEGER, " +
                    "`fechaModificacion` INTEGER, " +
                    "PRIMARY KEY(`id`))");
            
            // Migrar datos existentes si los hay
            database.execSQL("INSERT INTO beneficios_new (id, nombre, descripcion, tipo, valor, reglasJson, activo, fechaCreacion, fechaModificacion) " +
                    "SELECT id_beneficio, nombre, tipo, regla, COALESCE(descuento_pct, descuento_monto, 0), regla, " +
                    "CASE WHEN estado = 'activo' THEN 1 ELSE 0 END, " +
                    "COALESCE(lastSync, 0), COALESCE(lastSync, 0) " +
                    "FROM beneficios WHERE id_beneficio IS NOT NULL");
            
            // Eliminar tabla antigua y renombrar la nueva
            database.execSQL("DROP TABLE IF EXISTS beneficios");
            database.execSQL("ALTER TABLE beneficios_new RENAME TO beneficios");
        }
    };
    
    /**
     * Migración de versión 3 a 4 - Agregar tabla reportes
     */
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // La tabla reportes se creará automáticamente por Room
            // cuando detecte la nueva entidad ReporteEntity
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