package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.database.models.Canje;
import com.example.cafefidelidaqrdemo.database.models.Visita;
import com.example.cafefidelidaqrdemo.models.HistorialItem;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;
import com.example.cafefidelidaqrdemo.viewmodels.HistorialViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para gestión del historial de visitas y canjes
 */
public class HistorialRepository extends BaseRepository {
    
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final MutableLiveData<List<HistorialItem>> historialLiveData;
    
    public HistorialRepository(Context context, ApiService apiService) {
        super(2); // Usar constructor de BaseRepository con 2 threads
        this.database = new CafeFidelidadDB(context);
        this.apiService = apiService;
        this.executor = Executors.newFixedThreadPool(2);
        this.historialLiveData = new MutableLiveData<>();
    }
    
    public LiveData<List<HistorialItem>> getHistorialItems(
            String clienteId,
            HistorialViewModel.FiltroTipo filtroTipo,
            HistorialViewModel.FiltroEstado filtroEstado,
            Date fechaInicio,
            Date fechaFin,
            int page,
            int pageSize) {
        
        executor.execute(() -> {
            try {
                List<HistorialItem> historialItems = new ArrayList<>();
                
                // Obtener visitas del cliente
                if (filtroTipo == null || filtroTipo == HistorialViewModel.FiltroTipo.VISITAS) {
                    List<Visita> visitas = database.obtenerVisitasPorCliente(Integer.parseInt(clienteId));
                    for (Visita visita : visitas) {
                        // Convertir Visita a HistorialItem
                        HistorialItem item = new HistorialItem();
                        item.setTipo("VISITA");
                        item.setFecha(visita.getFechaVisita());
                        item.setPuntos(visita.getPuntosGanados());
                        // Agregar más campos según sea necesario
                        historialItems.add(item);
                    }
                }
                
                // Obtener canjes del cliente
                if (filtroTipo == null || filtroTipo == HistorialViewModel.FiltroTipo.CANJES) {
                    List<Canje> canjes = database.obtenerCanjesPorCliente(Integer.parseInt(clienteId));
                    for (Canje canje : canjes) {
                        // Convertir Canje a HistorialItem
                        HistorialItem item = new HistorialItem();
                        item.setTipo("CANJE");
                        item.setFecha(canje.getFechaCanje());
                        item.setPuntos(-canje.getPuntosUtilizados()); // Negativo porque son puntos gastados
                        item.setEstado(canje.getEstado());
                        // Agregar más campos según sea necesario
                        historialItems.add(item);
                    }
                }
                
                // Aplicar filtros de fecha si están definidos
                if (fechaInicio != null || fechaFin != null) {
                    historialItems = filtrarPorFecha(historialItems, fechaInicio, fechaFin);
                }
                
                // Aplicar paginación
                int startIndex = page * pageSize;
                int endIndex = Math.min(startIndex + pageSize, historialItems.size());
                if (startIndex < historialItems.size()) {
                    historialItems = historialItems.subList(startIndex, endIndex);
                } else {
                    historialItems = new ArrayList<>();
                }
                
                historialLiveData.postValue(historialItems);
                
            } catch (Exception e) {
                setErrorMessage("Error al cargar historial: " + e.getMessage());
                historialLiveData.postValue(new ArrayList<>());
            }
        });
        
        return historialLiveData;
    }
    
    private List<HistorialItem> filtrarPorFecha(List<HistorialItem> items, Date fechaInicio, Date fechaFin) {
        List<HistorialItem> itemsFiltrados = new ArrayList<>();
        for (HistorialItem item : items) {
            Date fechaItem = item.getFecha();
            if (fechaItem != null) {
                boolean incluir = true;
                if (fechaInicio != null && fechaItem.before(fechaInicio)) {
                    incluir = false;
                }
                if (fechaFin != null && fechaItem.after(fechaFin)) {
                    incluir = false;
                }
                if (incluir) {
                    itemsFiltrados.add(item);
                }
            }
        }
        return itemsFiltrados;
    }
    
    public void refreshHistorial(String clienteId, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                // Sincronizar datos con API si está disponible
                if (apiService != null) {
                    // Aquí se podría implementar la sincronización con el servidor
                    // Por ahora solo refrescamos los datos locales
                }
                
                // Recargar datos locales
                getHistorialItems(clienteId, null, null, null, null, 0, 50);
                callback.onSuccess();
                
            } catch (Exception e) {
                setErrorMessage("Error al refrescar historial: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        });
    }
    
    public void syncPendingData(SimpleCallback callback) {
        executor.execute(() -> {
            try {
                // Implementación básica de sincronización
                // Aquí se podrían sincronizar datos pendientes con el servidor
                callback.onSuccess();
            } catch (Exception e) {
                setErrorMessage("Error en sincronización: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        });
    }
    
    public LiveData<String> getError() {
        return getErrorMessage();
    }
    
    public void clearError() {
        setErrorMessage(null);
    }
}