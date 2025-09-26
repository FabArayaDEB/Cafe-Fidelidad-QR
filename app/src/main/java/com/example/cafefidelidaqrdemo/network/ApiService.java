package com.example.cafefidelidaqrdemo.network;

import com.example.cafefidelidaqrdemo.models.*;
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
    
    // ========== AUTENTICACIÓN ==========
    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credentials);
    
    @POST("auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> userData);
    
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String token);
    
    // ========== CLIENTES ==========
    @GET("clientes/{id}")
    Call<Cliente> getCliente(@Path("id") String clienteId);
    
    @GET("clientes/{id}")
    Call<Cliente> getClienteById(@Path("id") String clienteId);
    
    @POST("clientes")
    Call<Cliente> createCliente(@Body Cliente cliente);
    
    @PUT("clientes/{id}")
    Call<Cliente> updateCliente(@Path("id") String clienteId, @Body Cliente cliente);
    
    @GET("clientes/{id}/puntos")
    Call<Map<String, Object>> getPuntosCliente(@Path("id") String clienteId);
    
    // ========== PRODUCTOS ==========
    @GET("productos")
    Call<List<Producto>> getProductos();
    
    @GET("productos/{id}")
    Call<Producto> getProductoById(@Path("id") String id);
    
    @POST("productos")
    Call<Producto> createProducto(@Body Producto producto);
    
    @PUT("productos/{id}")
    Call<Producto> updateProducto(@Path("id") Long id, @Body Producto producto);
    
    @DELETE("productos/{id}")
    Call<Void> deleteProducto(@Path("id") Long id);
    
    // ========== BENEFICIOS ==========
    @GET("beneficios")
    Call<List<Beneficio>> getBeneficios();
    
    @POST("beneficios")
    Call<Beneficio> createBeneficio(@Body Beneficio beneficio);
    
    @PUT("beneficios/{id}")
    Call<Beneficio> updateBeneficio(@Path("id") Long id, @Body Beneficio beneficio);
    
    @DELETE("beneficios/{id}")
    Call<Void> deleteBeneficio(@Path("id") Long id);
    
    // ========== SUCURSALES ==========
    @GET("sucursales")
    Call<List<Sucursal>> getSucursales();
    
    @POST("sucursales")
    Call<Sucursal> createSucursal(@Body Sucursal sucursal);
    
    @PUT("sucursales/{id}")
    Call<Sucursal> updateSucursal(@Path("id") Long id, @Body Sucursal sucursal);
    
    @DELETE("sucursales/{id}")
    Call<Void> deleteSucursal(@Path("id") Long id);
    
    @GET("sucursales/{id}")
    Call<Sucursal> getSucursalById(@Path("id") Long id);
    
    // ========== CANJES ==========
    @POST("canjes")
    Call<Canje> createCanje(@Body Canje canje);
    
    @GET("canjes/cliente/{clienteId}")
    Call<List<Canje>> getCanjesCliente(@Path("clienteId") String clienteId);
    
    @GET("canjes/historial")
    Call<List<Canje>> getHistorialCanjes();
    
    // ========== REPORTES ==========
    @GET("reportes/ventas")
    Call<Map<String, Object>> getReporteVentas(@Query("fechaInicio") String fechaInicio, @Query("fechaFin") String fechaFin);
    
    @GET("reportes/clientes")
    Call<Map<String, Object>> getReporteClientes();
    
    @GET("reportes/productos")
    Call<Map<String, Object>> getReporteProductos();
    
    // ========== VISITAS ==========
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