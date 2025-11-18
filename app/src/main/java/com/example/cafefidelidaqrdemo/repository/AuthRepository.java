package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.models.Cliente;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository para autenticación local
 */
public class AuthRepository {
    
    private static AuthRepository instance;
    private SessionManager sessionManager;
    private Context context;
    
    // Credenciales simples
    private static final Map<String, LocalUser> USERS = new HashMap<>();
    static {
        USERS.put("cliente@test.com", new LocalUser("123456", "test", "cliente", "user_001"));
        USERS.put("admin@test.com", new LocalUser("123456", "Admin", "admin", "admin_001"));
    }
    
    private LocalUser currentUser;
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<LocalUser> currentUserLiveData = new MutableLiveData<>();
    
    private AuthRepository() {
        currentUser = null;
        currentUserLiveData.postValue(null);
        android.util.Log.d("AuthRepository", "AuthRepository inicializado");
    }
    
    /**
     * Establece el contexto para inicializar SessionManager
     */
    public void setContext(Context context) {
        this.context = context;
        if (context != null && sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
    }
    
    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }
    
    /**
     * Callback para operaciones de autenticación
     */
    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
    
    /**
     * Realiza login con email y contraseña - VERSIÓN LOCAL
     */
    public void login(String email, String password, AuthCallback<String> callback) {
        android.util.Log.d("AuthRepository", "Login simple - Email: " + email);
        
        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);
        
        // Validación simple y directa
        String cleanEmail = email.trim();
        String cleanPassword = password.trim();
        
        LocalUser user = USERS.get(cleanEmail);
        
        if (user != null && user.password.equals(cleanPassword)) {
            // Login exitoso (usuarios locales predefinidos: admin/cliente demo)
            android.util.Log.d("AuthRepository", "Login exitoso: " + user.name + " (" + user.role + ")");
            currentUser = user;
            currentUserLiveData.postValue(user);
            
            // Crear sesión
            if (sessionManager != null) {
                sessionManager.createSession(user.uid, cleanEmail, user.name);
            }
            
            isLoadingLiveData.postValue(false);
            callback.onSuccess(user.uid);
            return;
        }

        // Fallback: intentar autenticación contra SQLite (clientes reales)
        if (context == null) {
            String error = "Contexto no establecido para autenticación local";
            android.util.Log.e("AuthRepository", error);
            errorLiveData.postValue(error);
            isLoadingLiveData.postValue(false);
            callback.onError(error);
            return;
        }

        new Thread(() -> {
            try {
                ClienteRepository clienteRepository = new ClienteRepository(context);
                Cliente cliente = clienteRepository.getClienteByEmailSync(cleanEmail);
                
                if (cliente != null && cliente.getPassword() != null && cliente.getPassword().equals(cleanPassword)) {
                    // Construir usuario local como cliente
                    LocalUser sqliteUser = new LocalUser(cleanPassword, cliente.getNombre(), "cliente", cliente.getId());
                    currentUser = sqliteUser;
                    currentUserLiveData.postValue(sqliteUser);
                    
                    // Crear sesión persistente
                    if (sessionManager != null) {
                        sessionManager.createSession(cliente.getId(), cleanEmail, cliente.getNombre());
                    }
                    
                    android.util.Log.d("AuthRepository", "Login SQLite exitoso para: " + cleanEmail);
                    isLoadingLiveData.postValue(false);
                    callback.onSuccess(cliente.getId());
                } else {
                    String error = "Email o contraseña incorrectos";
                    android.util.Log.d("AuthRepository", "Login SQLite fallido para: " + cleanEmail);
                    errorLiveData.postValue(error);
                    isLoadingLiveData.postValue(false);
                    callback.onError(error);
                }
            } catch (Exception e) {
                String error = "Error en autenticación local: " + e.getMessage();
                android.util.Log.e("AuthRepository", error);
                errorLiveData.postValue(error);
                isLoadingLiveData.postValue(false);
                callback.onError(error);
            }
        }).start();
    }
    
    /**
     * Registra un nuevo usuario - DESHABILITADO EN VERSIÓN LOCAL
     */
    public void register(String email, String password, AuthCallback<String> callback) {
        // REGISTRO DESHABILITADO EN VERSIÓN LOCAL
        callback.onError("Registro no disponible en modo local. Use las credenciales predefinidas.");
        
        // FIREBASE COMENTADO
        // isLoadingLiveData.postValue(true);
        // 
        // firebaseAuth.createUserWithEmailAndPassword(email, password)
        //     .addOnCompleteListener(task -> {
        //         isLoadingLiveData.postValue(false);
        //         
        //         if (task.isSuccessful()) {
        //             FirebaseUser user = firebaseAuth.getCurrentUser();
        //             if (user != null) {
        //                 callback.onSuccess(user.getUid());
        //             } else {
        //                 callback.onError("Error al crear usuario");
        //             }
        //         } else {
        //             String error = task.getException() != null ? 
        //                 task.getException().getMessage() : "Error al registrar usuario";
        //             errorLiveData.postValue(error);
        //             callback.onError(error);
        //         }
        //     });
    }
    
    /**
     * Cierra sesión del usuario actual - VERSIÓN LOCAL
     */
    public void logout() {
        android.util.Log.d("AuthRepository", "Logout");
        currentUser = null;
        currentUserLiveData.postValue(null);
        
        if (sessionManager != null) {
            sessionManager.logout();
        }
    }
    
    /**
     * Obtiene el usuario actual - VERSIÓN LOCAL
     */
    public LocalUser getCurrentUser() {
        // Si no hay usuario en memoria pero existe una sesión, reconstruirlo
        if (currentUser == null && sessionManager != null && sessionManager.isLoggedIn()) {
            String userId = sessionManager.getUserId();
            String email = sessionManager.getUserEmail();
            String name = sessionManager.getUserName();
            String role = "cliente"; // Por defecto, los usuarios de SQLite son clientes
            if (email != null && email.equals("admin@test.com")) {
                role = "admin";
            }

            currentUser = new LocalUser("", name != null ? name : "Usuario", role, 
                    userId != null ? userId : "user_session");
            currentUserLiveData.postValue(currentUser);
            android.util.Log.d("AuthRepository", "Usuario reconstruido desde sesión: " + email + " (" + role + ")");
        }
        return currentUser;
    }
    
    /**
     * Obtiene el usuario actual con callback - VERSIÓN LOCAL
     */
    public void getCurrentUser(AuthCallback<String> callback) {
        if (currentUser != null) {
            callback.onSuccess(currentUser.uid);
        } else {
            callback.onError("No hay usuario autenticado");
        }
    }

    public String getCurrentUserUid() {
        LocalUser user = getCurrentUser();
        return user != null ? user.uid : null;
    }
    /**
     * Verifica si hay un usuario autenticado - VERSIÓN LOCAL
     */
    public boolean isUserLoggedIn() {
        // Considerar también la sesión persistente como autenticado
        if (currentUser != null) return true;
        return sessionManager != null && sessionManager.isLoggedIn();
    }
    
    /**
     * Obtiene el tipo de usuario actual (admin o cliente)
     */
    public String getCurrentUserType() {
        if (currentUser != null) {
            return currentUser.role;
        }
        return null;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isCurrentUserAdmin() {
        LocalUser user = getCurrentUser();
        return user != null && user.isAdmin();
    }
    
    /**
     * Verifica si el usuario actual es cliente
     */
    public boolean isCurrentUserCliente() {
        LocalUser user = getCurrentUser();
        return user != null && user.isCliente();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<LocalUser> getCurrentUserLiveData() {
        return currentUserLiveData;
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }
    
    /**
     * Obtiene las credenciales disponibles para mostrar al usuario
     */
    public String getAvailableCredentials() {
        return "Credenciales disponibles:\n" +
               "Cliente: cliente@test.com / cliente123\n" +
               "Administrador: admin@test.com / admin123";
    }
    
    /**
     * Método de prueba para verificar credenciales directamente
     */
    public boolean testCredentials(String email, String password) {
        android.util.Log.d("AuthRepository", "=== TEST CREDENTIALS ===");
        android.util.Log.d("AuthRepository", "Testing email: '" + email + "'");
        android.util.Log.d("AuthRepository", "Testing password: '" + password + "'");
        android.util.Log.d("AuthRepository", "Available users: " + USERS.keySet());
        
        String cleanEmail = email.trim();
        String cleanPassword = password.trim();
        
        LocalUser user = USERS.get(cleanEmail);
        boolean result = user != null && user.password.equals(cleanPassword);
        
        android.util.Log.d("AuthRepository", "Test result: " + result);
        android.util.Log.d("AuthRepository", "=== END TEST ===");
        
        return result;
    }
    
    /**
     * Clase para representar un usuario local
     */
    public static class LocalUser {
        public final String password;
        public final String name;
        public final String role; // "cliente" o "admin"
        public final String uid;
        
        public LocalUser(String password, String name, String role, String uid) {
            this.password = password;
            this.name = name;
            this.role = role;
            this.uid = uid;
        }
        
        public boolean isAdmin() {
            return "admin".equals(role);
        }
        
        public boolean isCliente() {
            return "cliente".equals(role);
        }
    }
}