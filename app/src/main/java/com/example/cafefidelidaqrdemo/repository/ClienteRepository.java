package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Cliente;

import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de Cliente con SQLite
 */
public class ClienteRepository {
    
    private static ClienteRepository instance;
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final Context context;
    
    // LiveData para observar cambios
    private final MutableLiveData<Cliente> currentClienteLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    
    public ClienteRepository(Context context) {
        this.context = context;
        this.database = CafeFidelidadDB.getInstance(context);
        this.apiService = RetrofitClient.getInstance(context).getApiService();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized ClienteRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ClienteRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    public static ClienteRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ClienteRepository must be initialized with context first");
        }
        return instance;
    }
    
    // ========== OPERACIONES CRUD ==========
    
    /**
     * Obtiene el cliente actual por ID
     */
    public LiveData<Cliente> getClienteById(int clienteId) {
        executor.execute(() -> {
            try {
                Cliente cliente = database.obtenerClientePorId(clienteId);
                currentClienteLiveData.postValue(cliente);
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar cliente: " + e.getMessage());
                currentClienteLiveData.postValue(null);
            }
        });
        
        return currentClienteLiveData;
    }
    
    /**
     * Obtiene un cliente por email
     */
    public LiveData<Cliente> getClienteByEmail(String email) {
        MutableLiveData<Cliente> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Cliente cliente = database.obtenerClientePorEmail(email);
                result.postValue(cliente);
            } catch (Exception e) {
                errorLiveData.postValue("Error al buscar cliente por email: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Obtiene todos los clientes
     */
    public LiveData<List<Cliente>> getAllClientes() {
        MutableLiveData<List<Cliente>> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                List<Cliente> clientes = database.obtenerTodosLosClientes();
                result.postValue(clientes);
            } catch (Exception e) {
                errorLiveData.postValue("Error al obtener clientes: " + e.getMessage());
                result.postValue(null);
            }
        });
        return result;
    }
    
    /**
     * Crea un nuevo cliente
     */
    public void createCliente(Cliente cliente, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                // Validar datos del cliente
                if (!validarCliente(cliente)) {
                    callback.onError("Datos del cliente inválidos");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                
                // Verificar si ya existe un cliente con el mismo email
                Cliente existente = database.obtenerClientePorEmail(cliente.getEmail());
                if (existente != null) {
                    callback.onError("Ya existe un cliente con ese email");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                
                // Insertar cliente
                long id = database.insertarCliente(cliente);
                if (id > 0) {
                    cliente.setId(String.valueOf(id));
                    callback.onSuccess(cliente);
                    successLiveData.postValue("Cliente creado exitosamente");
                } else {
                    callback.onError("Error al crear cliente");
                }
            } catch (Exception e) {
                callback.onError("Error al crear cliente: " + e.getMessage());
                errorLiveData.postValue("Error al crear cliente: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza los datos del cliente
     */
    public void updateCliente(Cliente cliente, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                if (!validarCliente(cliente)) {
                    callback.onError("Datos del cliente inválidos");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                
                int rowsAffected = database.actualizarCliente(cliente);
                if (rowsAffected > 0) {
                    callback.onSuccess(cliente);
                    successLiveData.postValue("Cliente actualizado exitosamente");
                    // Actualizar el cliente actual si es el mismo
                    currentClienteLiveData.postValue(cliente);
                } else {
                    callback.onError("No se pudo actualizar el cliente");
                }
            } catch (Exception e) {
                callback.onError("Error al actualizar cliente: " + e.getMessage());
                errorLiveData.postValue("Error al actualizar cliente: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Actualiza los puntos de un cliente
     */
    public void actualizarPuntosCliente(int clienteId, int nuevosPuntos, ClienteCallback callback) {
        executor.execute(() -> {
            try {
                Cliente cliente = database.obtenerClientePorId(clienteId);
                if (cliente != null) {
                    cliente.setPuntosAcumulados(nuevosPuntos);
                    int rowsAffected = database.actualizarCliente(cliente);
                    if (rowsAffected > 0) {
                        callback.onSuccess(cliente);
                        successLiveData.postValue("Puntos actualizados exitosamente");
                        // Actualizar el cliente actual si es el mismo
                        currentClienteLiveData.postValue(cliente);
                    } else {
                        callback.onError("No se pudieron actualizar los puntos");
                    }
                } else {
                    callback.onError("Cliente no encontrado");
                }
            } catch (Exception e) {
                callback.onError("Error al actualizar puntos: " + e.getMessage());
                errorLiveData.postValue("Error al actualizar puntos: " + e.getMessage());
            }
        });
    }
    
    /**
     * Elimina un cliente
     */
    public void eliminarCliente(int clienteId, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                int rowsAffected = database.eliminarCliente(clienteId);
                if (rowsAffected > 0) {
                    callback.onSuccess(null);
                    successLiveData.postValue("Cliente eliminado exitosamente");
                } else {
                    callback.onError("No se pudo eliminar el cliente");
                }
            } catch (Exception e) {
                callback.onError("Error al eliminar cliente: " + e.getMessage());
                errorLiveData.postValue("Error al eliminar cliente: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    // ========== AUTENTICACIÓN ==========
    
    /**
     * Autentica un cliente con email y password
     */
    public void autenticarCliente(String email, String password, ClienteCallback callback) {
        isLoadingLiveData.postValue(true);
        executor.execute(() -> {
            try {
                Cliente cliente = database.obtenerClientePorEmail(email);
                if (cliente != null && cliente.getPassword().equals(password)) {
                    callback.onSuccess(cliente);
                    currentClienteLiveData.postValue(cliente);
                    successLiveData.postValue("Autenticación exitosa");
                } else {
                    callback.onError("Email o contraseña incorrectos");
                }
            } catch (Exception e) {
                callback.onError("Error en autenticación: " + e.getMessage());
                errorLiveData.postValue("Error en autenticación: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Verifica si existe un cliente con el email dado
     */
    public void verificarEmailExiste(String email, EmailExistsCallback callback) {
        executor.execute(() -> {
            try {
                Cliente cliente = database.obtenerClientePorEmail(email);
                callback.onResult(cliente != null);
            } catch (Exception e) {
                callback.onResult(false);
                errorLiveData.postValue("Error al verificar email: " + e.getMessage());
            }
        });
    }
    
    // ========== MÉTODOS SÍNCRONOS ==========
    
    /**
     * Obtiene un cliente por ID de forma síncrona
     */
    public Cliente getClienteByIdSync(int clienteId) {
        try {
            return database.obtenerClientePorId(clienteId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtiene un cliente por email de forma síncrona
     */
    public Cliente getClienteByEmailSync(String email) {
        try {
            return database.obtenerClientePorEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ========== VALIDACIÓN ==========
    
    private boolean validarCliente(Cliente cliente) {
        return cliente != null &&
               cliente.getNombre() != null && !cliente.getNombre().trim().isEmpty() &&
               cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty() &&
               cliente.getPassword() != null && !cliente.getPassword().trim().isEmpty() &&
               isValidEmail(cliente.getEmail());
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    // ========== ESTADÍSTICAS ==========
    
    /**
     * Obtiene el conteo total de clientes
     */
    public LiveData<Integer> getCountClientes() {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoClientes();
                result.postValue(count);
            } catch (Exception e) {
                result.postValue(0);
            }
        });
        return result;
    }
    
    /**
     * Obtiene el conteo de clientes de forma síncrona
     */
    public int getCountClientesSync() {
        try {
            return database.obtenerConteoClientes();
        } catch (Exception e) {
            return 0;
        }
    }
    
    // ========== GETTERS PARA LIVEDATA ==========
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<String> getSuccess() {
        return successLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    public LiveData<Boolean> getIsOffline() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.setValue(!NetworkUtils.isNetworkAvailable(context));
        return result;
    }
    
    // ========== UTILIDADES ==========
    
    /**
     * Limpia los mensajes de error
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    public void clearSuccess() {
        successLiveData.setValue(null);
    }
    
    /**
     * Cierra la sesión del cliente actual
     */
    public void logout() {
        currentClienteLiveData.setValue(null);
        clearError();
        clearSuccess();
    }
    
    // ========== INTERFACES DE CALLBACK ==========
    
    public interface ClienteCallback {
        void onSuccess(Cliente cliente);
        void onError(String error);
    }
    
    public interface EmailExistsCallback {
        void onResult(boolean exists);
    }
    
    // ========== LIMPIEZA ==========
    
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}