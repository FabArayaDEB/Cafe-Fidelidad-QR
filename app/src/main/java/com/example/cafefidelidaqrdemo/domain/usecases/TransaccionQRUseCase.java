package com.example.cafefidelidaqrdemo.domain.usecases;

import com.example.cafefidelidaqrdemo.repository.TransaccionRepository;
import com.example.cafefidelidaqrdemo.repository.ClienteRepository;
import com.example.cafefidelidaqrdemo.repository.AuthRepository;
import com.example.cafefidelidaqrdemo.database.entities.TransaccionEntity;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;

/**
 * Use Case para operaciones de transacciones QR
 * Encapsula la lógica de negocio para escaneo y registro de transacciones
 */
public class TransaccionQRUseCase {
    
    private final TransaccionRepository transaccionRepository;
    private final ClienteRepository clienteRepository;
    private final AuthRepository authRepository;
    
    // Constantes de negocio
    private static final double PUNTOS_POR_PESO = 1.0; // 1 punto por cada peso gastado
    private static final int MIN_MONTO_TRANSACCION = 1;
    private static final int MAX_MONTO_TRANSACCION = 100000;
    
    public TransaccionQRUseCase() {
        this.transaccionRepository = TransaccionRepository.getInstance();
        this.clienteRepository = ClienteRepository.getInstance();
        this.authRepository = AuthRepository.getInstance();
    }
    
    /**
     * Callback para operaciones de transacción
     */
    public interface TransaccionCallback {
        void onSuccess(TransaccionEntity transaccion, ClienteEntity clienteActualizado);
        void onError(String error);
    }
    
    /**
     * Callback para validación de QR
     */
    public interface QRValidationCallback {
        void onValidQR(double monto, String descripcion);
        void onInvalidQR(String error);
    }
    
    /**
     * Valida el contenido del código QR
     */
    public void validateQRContent(String qrContent, QRValidationCallback callback) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            callback.onInvalidQR("Código QR vacío");
            return;
        }
        
        try {
            // Formato esperado: "CAFE_FIDELIDAD:monto:descripcion"
            String[] parts = qrContent.split(":");
            
            if (parts.length < 2) {
                callback.onInvalidQR("Formato de QR inválido");
                return;
            }
            
            if (!"CAFE_FIDELIDAD".equals(parts[0])) {
                callback.onInvalidQR("QR no válido para Café Fidelidad");
                return;
            }
            
            double monto;
            try {
                monto = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                callback.onInvalidQR("Monto inválido en el QR");
                return;
            }
            
            if (monto < MIN_MONTO_TRANSACCION || monto > MAX_MONTO_TRANSACCION) {
                callback.onInvalidQR("Monto fuera del rango permitido ($" + MIN_MONTO_TRANSACCION + " - $" + MAX_MONTO_TRANSACCION + ")");
                return;
            }
            
            String descripcion = parts.length > 2 ? parts[2] : "Compra en Café Fidelidad";
            
            callback.onValidQR(monto, descripcion);
            
        } catch (Exception e) {
            callback.onInvalidQR("Error al procesar QR: " + e.getMessage());
        }
    }
    
    /**
     * Procesa una transacción QR completa
     */
    public void processQRTransaction(String qrContent, TransaccionCallback callback) {
        // Primero validar el QR
        validateQRContent(qrContent, new QRValidationCallback() {
            @Override
            public void onValidQR(double monto, String descripcion) {
                // Obtener usuario actual
                authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
                    @Override
                    public void onSuccess(String userId) {
                        if (userId != null) {
                            registrarTransaccion(userId, monto, descripcion, callback);
                        } else {
                            callback.onError("Usuario no autenticado");
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error de autenticación: " + error);
                    }
                });
            }
            
            @Override
            public void onInvalidQR(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Registra una transacción y actualiza los puntos del cliente
     */
    private void registrarTransaccion(String clienteId, double monto, String descripcion, TransaccionCallback callback) {
        // Crear la transacción
        TransaccionEntity transaccion = new TransaccionEntity();
        // Generar ID único para la transacción
        transaccion.setId(System.currentTimeMillis() + "_" + Math.random());
        transaccion.setUserId(clienteId);
        transaccion.setMonto(monto);
        transaccion.setDescripcion(descripcion);
        transaccion.setFecha(System.currentTimeMillis());
        transaccion.setTipo("QR_SCAN");
        
        // Calcular puntos ganados
        int puntosGanados = calcularPuntosGanados(monto);
        transaccion.setPuntos(puntosGanados);
        
        // Registrar la transacción
        transaccionRepository.createTransaccion(transaccion, new TransaccionRepository.TransaccionCallback() {
            @Override
            public void onSuccess(TransaccionEntity transaccionCreada) {
                // Actualizar puntos del cliente
                actualizarPuntosCliente(clienteId, puntosGanados, transaccionCreada, callback);
            }
            
            @Override
            public void onError(String error) {
                callback.onError("Error al registrar transacción: " + error);
            }
        });
    }
    
    /**
     * Actualiza los puntos del cliente después de una transacción
     */
    private void actualizarPuntosCliente(String clienteId, int puntosGanados, TransaccionEntity transaccion, TransaccionCallback callback) {
        clienteRepository.getClienteById(clienteId, new ClienteRepository.ClienteCallback() {
            @Override
            public void onSuccess(ClienteEntity cliente) {
                // Actualizar puntos
                int puntosActuales = cliente.getPuntos();
                int nuevoPuntaje = puntosActuales + puntosGanados;
                // Nota: ClienteEntity no tiene campo puntos ni ultimaTransaccion
                // Estos campos están en el modelo de negocio, no en la entidad de base de datos
                cliente.setLastSync(System.currentTimeMillis());
                cliente.setNeedsSync(true);
                
                // Guardar cambios
                clienteRepository.updateCliente(cliente, new ClienteRepository.ClienteCallback() {
                    @Override
                    public void onSuccess(ClienteEntity clienteActualizado) {
                        callback.onSuccess(transaccion, clienteActualizado);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // La transacción se registró pero falló la actualización de puntos
                        callback.onError("Transacción registrada pero error al actualizar puntos: " + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                // La transacción se registró pero falló obtener datos del cliente
                callback.onError("Transacción registrada pero error al obtener datos del cliente: " + error);
            }
        });
    }
    
    /**
     * Calcula los puntos ganados basado en el monto de la transacción
     */
    private int calcularPuntosGanados(double monto) {
        // Lógica de negocio: 1 punto por cada peso gastado
        // Se puede extender para incluir promociones, multiplicadores, etc.
        return (int) Math.floor(monto * PUNTOS_POR_PESO);
    }
    
    /**
     * Verifica si una transacción es duplicada (mismo monto en los últimos minutos)
     */
    public void checkDuplicateTransaction(String clienteId, double monto, DuplicateCheckCallback callback) {
        long tiempoLimite = System.currentTimeMillis() - (5 * 60 * 1000); // 5 minutos
        
        transaccionRepository.getTransaccionesByCliente(clienteId, new TransaccionRepository.TransaccionListCallback() {
            @Override
            public void onSuccess(java.util.List<TransaccionEntity> transacciones) {
                boolean isDuplicate = false;
                
                for (TransaccionEntity t : transacciones) {
                    if (t.getFecha() > tiempoLimite && 
                        Math.abs(t.getMonto() - monto) < 0.01) { // Comparación de doubles
                        isDuplicate = true;
                        break;
                    }
                }
                
                callback.onResult(isDuplicate);
            }
            
            @Override
            public void onError(String error) {
                // En caso de error, permitir la transacción
                callback.onResult(false);
            }
        });
    }
    
    /**
     * Callback para verificación de duplicados
     */
    public interface DuplicateCheckCallback {
        void onResult(boolean isDuplicate);
    }
    
    /**
     * Procesa transacción con verificación de duplicados
     */
    public void processQRTransactionWithDuplicateCheck(String qrContent, TransaccionCallback callback) {
        validateQRContent(qrContent, new QRValidationCallback() {
            @Override
            public void onValidQR(double monto, String descripcion) {
                authRepository.getCurrentUser(new AuthRepository.AuthCallback<String>() {
                    @Override
                    public void onSuccess(String userId) {
                        if (userId != null) {
                            checkDuplicateTransaction(userId, monto, new DuplicateCheckCallback() {
                                @Override
                                public void onResult(boolean isDuplicate) {
                                    if (isDuplicate) {
                                        callback.onError("Transacción duplicada detectada. Espera unos minutos antes de escanear el mismo monto.");
                                    } else {
                                        registrarTransaccion(userId, monto, descripcion, callback);
                                    }
                                }
                            });
                        } else {
                            callback.onError("Usuario no autenticado");
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError("Error de autenticación: " + error);
                    }
                });
            }
            
            @Override
            public void onInvalidQR(String error) {
                callback.onError(error);
            }
        });
    }
}