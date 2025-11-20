package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.example.cafefidelidaqrdemo.utils.SessionManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para manejar la generación y gestión del QR personal del cliente
 */
public class ClienteQRViewModel extends AndroidViewModel {
    
    private final ClienteRepository clienteRepository;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    
    private final MutableLiveData<Bitmap> _qrBitmap = new MutableLiveData<>();
    public final LiveData<Bitmap> qrBitmap = _qrBitmap;
    
    private final MutableLiveData<Cliente> _clienteData = new MutableLiveData<>();
    public final LiveData<Cliente> clienteData = _clienteData;
    
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    
    public ClienteQRViewModel(@NonNull Application application) {
        super(application);
        this.clienteRepository = new ClienteRepository(application);
        this.sessionManager = new SessionManager(application);
        this.executor = Executors.newFixedThreadPool(2);
        
        loadClienteData();
    }
    
    /**
     * Carga los datos del cliente actual desde la sesión
     */
    private void loadClienteData() {
        _isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                String clienteId = sessionManager.getUserId();
                String clienteEmail = sessionManager.getUserEmail();
                String clienteNombre = sessionManager.getUserName();

                Cliente cliente = null;
                if (clienteId != null) {
                    // Intentar por ID numérico primero
                    try {
                        int idNum = Integer.parseInt(clienteId);
                        cliente = clienteRepository.getClienteByIdSync(idNum);
                        if (cliente == null && clienteEmail != null && !clienteEmail.isEmpty()) {
                            cliente = clienteRepository.getClienteByEmailSync(clienteEmail);
                        }
                    } catch (NumberFormatException nfe) {
                        // ID no numérico: intentar por email
                        if (clienteEmail != null && !clienteEmail.isEmpty()) {
                            cliente = clienteRepository.getClienteByEmailSync(clienteEmail);
                        }
                    }

                    if (cliente == null) {
                        cliente = construirClienteDesdeSesion(clienteId, clienteNombre, clienteEmail);
                    }
                } else {
                    // No hay ID en sesión: usar usuario de AuthRepository si existe
                    com.example.cafefidelidaqrdemo.repository.AuthRepository authRepo = com.example.cafefidelidaqrdemo.repository.AuthRepository.getInstance();
                    authRepo.setContext(getApplication());
                    com.example.cafefidelidaqrdemo.repository.AuthRepository.LocalUser localUser = authRepo.getCurrentUser();
                    if (localUser != null) {
                        cliente = construirClienteDesdeSesion(localUser.uid, localUser.name, (clienteEmail != null ? clienteEmail : "cliente@test.com"));
                    } else {
                        _error.postValue("Sesión no válida");
                    }
                }

                if (cliente != null) {
                    _clienteData.postValue(cliente);
                    generateQRCode(cliente);
                }
            } catch (Exception e) {
                _error.postValue("Error al cargar datos: " + e.getMessage());
            } finally {
                _isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Genera el código QR personal del cliente
     * @param cliente Datos del cliente
     */
    private void generateQRCode(Cliente cliente) {
        executor.execute(() -> {
            try {
                Bitmap qrBitmap = QRGenerator.generateClientQR(
                    String.valueOf(cliente.getId()),
                    cliente.getNombre(),
                    cliente.getEmail(),
                    generateMcId(cliente)
                );
                
                if (qrBitmap != null) {
                    _qrBitmap.postValue(qrBitmap);
                } else {
                    _error.postValue("Error al generar código QR");
                }
            } catch (Exception e) {
                _error.postValue("Error al generar QR: " + e.getMessage());
            }
        });
    }
    
    /**
     * Genera un ID único basado en los datos del cliente
     * @param cliente Datos del cliente
     * @return McID generado
     */
    private String generateMcId(Cliente cliente) {
        // Generar ID basado en nombre y email
        String nombre = cliente.getNombre().toUpperCase();
        String apellido = ""; // No hay apellido separado en ClienteEntity
        String email = cliente.getEmail().toLowerCase();
        
        // Tomar primeras letras del nombre y apellido
        String iniciales = "";
        if (nombre.length() > 0) iniciales += nombre.charAt(0);
        if (apellido.length() > 0) iniciales += apellido.charAt(0);
        
        // Tomar parte del email antes del @
        String emailPart = email.split("@")[0];
        if (emailPart.length() > 4) {
            emailPart = emailPart.substring(0, 4);
        }
        
        return iniciales + emailPart.toUpperCase();
    }

    private Cliente construirClienteDesdeSesion(String id, String nombre, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id != null ? id : "user_demo");
        cliente.setNombre(nombre != null && !nombre.isEmpty() ? nombre : "Cliente Demo");
        cliente.setEmail(email != null && !email.isEmpty() ? email : "cliente@test.com");
        cliente.setTelefono("");
        cliente.setEstado("activo");
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);
        return cliente;
    }
    
    /**
     * Refresca el código QR del cliente
     */
    public void refreshQR() {
        loadClienteData();
    }
    
    /**
     * Obtiene el saludo personalizado para el cliente
     * @return Saludo personalizado
     */
    public String getPersonalizedGreeting() {
        Cliente cliente = _clienteData.getValue();
        if (cliente != null) {
            return "Hola " + cliente.getNombre();
        }
        return "Hola";
    }
    
    /**
     * Obtiene el ID del cliente
     * @return McID del cliente
     */
    public String getClienteMcId() {
        Cliente cliente = _clienteData.getValue();
        if (cliente != null) {
            return "ID: " + generateMcId(cliente);
        }
        return "ID: ";
    }
    
    /**
     * Limpia los errores
     */
    public void clearError() {
        _error.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}