package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.models.Canje;
import com.example.cafefidelidaqrdemo.network.ApiClient;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.BeneficioRepository;
import com.example.cafefidelidaqrdemo.repository.CanjeRepository;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel para gestión de beneficios del cliente siguiendo patrones MVVM estrictos
 * Se enfoca únicamente en la preparación de datos para la UI
 */
public class MisBeneficiosViewModel extends AndroidViewModel {
    
    // Constants
    private static final String TAG = "MisBeneficiosViewModel";
    
    // Dependencies
    private final BeneficioRepository beneficioRepository;
    private final CanjeRepository canjeRepository;
    
    // UI State
    private final MutableLiveData<String> _clienteId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<String> _filtroTipo = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> _showExpired = new MutableLiveData<>(false);
    
    // Observable Data
    private final LiveData<List<Beneficio>> beneficiosDisponibles;
    private final LiveData<List<Canje>> historialCanjes;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final LiveData<Boolean> isOffline;
    
    // OTP Data
    private final LiveData<String> otpActual;
    private final LiveData<Long> tiempoRestante;
    private final LiveData<Boolean> otpValido;
    private final LiveData<String> estadoCanje;
    
    // Derived Data
    private final LiveData<Boolean> hasData;
    private final LiveData<Boolean> showEmptyState;
    private final LiveData<String> statusMessage;
    private final LiveData<Integer> beneficiosCount;
    private final LiveData<Integer> canjesCount;
    private final LiveData<Boolean> hasActiveOtp;
    
    public MisBeneficiosViewModel(@NonNull Application application) {
        super(application);
        
        // Initialize dependencies
        CafeFidelidadDB database = CafeFidelidadDB.getInstance(application);
        ApiService apiService = ApiClient.getApiService();
        
        this.beneficioRepository = new BeneficioRepository(application);
        this.canjeRepository = new CanjeRepository(application);
        
        // Configure repository observables
        this.beneficiosDisponibles = createBeneficiosDisponiblesLiveData();
        this.historialCanjes = createHistorialCanjesLiveData();
        this.isLoading = createIsLoadingLiveData();
        this.error = createErrorLiveData();
        this.isOffline = beneficioRepository.getIsOffline();
        
        // Configure OTP observables - TODO: Implement OTP functionality
        this.otpActual = new MutableLiveData<>("");
        this.tiempoRestante = new MutableLiveData<>(0L);
        this.otpValido = new MutableLiveData<>(false);
        this.estadoCanje = new MutableLiveData<>("inactivo");
        
        // Configure derived UI data
        this.hasData = createHasDataLiveData();
        this.showEmptyState = createShowEmptyStateLiveData();
        this.statusMessage = createStatusMessageLiveData();
        this.beneficiosCount = createBeneficiosCountLiveData();
        this.canjesCount = createCanjesCountLiveData();
        this.hasActiveOtp = createHasActiveOtpLiveData();
    }
    
    // LiveData Getters
    public LiveData<List<Beneficio>> getBeneficiosDisponibles() {
        return beneficiosDisponibles;
    }
    
    public LiveData<List<Canje>> getHistorialCanjes() {
        return historialCanjes;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsRefreshing() {
        return _isRefreshing;
    }
    
    public LiveData<Boolean> getIsOffline() {
        return isOffline;
    }
    
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
    
    public LiveData<Boolean> getHasData() {
        return hasData;
    }
    
    public LiveData<Boolean> getShowEmptyState() {
        return showEmptyState;
    }
    
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }
    
    public LiveData<Integer> getBeneficiosCount() {
        return beneficiosCount;
    }
    
    public LiveData<Integer> getCanjesCount() {
        return canjesCount;
    }
    
    public LiveData<Boolean> getHasActiveOtp() {
        return hasActiveOtp;
    }
    
    // UI Actions
    public void setClienteId(String clienteId) {
        if (!clienteId.equals(_clienteId.getValue())) {
            _clienteId.setValue(clienteId);
        }
    }
    
    public void loadBeneficios() {
        String clienteId = _clienteId.getValue();
        if (clienteId == null || clienteId.isEmpty()) {
            return;
        }
        
        beneficioRepository.refreshBeneficios(new BeneficioRepository.OnResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                // Success/error handled by repository's LiveData
            }
        });
    }
    
    public void refreshBeneficios() {
        if (Boolean.TRUE.equals(_isRefreshing.getValue())) {
            return;
        }
        
        _isRefreshing.setValue(true);
        
        beneficioRepository.refreshBeneficios(new BeneficioRepository.OnResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                _isRefreshing.setValue(false);
                // Success/error handled by repository's LiveData
            }
        });
    }
    
    public void forceSyncBeneficios() {
        beneficioRepository.forceSyncBeneficios(new BeneficioRepository.OnResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                // Success/error handled by repository's LiveData
            }
        });
    }
    
    public void setFiltroTipo(String tipo) {
        _filtroTipo.setValue(tipo);
    }
    
    public void setShowExpired(boolean showExpired) {
        _showExpired.setValue(showExpired);
    }
    
    public void clearFilters() {
        _filtroTipo.setValue("");
        _showExpired.setValue(false);
    }
    
    // OTP Actions
    public void solicitarOtp(String beneficioId, String sucursalId) {
        String clienteId = _clienteId.getValue();
        if (clienteId == null || clienteId.isEmpty()) {
            return;
        }
        
        canjeRepository.solicitarOtp(clienteId, beneficioId, sucursalId);
    }
    
    public void solicitarNuevoOtp(String beneficioId, String sucursalId) {
        String clienteId = _clienteId.getValue();
        if (clienteId == null || clienteId.isEmpty()) {
            return;
        }
        
        // TODO: Implement OTP functionality
        // canjeRepository.solicitarNuevoOtp(clienteId, beneficioId, sucursalId);
    }
    
    public void confirmarCanje(String otpCodigo, String cajeroId) {
        // TODO: Implement canje confirmation functionality
        // canjeRepository.confirmarCanje(otpCodigo, cajeroId);
    }
    
    public void limpiarEstadoOtp() {
        // TODO: Implement OTP state clearing functionality
        // canjeRepository.limpiarEstado();
    }
    
    public void clearError() {
        // Error clearing is handled by repositories
    }
    
    public boolean verificarOtpActivo() {
        Boolean otpValidoValue = otpValido.getValue();
        return Boolean.TRUE.equals(otpValidoValue);
    }
    
    public void cargarBeneficiosDisponibles(String clienteId) {
        setClienteId(clienteId);
        loadBeneficios();
    }
    
    public LiveData<String> getMensajeError() {
        return error;
    }
    
    public void limpiarEstado() {
        limpiarEstadoOtp();
        clearError();
    }
    
    // Utility Methods
    public boolean puedeCanjearse(Beneficio beneficio) {
        if (beneficio == null || !beneficio.isActivo()) {
            return false;
        }
        
        // Check expiration
        if (beneficio.getFechaVencimiento() > 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > beneficio.getFechaVencimiento()) {
                return false;
            }
        }
        
        return true;
    }
    
    public String getDescripcionBeneficio(Beneficio beneficio) {
        if (beneficio == null) {
            return "";
        }
        
        StringBuilder descripcion = new StringBuilder();
        descripcion.append("Tipo: ").append(beneficio.getTipo()).append("\n");
        
        if (beneficio.getValorDescuento() > 0) {
            if ("descuento".equals(beneficio.getTipo())) {
                descripcion.append("Descuento: ").append(beneficio.getValorDescuento()).append("%\n");
            } else {
                descripcion.append("Valor: $").append(beneficio.getValorDescuento()).append("\n");
            }
        }
        
        return descripcion.toString();
    }
    
    public String getVigenciaFormateada(Beneficio beneficio) {
        if (beneficio == null || beneficio.getFechaVencimiento() == 0) {
            return "Sin fecha de vencimiento";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return "Válido hasta: " + sdf.format(new Date(beneficio.getFechaVencimiento()));
    }
    
    // Private Helper Methods
    private LiveData<List<Beneficio>> createBeneficiosDisponiblesLiveData() {
        return beneficioRepository.getBeneficiosDisponiblesParaCliente();
    }
    
    private LiveData<List<Canje>> createHistorialCanjesLiveData() {
        return Transformations.switchMap(_clienteId, clienteId -> {
            if (clienteId == null || clienteId.isEmpty()) {
                return new MutableLiveData<>();
            }
            return canjeRepository.getHistorialCanjesCliente(clienteId);
        });
    }
    
    private LiveData<Boolean> createIsLoadingLiveData() {
        MediatorLiveData<Boolean> result = new MediatorLiveData<>();
        
        result.addSource(beneficioRepository.getIsLoading(), loading -> {
            Boolean canjeLoading = canjeRepository.getIsLoading().getValue();
            result.setValue(Boolean.TRUE.equals(loading) || Boolean.TRUE.equals(canjeLoading));
        });
        
        result.addSource(canjeRepository.getIsLoading(), loading -> {
            Boolean beneficioLoading = beneficioRepository.getIsLoading().getValue();
            result.setValue(Boolean.TRUE.equals(beneficioLoading) || Boolean.TRUE.equals(loading));
        });
        
        return result;
    }
    
    private LiveData<String> createErrorLiveData() {
        MediatorLiveData<String> result = new MediatorLiveData<>();
        
        result.addSource(beneficioRepository.getError(), error -> {
            String canjeError = canjeRepository.getError().getValue();
            if (error != null) {
                result.setValue(error);
            } else if (canjeError != null) {
                result.setValue(canjeError);
            } else {
                result.setValue(null);
            }
        });
        
        result.addSource(canjeRepository.getError(), error -> {
            String beneficioError = beneficioRepository.getError().getValue();
            if (error != null) {
                result.setValue(error);
            } else if (beneficioError != null) {
                result.setValue(beneficioError);
            } else {
                result.setValue(null);
            }
        });
        
        return result;
    }
    
    private LiveData<Boolean> createHasDataLiveData() {
        MediatorLiveData<Boolean> result = new MediatorLiveData<>();
        
        result.addSource(beneficiosDisponibles, beneficios -> {
            List<Canje> canjes = historialCanjes.getValue();
            result.setValue((beneficios != null && !beneficios.isEmpty()) ||
                          (canjes != null && !canjes.isEmpty()));
        });
        
        result.addSource(historialCanjes, canjes -> {
            List<Beneficio> beneficios = beneficiosDisponibles.getValue();
            result.setValue((beneficios != null && !beneficios.isEmpty()) ||
                          (canjes != null && !canjes.isEmpty()));
        });
        
        return result;
    }
    
    private LiveData<Boolean> createShowEmptyStateLiveData() {
        return Transformations.map(hasData, hasData -> !Boolean.TRUE.equals(hasData));
    }
    
    private LiveData<String> createStatusMessageLiveData() {
        return Transformations.map(beneficiosDisponibles, beneficios -> {
            if (beneficios == null || beneficios.isEmpty()) {
                return "No tienes beneficios disponibles en este momento.";
            }
            return "Tienes " + beneficios.size() + " beneficios disponibles.";
        });
    }
    
    private LiveData<Integer> createBeneficiosCountLiveData() {
        return Transformations.map(beneficiosDisponibles, beneficios -> {
            return beneficios != null ? beneficios.size() : 0;
        });
    }
    
    private LiveData<Integer> createCanjesCountLiveData() {
        return Transformations.map(historialCanjes, canjes -> {
            return canjes != null ? canjes.size() : 0;
        });
    }
    
    private LiveData<Boolean> createHasActiveOtpLiveData() {
        return Transformations.map(otpValido, valido -> Boolean.TRUE.equals(valido));
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Repository cleanup is handled automatically
    }
}