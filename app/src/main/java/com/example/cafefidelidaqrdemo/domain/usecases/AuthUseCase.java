package com.example.cafefidelidaqrdemo.domain.usecases;

import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import android.content.Context;

/**
 * Use Case para operaciones de autenticación
 * Encapsula la lógica de negocio relacionada con autenticación
 */
public class AuthUseCase {
    
    private final AuthRepository authRepository;
    private final ClienteRepository clienteRepository;
    
    private Context context;
    
    public AuthUseCase(Context context) {
        this.context = context;
        this.authRepository = AuthRepository.getInstance();
        this.clienteRepository = new ClienteRepository(context);
    }
    
    /**
     * Callback para operaciones de autenticación
     */
    public interface AuthCallback {
        void onSuccess(ClienteEntity cliente);
        void onError(String error);
    }
    
    /**
     * Realiza el login del usuario y obtiene sus datos
     */
    public void loginUser(String email, String password, AuthCallback callback) {
        // Validar parámetros
        if (email == null || email.trim().isEmpty()) {
            callback.onError("El email es requerido");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            callback.onError("La contraseña es requerida");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError("Formato de email inválido");
            return;
        }
        
        if (password.length() < 6) {
            callback.onError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        
        // Realizar login
        authRepository.login(email, password, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                // Una vez autenticado, obtener datos del cliente
                clienteRepository.getClienteById(userId, new ClienteRepository.ClienteCallback() {
                    @Override
                    public void onSuccess(ClienteEntity cliente) {
                        callback.onSuccess(cliente);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error al obtener datos del usuario: " + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(translateAuthError(error));
            }
        });
    }
    
    /**
     * Registra un nuevo usuario
     */
    public void registerUser(String email, String password, String nombre, String telefono, AuthCallback callback) {
        // Validar parámetros
        if (email == null || email.trim().isEmpty()) {
            callback.onError("El email es requerido");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            callback.onError("La contraseña es requerida");
            return;
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            callback.onError("El nombre es requerido");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError("Formato de email inválido");
            return;
        }
        
        if (password.length() < 6) {
            callback.onError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        
        // Crear cliente
        ClienteEntity nuevoCliente = new ClienteEntity();
        nuevoCliente.setEmail(email);
        nuevoCliente.setNombre(nombre);
        nuevoCliente.setTelefono(telefono);
        nuevoCliente.setCreado_en(System.currentTimeMillis());
        
        // Registrar usuario
        authRepository.register(email, password, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                // Establecer ID del usuario
                nuevoCliente.setId_cliente(userId);
                
                // Guardar datos del cliente
                clienteRepository.createCliente(nuevoCliente, new ClienteRepository.ClienteCallback() {
                    @Override
                    public void onSuccess(ClienteEntity cliente) {
                        callback.onSuccess(cliente);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error al crear perfil del usuario: " + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(translateAuthError(error));
            }
        });
    }
    
    /**
     * Cierra la sesión del usuario
     */
    public void logout(AuthRepository.AuthCallback<Void> callback) {
        authRepository.logout();
        if (callback != null) {
            callback.onSuccess(null);
        }
    }
    
    /**
     * Obtiene el usuario actual
     */
    public void getCurrentUser(AuthCallback callback) {
        authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String userId) {
                if (userId != null) {
                    clienteRepository.getClienteById(userId, new ClienteRepository.ClienteCallback() {
                        @Override
                        public void onSuccess(ClienteEntity cliente) {
                            callback.onSuccess(cliente);
                        }
                        
                        @Override
                        public void onError(String error) {
                            callback.onError("Error al obtener datos del usuario: " + error);
                        }
                    });
                } else {
                    callback.onError("Usuario no autenticado");
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Traduce errores de autenticación a mensajes amigables
     */
    private String translateAuthError(String error) {
        if (error == null) return "Error desconocido";
        
        if (error.contains("user-not-found")) {
            return "No existe una cuenta con este email";
        } else if (error.contains("wrong-password")) {
            return "Contraseña incorrecta";
        } else if (error.contains("email-already-in-use")) {
            return "Ya existe una cuenta con este email";
        } else if (error.contains("weak-password")) {
            return "La contraseña es muy débil";
        } else if (error.contains("invalid-email")) {
            return "Formato de email inválido";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet";
        } else {
            return error;
        }
    }
}