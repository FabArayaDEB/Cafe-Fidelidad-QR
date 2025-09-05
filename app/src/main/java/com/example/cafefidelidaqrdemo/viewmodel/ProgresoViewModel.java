package com.example.cafefidelidaqrdemo.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cafefidelidaqrdemo.models.Beneficio;
import com.example.cafefidelidaqrdemo.repository.BeneficioRepository;
import com.example.cafefidelidaqrdemo.repository.VisitaRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para calcular progreso local/remoto y manejar eventual consistency
 * Implementa la lógica para CU-04.2
 */
public class ProgresoViewModel extends AndroidViewModel {
    
    private final BeneficioRepository beneficioRepository;
    private final VisitaRepository visitaRepository;
    private final ExecutorService executor;
    private final Gson gson;
    
    // LiveData principales
    private final MutableLiveData<ProgresoGeneral> progresoGeneralLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Beneficio>> beneficiosDisponiblesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ProximoBeneficio>> proximosBeneficiosLiveData = new MutableLiveData<>();
    private final MutableLiveData<SyncStatus> syncStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasDataLiveData = new MutableLiveData<>(false);
    
    // Datos en caché
    private int totalVisitasLocal = 0;
    private int totalVisitasRemoto = 0;
    private Date ultimaSincronizacion;
    
    public ProgresoViewModel(@NonNull Application application) {
        super(application);
        this.beneficioRepository = new BeneficioRepository(application);
        this.visitaRepository = new VisitaRepository(application);
        this.executor = Executors.newFixedThreadPool(4);
        this.gson = new Gson();
        
        // Configurar estado inicial
        syncStatusLiveData.setValue(new SyncStatus(SyncEstado.SINCRONIZANDO, "Cargando datos..."));
    }
    
    // Getters para LiveData
    public LiveData<ProgresoGeneral> getProgresoGeneral() {
        return progresoGeneralLiveData;
    }
    
    public LiveData<List<Beneficio>> getBeneficiosDisponibles() {
        return beneficiosDisponiblesLiveData;
    }
    
    public LiveData<List<ProximoBeneficio>> getProximosBeneficios() {
        return proximosBeneficiosLiveData;
    }
    
    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getHasData() {
        return hasDataLiveData;
    }
    
    // Métodos principales
    public void loadProgresoData() {
        isLoadingLiveData.setValue(true);
        syncStatusLiveData.setValue(new SyncStatus(SyncEstado.SINCRONIZANDO, "Cargando datos..."));
        
        executor.execute(() -> {
            try {
                // Cargar datos locales primero
                loadLocalData();
                
                // Intentar sincronizar con servidor
                syncWithServer();
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar datos: " + e.getMessage());
                syncStatusLiveData.postValue(new SyncStatus(SyncEstado.ERROR, e.getMessage()));
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    public void refreshProgresoData() {
        loadProgresoData();
    }
    
    private void loadLocalData() {
        try {
            // Obtener total de visitas locales
            totalVisitasLocal = visitaRepository.getTotalVisitasSync();
            
            // Calcular progreso con datos locales
            calculateProgreso(totalVisitasLocal, true);
            
            // Marcar como datos estimados si no hay sincronización reciente
            if (ultimaSincronizacion == null || 
                (new Date().getTime() - ultimaSincronizacion.getTime()) > 300000) { // 5 minutos
                syncStatusLiveData.postValue(
                    new SyncStatus(SyncEstado.ESTIMADO, "Datos locales (estimado)")
                );
            }
            
        } catch (Exception e) {
            errorLiveData.postValue("Error al cargar datos locales: " + e.getMessage());
        }
    }
    
    private void syncWithServer() {
        try {
            // Simular sincronización con servidor
            // En implementación real, aquí se haría la llamada a la API
            
            // Por ahora, usar datos locales como "remotos"
            totalVisitasRemoto = totalVisitasLocal;
            ultimaSincronizacion = new Date();
            
            // Recalcular con datos "sincronizados"
            calculateProgreso(totalVisitasRemoto, false);
            
            syncStatusLiveData.postValue(
                new SyncStatus(SyncEstado.SINCRONIZADO, "Datos actualizados")
            );
            
        } catch (Exception e) {
            // Si falla la sincronización, mantener datos locales
            syncStatusLiveData.postValue(
                new SyncStatus(SyncEstado.ESTIMADO, "Sin conexión - datos estimados")
            );
        }
    }
    
    private void calculateProgreso(int totalVisitas, boolean isLocal) {
        try {
            // Obtener beneficios vigentes
            List<Beneficio> beneficiosVigentes = beneficioRepository.getBeneficiosVigentesSync();
            
            if (beneficiosVigentes.isEmpty()) {
                hasDataLiveData.postValue(false);
                return;
            }
            
            hasDataLiveData.postValue(true);
            
            // Calcular beneficios disponibles
            List<Beneficio> disponibles = calculateBeneficiosDisponibles(totalVisitas, beneficiosVigentes);
            beneficiosDisponiblesLiveData.postValue(disponibles);
            
            // Calcular próximos beneficios
            List<ProximoBeneficio> proximos = calculateProximosBeneficios(totalVisitas, beneficiosVigentes);
            proximosBeneficiosLiveData.postValue(proximos);
            
            // Calcular progreso general
            ProgresoGeneral progreso = calculateProgresoGeneral(totalVisitas, beneficiosVigentes, proximos);
            progresoGeneralLiveData.postValue(progreso);
            
        } catch (Exception e) {
            errorLiveData.postValue("Error al calcular progreso: " + e.getMessage());
        }
    }
    
    private List<Beneficio> calculateBeneficiosDisponibles(int totalVisitas, List<Beneficio> beneficios) {
        List<Beneficio> disponibles = new ArrayList<>();
        
        for (Beneficio beneficio : beneficios) {
            if (isBeneficioDisponible(totalVisitas, beneficio)) {
                disponibles.add(beneficio);
            }
        }
        
        return disponibles;
    }
    
    private List<ProximoBeneficio> calculateProximosBeneficios(int totalVisitas, List<Beneficio> beneficios) {
        List<ProximoBeneficio> proximos = new ArrayList<>();
        
        for (Beneficio beneficio : beneficios) {
            ProximoBeneficio proximo = calculateProximoBeneficio(totalVisitas, beneficio);
            if (proximo != null) {
                proximos.add(proximo);
            }
        }
        
        // Ordenar por proximidad (menor número de visitas faltantes primero)
        proximos.sort((a, b) -> Integer.compare(a.getVisitasFaltantes(), b.getVisitasFaltantes()));
        
        return proximos;
    }
    
    private ProgresoGeneral calculateProgresoGeneral(int totalVisitas, List<Beneficio> beneficios, List<ProximoBeneficio> proximos) {
        ProgresoGeneral progreso = new ProgresoGeneral();
        progreso.setTotalVisitas(totalVisitas);
        
        if (proximos.isEmpty()) {
            // No hay próximos beneficios
            progreso.setProgresoHaciaProximo(-1);
            progreso.setVisitasActuales(totalVisitas);
            progreso.setVisitasParaProximo(0);
        } else {
            // Tomar el próximo beneficio más cercano
            ProximoBeneficio proximoMasCercano = proximos.get(0);
            
            int visitasRequeridas = proximoMasCercano.getVisitasRequeridas();
            int visitasFaltantes = proximoMasCercano.getVisitasFaltantes();
            int visitasActuales = visitasRequeridas - visitasFaltantes;
            
            progreso.setVisitasActuales(visitasActuales);
            progreso.setVisitasParaProximo(visitasRequeridas);
            progreso.setProgresoHaciaProximo((double) visitasActuales / visitasRequeridas);
        }
        
        return progreso;
    }
    
    private boolean isBeneficioDisponible(int totalVisitas, Beneficio beneficio) {
        try {
            JsonObject reglas = gson.fromJson(beneficio.getReglasJson(), JsonObject.class);
            
            if (reglas.has("cadaNVisitas")) {
                int visitasRequeridas = reglas.get("cadaNVisitas").getAsInt();
                return totalVisitas >= visitasRequeridas && (totalVisitas % visitasRequeridas == 0);
            }
            
            if (reglas.has("montoMinimo")) {
                // Para monto mínimo, necesitaríamos datos de montos de compra
                // Por simplicidad, asumimos que está disponible si tiene suficientes visitas
                return totalVisitas >= 1;
            }
            
            if (reglas.has("fechaEspecial")) {
                // Verificar si hoy es la fecha especial
                // Por simplicidad, retornamos false
                return false;
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private ProximoBeneficio calculateProximoBeneficio(int totalVisitas, Beneficio beneficio) {
        try {
            JsonObject reglas = gson.fromJson(beneficio.getReglasJson(), JsonObject.class);
            
            if (reglas.has("cadaNVisitas")) {
                int visitasRequeridas = reglas.get("cadaNVisitas").getAsInt();
                
                // Si ya está disponible, no es un "próximo" beneficio
                if (isBeneficioDisponible(totalVisitas, beneficio)) {
                    return null;
                }
                
                // Calcular cuántas visitas faltan para el próximo múltiplo
                int proximoMultiplo = ((totalVisitas / visitasRequeridas) + 1) * visitasRequeridas;
                int visitasFaltantes = proximoMultiplo - totalVisitas;
                
                return new ProximoBeneficio(
                    beneficio,
                    visitasRequeridas,
                    visitasFaltantes,
                    (double) totalVisitas / visitasRequeridas
                );
            }
            
            if (reglas.has("montoMinimo")) {
                // Para monto mínimo, asumir que falta 1 visita
                return new ProximoBeneficio(
                    beneficio,
                    1,
                    1,
                    0.0
                );
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    // Clases de datos
    public static class ProgresoGeneral {
        private int totalVisitas;
        private int visitasActuales;
        private int visitasParaProximo;
        private double progresoHaciaProximo;
        
        // Getters y setters
        public int getTotalVisitas() { return totalVisitas; }
        public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
        
        public int getVisitasActuales() { return visitasActuales; }
        public void setVisitasActuales(int visitasActuales) { this.visitasActuales = visitasActuales; }
        
        public int getVisitasParaProximo() { return visitasParaProximo; }
        public void setVisitasParaProximo(int visitasParaProximo) { this.visitasParaProximo = visitasParaProximo; }
        
        public double getProgresoHaciaProximo() { return progresoHaciaProximo; }
        public void setProgresoHaciaProximo(double progresoHaciaProximo) { this.progresoHaciaProximo = progresoHaciaProximo; }
    }
    
    public static class ProximoBeneficio {
        private final Beneficio beneficio;
        private final int visitasRequeridas;
        private final int visitasFaltantes;
        private final double progreso;
        
        public ProximoBeneficio(Beneficio beneficio, int visitasRequeridas, int visitasFaltantes, double progreso) {
            this.beneficio = beneficio;
            this.visitasRequeridas = visitasRequeridas;
            this.visitasFaltantes = visitasFaltantes;
            this.progreso = progreso;
        }
        
        // Getters
        public Beneficio getBeneficio() { return beneficio; }
        public int getVisitasRequeridas() { return visitasRequeridas; }
        public int getVisitasFaltantes() { return visitasFaltantes; }
        public double getProgreso() { return progreso; }
    }
    
    public static class SyncStatus {
        private final SyncEstado estado;
        private final String mensaje;
        
        public SyncStatus(SyncEstado estado, String mensaje) {
            this.estado = estado;
            this.mensaje = mensaje;
        }
        
        public SyncEstado getEstado() { return estado; }
        public String getMensaje() { return mensaje; }
    }
    
    public enum SyncEstado {
        SINCRONIZADO,
        ESTIMADO,
        SINCRONIZANDO,
        ERROR
    }
}