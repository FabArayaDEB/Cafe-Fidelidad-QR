package com.example.cafefidelidaqrdemo.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.example.cafefidelidaqrdemo.repository.BeneficioRepository;
import com.example.cafefidelidaqrdemo.database.repository.CanjeRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para FragmentMisBeneficios
 * Maneja la lógica de negocio para mostrar beneficios y gestionar OTP
 */
public class MisBeneficiosViewModel extends AndroidViewModel {
    
    private static final String TAG = "MisBeneficiosViewModel";
    
    // Repositories
    private BeneficioRepository beneficioRepository;
    private CanjeRepository canjeRepository;
    private ExecutorService executor;
    
    // LiveData para beneficios
    private MutableLiveData<List<BeneficioEntity>> beneficiosDisponibles = new MutableLiveData<>();
    private MutableLiveData<List<CanjeEntity>> historialCanjes = new MutableLiveData<>();
    
    // LiveData para estado de carga
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> mensajeEstado = new MutableLiveData<>();
    
    public MisBeneficiosViewModel(@NonNull Application application) {
        super(application);
        beneficioRepository = new BeneficioRepository(application);
        canjeRepository = new CanjeRepository(application);
        executor = Executors.newFixedThreadPool(2);
    }
    
    // ========== MÉTODOS PARA BENEFICIOS ==========
    
    /**
     * Carga los beneficios disponibles para el cliente
     */
    public void cargarBeneficiosDisponibles(String clienteId) {
        isLoading.setValue(true);
        
        // Observar beneficios activos desde el repository
        beneficioRepository.getBeneficiosActivos().observeForever(beneficiosActivos -> {
            if (beneficiosActivos != null) {
                executor.execute(() -> {
                    try {
                        // Filtrar beneficios ya canjeados por el cliente
                        List<BeneficioEntity> beneficiosDisponiblesLista = filtrarBeneficiosDisponibles(beneficiosActivos, clienteId);
                        
                        beneficiosDisponibles.postValue(beneficiosDisponiblesLista);
                        isLoading.postValue(false);
                        
                        if (beneficiosDisponiblesLista.isEmpty()) {
                            mensajeEstado.postValue("No tienes beneficios disponibles en este momento.");
                        } else {
                            mensajeEstado.postValue("Tienes " + beneficiosDisponiblesLista.size() + " beneficios disponibles.");
                        }
                        
                    } catch (Exception e) {
                        isLoading.postValue(false);
                        mensajeEstado.postValue("Error al cargar beneficios: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Filtra beneficios que ya han sido canjeados por el cliente
     */
    private List<BeneficioEntity> filtrarBeneficiosDisponibles(List<BeneficioEntity> beneficios, String clienteId) {
        // TODO: Implementar lógica de filtrado basada en:
        // 1. Beneficios ya canjeados por el cliente
        // 2. Límites de uso por beneficio
        // 3. Vigencia de beneficios
        // 4. Sucursales aplicables
        
        // Por ahora retornamos todos los beneficios activos
        return beneficios;
    }
    
    /**
     * Carga el historial de canjes del cliente
     */
    public void cargarHistorialCanjes(String clienteId) {
        // El historial se observa directamente desde el repository
        // ya que usa LiveData
    }
    
    /**
     * Verifica si el cliente tiene un OTP activo
     */
    public void verificarOtpActivo(String clienteId) {
        executor.execute(() -> {
            try {
                long tiempoActual = System.currentTimeMillis();
                // Esta verificación se hace automáticamente en el CanjeRepository
                // a través de los observers
            } catch (Exception e) {
                // Log error
            }
        });
    }
    
    // ========== MÉTODOS PARA OTP (DELEGADOS AL CANJE REPOSITORY) ==========
    
    /**
     * Solicita un OTP para canjear un beneficio
     */
    public void solicitarOtp(String clienteId, String beneficioId, String sucursalId) {
        canjeRepository.solicitarOtp(clienteId, beneficioId, sucursalId);
    }
    
    /**
     * Solicita un nuevo OTP cuando el anterior ha expirado
     */
    public void solicitarNuevoOtp(String clienteId, String beneficioId, String sucursalId) {
        canjeRepository.solicitarNuevoOtp(clienteId, beneficioId, sucursalId);
    }
    
    /**
     * Confirma el canje usando el código OTP
     */
    public void confirmarCanje(String otpCodigo, String cajeroId) {
        canjeRepository.confirmarCanje(otpCodigo, cajeroId);
    }
    
    /**
     * Limpia el estado del OTP
     */
    public void limpiarEstado() {
        // Limpiar estados locales si es necesario
        mensajeEstado.setValue("");
    }
    
    // ========== GETTERS PARA LIVEDATA ==========
    
    public LiveData<List<BeneficioEntity>> getBeneficiosDisponibles() {
        return beneficiosDisponibles;
    }
    
    public LiveData<List<CanjeEntity>> getHistorialCanjes(String clienteId) {
        return canjeRepository.getHistorialCanjesCliente(clienteId);
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getMensajeEstado() {
        return mensajeEstado;
    }
    
    // Delegados del CanjeRepository para OTP
    public LiveData<String> getOtpActual() {
        return canjeRepository.getOtpActual();
    }
    
    public LiveData<Long> getTiempoRestante() {
        return canjeRepository.getTiempoRestante();
    }
    
    public LiveData<Boolean> getOtpValido() {
        return canjeRepository.getOtpValido();
    }
    
    public LiveData<String> getEstadoCanje() {
        return canjeRepository.getEstadoCanje();
    }
    
    public LiveData<String> getMensajeError() {
        return canjeRepository.getMensajeError();
    }
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    /**
     * Verifica si un beneficio puede ser canjeado por el cliente
     */
    public boolean puedeCanjearse(BeneficioEntity beneficio, String clienteId) {
        // TODO: Implementar validaciones:
        // 1. Beneficio activo
        // 2. Dentro de vigencia
        // 3. No excede límites de uso
        // 4. Cliente cumple requisitos
        // 5. Sucursal aplicable
        
        return beneficio.isActivo();
    }
    
    /**
     * Obtiene información detallada de un beneficio
     */
    public String getDescripcionBeneficio(BeneficioEntity beneficio) {
        StringBuilder descripcion = new StringBuilder();
        
        descripcion.append("Tipo: ").append(beneficio.getTipo()).append("\n");
        
        if (beneficio.getDescuento_pct() > 0) {
            descripcion.append("Descuento: ").append(beneficio.getDescuento_pct()).append("%\n");
        }
        
        if (beneficio.getDescuento_monto() > 0) {
            descripcion.append("Descuento: $").append(beneficio.getDescuento_monto()).append("\n");
        }
        
        // TODO: BeneficioEntity no tiene método getDescripcion()
        // if (beneficio.getDescripcion() != null && !beneficio.getDescripcion().isEmpty()) {
        //     descripcion.append("\n").append(beneficio.getDescripcion());
        // }
        
        return descripcion.toString();
    }
    
    /**
     * Formatea la fecha de vigencia del beneficio
     */
    public String getVigenciaFormateada(BeneficioEntity beneficio) {
        if (beneficio.getVigencia_fin() > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return "Válido hasta: " + sdf.format(new java.util.Date(beneficio.getVigencia_fin()));
        }
        return "Sin fecha de vencimiento";
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}