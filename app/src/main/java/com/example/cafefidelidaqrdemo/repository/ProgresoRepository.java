package com.example.cafefidelidaqrdemo.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cafefidelidaqrdemo.database.CafeFidelidadDB;
import com.example.cafefidelidaqrdemo.database.models.Beneficio;
import com.example.cafefidelidaqrdemo.database.models.Visita;
import com.example.cafefidelidaqrdemo.database.models.Canje;
import com.example.cafefidelidaqrdemo.database.models.Cliente;
import com.example.cafefidelidaqrdemo.models.ProgresoGeneral;
import com.example.cafefidelidaqrdemo.models.ProximoBeneficio;
import com.example.cafefidelidaqrdemo.models.SyncStatus;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.repository.base.BaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para manejar operaciones de progreso y beneficios
 */
public class ProgresoRepository extends BaseRepository {
    
    private final CafeFidelidadDB database;
    private final ApiService apiService;
    private final ExecutorService executor;
    private final MutableLiveData<List<Beneficio>> beneficiosLiveData;
    private final MutableLiveData<List<ProximoBeneficio>> proximosBeneficiosLiveData;
    
    public ProgresoRepository(Context context, ApiService apiService) {
        super(2);
        this.database = new CafeFidelidadDB(context);
        this.apiService = apiService;
        this.executor = Executors.newFixedThreadPool(2);
        this.beneficiosLiveData = new MutableLiveData<>();
        this.proximosBeneficiosLiveData = new MutableLiveData<>();
    }
    
    public LiveData<ProgresoGeneral> getProgresoGeneral(String clienteId) {
        MutableLiveData<ProgresoGeneral> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int clienteIdInt = Integer.parseInt(clienteId);
                
                // Obtener información del cliente
                Cliente cliente = database.obtenerClientePorId(clienteIdInt);
                if (cliente == null) {
                    setErrorMessage("Cliente no encontrado");
                    return;
                }
                
                // Calcular estadísticas
                List<Visita> visitas = database.obtenerVisitasPorCliente(clienteIdInt);
                List<Canje> canjes = database.obtenerCanjesPorCliente(clienteIdInt);
                
                ProgresoGeneral progreso = new ProgresoGeneral();
                progreso.setPuntosActuales(cliente.getPuntosAcumulados());
                progreso.setTotalVisitas(visitas.size());
                progreso.setTotalCanjes(canjes.size());
                
                // Calcular puntos ganados este mes (simplificado)
                int puntosEsteMes = 0;
                for (Visita visita : visitas) {
                    puntosEsteMes += visita.getPuntosGanados();
                }
                progreso.setPuntosEsteMes(puntosEsteMes);
                
                // Calcular próximo beneficio disponible
                List<Beneficio> beneficiosActivos = database.obtenerBeneficiosActivos();
                Beneficio proximoBeneficio = null;
                int menorPuntosRequeridos = Integer.MAX_VALUE;
                
                for (Beneficio beneficio : beneficiosActivos) {
                    if (beneficio.getPuntosRequeridos() > cliente.getPuntosAcumulados() && 
                        beneficio.getPuntosRequeridos() < menorPuntosRequeridos) {
                        menorPuntosRequeridos = beneficio.getPuntosRequeridos();
                        proximoBeneficio = beneficio;
                    }
                }
                
                if (proximoBeneficio != null) {
                    progreso.setPuntosParaProximoBeneficio(
                        proximoBeneficio.getPuntosRequeridos() - cliente.getPuntosAcumulados()
                    );
                    progreso.setProximoBeneficioNombre(proximoBeneficio.getNombre());
                }
                
                result.postValue(progreso);
                
            } catch (Exception e) {
                setErrorMessage("Error obteniendo progreso: " + e.getMessage());
                result.postValue(new ProgresoGeneral());
            }
        });
        return result;
    }
     
     public LiveData<List<Beneficio>> getBeneficiosDisponibles(String clienteId) {
         executor.execute(() -> {
             try {
                 int clienteIdInt = Integer.parseInt(clienteId);
                 Cliente cliente = database.obtenerClientePorId(clienteIdInt);
                 
                 if (cliente != null) {
                     List<Beneficio> todosLosBeneficios = database.obtenerBeneficiosActivos();
                     List<Beneficio> beneficiosDisponibles = new ArrayList<>();
                     
                     for (Beneficio beneficio : todosLosBeneficios) {
                         if (beneficio.getPuntosRequeridos() <= cliente.getPuntosAcumulados()) {
                             beneficiosDisponibles.add(beneficio);
                         }
                     }
                     
                     beneficiosLiveData.postValue(beneficiosDisponibles);
                 } else {
                     beneficiosLiveData.postValue(new ArrayList<>());
                 }
                 
             } catch (Exception e) {
                 setErrorMessage("Error obteniendo beneficios disponibles: " + e.getMessage());
                 beneficiosLiveData.postValue(new ArrayList<>());
             }
         });
         
         return beneficiosLiveData;
     }
     
     public LiveData<List<ProximoBeneficio>> getProximosBeneficios(String clienteId) {
         executor.execute(() -> {
             try {
                 int clienteIdInt = Integer.parseInt(clienteId);
                 Cliente cliente = database.obtenerClientePorId(clienteIdInt);
                 
                 if (cliente != null) {
                     List<Beneficio> todosLosBeneficios = database.obtenerBeneficiosActivos();
                     List<ProximoBeneficio> proximosBeneficios = new ArrayList<>();
                     
                     for (Beneficio beneficio : todosLosBeneficios) {
                         if (beneficio.getPuntosRequeridos() > cliente.getPuntosAcumulados()) {
                             ProximoBeneficio proximo = new ProximoBeneficio();
                             proximo.setNombre(beneficio.getNombre());
                             proximo.setDescripcion(beneficio.getDescripcion());
                             proximo.setPuntosRequeridos(beneficio.getPuntosRequeridos());
                             proximo.setPuntosFaltantes(
                                 beneficio.getPuntosRequeridos() - cliente.getPuntosAcumulados()
                             );
                             proximosBeneficios.add(proximo);
                         }
                     }
                     
                     proximosBeneficiosLiveData.postValue(proximosBeneficios);
                 } else {
                     proximosBeneficiosLiveData.postValue(new ArrayList<>());
                 }
                 
             } catch (Exception e) {
                 setErrorMessage("Error obteniendo próximos beneficios: " + e.getMessage());
                 proximosBeneficiosLiveData.postValue(new ArrayList<>());
             }
         });
         
         return proximosBeneficiosLiveData;
     }
     
     public void refreshProgreso(String clienteId, BaseRepository.SimpleCallback callback) {
          setLoading(true);
          executor.execute(() -> {
              try {
                  // Refrescar datos del progreso
                  getProgresoGeneral(clienteId);
                  getBeneficiosDisponibles(clienteId);
                  getProximosBeneficios(clienteId);
                  
                  // Sincronizar con API si está disponible
                  if (apiService != null) {
                      // Aquí se podría implementar la sincronización con el servidor
                  }
                  
                  setLoading(false);
                  if (callback != null) {
                      callback.onSuccess();
                  }
              } catch (Exception e) {
                  setLoading(false);
                  setErrorMessage("Error refrescando progreso: " + e.getMessage());
                  if (callback != null) {
                      callback.onError(e.getMessage());
                  }
              }
          });
      }
     
     // Métodos adicionales requeridos por ProgresoViewModel
     public LiveData<String> getError() {
         return getErrorMessage();
     }
     
     public LiveData<SyncStatus> getSyncStatus() {
         MutableLiveData<SyncStatus> result = new MutableLiveData<>();
         // Implementación básica
         SyncStatus status = new SyncStatus();
         status.setLastSyncTime(System.currentTimeMillis());
         status.setSyncing(false);
         result.setValue(status);
         return result;
     }
     
     public void forceSyncProgreso(String clienteId, BaseRepository.SimpleCallback callback) {
         setLoading(true);
         executor.execute(() -> {
             try {
                 // Forzar sincronización con el servidor
                 if (apiService != null) {
                     // Implementar sincronización forzada con API
                 }
                 
                 // Refrescar datos locales
                 refreshProgreso(clienteId, null);
                 
                 setLoading(false);
                 if (callback != null) {
                     callback.onSuccess();
                 }
             } catch (Exception e) {
                 setLoading(false);
                 setErrorMessage("Error sincronizando progreso: " + e.getMessage());
                 if (callback != null) {
                     callback.onError(e.getMessage());
                 }
             }
         });
     }
     
     public void clearError() {
         setErrorMessage(null);
     }
}