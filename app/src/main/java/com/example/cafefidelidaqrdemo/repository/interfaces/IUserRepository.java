package com.example.cafefidelidaqrdemo.repository.interfaces;

import androidx.lifecycle.LiveData;

import com.example.cafefidelidaqrdemo.database.entities.UsuarioEntity;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.List;

/**
 * Interfaz que define el contrato para el repositorio de usuarios
 * Siguiendo principios SOLID para la arquitectura MVVM
 */
public interface IUserRepository {
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Obtiene todos los usuarios
     */
    LiveData<List<UsuarioEntity>> getAllUsers();
    
    /**
     * Obtiene un usuario por ID
     */
    LiveData<UsuarioEntity> getUserById(String userId);
    
    /**
     * Obtiene el usuario actual autenticado
     */
    LiveData<UsuarioEntity> getCurrentUser();
    
    /**
     * Crea un nuevo usuario
     */
    void createUser(UsuarioEntity user, BaseRepository.SimpleCallback callback);
    
    /**
     * Actualiza un usuario existente
     */
    void updateUser(UsuarioEntity user, BaseRepository.SimpleCallback callback);
    
    /**
     * Elimina un usuario
     */
    void deleteUser(String userId, BaseRepository.SimpleCallback callback);
    
    // ==================== OPERACIONES DE AUTENTICACIÓN ====================
    
    /**
     * Autentica un usuario con email y contraseña
     */
    void authenticateUser(String email, String password, BaseRepository.RepositoryCallback<UsuarioEntity> callback);
    
    /**
     * Registra un nuevo usuario
     */
    void registerUser(String email, String password, String nombre, BaseRepository.RepositoryCallback<UsuarioEntity> callback);
    
    /**
     * Cierra la sesión del usuario actual
     */
    void logout(BaseRepository.SimpleCallback callback);
    
    /**
     * Verifica si hay un usuario autenticado
     */
    LiveData<Boolean> isUserAuthenticated();
    
    // ==================== OPERACIONES DE PERFIL ====================
    
    /**
     * Actualiza el perfil del usuario
     */
    void updateProfile(String userId, String nombre, String telefono, String fechaNacimiento, BaseRepository.SimpleCallback callback);
    
    /**
     * Actualiza la foto de perfil
     */
    void updateProfilePhoto(String userId, String photoUrl, BaseRepository.SimpleCallback callback);
    
    /**
     * Cambia la contraseña del usuario
     */
    void changePassword(String userId, String currentPassword, String newPassword, BaseRepository.SimpleCallback callback);
    
    // ==================== SINCRONIZACIÓN ====================
    
    /**
     * Sincroniza datos del usuario con el servidor
     */
    void syncUserData(String userId, BaseRepository.SimpleCallback callback);
    
    /**
     * Obtiene el estado de sincronización
     */
    LiveData<Boolean> getSyncStatus();
    
    // ==================== LIMPIEZA ====================
    
    /**
     * Limpia el cache local del usuario
     */
    void clearUserCache();
}