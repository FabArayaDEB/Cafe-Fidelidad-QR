package com.example.cafefidelidaqrdemo.network;

import com.example.cafefidelidaqrdemo.models.*;
import com.example.cafefidelidaqrdemo.database.entities.*;
import com.example.cafefidelidaqrdemo.network.response.VisitaResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interfaz de servicios API simplificada
 * Define los endpoints básicos para el funcionamiento del sistema
 */
public interface ApiService {
    
    /**
     * Método estático para obtener instancia (compatibilidad)
     */
    static ApiService getInstance() {
        return ApiClient.getApiService();
    }
    
    // AUTENTICACIÓN
    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credentials);
    
    @POST("auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> userData);
    
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String token);
    
    // CLIENTES
    @GET("clientes/{id}")
    Call<ClienteEntity> getCliente(@Path("id") String clienteId);
    
    @GET("clientes/{id}")
    Call<Cliente> getClienteById(@Path("id") String clienteId);
    
    @POST("clientes")
    Call<Cliente> createCliente(@Body Cliente cliente);
    
    @PUT("clientes/{id}")
    Call<ClienteEntity> updateCliente(@Path("id") String clienteId, @Body ClienteEntity cliente);
    
    @GET("clientes/{id}/puntos")
    Call<Map<String, Object>> getPuntosCliente(@Path("id") String clienteId);
    
    //PRODUCTOS
    @GET("productos")
    Call<List<ProductoEntity>> getProductos();
    
    @GET("productos/{id}")
    Call<ProductoEntity> getProductoById(@Path("id") String id);
    
    @POST("productos")
    Call<ProductoEntity> createProducto(@Body ProductoEntity producto);
    
    @PUT("productos/{id}")
    Call<ProductoEntity> updateProducto(@Path("id") Long id, @Body ProductoEntity producto);
    
    @DELETE("productos/{id}")
    Call<Void> deleteProducto(@Path("id") Long id);
    
    //BENEFICIOS
    @GET("beneficios")
    Call<List<BeneficioEntity>> getBeneficios();
    
    @POST("beneficios")
    Call<BeneficioEntity> createBeneficio(@Body BeneficioEntity beneficio);
    
    @PUT("beneficios/{id}")
    Call<BeneficioEntity> updateBeneficio(@Path("id") Long id, @Body BeneficioEntity beneficio);
    
    @DELETE("beneficios/{id}")
    Call<Void> deleteBeneficio(@Path("id") Long id);
    
    //SUCURSALES
    @GET("sucursales")
    Call<List<SucursalEntity>> getSucursales();
    
    @POST("sucursales")
    Call<SucursalEntity> createSucursal(@Body SucursalEntity sucursal);
    
    @PUT("sucursales/{id}")
    Call<SucursalEntity> updateSucursal(@Path("id") Long id, @Body SucursalEntity sucursal);
    
    @DELETE("sucursales/{id}")
    Call<Void> deleteSucursal(@Path("id") Long id);
    
    @GET("sucursales/{id}")
    Call<Sucursal> getSucursalById(@Path("id") Long id);
    
    // TRANSACCIONES
    @POST("transacciones")
    Call<TransaccionEntity> createTransaccion(@Body TransaccionEntity transaccion);
    
    @GET("transacciones/cliente/{clienteId}")
    Call<List<TransaccionEntity>> getTransaccionesCliente(@Path("clienteId") String clienteId);
    
    @GET("transacciones/historial")
    Call<List<TransaccionEntity>> getHistorialTransacciones();
    
    // REPORTES
    @GET("reportes/ventas")
    Call<Map<String, Object>> getReporteVentas(@Query("fechaInicio") String fechaInicio, @Query("fechaFin") String fechaFin);
    
    @GET("reportes/clientes")
    Call<Map<String, Object>> getReporteClientes();
    
    @GET("reportes/productos")
    Call<Map<String, Object>> getReporteProductos();
    
    // VISITAS
    @POST("visitas")
    Call<Visita> createVisita(@Body Visita visita);
    
    @POST("visitas/registrar")
    Call<VisitaResponse> registrarVisita(@Body VisitaRequest request);
    
    @GET("visitas/cliente/{clienteId}")
    Call<List<Visita>> getVisitasCliente(@Path("clienteId") String clienteId);
    
    /**
     * Clase para request de visitas
     */
    class VisitaRequest {
        private String clienteId;
        private Long sucursalId;
        private String fecha;
        private int puntos;
        
        public VisitaRequest(String clienteId, Long sucursalId, String fecha, int puntos) {
            this.clienteId = clienteId;
            this.sucursalId = sucursalId;
            this.fecha = fecha;
            this.puntos = puntos;
        }
        
        // Getters y setters
        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }
        
        public Long getSucursalId() { return sucursalId; }
        public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
        
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        
        public int getPuntos() { return puntos; }
        public void setPuntos(int puntos) { this.puntos = puntos; }
    }
}