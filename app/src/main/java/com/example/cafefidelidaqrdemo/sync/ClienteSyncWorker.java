package com.example.cafefidelidaqrdemo.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.cafefidelidaqrdemo.database.CafeFidelidadDatabase;
import com.example.cafefidelidaqrdemo.database.dao.ClienteDao;
import com.example.cafefidelidaqrdemo.database.entities.ClienteEntity;
import com.example.cafefidelidaqrdemo.models.Cliente;
import com.example.cafefidelidaqrdemo.network.ApiService;
import com.example.cafefidelidaqrdemo.utils.NetworkUtils;
import java.util.List;
import retrofit2.Response;

/**
 * Worker para sincronización de clientes en segundo plano
 */
public class ClienteSyncWorker extends Worker {
    
    private final ClienteDao clienteDao;
    private final ApiService apiService;
    
    public ClienteSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        CafeFidelidadDatabase database = CafeFidelidadDatabase.getInstance(context);
        this.clienteDao = database.clienteDao();
        this.apiService = ApiService.getInstance();
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            // Verificar conectividad
            if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                return Result.retry();
            }
            
            // Obtener clientes pendientes de sincronización
            List<ClienteEntity> pendingClientes = clienteDao.getPendientesSync();
            
            if (pendingClientes.isEmpty()) {
                return Result.success();
            }
            
            int successCount = 0;
            int totalCount = pendingClientes.size();
            
            for (ClienteEntity cliente : pendingClientes) {
                try {
                    // Convertir a modelo para API
                    Cliente clienteModel = convertToModel(cliente);
                    
                    // Enviar a API
                    Response<Cliente> response = apiService.updateCliente(cliente.getId_cliente(), clienteModel).execute();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Cliente syncedCliente = response.body();
                        
                        // Actualizar estado de sincronización
                        cliente.setSynced(true);
                        cliente.setNeedsSync(false);
                        cliente.setLastSync(System.currentTimeMillis());
                        
                        // Actualizar en base de datos local
                        clienteDao.update(cliente);
                    }
                    
                    successCount++;
                    
                } catch (Exception e) {
                    // Log del error pero continuar con otros clientes
                    android.util.Log.e("ClienteSyncWorker", "Error sincronizando cliente " + cliente.getId_cliente(), e);
                }
            }
            
            // Determinar resultado basado en éxito
            if (successCount == totalCount) {
                return Result.success();
            } else if (successCount > 0) {
                // Sincronización parcial - reintentar los fallidos
                return Result.retry();
            } else {
                // Falló completamente - reintentar más tarde
                return Result.retry();
            }
            
        } catch (Exception e) {
            android.util.Log.e("ClienteSyncWorker", "Error general en sincronización", e);
            return Result.retry();
        }
    }
    
    /**
     * Convierte ClienteEntity a Cliente (modelo para API)
     */
    private Cliente convertToModel(ClienteEntity entity) {
        Cliente cliente = new Cliente();
        cliente.setId(entity.getId_cliente());
        cliente.setNombre(entity.getNombre());
        cliente.setEmail(entity.getEmail());
        cliente.setTelefono(entity.getTelefono());
        cliente.setFechaNacimiento(entity.getFecha_nac());
        cliente.setEstado(entity.getEstado());
        cliente.setFechaCreacion(entity.getCreado_en());
        return cliente;
    }
}