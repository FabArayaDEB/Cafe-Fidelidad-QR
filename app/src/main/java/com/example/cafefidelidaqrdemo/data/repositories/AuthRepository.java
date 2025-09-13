package com.example.cafefidelidaqrdemo.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de autenticación
 */
public class AuthRepository {
    
    private static AuthRepository instance;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final ExecutorService executor;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> currentUserLiveData = new MutableLiveData<>();
    
    private AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        this.executor = Executors.newFixedThreadPool(2);
        
        // Observar cambios en el estado de autenticación
        firebaseAuth.addAuthStateListener(auth -> {
            currentUserLiveData.postValue(auth.getCurrentUser());
        });
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
     * Realiza login con email y contraseña
     */
    public void login(String email, String password, AuthCallback<String> callback) {
        isLoadingLiveData.postValue(true);
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                isLoadingLiveData.postValue(false);
                
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        callback.onSuccess(user.getUid());
                    } else {
                        callback.onError("Error al obtener datos del usuario");
                    }
                } else {
                    String error = task.getException() != null ? 
                        task.getException().getMessage() : "Error de autenticación";
                    errorLiveData.postValue(error);
                    callback.onError(error);
                }
            });
    }
    
    /**
     * Registra un nuevo usuario
     */
    public void register(String email, String password, AuthCallback<String> callback) {
        isLoadingLiveData.postValue(true);
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                isLoadingLiveData.postValue(false);
                
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        callback.onSuccess(user.getUid());
                    } else {
                        callback.onError("Error al crear usuario");
                    }
                } else {
                    String error = task.getException() != null ? 
                        task.getException().getMessage() : "Error al registrar usuario";
                    errorLiveData.postValue(error);
                    callback.onError(error);
                }
            });
    }
    
    /**
     * Cierra sesión del usuario actual
     */
    public void logout() {
        firebaseAuth.signOut();
    }
    
    /**
     * Obtiene el usuario actual
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Obtiene el usuario actual con callback
     */
    public void getCurrentUser(AuthCallback<String> callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            callback.onSuccess(user.getUid());
        } else {
            callback.onError("No hay usuario autenticado");
        }
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<FirebaseUser> getCurrentUserLiveData() {
        return currentUserLiveData;
    }
    
    /**
     * Limpia errores
     */
    public void clearError() {
        errorLiveData.postValue(null);
    }
}