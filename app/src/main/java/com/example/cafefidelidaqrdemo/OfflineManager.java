package com.example.cafefidelidaqrdemo;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.UsuarioDao;
import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor de almacenamiento offline para usuarios
 * Maneja la sincronización entre Firebase y SQLite local
 */
public class OfflineManager {
    private static final String TAG = "OfflineManager";
    private static OfflineManager instance;
    private CafeFidelidadDatabase database;
    private UsuarioDao usuarioDao;
    private ExecutorService executor;
    
    private OfflineManager(Context context) {
        database = Room.databaseBuilder(
                context.getApplicationContext(),
                CafeFidelidadDatabase.class,
                "cafe_fidelidad_database"
        ).build();
        
        usuarioDao = database.usuarioDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized OfflineManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineManager(context);
        }
        return instance;
    }
    
    /**
     * Guarda un usuario en la base de datos SQLite local
     * @param userData HashMap con los datos del usuario de Firebase
     * @param callback Callback para notificar el resultado
     */
    public void saveUserToLocal(HashMap<String, Object> userData, SaveUserCallback callback) {
        executor.execute(() -> {
            try {
                UsuarioEntity usuario = new UsuarioEntity();
                
                // Mapear datos de Firebase a UsuarioEntity
                usuario.setUid((String) userData.get("uid"));
                usuario.setNames((String) userData.get("names"));
                usuario.setEmail((String) userData.get("email"));
                usuario.setProveedor((String) userData.get("proveedor"));
                usuario.setEstado((String) userData.get("estado"));
                usuario.setImagen((String) userData.get("imagen"));
                usuario.setTelefono((String) userData.get("telefono"));
                usuario.setFechaNacimiento((String) userData.get("fechaNacimiento"));
                
                // Convertir valores numéricos
                Object puntosObj = userData.get("puntos");
                if (puntosObj instanceof Long) {
                    usuario.setPuntos(((Long) puntosObj).intValue());
                } else if (puntosObj instanceof Integer) {
                    usuario.setPuntos((Integer) puntosObj);
                } else {
                    usuario.setPuntos(0);
                }
                
                usuario.setNivel((String) userData.get("nivel"));
                
                Object totalComprasObj = userData.get("totalCompras");
                if (totalComprasObj instanceof Double) {
                    usuario.setTotalCompras((Double) totalComprasObj);
                } else if (totalComprasObj instanceof Long) {
                    usuario.setTotalCompras(((Long) totalComprasObj).doubleValue());
                } else {
                    usuario.setTotalCompras(0.0);
                }
                
                Object dateObj = userData.get("date");
                if (dateObj instanceof Long) {
                    usuario.setDate((Long) dateObj);
                } else {
                    usuario.setDate(System.currentTimeMillis());
                }
                
                Object ultimaVisitaObj = userData.get("ultimaVisita");
                if (ultimaVisitaObj instanceof Long) {
                    usuario.setUltimaVisita((Long) ultimaVisitaObj);
                } else {
                    usuario.setUltimaVisita(System.currentTimeMillis());
                }
                
                // Configurar flags de sincronización
                usuario.setNeedsSync(false);
                usuario.setLastSync(System.currentTimeMillis());
                
                // Insertar o actualizar usuario
                usuarioDao.insertUsuario(usuario);
                
                // Marcar como sincronizado
                usuarioDao.markAsSynced(usuario.getUid(), System.currentTimeMillis());
                
                Log.d(TAG, "Usuario guardado en SQLite: " + usuario.getEmail());
                
                // Notificar éxito en el hilo principal
                if (callback != null) {
                    callback.onSuccess();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar usuario en SQLite", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Obtiene un usuario de la base de datos local por UID
     * @param uid UID del usuario
     * @param callback Callback para recibir el resultado
     */
    public void getUserFromLocal(String uid, GetUserCallback callback) {
        executor.execute(() -> {
            try {
                UsuarioEntity usuario = usuarioDao.getUsuarioById(uid);
                if (callback != null) {
                    if (usuario != null) {
                        callback.onSuccess(usuario);
                    } else {
                        callback.onUserNotFound();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener usuario de SQLite", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Verifica si un usuario existe en la base de datos local
     * @param uid UID del usuario
     * @param callback Callback para recibir el resultado
     */
    public void userExistsInLocal(String uid, UserExistsCallback callback) {
        executor.execute(() -> {
            try {
                UsuarioEntity usuario = usuarioDao.getUsuarioById(uid);
                boolean exists = (usuario != null);
                if (callback != null) {
                    callback.onResult(exists);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al verificar existencia de usuario", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * Actualiza los puntos de un usuario en la base de datos local
     * @param uid UID del usuario
     * @param puntos Nuevos puntos
     * @param totalCompras Total de compras
     * @param nivel Nivel del usuario
     * @param callback Callback para notificar el resultado
     */
    public void updateUserPoints(String uid, int puntos, double totalCompras, String nivel, UpdateCallback callback) {
        executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                usuarioDao.updatePuntosYCompras(uid, puntos, totalCompras, nivel, currentTime);
                
                Log.d(TAG, "Puntos actualizados para usuario: " + uid);
                
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar puntos", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    // Interfaces para callbacks
    public interface SaveUserCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface GetUserCallback {
        void onSuccess(UsuarioEntity usuario);
        void onUserNotFound();
        void onError(String error);
    }
    
    public interface UserExistsCallback {
        void onResult(boolean exists);
        void onError(String error);
    }
    
    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Cierra la base de datos y libera recursos
     */
    public void close() {
        if (database != null) {
            database.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}