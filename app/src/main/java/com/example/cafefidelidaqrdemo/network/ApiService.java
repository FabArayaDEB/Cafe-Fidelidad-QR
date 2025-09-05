package com.example.cafefidelidaqrdemo.network;

import com.example.cafefidelidaqrdemo.models.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

/**
 * Interfaz para servicios de API REST
 */
public interface ApiService {
    
    // Endpoints para Sucursales
    @GET("sucursales")
    Call<List<Sucursal>> getSucursales();
    
    @GET("sucursales/{id}")
    Call<Sucursal> getSucursalById(@Path("id") Long id);
    
    @POST("sucursales")
    Call<Sucursal> createSucursal(@Body Sucursal sucursal);
    
    @PUT("sucursales/{id}")
    Call<Sucursal> updateSucursal(@Path("id") Long id, @Body Sucursal sucursal);
    
    @DELETE("sucursales/{id}")
    Call<Void> deleteSucursal(@Path("id") Long id);
    
    // Endpoints para Productos
    @GET("productos")
    Call<List<Producto>> getProductos();
    
    @GET("productos/{id}")
    Call<Producto> getProductoById(@Path("id") String id);
    
    @POST("productos")
    Call<Producto> createProducto(@Body Producto producto);
    
    @PUT("productos/{id}")
    Call<Producto> updateProducto(@Path("id") String id, @Body Producto producto);
    
    @DELETE("productos/{id}")
    Call<Void> deleteProducto(@Path("id") String id);
    
    // Endpoints para Clientes
    @GET("clientes")
    Call<List<Cliente>> getClientes();
    
    @GET("clientes/{id}")
    Call<Cliente> getClienteById(@Path("id") String id);
    
    @POST("clientes")
    Call<Cliente> createCliente(@Body Cliente cliente);
    
    @PUT("clientes/{id}")
    Call<Cliente> updateCliente(@Path("id") String id, @Body Cliente cliente);
    
    @DELETE("clientes/{id}")
    Call<Void> deleteCliente(@Path("id") String id);
    
    // Endpoints para Visitas
    @GET("visitas")
    Call<List<Visita>> getVisitas();
    
    @GET("visitas/{id}")
    Call<Visita> getVisitaById(@Path("id") String id);
    
    @POST("visitas")
    Call<Visita> createVisita(@Body Visita visita);
    
    @PUT("visitas/{id}")
    Call<Visita> updateVisita(@Path("id") String id, @Body Visita visita);
    
    @DELETE("visitas/{id}")
    Call<Void> deleteVisita(@Path("id") String id);
    
    @GET("visitas/cliente/{clienteId}")
    Call<List<Visita>> getVisitasByCliente(@Path("clienteId") String clienteId);
    
    // Endpoints para Beneficios
    @GET("beneficios")
    Call<List<Beneficio>> getBeneficios();
    
    @GET("beneficios/{id}")
    Call<Beneficio> getBeneficioById(@Path("id") String id);
    
    @POST("beneficios")
    Call<Beneficio> createBeneficio(@Body Beneficio beneficio);
    
    @PUT("beneficios/{id}")
    Call<Beneficio> updateBeneficio(@Path("id") String id, @Body Beneficio beneficio);
    
    @DELETE("beneficios/{id}")
    Call<Void> deleteBeneficio(@Path("id") String id);
    
    @GET("beneficios/sucursal/{sucursalId}")
    Call<List<Beneficio>> getBeneficiosBySucursal(@Path("sucursalId") String sucursalId);
    
    // Endpoints para Canjes
    @GET("canjes")
    Call<List<Canje>> getCanjes();
    
    @GET("canjes/{id}")
    Call<Canje> getCanjeById(@Path("id") String id);
    
    @POST("canjes")
    Call<Canje> createCanje(@Body Canje canje);
    
    @PUT("canjes/{id}")
    Call<Canje> updateCanje(@Path("id") String id, @Body Canje canje);
    
    @DELETE("canjes/{id}")
    Call<Void> deleteCanje(@Path("id") String id);
    
    @GET("canjes/cliente/{clienteId}")
    Call<List<Canje>> getCanjesByCliente(@Path("clienteId") String clienteId);
    
    // Endpoints para Reglas
    @GET("reglas")
    Call<List<Regla>> getReglas();
    
    @GET("reglas/{id}")
    Call<Regla> getReglaById(@Path("id") String id);
    
    @POST("reglas")
    Call<Regla> createRegla(@Body Regla regla);
    
    @PUT("reglas/{id}")
    Call<Regla> updateRegla(@Path("id") String id, @Body Regla regla);
    
    @DELETE("reglas/{id}")
    Call<Void> deleteRegla(@Path("id") String id);
    
    // Endpoints para sincronización
    @POST("sync/batch")
    Call<SyncResponse> syncBatch(@Body SyncRequest request);
    
    @GET("sync/status")
    Call<SyncStatus> getSyncStatus();
    
    // Clase para respuesta de sincronización
    class SyncResponse {
        private boolean success;
        private String message;
        private int processedItems;
        private int errorItems;
        
        // Getters y setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getProcessedItems() { return processedItems; }
        public void setProcessedItems(int processedItems) { this.processedItems = processedItems; }
        
        public int getErrorItems() { return errorItems; }
        public void setErrorItems(int errorItems) { this.errorItems = errorItems; }
    }
    
    // Clase para request de sincronización
    class SyncRequest {
        private List<Object> items;
        private String type;
        private long timestamp;
        
        // Getters y setters
        public List<Object> getItems() { return items; }
        public void setItems(List<Object> items) { this.items = items; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    // Clase para estado de sincronización
    class SyncStatus {
        private boolean serverAvailable;
        private long lastSync;
        private int pendingItems;
        
        // Getters y setters
        public boolean isServerAvailable() { return serverAvailable; }
        public void setServerAvailable(boolean serverAvailable) { this.serverAvailable = serverAvailable; }
        
        public long getLastSync() { return lastSync; }
        public void setLastSync(long lastSync) { this.lastSync = lastSync; }
        
        public int getPendingItems() { return pendingItems; }
        public void setPendingItems(int pendingItems) { this.pendingItems = pendingItems; }
    }
    
    // Método estático para obtener instancia (implementado en ApiClient)
    static ApiService getInstance() {
        return ApiClient.getApiService();
    }
}