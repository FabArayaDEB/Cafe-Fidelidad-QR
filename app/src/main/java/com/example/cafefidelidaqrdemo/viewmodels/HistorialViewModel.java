package com.example.cafefidelidaqrdemo.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.VisitaDao;
import com.example.cafefidelidaqrdemo.database.dao.CanjeDao;
import com.example.cafefidelidaqrdemo.database.entities.VisitaEntity;
import com.example.cafefidelidaqrdemo.database.entities.CanjeEntity;
import com.example.cafefidelidaqrdemo.models.HistorialItem;
import com.example.cafefidelidaqrdemo.sync.SyncManager;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para el historial de visitas y canjes
 */
public class HistorialViewModel extends AndroidViewModel {
    
    public static final int PAGE_SIZE = 20;
    
    private final VisitaDao visitaDao;
    private final CanjeDao canjeDao;
    private final ExecutorService executor;
    
    // LiveData
    private final MutableLiveData<List<HistorialItem>> historialItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> syncStatusLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> networkStatusLiveData = new MutableLiveData<>(true);
    
    // Estado de paginación
    private int currentPage = 0;
    private String currentClienteId = null;
    private FiltroTipo currentFiltroTipo = FiltroTipo.TODOS;
    private FiltroEstado currentFiltroEstado = FiltroEstado.TODOS;
    private Date currentFechaInicio = null;
    private Date currentFechaFin = null;
    
    // Cache de datos
    private final List<HistorialItem> allItems = new ArrayList<>();
    
    public enum FiltroTipo {
        TODOS, VISITAS, CANJES
    }
    
    public enum FiltroEstado {
        TODOS, PENDIENTES, ENVIADOS
    }
    
    public HistorialViewModel(@NonNull Application application) {
        super(application);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(application);
        this.visitaDao = database.visitaDao();
        this.canjeDao = database.canjeDao();
        this.executor = Executors.newFixedThreadPool(4);
        
        // Verificar estado de red inicial
        networkStatusLiveData.setValue(NetworkUtils.isNetworkAvailable(application));
    }
    
    /**
     * Carga el historial inicial
     */
    public void loadHistorial(String clienteId) {
        currentClienteId = clienteId;
        currentPage = 0;
        allItems.clear();
        loadHistorialData(true);
    }
    
    /**
     * Refresca el historial desde el servidor
     */
    public void refreshHistorial(String clienteId) {
        currentClienteId = clienteId;
        currentPage = 0;
        allItems.clear();
        
        // Verificar conectividad
        boolean isConnected = NetworkUtils.isNetworkAvailable(getApplication());
        networkStatusLiveData.setValue(isConnected);
        
        if (isConnected) {
            // Programar sincronización para obtener datos frescos
            SyncManager.scheduleVisitaSync(getApplication());
            SyncManager.scheduleCanjeSync(getApplication());
        }
        
        loadHistorialData(true);
    }
    
    /**
     * Carga más datos para paginación
     */
    public void loadMoreHistorial(String clienteId) {
        if (!isLoadingLiveData.getValue() && clienteId.equals(currentClienteId)) {
            currentPage++;
            loadHistorialData(false);
        }
    }
    
    /**
     * Aplica filtros al historial
     */
    public void applyFilters(FiltroTipo tipo, FiltroEstado estado, Date fechaInicio, Date fechaFin) {
        currentFiltroTipo = tipo;
        currentFiltroEstado = estado;
        currentFechaInicio = fechaInicio;
        currentFechaFin = fechaFin;
        currentPage = 0;
        allItems.clear();
        
        if (currentClienteId != null) {
            loadHistorialData(true);
        }
    }
    
    /**
     * Carga los datos del historial
     */
    private void loadHistorialData(boolean isRefresh) {
        if (currentClienteId == null) return;
        
        isLoadingLiveData.setValue(true);
        
        executor.execute(() -> {
            try {
                List<HistorialItem> newItems = new ArrayList<>();
                
                // Calcular offset para paginación
                int offset = currentPage * PAGE_SIZE;
                
                // Cargar visitas si corresponde
                if (currentFiltroTipo == FiltroTipo.TODOS || currentFiltroTipo == FiltroTipo.VISITAS) {
                    List<VisitaEntity> visitas = loadVisitasWithFilters(offset, PAGE_SIZE);
                    for (VisitaEntity visita : visitas) {
                        newItems.add(convertVisitaToHistorialItem(visita));
                    }
                }
                
                // Cargar canjes si corresponde
                if (currentFiltroTipo == FiltroTipo.TODOS || currentFiltroTipo == FiltroTipo.CANJES) {
                    List<CanjeEntity> canjes = loadCanjesWithFilters(offset, PAGE_SIZE);
                    for (CanjeEntity canje : canjes) {
                        newItems.add(convertCanjeToHistorialItem(canje));
                    }
                }
                
                // Ordenar por fecha (más reciente primero)
                Collections.sort(newItems, (a, b) -> b.getFechaHora().compareTo(a.getFechaHora()));
                
                // Aplicar límite de página
                if (newItems.size() > PAGE_SIZE) {
                    newItems = newItems.subList(0, PAGE_SIZE);
                }
                
                // Actualizar cache
                if (isRefresh) {
                    allItems.clear();
                }
                allItems.addAll(newItems);
                
                // Actualizar UI
                historialItemsLiveData.postValue(new ArrayList<>(allItems));
                
                // Verificar estado de sincronización
                checkSyncStatus();
                
            } catch (Exception e) {
                errorLiveData.postValue("Error al cargar historial: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    /**
     * Carga visitas con filtros aplicados
     */
    private List<VisitaEntity> loadVisitasWithFilters(int offset, int limit) {
        if (currentFechaInicio != null && currentFechaFin != null) {
            // Usar método que existe en VisitaDao
            List<VisitaEntity> allVisitas = visitaDao.getByClienteAndRangoFecha(currentClienteId, 
                currentFechaInicio.getTime(), currentFechaFin.getTime());
            return paginateList(allVisitas, offset, limit);
        } else if (currentFechaInicio != null) {
            // Filtrar manualmente por fecha de inicio
            List<VisitaEntity> allVisitas = visitaDao.getByCliente(currentClienteId);
            List<VisitaEntity> filtered = new ArrayList<>();
            for (VisitaEntity visita : allVisitas) {
                if (visita.getFecha_hora() >= currentFechaInicio.getTime()) {
                    filtered.add(visita);
                }
            }
            return paginateList(filtered, offset, limit);
        } else if (currentFechaFin != null) {
            // Filtrar manualmente por fecha fin
            List<VisitaEntity> allVisitas = visitaDao.getByCliente(currentClienteId);
            List<VisitaEntity> filtered = new ArrayList<>();
            for (VisitaEntity visita : allVisitas) {
                if (visita.getFecha_hora() <= currentFechaFin.getTime()) {
                    filtered.add(visita);
                }
            }
            return paginateList(filtered, offset, limit);
        } else {
            // Sin filtros de fecha, obtener todas las visitas del cliente
            List<VisitaEntity> allVisitas = visitaDao.getByCliente(currentClienteId);
            
            // Filtrar por estado si es necesario
            if (currentFiltroEstado == FiltroEstado.PENDIENTES) {
                List<VisitaEntity> filtered = new ArrayList<>();
                for (VisitaEntity visita : allVisitas) {
                    if ("PENDIENTE".equals(visita.getEstado_sync())) {
                        filtered.add(visita);
                    }
                }
                return paginateList(filtered, offset, limit);
            } else if (currentFiltroEstado == FiltroEstado.ENVIADOS) {
                List<VisitaEntity> filtered = new ArrayList<>();
                for (VisitaEntity visita : allVisitas) {
                    if ("ENVIADO".equals(visita.getEstado_sync())) {
                        filtered.add(visita);
                    }
                }
                return paginateList(filtered, offset, limit);
            } else {
                return paginateList(allVisitas, offset, limit);
            }
        }
    }
    
    /**
     * Método auxiliar para paginar una lista
     */
    private <T> List<T> paginateList(List<T> list, int offset, int limit) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        
        int start = Math.min(offset, list.size());
        int end = Math.min(offset + limit, list.size());
        
        if (start >= end) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(list.subList(start, end));
    }
    
    /**
     * Carga canjes con filtros aplicados
     */
    private List<CanjeEntity> loadCanjesWithFilters(int offset, int limit) {
        // Obtener todos los canjes del cliente
        List<CanjeEntity> allCanjes = new ArrayList<>();
        LiveData<List<CanjeEntity>> canjesLiveData = canjeDao.getCanjesByCliente(currentClienteId);
        
        // Como necesitamos datos síncronos, usamos un enfoque simplificado
        // En una implementación real, esto debería manejarse de forma asíncrona
        
        // Filtrar por fechas si están definidas
        List<CanjeEntity> filteredCanjes = new ArrayList<>();
        
        if (currentFechaInicio != null && currentFechaFin != null) {
            // Usar el método que existe en CanjeDao
            LiveData<List<CanjeEntity>> rangeCanjes = canjeDao.getCanjesClienteEnRango(
                currentClienteId, currentFechaInicio.getTime(), currentFechaFin.getTime());
            // TODO: Convertir LiveData a List síncrono
        } else {
            // Obtener todos los canjes y filtrar manualmente
            // TODO: Implementar filtrado manual por fechas y estado
            
            // Filtrar por estado si es necesario
            if (currentFiltroEstado == FiltroEstado.PENDIENTES) {
                LiveData<List<CanjeEntity>> estadoCanjes = canjeDao.getCanjesByClienteYEstado(currentClienteId, "PENDIENTE");
                // TODO: Convertir LiveData a List síncrono
            } else if (currentFiltroEstado == FiltroEstado.ENVIADOS) {
                LiveData<List<CanjeEntity>> estadoCanjes = canjeDao.getCanjesByClienteYEstado(currentClienteId, "ENVIADO");
                // TODO: Convertir LiveData a List síncrono
            }
        }
        
        // Aplicar paginación
        return paginateList(filteredCanjes, offset, limit);
    }
    
    /**
     * Convierte VisitaEntity a HistorialItem
     */
    private HistorialItem convertVisitaToHistorialItem(VisitaEntity visita) {
        HistorialItem item = new HistorialItem();
        item.setId(visita.getId_visita());
        item.setTipo(HistorialItem.Tipo.VISITA);
        item.setFechaHora(new Date(visita.getFecha_hora()));
        item.setEstadoSync(visita.getEstado_sync());
        item.setSucursalId(visita.getId_sucursal());
        item.setOrigen(visita.getOrigen());
        item.setHashQr(visita.getHash_qr());
        return item;
    }
    
    /**
     * Convierte CanjeEntity a HistorialItem
     */
    private HistorialItem convertCanjeToHistorialItem(CanjeEntity canje) {
        HistorialItem item = new HistorialItem();
        item.setId(canje.getId_canje());
        item.setTipo(HistorialItem.Tipo.CANJE);
        item.setFechaHora(new Date(canje.getFecha_solicitud()));
        item.setEstadoSync(canje.getEstado());
        item.setSucursalId(canje.getId_sucursal());
        item.setBeneficioId(canje.getId_beneficio());
        item.setCodigoOtp(canje.getOtp_codigo());
        return item;
    }
    
    /**
     * Verifica el estado de sincronización
     */
    private void checkSyncStatus() {
        executor.execute(() -> {
            try {
                // Verificar si hay datos pendientes
                // Usar métodos que existen en los DAOs
                long tiempoActual = System.currentTimeMillis();
                int canjesPendientes = canjeDao.getCanjesPendientesValidos(tiempoActual).size();
                
                // Para visitas, simplificar la verificación
                boolean hasPendingData = canjesPendientes > 0;
                syncStatusLiveData.postValue(!hasPendingData);
                
            } catch (Exception e) {
                // Ignorar errores de verificación de sync
                syncStatusLiveData.postValue(true); // Asumir sincronizado en caso de error
            }
        });
    }
    
    /**
     * Fuerza la sincronización
     */
    public void forceSync() {
        SyncManager.forceSyncAll(getApplication());
        
        // Recargar datos después de un breve delay
        executor.execute(() -> {
            try {
                Thread.sleep(2000); // Esperar 2 segundos
                if (currentClienteId != null) {
                    loadHistorialData(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    /**
     * Limpia el error actual
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
    
    // Getters para LiveData
    public LiveData<List<HistorialItem>> getHistorialItems() {
        return historialItemsLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getSyncStatus() {
        return syncStatusLiveData;
    }
    
    public LiveData<Boolean> getNetworkStatus() {
        return networkStatusLiveData;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}