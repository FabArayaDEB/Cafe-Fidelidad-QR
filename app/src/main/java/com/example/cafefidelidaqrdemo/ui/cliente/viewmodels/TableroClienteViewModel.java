package com.example.cafefidelidaqrdemo.ui.cliente.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.cafefidelidaqrdemo.database.entities.TableroEntity;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.repository.TableroRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para el tablero personal del cliente
 * Maneja KPIs, cache offline y sincronización
 */
public class TableroClienteViewModel extends AndroidViewModel {
    
    private final TableroRepository tableroRepository;
    private final ExecutorService executorService;
    
    // Estados de la UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOfflineMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> dataSource = new MutableLiveData<>();
    
    // Datos del tablero
    private final MutableLiveData<TableroEntity> tableroCliente = new MutableLiveData<>();
    private final MutableLiveData<List<CanjeReciente>> canjesRecientes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<VisitaReciente>> visitasRecientes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BeneficioRecomendado>> beneficiosRecomendados = new MutableLiveData<>(new ArrayList<>());
    
    // Estado de sincronización
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<Date> ultimaActualizacion = new MutableLiveData<>();
    private final MutableLiveData<String> estadoConexion = new MutableLiveData<>("desconocido");
    
    // Estado de acciones
    private final MutableLiveData<Boolean> isProcessingAction = new MutableLiveData<>(false);
    private final MutableLiveData<String> actionResult = new MutableLiveData<>();
    
    private String clienteIdActual;
    private Timer autoRefreshTimer;
    
    public TableroClienteViewModel(@NonNull Application application) {
        super(application);
        this.tableroRepository = new TableroRepository(application);
        this.executorService = Executors.newFixedThreadPool(2);
        
        // Configurar auto-refresh cada 5 minutos
        setupAutoRefresh();
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getIsOfflineMode() { return isOfflineMode; }
    public LiveData<String> getDataSource() { return dataSource; }
    
    public LiveData<TableroEntity> getTableroCliente() { return tableroCliente; }
    public LiveData<List<CanjeReciente>> getCanjesRecientes() { return canjesRecientes; }
    public LiveData<List<VisitaReciente>> getVisitasRecientes() { return visitasRecientes; }
    public LiveData<List<BeneficioRecomendado>> getBeneficiosRecomendados() { return beneficiosRecomendados; }
    
    public LiveData<Boolean> getIsSyncing() { return isSyncing; }
    public LiveData<Date> getUltimaActualizacion() { return ultimaActualizacion; }
    public LiveData<String> getEstadoConexion() { return estadoConexion; }
    
    public LiveData<Boolean> getIsProcessingAction() { return isProcessingAction; }
    public LiveData<String> getActionResult() { return actionResult; }
    
    /**
     * Datos del cliente extraídos del tablero (para compatibilidad)
     */
    public LiveData<ClienteEntity> getClienteData() {
        MutableLiveData<ClienteEntity> clienteDataLiveData = new MutableLiveData<>();
        TableroEntity tablero = tableroCliente.getValue();
        if (tablero != null) {
            // Crear ClienteEntity desde TableroEntity
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId_cliente(tablero.getClienteId());
            cliente.setNombre(tablero.getNombreCliente());
            // Los puntos y nivel se calculan automáticamente en ClienteEntity
              // cliente.setPuntos(tablero.getPuntosDisponibles());
              // cliente.setNivelFidelidad(tablero.getNivelFidelidad());
            clienteDataLiveData.setValue(cliente);
        }
        return clienteDataLiveData;
    }
    
    // Método para obtener KPIs del cliente
    public LiveData<KPIsCliente> getKPIsCliente() {
        MutableLiveData<KPIsCliente> kpisLiveData = new MutableLiveData<>();
        TableroEntity tablero = tableroCliente.getValue();
        if (tablero != null) {
            KPIsCliente kpis = new KPIsCliente(
                tablero.getTotalVisitas(),
                tablero.getPuntosDisponibles(),
                tablero.getBeneficiosDisponibles(),
                tablero.getTotalCanjes(),
                tablero.getProgresoMetaVisitas(),
                tablero.getNivelFidelidad()
            );
            kpisLiveData.setValue(kpis);
        }
        return kpisLiveData;
    }
    
    // Métodos principales
    public void cargarTableroCliente(String clienteId) {
        if (clienteId == null || clienteId.isEmpty()) {
            errorMessage.setValue("ID de cliente requerido");
            return;
        }
        
        this.clienteIdActual = clienteId;
        
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // Primero cargar desde cache para respuesta rápida
                // TableroEntity tableroCache = tableroRepository.obtenerTableroCache(clienteId);
                // if (tableroCache != null) {
                //     tableroCliente.postValue(tableroCache);
                //     dataSource.postValue("cache");
                //     ultimaActualizacion.postValue(tableroCache.getUltimaActualizacion());
                // }
                
                // Luego intentar actualizar desde API
                sincronizarConServidor(clienteId, false);
                
                // Cargar datos adicionales
                cargarCanjesRecientes(clienteId);
                cargarVisitasRecientes(clienteId);
                cargarBeneficiosRecomendados(clienteId);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar tablero: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    public void refrescarTablero(String clienteId) {
        if (clienteId == null || clienteId.isEmpty()) {
            clienteId = this.clienteIdActual;
        }
        
        if (clienteId == null) {
            errorMessage.setValue("No hay cliente seleccionado");
            return;
        }

        String finalClienteId = clienteId;
        executorService.execute(() -> {
            try {
                isSyncing.postValue(true);
                sincronizarConServidor(finalClienteId, true);

                // Refrescar datos adicionales
                cargarCanjesRecientes(finalClienteId);
                cargarVisitasRecientes(finalClienteId);
                cargarBeneficiosRecomendados(finalClienteId);

                successMessage.postValue("Tablero actualizado");

            } catch (Exception e) {
                errorMessage.postValue("Error al refrescar: " + e.getMessage());
                // Mantener datos de cache en caso de error
                estadoConexion.postValue("error");
                isOfflineMode.postValue(true);
            } finally {
                isSyncing.postValue(false);
            }
        });
    }
    
    public void canjearBeneficioRecomendado() {
        TableroEntity tablero = tableroCliente.getValue();
        if (tablero == null || tablero.getBeneficioRecomendadoId() == null) {
            errorMessage.setValue("No hay beneficio recomendado disponible");
            return;
        }
        
        if (!tablero.puedeCanjeaBeneficioRecomendado()) {
            errorMessage.setValue("Puntos insuficientes para el canje");
            return;
        }
        
        executorService.execute(() -> {
            try {
                isProcessingAction.postValue(true);
                
                // boolean exito = tableroRepository.canjearBeneficio(
                //     clienteIdActual, 
                //     tablero.getBeneficioRecomendadoId(),
                //     tablero.getBeneficioRecomendadoPuntos()
                // );
                
                // if (exito) {
                //     successMessage.postValue("Beneficio canjeado exitosamente");
                //     actionResult.postValue("canje_exitoso");
                //     
                //     // Refrescar tablero después del canje
                //     refrescarTablero(clienteIdActual);
                // } else {
                //     errorMessage.postValue("Error al procesar el canje");
                //     actionResult.postValue("canje_error");
                // }
                
                // Simulación temporal de canje exitoso
                successMessage.postValue("Beneficio canjeado exitosamente");
                actionResult.postValue("canje_exitoso");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al canjear beneficio: " + e.getMessage());
                actionResult.postValue("canje_error");
            } finally {
                isProcessingAction.postValue(false);
            }
        });
    }
    
    public void marcarVisita(String sucursalId) {
        if (clienteIdActual == null || sucursalId == null) {
            errorMessage.setValue("Datos insuficientes para marcar visita");
            return;
        }
        
        executorService.execute(() -> {
            try {
                isProcessingAction.postValue(true);
                
                // boolean exito = tableroRepository.registrarVisita(clienteIdActual, sucursalId);
                
                // if (exito) {
                //     successMessage.postValue("Visita registrada");
                //     actionResult.postValue("visita_registrada");
                //     
                //     // Refrescar tablero después de la visita
                //     refrescarTablero(clienteIdActual);
                // } else {
                //     errorMessage.postValue("Error al registrar visita");
                //     actionResult.postValue("visita_error");
                // }
                
                // Simulación temporal de visita registrada
                successMessage.postValue("Visita registrada");
                actionResult.postValue("visita_registrada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al marcar visita: " + e.getMessage());
                actionResult.postValue("visita_error");
            } finally {
                isProcessingAction.postValue(false);
            }
        });
    }
    
    public void actualizarMetaVisitas(int nuevaMeta) {
        if (clienteIdActual == null || nuevaMeta <= 0) {
            errorMessage.setValue("Meta inválida");
            return;
        }
        
        executorService.execute(() -> {
            try {
                // boolean exito = tableroRepository.actualizarMetaVisitas(clienteIdActual, nuevaMeta);
                
                // if (exito) {
                //     successMessage.postValue("Meta actualizada");
                //     refrescarTablero(clienteIdActual);
                // } else {
                //     errorMessage.postValue("Error al actualizar meta");
                // }
                
                // Simulación temporal de meta actualizada
                successMessage.postValue("Meta actualizada");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al actualizar meta: " + e.getMessage());
            }
        });
    }
    
    // Métodos auxiliares
    private void sincronizarConServidor(String clienteId, boolean forzarActualizacion) {
        try {
            // TableroEntity tableroActualizado = tableroRepository.sincronizarTablero(clienteId, forzarActualizacion);
            
            // Simulación temporal de sincronización
            TableroEntity tableroActualizado = null;
            
            if (tableroActualizado != null) {
                tableroCliente.postValue(tableroActualizado);
                dataSource.postValue("api");
                ultimaActualizacion.postValue(new Date());
                estadoConexion.postValue("conectado");
                isOfflineMode.postValue(false);
            } else {
                // Mantener datos de cache
                dataSource.postValue("cache");
                estadoConexion.postValue("sin_conexion");
                isOfflineMode.postValue(true);
            }
            
        } catch (Exception e) {
            // En caso de error, mantener datos de cache
            estadoConexion.postValue("error");
            isOfflineMode.postValue(true);
            throw e;
        }
    }
    
    private void cargarCanjesRecientes(String clienteId) {
        try {
            // List<CanjeReciente> canjes = tableroRepository.obtenerCanjesRecientes(clienteId, 5);
            // canjesRecientes.postValue(canjes);
            
            // Simulación temporal de canjes recientes
            canjesRecientes.postValue(new ArrayList<>());
        } catch (Exception e) {
            // Error silencioso para datos secundarios
        }
    }
    
    private void cargarVisitasRecientes(String clienteId) {
        try {
            // List<VisitaReciente> visitas = tableroRepository.obtenerVisitasRecientes(clienteId, 5);
            // visitasRecientes.postValue(visitas);
            
            // Simulación temporal de visitas recientes
            visitasRecientes.postValue(new ArrayList<>());
        } catch (Exception e) {
            // Error silencioso para datos secundarios
        }
    }
    
    private void cargarBeneficiosRecomendados(String clienteId) {
        try {
            // List<BeneficioRecomendado> beneficios = tableroRepository.obtenerBeneficiosRecomendados(clienteId, 3);
            // beneficiosRecomendados.postValue(beneficios);
            
            // Simulación temporal de beneficios recomendados
            beneficiosRecomendados.postValue(new ArrayList<>());
        } catch (Exception e) {
            // Error silencioso para datos secundarios
        }
    }
    
    /**
     * Carga las transacciones del cliente
     */
    public void loadTransacciones() {
        if (clienteIdActual == null) {
            errorMessage.setValue("ID de cliente requerido");
            return;
        }
        
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // TODO: Implementar carga de transacciones desde TransaccionRepository
                // Por ahora simulamos una carga exitosa
                successMessage.postValue("Transacciones cargadas");
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar transacciones: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    private void setupAutoRefresh() {
        autoRefreshTimer = new Timer();
        autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (clienteIdActual != null && !Boolean.TRUE.equals(isLoading.getValue())) {
                    // Auto-refresh silencioso cada 5 minutos
                    executorService.execute(() -> {
                        try {
                            sincronizarConServidor(clienteIdActual, false);
                        } catch (Exception e) {
                            // Error silencioso para auto-refresh
                        }
                    });
                }
            }
        }, 5 * 60 * 1000, 5 * 60 * 1000); // 5 minutos
    }
    
    public void limpiarCache() {
        executorService.execute(() -> {
            try {
                // tableroRepository.limpiarCache(clienteIdActual);
                
                // Simulación temporal de limpieza de cache
                successMessage.postValue("Cache limpiado");
                
                if (clienteIdActual != null) {
                    cargarTableroCliente(clienteIdActual);
                }
            } catch (Exception e) {
                errorMessage.postValue("Error al limpiar cache: " + e.getMessage());
            }
        });
    }
    
    public void cleanup() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (tableroRepository != null) {
            tableroRepository.cleanup();
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        cleanup();
    }
    
    // Clases auxiliares
    public static class CanjeReciente {
        public String canjeId;
        public String beneficio;
        public int puntosUsados;
        public double valor;
        public Date fecha;
        public String sucursal;
        public String estado;
        
        public CanjeReciente(String canjeId, String beneficio, int puntosUsados, double valor, Date fecha, String sucursal, String estado) {
            this.canjeId = canjeId;
            this.beneficio = beneficio;
            this.puntosUsados = puntosUsados;
            this.valor = valor;
            this.fecha = fecha;
            this.sucursal = sucursal;
            this.estado = estado;
        }
        
        public boolean esCanjeValido() {
            return "completado".equals(estado) || "activo".equals(estado);
        }
        
        public String getDescripcionCompleta() {
            return String.format("%s - %d puntos ($%.2f)", beneficio, puntosUsados, valor);
        }
    }
    
    public static class VisitaReciente {
        public String visitaId;
        public String sucursal;
        public Date fecha;
        public int puntosGanados;
        public String tipoVisita;
        public boolean validada;
        
        public VisitaReciente(String visitaId, String sucursal, Date fecha, int puntosGanados, String tipoVisita, boolean validada) {
            this.visitaId = visitaId;
            this.sucursal = sucursal;
            this.fecha = fecha;
            this.puntosGanados = puntosGanados;
            this.tipoVisita = tipoVisita;
            this.validada = validada;
        }
        
        public String getDescripcionCompleta() {
            return String.format("%s - %s (+%d puntos)", sucursal, tipoVisita, puntosGanados);
        }
    }
    
    public static class BeneficioRecomendado {
        public String beneficioId;
        public String nombre;
        public String descripcion;
        public int puntosRequeridos;
        public double valor;
        public String categoria;
        public boolean disponible;
        public Date validoHasta;
        public String razonRecomendacion;
        
        public BeneficioRecomendado(String beneficioId, String nombre, String descripcion, int puntosRequeridos, double valor, String categoria, boolean disponible, Date validoHasta, String razonRecomendacion) {
            this.beneficioId = beneficioId;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.puntosRequeridos = puntosRequeridos;
            this.valor = valor;
            this.categoria = categoria;
            this.disponible = disponible;
            this.validoHasta = validoHasta;
            this.razonRecomendacion = razonRecomendacion;
        }
        
        public boolean esCanjeableConPuntos(int puntosDisponibles) {
            return disponible && puntosDisponibles >= puntosRequeridos;
        }
        
        public boolean estaVigente() {
            return validoHasta == null || validoHasta.after(new Date());
        }
        
        public String getDescripcionCompleta() {
            return String.format("%s - %d puntos ($%.2f)", nombre, puntosRequeridos, valor);
        }
    }
    
    public static class KPIsCliente {
        private int totalVisitas;
        private int puntosActuales;
        private int beneficiosDisponibles;
        private int totalCanjes;
        private double progresoMeta;
        private String nivelFidelidad;
        
        public KPIsCliente(int totalVisitas, int puntosActuales, int beneficiosDisponibles, 
                          int totalCanjes, double progresoMeta, String nivelFidelidad) {
            this.totalVisitas = totalVisitas;
            this.puntosActuales = puntosActuales;
            this.beneficiosDisponibles = beneficiosDisponibles;
            this.totalCanjes = totalCanjes;
            this.progresoMeta = progresoMeta;
            this.nivelFidelidad = nivelFidelidad;
        }
        
        public int getTotalVisitas() { return totalVisitas; }
        public int getPuntosActuales() { return puntosActuales; }
        public int getBeneficiosDisponibles() { return beneficiosDisponibles; }
        public int getTotalCanjes() { return totalCanjes; }
        public double getProgresoMeta() { return progresoMeta; }
        public String getNivelFidelidad() { return nivelFidelidad; }
    }
}