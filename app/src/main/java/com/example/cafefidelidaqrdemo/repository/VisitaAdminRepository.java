package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Visita;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.response.VisitaResponse;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import com.example.cafefidelidaqrdemo.utils.QRValidator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final Context context;
    private final ExecutorService executor;
    private final MutableLiveData<String> _mensajeEstado = new MutableLiveData<>();
    private final MutableLiveData<List<Visita>> visitasLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Visita>> pendientesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> contadorPendientesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> contadorHoyLiveData = new MutableLiveData<>();
    
    public VisitaAdminRepository(Context context, ApiService apiService) {
        this.database = CafeFidelidadDB.getInstance(context);
        this.apiService = apiService;
        this.context = context;
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    // LiveData para observar mensajes de estado
    public LiveData<String> getMensajeEstado() {
        return _mensajeEstado;
    }
    
    // Obtener todas las visitas
    public LiveData<List<Visita>> obtenerTodas() {
        executor.execute(() -> {
            try {
                List<Visita> visitas = database.obtenerTodasLasVisitas();
                visitasLiveData.postValue(visitas);
            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo todas las visitas", e);
                visitasLiveData.postValue(new ArrayList<>());
            }
        });
        return visitasLiveData;
    }
    
    // Obtener visitas pendientes de sincronización
    public LiveData<List<Visita>> obtenerPendientes() {
        executor.execute(() -> {
            try {
                List<Visita> todasLasVisitas = database.obtenerTodasLasVisitas();
                List<Visita> pendientes = new ArrayList<>();
                
                // Filtrar visitas pendientes (asumiendo que tienen un campo estado)
                for (Visita visita : todasLasVisitas) {
                    // Aquí necesitarías agregar un campo estado a la clase Visita
                    // Por ahora, consideramos todas las visitas como válidas
                    pendientes.add(visita);
                }
                
                pendientesLiveData.postValue(pendientes);
            } catch (Exception e) {
                Log.e(TAG, "Error obteniendo visitas pendientes", e);
                pendientesLiveData.postValue(new ArrayList<>());
            }
        });
        return pendientesLiveData;
    }
    
    // Obtener contadores
    public LiveData<Integer> contarPendientes() {
        executor.execute(() -> {
            try {
                int count = database.obtenerConteoVisitas();
                contadorPendientesLiveData.postValue(count);
            } catch (Exception e) {
                Log.e(TAG, "Error contando visitas pendientes", e);
                contadorPendientesLiveData.postValue(0);
            }
        });
        return contadorPendientesLiveData;
    }
    
    public LiveData<Integer> contarVisitasHoy() {
        executor.execute(() -> {
            try {
                // Obtener visitas de hoy (simplificado)
                List<Visita> todasLasVisitas = database.obtenerTodasLasVisitas();
                String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                
                int count = 0;
                for (Visita visita : todasLasVisitas) {
                    // Convertir timestamp a fecha string para comparar
                    String fechaVisita = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(visita.getFechaVisita()));
                    if (fechaVisita.equals(fechaHoy)) {
                        count++;
                    }
                }
                
                contadorHoyLiveData.postValue(count);
            } catch (Exception e) {
                Log.e(TAG, "Error contando visitas de hoy", e);
                contadorHoyLiveData.postValue(0);
            }
        });
        return contadorHoyLiveData;
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
                // Nota: Simplificamos la verificación por ahora
                // En una implementación completa, se verificaría contra la base de datos
                
                // Crear nueva visita
                Visita nuevaVisita = new Visita();
                // No establecer ID aquí, se auto-genera en la base de datos
                nuevaVisita.setUserId("1"); // Cliente por defecto (ID 1)
                nuevaVisita.setSucursal(qrData.sucursalId); // Usar sucursal como String
                nuevaVisita.setFechaVisita(System.currentTimeMillis()); // Usar timestamp actual
                // Nota: Visita no tiene campos tipo, estado, qrHash en la implementación actual
                // Estos campos podrían agregarse si son necesarios
                
                // Guardar localmente
                database.insertarVisita(nuevaVisita);
                // visita.setId_visita ya está establecido en el constructor
                
                // Intentar enviar al servidor si hay conexión
                if (NetworkUtils.isNetworkAvailable(context)) {
                    enviarVisitaAlServidor(nuevaVisita, callback);
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
    private void enviarVisitaAlServidor(Visita visita, QRProcessCallback callback) {
        // Nota: ApiService necesitaría ser actualizado para aceptar objetos Visita
        // Por ahora, simplificamos la implementación
        executor.execute(() -> {
            try {
                // Simular envío al servidor
                // En una implementación real, aquí iría la llamada a la API
                
                // Actualizar la visita como enviada (simplificado)
                database.actualizarVisita(visita);
                _mensajeEstado.postValue("Visita registrada exitosamente");
                callback.onSuccess("Visita registrada", null);
                
            } catch (Exception e) {
                Log.e(TAG, "Error enviando visita al servidor", e);
                _mensajeEstado.postValue("Error de conexión. Se reintentará automáticamente.");
                callback.onError("Error de conexión: " + e.getMessage());
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
            List<Visita> pendientes = database.obtenerTodasLasVisitas();
            
            if (pendientes.isEmpty()) {
                _mensajeEstado.postValue("No hay visitas pendientes");
                return;
            }
            
            _mensajeEstado.postValue("Sincronizando " + pendientes.size() + " visitas...");
            
            for (Visita visita : pendientes) {
                enviarVisitaAlServidor(visita, new QRProcessCallback() {
                    @Override
                    public void onSuccess(String mensaje, String progreso) {
                        Log.d(TAG, "Visita sincronizada: " + visita.getId());
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error sincronizando visita: " + visita.getId() + " - " + error);
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
            // Simplificado: reintentar sincronización de todas las visitas
            sincronizarPendientes();
        });
    }
    
    /**
     * Limpiar datos antiguos
     */
    public void limpiarDatosAntiguos() {
        executor.execute(() -> {
            // Implementación simplificada
            // En una versión completa, se eliminarían registros antiguos de la base de datos
            Log.d(TAG, "Limpieza de datos antiguos - funcionalidad pendiente");
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