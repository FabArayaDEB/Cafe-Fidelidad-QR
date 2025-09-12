package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.repository.CompraRepository;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.database.entities.CompraEntity;
import com.example.cafefidelidaqrdemo.utils.QRGenerator;
import com.example.cafefidelidaqrdemo.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRScannerViewModel extends AndroidViewModel {
    private ClienteRepository clienteRepository;
    private CompraRepository compraRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;
    
    private MutableLiveData<ScanResult> scanResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> purchaseResult = new MutableLiveData<>();
    
    public QRScannerViewModel(@NonNull Application application) {
        super(application);
        clienteRepository = new ClienteRepository(application);
        compraRepository = new CompraRepository(application);
        sessionManager = new SessionManager(application);
        executor = Executors.newFixedThreadPool(2);
    }
    
    public LiveData<ScanResult> getScanResult() {
        return scanResult;
    }
    
    public LiveData<Boolean> getPurchaseResult() {
        return purchaseResult;
    }
    
    public void processQRCode(String qrContent) {
        executor.execute(() -> {
            try {
                // Validar y parsear el código QR
                if (!QRGenerator.isValidClientQR(qrContent)) {
                    scanResult.postValue(new ScanResult(false, "Código QR inválido", null));
                    return;
                }
                
                QRGenerator.ClienteQRData qrData = QRGenerator.parseClientQR(qrContent);
                if (qrData == null) {
                    scanResult.postValue(new ScanResult(false, "No se pudo leer la información del cliente", null));
                    return;
                }
                
                // Verificar que el cliente existe en la base de datos
                ClienteEntity cliente = clienteRepository.getClienteByEmailSync(qrData.getEmail());
                if (cliente == null) {
                    scanResult.postValue(new ScanResult(false, "Cliente no encontrado en el sistema", null));
                    return;
                }
                
                // Verificar que los datos del QR coinciden con los de la base de datos
                if (!verifyClientData(cliente, qrData)) {
                    scanResult.postValue(new ScanResult(false, "Los datos del QR no coinciden con el cliente registrado", null));
                    return;
                }
                
                scanResult.postValue(new ScanResult(true, "Cliente verificado", qrData));
                
            } catch (Exception e) {
                scanResult.postValue(new ScanResult(false, "Error al procesar el código QR: " + e.getMessage(), null));
            }
        });
    }
    
    private boolean verifyClientData(ClienteEntity cliente, QRGenerator.ClienteQRData qrData) {
        // Verificar que los datos básicos coinciden
        return cliente.getEmail().equals(qrData.getEmail()) &&
               cliente.getNombre().equals(qrData.getNombre());
    }
    
    public void registerPurchase(QRGenerator.ClienteQRData clienteData, double amount, String description) {
        executor.execute(() -> {
            try {
                // Obtener información del trabajador actual
                String trabajadorEmail = sessionManager.getUserEmail();
                if (trabajadorEmail == null || trabajadorEmail.isEmpty()) {
                    purchaseResult.postValue(false);
                    return;
                }
                
                // Obtener cliente de la base de datos
                ClienteEntity cliente = clienteRepository.getClienteByEmailSync(clienteData.getEmail());
                if (cliente == null) {
                    purchaseResult.postValue(false);
                    return;
                }
                
                // Crear nueva compra
                CompraEntity compra = new CompraEntity(
                    Long.parseLong(cliente.getId_cliente()),
                    amount,
                    new java.util.Date(),
                    description.isEmpty() ? "Compra registrada" : description,
                    cliente.getId_cliente()
                );
                
                // Calcular puntos ganados (1 punto por cada peso gastado)
                int puntosGanados = (int) Math.floor(amount);
                
                // Insertar compra
                long compraId = compraRepository.insertCompraSync(compra);
                
                if (compraId > 0) {
                    // Marcar como pendiente de sincronización
                    cliente.setNeedsSync(true);
                    
                    // Guardar cambios
                    clienteRepository.updateCliente(cliente);
                    
                    purchaseResult.postValue(true);
                } else {
                    purchaseResult.postValue(false);
                }
                
            } catch (Exception e) {
                purchaseResult.postValue(false);
            }
        });
    }
    
    private String calculateLevel(int puntos) {
        if (puntos >= 1000) {
            return "Oro";
        } else if (puntos >= 500) {
            return "Plata";
        } else {
            return "Bronce";
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    // Clase para encapsular el resultado del escaneo
    public static class ScanResult {
        private boolean success;
        private String errorMessage;
        private QRGenerator.ClienteQRData clienteData;
        
        public ScanResult(boolean success, String errorMessage, QRGenerator.ClienteQRData clienteData) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.clienteData = clienteData;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public QRGenerator.ClienteQRData getClienteData() {
            return clienteData;
        }
    }
}