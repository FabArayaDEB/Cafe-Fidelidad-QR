package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.response.VisitaResponse;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.utils.QRValidator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Repository para gestionar visitas con QR
 * Maneja validación, registro offline/online y sincronización
 */
public class VisitaAdminRepository {
    
    private static final String TAG = "VisitaRepository";
    private static final String QR_PATTERN = "qr://cafe/sucursal/([^/]+)/([^/]+)/([^/]+)/([^/]+)";
    private static final long QR_VALIDITY_MINUTES = 5; // QR válido por 5 minutos
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private final VisitaDao visitaDao;
    private final ApiService apiService;
    private final Context context;
    private final ExecutorService executor;
    private final MutableLiveData<String> _mensajeEstado = new MutableLiveData<>();
    
    public VisitaAdminRepository(VisitaDao visitaDao, ApiService apiService, Context context) {
        this.visitaDao = visitaDao;
        this.apiService = apiService;
        this.context = context;
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // LiveData para observar mensajes de estado
    public LiveData<String> getMensajeEstado() {
        return _mensajeEstado;
    }
    
    // Obtener todas las visitas
    public LiveData<List<VisitaEntity>> obtenerTodas() {
        return visitaDao.obtenerTodas();
    }
    
    // Obtener visitas pendientes de sincronización
    public LiveData<List<VisitaEntity>> obtenerPendientes() {
        return visitaDao.obtenerPorEstado("PENDIENTE");
    }
    
    // Obtener contadores
    public LiveData<Integer> contarPendientes() {
        return visitaDao.contarPendientes();
    }
    
    public LiveData<Integer> contarVisitasHoy() {
        return visitaDao.contarVisitasHoyEnviadas();
    }
    
    /**
     * Procesar QR escaneado
     */
    public void procesarQR(String qrContent, QRProcessCallback callback) {
        executor.execute(() -> {
            try {
                // Validar formato del QR
                QRData qrData = validarFormatoQR(qrContent);
                if (qrData == null) {
                    callback.onError("QR inválido: formato no reconocido");
                    return;
                }
                
                // Validar vigencia del QR
                if (!validarVigenciaQR(qrData.timestamp)) {
                    callback.onError("QR expirado: el código ha caducado");
                    return;
                }
                
                // Validar firma del QR
                if (!validarFirmaQR(qrData)) {
                    callback.onError("QR inválido: firma no válida");
                    return;
                }
                
                // Verificar si ya existe este QR
                String hashQr = generarHashQR(qrContent);
                if (visitaDao.existeHashQr(hashQr) > 0) {
                    callback.onError("QR ya utilizado anteriormente");
                    return;
                }
                
                // Crear entidad de visita
                String visitaId = UUID.randomUUID().toString();
                VisitaEntity visita = new VisitaEntity(
                    visitaId,
                    "cliente_default", // TODO: obtener clienteId del contexto
                    qrData.sucursalId,
                    System.currentTimeMillis(),
                    "QR",
                    "PENDIENTE",
                    hashQr
                );
                
                // Guardar en base de datos local
                visitaDao.insert(visita);
                // visita.setId_visita ya está establecido en el constructor
                
                // Intentar enviar al servidor si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    enviarVisitaAlServidor(visita, callback);
                } else {
                    // Modo offline
                    _mensajeEstado.postValue("Registro en cola - se enviará cuando haya conexión");
                    callback.onSuccess("Visita registrada localmente", null);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error procesando QR", e);
                callback.onError("Error interno: " + e.getMessage());
            }
        });
    }
    
    /**
     * Validar formato del QR
     */
    private QRData validarFormatoQR(String qrContent) {
        Pattern pattern = Pattern.compile(QR_PATTERN);
        Matcher matcher = pattern.matcher(qrContent);
        
        if (!matcher.matches()) {
            return null;
        }
        
        try {
            String sucursalId = matcher.group(1);
            long timestamp = Long.parseLong(matcher.group(2));
            String nonce = matcher.group(3);
            String firma = matcher.group(4);
            
            return new QRData(sucursalId, timestamp, nonce, firma);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Validar vigencia del QR (5 minutos)
     */
    private boolean validarVigenciaQR(long timestamp) {
        long currentTime = System.currentTimeMillis() / 1000; // Convertir a segundos
        long timeDiff = currentTime - timestamp;
        return timeDiff <= (QR_VALIDITY_MINUTES * 60);
    }
    
    /**
     * Validar firma del QR (implementación básica)
     */
    private boolean validarFirmaQR(QRData qrData) {
        try {
            // Generar firma esperada
            String dataToSign = qrData.sucursalId + "|" + qrData.timestamp + "|" + qrData.nonce;
            String expectedSignature = generateSignature(dataToSign);
            
            // Comparar con la firma del QR
            return expectedSignature.equals(qrData.firma);
        } catch (Exception e) {
            Log.e(TAG, "Error validando firma QR", e);
            return false;
        }
    }
    
    /**
     * Generar hash del QR para evitar duplicados
     */
    private String generarHashQR(String qrContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(qrContent.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error generando hash QR", e);
            return qrContent; // Fallback
        }
    }
    
    /**
     * Generar firma (implementación básica - en producción usar clave secreta)
     */
    private String generateSignature(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash).substring(0, 16); // Tomar primeros 16 caracteres
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Enviar visita al servidor
     */
    private void enviarVisitaAlServidor(VisitaEntity visita, QRProcessCallback callback) {
        ApiService.VisitaRequest request = new ApiService.VisitaRequest(
            visita.getHash_qr(),
            "cliente_default", // TODO: obtener clienteId del contexto
            visita.getFecha_hora(),
            visita.getId_sucursal()
        );
        Call<VisitaResponse> call = apiService.registrarVisita(request);
        
        call.enqueue(new Callback<VisitaResponse>() {
            @Override
            public void onResponse(Call<VisitaResponse> call, Response<VisitaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VisitaResponse visitaResponse = response.body();
                    
                    // Actualizar estado en base de datos
                    executor.execute(() -> {
                         visita.marcarEnviado();
                         visitaDao.update(visita);
                     });
                    
                    _mensajeEstado.postValue("Visita registrada exitosamente");
                    callback.onSuccess("Visita registrada", visitaResponse.getProgreso());
                } else {
                    // Error del servidor
                    String error = "Error del servidor: " + response.code();
                    executor.execute(() -> {
                        visita.marcarError();
                  visitaDao.update(visita);
                    });
                    
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(Call<VisitaResponse> call, Throwable t) {
                // Error de red
                String error = "Error de conexión: " + t.getMessage();
                executor.execute(() -> {
                    visita.marcarError();
                      visitaDao.update(visita);
                });
                
                callback.onError(error);
            }
        });
    }
    
    /**
     * Sincronizar visitas pendientes
     */
    public void sincronizarPendientes() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _mensajeEstado.postValue("Sin conexión a internet");
            return;
        }
        
        executor.execute(() -> {
            List<VisitaEntity> pendientes = visitaDao.getPendientes();
            
            if (pendientes.isEmpty()) {
                _mensajeEstado.postValue("No hay visitas pendientes");
                return;
            }
            
            _mensajeEstado.postValue("Sincronizando " + pendientes.size() + " visitas...");
            
            for (VisitaEntity visita : pendientes) {
                enviarVisitaAlServidor(visita, new QRProcessCallback() {
                    @Override
                    public void onSuccess(String mensaje, String progreso) {
                        Log.d(TAG, "Visita sincronizada: " + visita.getId_visita());
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error sincronizando visita: " + visita.getId_visita() + " - " + error);
                    }
                });
            }
        });
    }
    
    /**
     * Reintentar visitas con error
     */
    public void reintentarErrores() {
        executor.execute(() -> {
            visitaDao.reintentarTodosLosErrores();
            sincronizarPendientes();
        });
    }
    
    /**
     * Limpiar datos antiguos
     */
    public void limpiarDatosAntiguos() {
        executor.execute(() -> {
            long unMesAtras = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            visitaDao.eliminarAntiguasEnviadas(unMesAtras);
            visitaDao.eliminarErroresAntiguos(unMesAtras);
        });
    }
    
    /**
     * Clase para datos del QR
     */
    private static class QRData {
        final String sucursalId;
        final long timestamp;
        final String nonce;
        final String firma;
        
        QRData(String sucursalId, long timestamp, String nonce, String firma) {
            this.sucursalId = sucursalId;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.firma = firma;
        }
    }
    
    /**
     * Callback para procesamiento de QR
     */
    public interface QRProcessCallback {
        void onSuccess(String mensaje, String progreso);
        void onError(String error);
    }
}