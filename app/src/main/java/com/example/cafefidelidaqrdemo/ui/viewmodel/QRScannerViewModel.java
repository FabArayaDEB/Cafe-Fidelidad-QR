package com.example.cafefidelidaqrdemo.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.data.repository.VisitaRepository;
import com.example.cafefidelidaqrdemo.data.entity.VisitaEntity;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.network.ApiClient;
import java.util.List;

/**
 * ViewModel para el escáner de códigos QR
 * Maneja la lógica de procesamiento de QR y registro de visitas
 */
public class QRScannerViewModel extends AndroidViewModel {
    
    private final VisitaRepository visitaRepository;
    
    // Estados de procesamiento
    private final MutableLiveData<EstadoProcesamiento> _estadoProcesamiento = new MutableLiveData<>(EstadoProcesamiento.IDLE);
    private final MutableLiveData<String> _mensaje = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<String> _progresoActualizado = new MutableLiveData<>();
    
    // Datos de visitas
    private final LiveData<List<VisitaEntity>> visitasPendientes;
    private final LiveData<Integer> contadorPendientes;
    private final LiveData<Integer> visitasHoy;
    
    public QRScannerViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repository
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        this.visitaRepository = new VisitaRepository(
            database.visitaDao(),
            ApiClient.getApiService(),
            application
        );
        
        // Inicializar LiveData
        this.visitasPendientes = visitaRepository.obtenerPendientes();
        this.contadorPendientes = visitaRepository.contarPendientes();
        this.visitasHoy = visitaRepository.contarVisitasHoy();
        
        // Observar mensajes del repository
        visitaRepository.getMensajeEstado().observeForever(mensaje -> {
            if (mensaje != null) {
                _mensaje.postValue(mensaje);
            }
        });
    }
    
    // Getters para LiveData
    public LiveData<EstadoProcesamiento> getEstadoProcesamiento() {
        return _estadoProcesamiento;
    }
    
    public LiveData<String> getMensaje() {
        return _mensaje;
    }
    
    public LiveData<String> getError() {
        return _error;
    }
    
    public LiveData<String> getProgresoActualizado() {
        return _progresoActualizado;
    }
    
    public LiveData<List<VisitaEntity>> getVisitasPendientes() {
        return visitasPendientes;
    }
    
    public LiveData<Integer> getContadorPendientes() {
        return contadorPendientes;
    }
    
    public LiveData<Integer> getVisitasHoy() {
        return visitasHoy;
    }
    
    /**
     * Procesar código QR escaneado
     */
    public void procesarQR(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            _error.postValue("QR vacío o inválido");
            return;
        }
        
        // Limpiar mensajes anteriores
        _mensaje.postValue("");
        _error.postValue("");
        _progresoActualizado.postValue("");
        
        // Cambiar estado a procesando
        _estadoProcesamiento.postValue(EstadoProcesamiento.PROCESSING);
        
        // Procesar QR con el repository
        visitaRepository.procesarQR(qrContent, new VisitaRepository.QRProcessCallback() {
            @Override
            public void onSuccess(String mensaje, String progreso) {
                _estadoProcesamiento.postValue(EstadoProcesamiento.SUCCESS);
                _mensaje.postValue(mensaje);
                
                if (progreso != null && !progreso.isEmpty()) {
                    _progresoActualizado.postValue(progreso);
                }
                
                // Volver a estado idle después de un breve delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    _estadoProcesamiento.postValue(EstadoProcesamiento.IDLE);
                }, 2000);
            }
            
            @Override
            public void onError(String error) {
                _estadoProcesamiento.postValue(EstadoProcesamiento.ERROR);
                _error.postValue(error);
                
                // Volver a estado idle después de mostrar el error
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    _estadoProcesamiento.postValue(EstadoProcesamiento.IDLE);
                }, 3000);
            }
        });
    }
    
    /**
     * Sincronizar visitas pendientes
     */
    public void sincronizarPendientes() {
        _estadoProcesamiento.postValue(EstadoProcesamiento.PROCESSING);
        visitaRepository.sincronizarPendientes();
        
        // Volver a estado idle
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            _estadoProcesamiento.postValue(EstadoProcesamiento.IDLE);
        }, 2000);
    }
    
    /**
     * Reintentar visitas con error
     */
    public void reintentarErrores() {
        _estadoProcesamiento.postValue(EstadoProcesamiento.PROCESSING);
        visitaRepository.reintentarErrores();
        
        // Volver a estado idle
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            _estadoProcesamiento.postValue(EstadoProcesamiento.IDLE);
        }, 2000);
    }
    
    /**
     * Limpiar mensajes
     */
    public void limpiarMensajes() {
        _mensaje.postValue("");
        _error.postValue("");
        _progresoActualizado.postValue("");
    }
    
    /**
     * Validar formato básico de QR antes de procesar
     */
    public boolean validarFormatoBasico(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return false;
        }
        
        // Verificar que empiece con el protocolo esperado
        return qrContent.startsWith("qr://cafe/sucursal/");
    }
    
    /**
     * Obtener información básica del QR sin procesarlo completamente
     */
    public String obtenerInfoQR(String qrContent) {
        if (!validarFormatoBasico(qrContent)) {
            return "QR inválido";
        }
        
        try {
            // Extraer ID de sucursal del QR
            String[] parts = qrContent.split("/");
            if (parts.length >= 4) {
                String sucursalId = parts[3];
                return "Sucursal: " + sucursalId;
            }
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        
        return "QR de sucursal";
    }
    
    /**
     * Limpiar datos antiguos
     */
    public void limpiarDatosAntiguos() {
        visitaRepository.limpiarDatosAntiguos();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar recursos si es necesario
    }
    
    /**
     * Estados de procesamiento del QR
     */
    public enum EstadoProcesamiento {
        IDLE,       // Sin actividad
        PROCESSING, // Procesando QR
        SUCCESS,    // QR procesado exitosamente
        ERROR       // Error al procesar QR
    }
}