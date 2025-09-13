package com.example.cafefidelidaqrdemo.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.CanjeDao;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.network.RetrofitClient;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para gestionar canjes de beneficios con OTP
 * Maneja la lógica de negocio, validación de tiempo y sincronización con API
 */
public class CanjeRepository {
    
    private static final String TAG = "CanjeRepository";
    private static final long OTP_DURACION_MS = 60000; // 60 segundos
    private static final int OTP_LONGITUD = 6;
    
    private CanjeDao canjeDao;
    private ApiService apiService;
    private ExecutorService executor;
    private Application application;
    
    // LiveData para observar estados
    private MutableLiveData<String> otpActual = new MutableLiveData<>();
    private MutableLiveData<Long> tiempoRestante = new MutableLiveData<>();
    private MutableLiveData<Boolean> otpValido = new MutableLiveData<>();
    private MutableLiveData<String> estadoCanje = new MutableLiveData<>();
    private MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    public CanjeRepository(Application application) {
        this.application = application;
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        canjeDao = database.canjeDao();
        apiService = RetrofitClient.getInstance().getApiService();
        executor = Executors.newFixedThreadPool(4);
    }
    
    // ========== MÉTODOS PÚBLICOS PARA OTP ==========
    
    /**
     * Solicita un nuevo OTP para canjear un beneficio
     */
    public void solicitarOtp(String idCliente, String idBeneficio, String idSucursal) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Verificar si ya existe un OTP activo para este cliente
                long tiempoActual = System.currentTimeMillis();
                CanjeEntity otpExistente = canjeDao.getOtpActivoCliente(idCliente, tiempoActual);
                
                if (otpExistente != null) {
                    // Ya existe un OTP activo
                    otpActual.postValue(otpExistente.getOtp_codigo());
                    actualizarTiempoRestante(otpExistente);
                    otpValido.postValue(true);
                    estadoCanje.postValue("OTP_ACTIVO");
                    return;
                }
                
                // Verificar conexión a internet
                if (!NetworkUtils.isNetworkAvailable(application)) {
                    mensajeError.postValue("Sin conexión a internet. No se puede generar OTP por seguridad.");
                    estadoCanje.postValue("ERROR_CONEXION");
                    return;
                }
                
                // Verificar si ya existe un canje para este beneficio
                int canjesExistentes = canjeDao.verificarCanjeExistente(idCliente, idBeneficio);
                if (canjesExistentes > 0) {
                    mensajeError.postValue("Este beneficio ya ha sido canjeado o está en proceso.");
                    estadoCanje.postValue("ERROR_DOBLE_CANJE");
                    return;
                }
                
                // Crear nuevo canje con OTP
                String idCanje = UUID.randomUUID().toString();
                String otpCodigo = generarOtp();
                long expiracion = tiempoActual + OTP_DURACION_MS;
                
                CanjeEntity nuevoCanje = new CanjeEntity(idCanje, idBeneficio, idCliente, idSucursal);
                nuevoCanje.setOtp_codigo(otpCodigo);
                nuevoCanje.setOtp_expiracion(expiracion);
                nuevoCanje.setEstado("PENDIENTE");
                
                // Guardar en base de datos local
                canjeDao.insert(nuevoCanje);
                
                // Sincronizar con API
                sincronizarCanjeConApi(nuevoCanje);
                
                // Notificar éxito
                otpActual.postValue(otpCodigo);
                actualizarTiempoRestante(nuevoCanje);
                otpValido.postValue(true);
                estadoCanje.postValue("OTP_GENERADO");
                
                // Iniciar temporizador para expiración automática
                iniciarTemporizadorExpiracion(nuevoCanje);
                
            } catch (Exception e) {
                mensajeError.postValue("Error al generar OTP: " + e.getMessage());
                estadoCanje.postValue("ERROR");
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Solicita un nuevo OTP cuando el anterior ha expirado
     */
    public void solicitarNuevoOtp(String idCliente, String idBeneficio, String idSucursal) {
        executor.execute(() -> {
            try {
                // Expirar OTPs vencidos
                long tiempoActual = System.currentTimeMillis();
                canjeDao.expirarOtpsVencidos(tiempoActual);
                
                // Solicitar nuevo OTP
                solicitarOtp(idCliente, idBeneficio, idSucursal);
                
            } catch (Exception e) {
                mensajeError.postValue("Error al solicitar nuevo OTP: " + e.getMessage());
                estadoCanje.postValue("ERROR");
            }
        });
    }
    
    /**
     * Confirma el canje usando el código OTP
     */
    public void confirmarCanje(String otpCodigo, String cajeroId) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                long tiempoActual = System.currentTimeMillis();
                
                // Buscar canje por OTP activo
                CanjeEntity canje = canjeDao.getByOtpActivo(otpCodigo);
                
                if (canje == null) {
                    mensajeError.postValue("Código OTP no válido o ya utilizado.");
                    estadoCanje.postValue("ERROR_OTP_INVALIDO");
                    return;
                }
                
                // Verificar si el OTP no ha expirado
                if (!canje.isOtpValido()) {
                    mensajeError.postValue("El código OTP ha expirado.");
                    estadoCanje.postValue("ERROR_OTP_EXPIRADO");
                    return;
                }
                
                // Marcar OTP como usado y canje como completado
                int filasActualizadas = canjeDao.marcarOtpUsado(otpCodigo, tiempoActual, cajeroId);
                
                if (filasActualizadas > 0) {
                    // Sincronizar con API
                    CanjeEntity canjeActualizado = canjeDao.getById(canje.getId_canje());
                    sincronizarCanjeConApi(canjeActualizado);
                    
                    otpValido.postValue(false);
                    estadoCanje.postValue("CANJE_COMPLETADO");
                } else {
                    mensajeError.postValue("Error al confirmar el canje.");
                    estadoCanje.postValue("ERROR");
                }
                
            } catch (Exception e) {
                mensajeError.postValue("Error al confirmar canje: " + e.getMessage());
                estadoCanje.postValue("ERROR");
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    // ========== MÉTODOS DE CONSULTA ==========
    
    public LiveData<List<CanjeEntity>> getCanjesByCliente(String idCliente) {
        return canjeDao.getCanjesByCliente(idCliente);
    }
    
    public LiveData<List<CanjeEntity>> getHistorialCanjesCliente(String idCliente) {
        return canjeDao.getHistorialCanjesCliente(idCliente);
    }
    
    public LiveData<List<CanjeEntity>> getCanjesByEstado(String estado) {
        return canjeDao.getCanjesByEstado(estado);
    }
    
    public CanjeEntity getCanjeById(String idCanje) {
        return canjeDao.getById(idCanje);
    }
    
    // ========== GETTERS PARA LIVEDATA ==========
    
    public LiveData<String> getOtpActual() {
        return otpActual;
    }
    
    public LiveData<Long> getTiempoRestante() {
        return tiempoRestante;
    }
    
    public LiveData<Boolean> getOtpValido() {
        return otpValido;
    }
    
    public LiveData<String> getEstadoCanje() {
        return estadoCanje;
    }
    
    public LiveData<String> getMensajeError() {
        return mensajeError;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return mensajeError;
    }
    
    /**
     * Limpia el estado actual del canje y OTP
     */
    public void limpiarEstado() {
        otpActual.postValue(null);
        tiempoRestante.postValue(0L);
        otpValido.postValue(false);
        estadoCanje.postValue("INICIAL");
        mensajeError.postValue(null);
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    /**
     * Genera un código OTP de 6 dígitos
     */
    private String generarOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Entre 100000 y 999999
        return String.valueOf(otp);
    }
    
    /**
     * Actualiza el tiempo restante del OTP
     */
    private void actualizarTiempoRestante(CanjeEntity canje) {
        long restante = canje.getTiempoRestanteOtp();
        tiempoRestante.postValue(restante);
    }
    
    /**
     * Inicia un temporizador para expirar automáticamente el OTP
     */
    private void iniciarTemporizadorExpiracion(CanjeEntity canje) {
        executor.execute(() -> {
            try {
                while (canje.isOtpValido()) {
                    Thread.sleep(1000); // Actualizar cada segundo
                    actualizarTiempoRestante(canje);
                    
                    if (!canje.isOtpValido()) {
                        // OTP expirado
                        canjeDao.expirarOtpsVencidos(System.currentTimeMillis());
                        otpValido.postValue(false);
                        estadoCanje.postValue("OTP_EXPIRADO");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * Sincroniza el canje con la API
     */
    private void sincronizarCanjeConApi(CanjeEntity canje) {
        // TODO: Implementar llamada a API cuando esté disponible
        // Por ahora solo marcamos como sincronizado localmente
        executor.execute(() -> {
            try {
                // Simular llamada a API
                Thread.sleep(1000);
                
                // Marcar como sincronizado
                canjeDao.marcarSincronizado(canje.getId_canje(), System.currentTimeMillis());
                
            } catch (Exception e) {
                // En caso de error, marcar para reintento
                canjeDao.marcarParaSincronizar(canje.getId_canje());
            }
        });
    }
    
    /**
     * Limpia canjes expirados antiguos
     */
    public void limpiarCanjesToExpirados() {
        executor.execute(() -> {
            try {
                long fechaLimite = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 horas atrás
                canjeDao.limpiarCanjesToExpirados(fechaLimite);
                canjeDao.limpiarCanjesCancelados(fechaLimite);
            } catch (Exception e) {
                // Log error
            }
        });
    }
    
    /**
     * Sincroniza canjes pendientes con la API
     */
    public void sincronizarCanjesPendientes() {
        executor.execute(() -> {
            try {
                List<CanjeEntity> canjesPendientes = canjeDao.getCanjesParaSincronizar();
                for (CanjeEntity canje : canjesPendientes) {
                    sincronizarCanjeConApi(canje);
                }
            } catch (Exception e) {
                // Log error
            }
        });
    }
}