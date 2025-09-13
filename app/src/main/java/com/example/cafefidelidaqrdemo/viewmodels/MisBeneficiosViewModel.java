package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.entities.BeneficioEntity;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
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
    private final LiveData<List<BeneficioEntity>> beneficiosDisponibles;
    private final LiveData<List<CanjeEntity>> historialCanjes;
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
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        ApiService apiService = ApiClient.getApiService();
        
        this.beneficioRepository = new BeneficioRepository(application);
        this.canjeRepository = new CanjeRepository(application);
        
        // Configure repository observables
        this.beneficiosDisponibles = createBeneficiosDisponiblesLiveData();
        this.historialCanjes = createHistorialCanjesLiveData();
        this.isLoading = createIsLoadingLiveData();
        this.error = createErrorLiveData();
        this.isOffline = beneficioRepository.getIsOffline();
        
        // Configure OTP observables
        this.otpActual = canjeRepository.getOtpActual();
        this.tiempoRestante = canjeRepository.getTiempoRestante();
        this.otpValido = canjeRepository.getOtpValido();
        this.estadoCanje = canjeRepository.getEstadoCanje();
        
        // Configure derived UI data
        this.hasData = createHasDataLiveData();
        this.showEmptyState = createShowEmptyStateLiveData();
        this.statusMessage = createStatusMessageLiveData();
        this.beneficiosCount = createBeneficiosCountLiveData();
        this.canjesCount = createCanjesCountLiveData();
        this.hasActiveOtp = createHasActiveOtpLiveData();
    }
    
    // LiveData Getters
    public LiveData<List<BeneficioEntity>> getBeneficiosDisponibles() {
        return beneficiosDisponibles;
    }
    
    public LiveData<List<CanjeEntity>> getHistorialCanjes() {
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
        
        beneficioRepository.refreshBeneficios(new BaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Success handled by LiveData observers
            }
            
            @Override
            public void onError(String errorMessage) {
                // Error handled by LiveData observers
            }
        });
    }
    
    public void refreshBeneficios() {
        if (Boolean.TRUE.equals(_isRefreshing.getValue())) {
            return;
        }
        
        _isRefreshing.setValue(true);
        
        beneficioRepository.forceSyncBeneficios(new BaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                _isRefreshing.setValue(false);
            }
            
            @Override
            public void onError(String errorMessage) {
                _isRefreshing.setValue(false);
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
        
        canjeRepository.solicitarNuevoOtp(clienteId, beneficioId, sucursalId);
    }
    
    public void confirmarCanje(String otpCodigo, String cajeroId) {
        canjeRepository.confirmarCanje(otpCodigo, cajeroId);
    }
    
    public void limpiarEstadoOtp() {
        canjeRepository.limpiarEstado();
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
    public boolean puedeCanjearse(BeneficioEntity beneficio) {
        if (beneficio == null || !beneficio.isActivo()) {
            return false;
        }
        
        // Check expiration
        if (beneficio.getVigencia_fin() > 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > beneficio.getVigencia_fin()) {
                return false;
            }
        }
        
        return true;
    }
    
    public String getDescripcionBeneficio(BeneficioEntity beneficio) {
        if (beneficio == null) {
            return "";
        }
        
        StringBuilder descripcion = new StringBuilder();
        descripcion.append("Tipo: ").append(beneficio.getTipo()).append("\n");
        
        if (beneficio.getDescuento_pct() > 0) {
            descripcion.append("Descuento: ").append(beneficio.getDescuento_pct()).append("%\n");
        }
        
        if (beneficio.getDescuento_monto() > 0) {
            descripcion.append("Descuento: $").append(beneficio.getDescuento_monto()).append("\n");
        }
        
        return descripcion.toString();
    }
    
    public String getVigenciaFormateada(BeneficioEntity beneficio) {
        if (beneficio == null || beneficio.getVigencia_fin() <= 0) {
            return "Sin fecha de vencimiento";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return "Válido hasta: " + sdf.format(new Date(beneficio.getVigencia_fin()));
    }
    
    // Private Helper Methods
    private LiveData<List<BeneficioEntity>> createBeneficiosDisponiblesLiveData() {
        return beneficioRepository.getBeneficiosDisponiblesParaCliente();
    }
    
    private LiveData<List<CanjeEntity>> createHistorialCanjesLiveData() {
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
            List<CanjeEntity> canjes = historialCanjes.getValue();
            result.setValue((beneficios != null && !beneficios.isEmpty()) ||
                          (canjes != null && !canjes.isEmpty()));
        });
        
        result.addSource(historialCanjes, canjes -> {
            List<BeneficioEntity> beneficios = beneficiosDisponibles.getValue();
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