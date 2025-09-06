package com.example.cafefidelidaqrdemo.network;

import com.example.cafefidelidaqrdemo.models.*;
import com.example.cafefidelidaqrdemo.network.response.VisitaResponse;
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
    
    // Endpoint específico para registro de visitas con QR
    @POST("visitas/registrar")
    Call<VisitaResponse> registrarVisita(@Body VisitaRequest request);
    
    @POST("qr/validar")
    Call<QRValidationResponse> validarQR(@Body QRValidationRequest request);
    
    // Clase para request de registro de visita
    class VisitaRequest {
        private String hashQr;
        private String clienteId;
        private long timestamp;
        private String ubicacion;
        
        public VisitaRequest(String hashQr, String clienteId, long timestamp, String ubicacion) {
            this.hashQr = hashQr;
            this.clienteId = clienteId;
            this.timestamp = timestamp;
            this.ubicacion = ubicacion;
        }
        
        // Getters y setters
        public String getHashQr() { return hashQr; }
        public void setHashQr(String hashQr) { this.hashQr = hashQr; }
        
        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    }
    
    // Clase para request de validación QR
    class QRValidationRequest {
        private String qrPayload;
        private String deviceId;
        private long timestamp;
        private String ubicacion;
        
        public QRValidationRequest(String qrPayload, String deviceId, long timestamp, String ubicacion) {
            this.qrPayload = qrPayload;
            this.deviceId = deviceId;
            this.timestamp = timestamp;
            this.ubicacion = ubicacion;
        }
        
        // Getters y setters
        public String getQrPayload() { return qrPayload; }
        public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getUbicacion() { return ubicacion; }
        public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    }
    
    // Clase para response de validación QR
    class QRValidationResponse {
        private boolean success;
        private String message;
        private QRValidationData data;
        private ValidationError error;
        
        // Getters y setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public QRValidationData getData() { return data; }
        public void setData(QRValidationData data) { this.data = data; }
        
        public ValidationError getError() { return error; }
        public void setError(ValidationError error) { this.error = error; }
    }
    
    // Datos de validación QR
    class QRValidationData {
        private String nonce;
        private Long sucursalId;
        private String sucursalNombre;
        private long timestamp;
        private boolean isValid;
        private long serverTimestamp;
        
        // Getters y setters
        public String getNonce() { return nonce; }
        public void setNonce(String nonce) { this.nonce = nonce; }
        
        public Long getSucursalId() { return sucursalId; }
        public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
        
        public String getSucursalNombre() { return sucursalNombre; }
        public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public long getServerTimestamp() { return serverTimestamp; }
        public void setServerTimestamp(long serverTimestamp) { this.serverTimestamp = serverTimestamp; }
    }
    
    // Error de validación
    class ValidationError {
        private String code;
        private String description;
        private String field;
        
        // Getters y setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
    }
    
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
    
    // Endpoints administrativos para Productos
    @GET("admin/productos")
    Call<List<Producto>> getProductosAdmin(@Query("page") int page, @Query("size") int size);
    
    @GET("admin/productos/search")
    Call<List<Producto>> searchProductosAdmin(@Query("query") String query, @Query("activo") Boolean activo);
    
    @POST("admin/productos")
    Call<AdminResponse<Producto>> createProductoAdmin(@Body AdminProductoRequest request);
    
    @PUT("admin/productos/{id}")
    Call<AdminResponse<Producto>> updateProductoAdmin(@Path("id") String id, @Body AdminProductoRequest request);
    
    @PATCH("admin/productos/{id}/estado")
    Call<AdminResponse<Producto>> toggleProductoEstado(@Path("id") String id, @Body EstadoRequest request);
    
    @DELETE("admin/productos/{id}")
    Call<AdminResponse<Void>> deleteProductoAdmin(@Path("id") String id, @Query("version") long version);
    
    // Endpoints administrativos para Sucursales
    @GET("admin/sucursales")
    Call<List<Sucursal>> getSucursalesAdmin(@Query("page") int page, @Query("size") int size);
    
    @GET("admin/sucursales/search")
    Call<List<Sucursal>> searchSucursalesAdmin(@Query("query") String query, @Query("activo") Boolean activo);
    
    @POST("admin/sucursales")
    Call<AdminResponse<Sucursal>> createSucursalAdmin(@Body AdminSucursalRequest request);
    
    @PUT("admin/sucursales/{id}")
    Call<AdminResponse<Sucursal>> updateSucursalAdmin(@Path("id") Long id, @Body AdminSucursalRequest request);
    
    @PATCH("admin/sucursales/{id}/estado")
    Call<AdminResponse<Sucursal>> toggleSucursalEstado(@Path("id") Long id, @Body EstadoRequest request);
    
    @DELETE("admin/sucursales/{id}")
    Call<AdminResponse<Void>> deleteSucursalAdmin(@Path("id") Long id, @Query("version") long version);
    
    // Endpoints administrativos para Beneficios
    @GET("admin/beneficios")
    Call<List<Beneficio>> getBeneficiosAdmin(@Query("page") int page, @Query("size") int size);
    
    @GET("admin/beneficios/search")
    Call<List<Beneficio>> searchBeneficiosAdmin(@Query("query") String query, @Query("activo") Boolean activo);
    
    @POST("admin/beneficios")
    Call<AdminResponse<Beneficio>> createBeneficioAdmin(@Body AdminBeneficioRequest request);
    
    @PUT("admin/beneficios/{id}")
    Call<AdminResponse<Beneficio>> updateBeneficioAdmin(@Path("id") String id, @Body AdminBeneficioRequest request);
    
    @PATCH("admin/beneficios/{id}/estado")
    Call<AdminResponse<Beneficio>> toggleBeneficioEstado(@Path("id") String id, @Body EstadoRequest request);
    
    @DELETE("admin/beneficios/{id}")
    Call<AdminResponse<Void>> deleteBeneficioAdmin(@Path("id") String id, @Query("version") long version);
    
    // Endpoints para validación de dependencias
    @GET("admin/beneficios/{id}/dependencias")
    Call<DependenciasResponse> checkBeneficioDependencias(@Path("id") String id);
    
    @GET("admin/productos/{id}/dependencias")
    Call<DependenciasResponse> checkProductoDependencias(@Path("id") String id);
    
    @GET("admin/sucursales/{id}/dependencias")
    Call<DependenciasResponse> checkSucursalDependencias(@Path("id") Long id);
    
    // Endpoints para estadísticas administrativas
    @GET("admin/estadisticas/dashboard")
    Call<AdminDashboardStats> getDashboardStats();
    
    @GET("admin/actividades/recientes")
    Call<List<ActividadReciente>> getActividadesRecientes(@Query("limit") int limit);
    
    // Endpoints para reportes administrativos
    @GET("admin/reportes")
    Call<List<ReporteAdmin>> getReportesAdmin(
        @Query("tipo") String tipo,
        @Query("fechaInicio") String fechaInicio,
        @Query("fechaFin") String fechaFin,
        @Query("sucursalId") String sucursalId,
        @Query("beneficioId") String beneficioId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    @GET("admin/reportes/metricas")
    Call<ReporteMetricas> getReporteMetricas(
        @Query("tipo") String tipo,
        @Query("fechaInicio") String fechaInicio,
        @Query("fechaFin") String fechaFin,
        @Query("sucursalId") String sucursalId,
        @Query("beneficioId") String beneficioId
    );
    
    @GET("admin/reportes/top-clientes")
    Call<List<TopClienteReporte>> getTopClientes(
        @Query("fechaInicio") String fechaInicio,
        @Query("fechaFin") String fechaFin,
        @Query("sucursalId") String sucursalId,
        @Query("limit") int limit
    );
    
    @GET("admin/reportes/rendimiento-sucursales")
    Call<List<RendimientoSucursalReporte>> getRendimientoSucursales(
        @Query("fechaInicio") String fechaInicio,
        @Query("fechaFin") String fechaFin
    );
    
    @GET("admin/reportes/tendencias")
    Call<List<TendenciaTemporalReporte>> getTendenciasTemporal(
        @Query("tipo") String tipo,
        @Query("fechaInicio") String fechaInicio,
        @Query("fechaFin") String fechaFin
    );
    
    @GET("admin/reportes/opciones")
    Call<OpcionesReporte> getOpcionesReporte();
    
    @POST("admin/reportes/exportar")
    Call<ExportResponse> exportarReporte(@Body ExportRequest request);
    
    // Endpoints para tablero del cliente
    @GET("cliente/{clienteId}/tablero")
    Call<TableroCliente> getTableroCliente(@Path("clienteId") String clienteId);
    
    @POST("cliente/{clienteId}/tablero/refresh")
    Call<TableroCliente> refreshTableroCliente(@Path("clienteId") String clienteId);
    
    @GET("cliente/{clienteId}/canjes-recientes")
    Call<List<CanjeRecienteCliente>> getCanjesRecientes(
        @Path("clienteId") String clienteId,
        @Query("limit") int limit
    );
    
    @GET("cliente/{clienteId}/visitas-recientes")
    Call<List<VisitaRecienteCliente>> getVisitasRecientes(
        @Path("clienteId") String clienteId,
        @Query("limit") int limit
    );
    
    @GET("cliente/{clienteId}/beneficios-recomendados")
    Call<List<BeneficioRecomendadoCliente>> getBeneficiosRecomendados(
        @Path("clienteId") String clienteId,
        @Query("limit") int limit
    );
    
    @POST("cliente/{clienteId}/canjear-beneficio")
    Call<CanjeResponse> canjearBeneficio(
        @Path("clienteId") String clienteId,
        @Body CanjeBeneficioRequest request
    );
    
    @POST("cliente/{clienteId}/registrar-visita")
    Call<VisitaRegistroResponse> registrarVisitaCliente(
        @Path("clienteId") String clienteId,
        @Body VisitaRegistroRequest request
    );
    
    @PUT("cliente/{clienteId}/meta-visitas")
    Call<MetaResponse> actualizarMetaVisitas(
        @Path("clienteId") String clienteId,
        @Body MetaVisitasRequest request
    );
    
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
    
    // Clases para requests administrativos
    class AdminProductoRequest {
        private String nombre;
        private String descripcion;
        private double precio;
        private int stockDisponible;
        private boolean activo;
        private long version;
        
        public AdminProductoRequest(String nombre, String descripcion, double precio, int stockDisponible, boolean activo, long version) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.stockDisponible = stockDisponible;
            this.activo = activo;
            this.version = version;
        }
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public double getPrecio() { return precio; }
        public void setPrecio(double precio) { this.precio = precio; }
        
        public int getStockDisponible() { return stockDisponible; }
        public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
        
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }
    }
    
    class AdminSucursalRequest {
        private String nombre;
        private String direccion;
        private double latitud;
        private double longitud;
        private String horaApertura;
        private String horaCierre;
        private int capacidadMaxima;
        private boolean activo;
        private long version;
        
        public AdminSucursalRequest(String nombre, String direccion, double latitud, double longitud, 
                                  String horaApertura, String horaCierre, int capacidadMaxima, boolean activo, long version) {
            this.nombre = nombre;
            this.direccion = direccion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.horaApertura = horaApertura;
            this.horaCierre = horaCierre;
            this.capacidadMaxima = capacidadMaxima;
            this.activo = activo;
            this.version = version;
        }
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        
        public double getLatitud() { return latitud; }
        public void setLatitud(double latitud) { this.latitud = latitud; }
        
        public double getLongitud() { return longitud; }
        public void setLongitud(double longitud) { this.longitud = longitud; }
        
        public String getHoraApertura() { return horaApertura; }
        public void setHoraApertura(String horaApertura) { this.horaApertura = horaApertura; }
        
        public String getHoraCierre() { return horaCierre; }
        public void setHoraCierre(String horaCierre) { this.horaCierre = horaCierre; }
        
        public int getCapacidadMaxima() { return capacidadMaxima; }
        public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
        
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }
    }
    
    class AdminBeneficioRequest {
        private String nombre;
        private String descripcion;
        private int puntosRequeridos;
        private boolean activo;
        private long version;
        
        public AdminBeneficioRequest(String nombre, String descripcion, int puntosRequeridos, boolean activo, long version) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.puntosRequeridos = puntosRequeridos;
            this.activo = activo;
            this.version = version;
        }
        
        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public int getPuntosRequeridos() { return puntosRequeridos; }
        public void setPuntosRequeridos(int puntosRequeridos) { this.puntosRequeridos = puntosRequeridos; }
        
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }
    }
    
    class EstadoRequest {
        private boolean activo;
        private long version;
        
        public EstadoRequest(boolean activo, long version) {
            this.activo = activo;
            this.version = version;
        }
        
        // Getters y setters
        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        
        public long getVersion() { return version; }
        public void setVersion(long version) { this.version = version; }
    }
    
    // Clases para responses administrativos
    class AdminResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private long newVersion;
        private ValidationError error;
        
        // Getters y setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public long getNewVersion() { return newVersion; }
        public void setNewVersion(long newVersion) { this.newVersion = newVersion; }
        
        public ValidationError getError() { return error; }
        public void setError(ValidationError error) { this.error = error; }
    }
    
    class DependenciasResponse {
        private boolean tieneDependencias;
        private List<String> dependencias;
        private boolean puedeEliminar;
        private String mensaje;
        
        // Getters y setters
        public boolean isTieneDependencias() { return tieneDependencias; }
        public void setTieneDependencias(boolean tieneDependencias) { this.tieneDependencias = tieneDependencias; }
        
        public List<String> getDependencias() { return dependencias; }
        public void setDependencias(List<String> dependencias) { this.dependencias = dependencias; }
        
        public boolean isPuedeEliminar() { return puedeEliminar; }
        public void setPuedeEliminar(boolean puedeEliminar) { this.puedeEliminar = puedeEliminar; }
        
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
    
    class AdminDashboardStats {
        private int totalProductos;
        private int productosActivos;
        private int totalSucursales;
        private int sucursalesActivas;
        private int totalBeneficios;
        private int beneficiosActivos;
        private int visitasHoy;
        private int canjesHoy;
        
        // Getters y setters
        public int getTotalProductos() { return totalProductos; }
        public void setTotalProductos(int totalProductos) { this.totalProductos = totalProductos; }
        
        public int getProductosActivos() { return productosActivos; }
        public void setProductosActivos(int productosActivos) { this.productosActivos = productosActivos; }
        
        public int getTotalSucursales() { return totalSucursales; }
        public void setTotalSucursales(int totalSucursales) { this.totalSucursales = totalSucursales; }
        
        public int getSucursalesActivas() { return sucursalesActivas; }
        public void setSucursalesActivas(int sucursalesActivas) { this.sucursalesActivas = sucursalesActivas; }
        
        public int getTotalBeneficios() { return totalBeneficios; }
        public void setTotalBeneficios(int totalBeneficios) { this.totalBeneficios = totalBeneficios; }
        
        public int getBeneficiosActivos() { return beneficiosActivos; }
        public void setBeneficiosActivos(int beneficiosActivos) { this.beneficiosActivos = beneficiosActivos; }
        
        public int getVisitasHoy() { return visitasHoy; }
        public void setVisitasHoy(int visitasHoy) { this.visitasHoy = visitasHoy; }
        
        public int getCanjesHoy() { return canjesHoy; }
        public void setCanjesHoy(int canjesHoy) { this.canjesHoy = canjesHoy; }
    }
    
    class ActividadReciente {
        private String tipo;
        private String descripcion;
        private String usuario;
        private long timestamp;
        private String entidad;
        private String entidadId;
        
        // Getters y setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getEntidad() { return entidad; }
        public void setEntidad(String entidad) { this.entidad = entidad; }
        
        public String getEntidadId() { return entidadId; }
        public void setEntidadId(String entidadId) { this.entidadId = entidadId; }
    }
    
    // Clases para reportes administrativos
    class ReporteAdmin {
        private String id;
        private String tipo;
        private String fechaGeneracion;
        private int totalVisitas;
        private int totalCanjes;
        private int clientesUnicos;
        private double valorTotalCanjes;
        private String sucursalId;
        private String beneficioId;
        
        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(String fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public int getTotalVisitas() { return totalVisitas; }
        public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
        
        public int getTotalCanjes() { return totalCanjes; }
        public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
        
        public int getClientesUnicos() { return clientesUnicos; }
        public void setClientesUnicos(int clientesUnicos) { this.clientesUnicos = clientesUnicos; }
        
        public double getValorTotalCanjes() { return valorTotalCanjes; }
        public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
        
        public String getSucursalId() { return sucursalId; }
        public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
        
        public String getBeneficioId() { return beneficioId; }
        public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
    }
    
    class ReporteMetricas {
        private int totalVisitas;
        private int totalCanjes;
        private int totalClientes;
        private double valorTotalCanjes;
        private double promedioVisitasDiarias;
        private double promedioCanjesDiarios;
        private double promedioVisitasPorCliente;
        private double promedioCanjesPorCliente;
        private double tasaConversion;
        private double valorPromedioCanje;
        
        // Getters y setters
        public int getTotalVisitas() { return totalVisitas; }
        public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
        
        public int getTotalCanjes() { return totalCanjes; }
        public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
        
        public int getTotalClientes() { return totalClientes; }
        public void setTotalClientes(int totalClientes) { this.totalClientes = totalClientes; }
        
        public double getValorTotalCanjes() { return valorTotalCanjes; }
        public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
        
        public double getPromedioVisitasDiarias() { return promedioVisitasDiarias; }
        public void setPromedioVisitasDiarias(double promedioVisitasDiarias) { this.promedioVisitasDiarias = promedioVisitasDiarias; }
        
        public double getPromedioCanjesDiarios() { return promedioCanjesDiarios; }
        public void setPromedioCanjesDiarios(double promedioCanjesDiarios) { this.promedioCanjesDiarios = promedioCanjesDiarios; }
        
        public double getPromedioVisitasPorCliente() { return promedioVisitasPorCliente; }
        public void setPromedioVisitasPorCliente(double promedioVisitasPorCliente) { this.promedioVisitasPorCliente = promedioVisitasPorCliente; }
        
        public double getPromedioCanjesPorCliente() { return promedioCanjesPorCliente; }
        public void setPromedioCanjesPorCliente(double promedioCanjesPorCliente) { this.promedioCanjesPorCliente = promedioCanjesPorCliente; }
        
        public double getTasaConversion() { return tasaConversion; }
        public void setTasaConversion(double tasaConversion) { this.tasaConversion = tasaConversion; }
        
        public double getValorPromedioCanje() { return valorPromedioCanje; }
        public void setValorPromedioCanje(double valorPromedioCanje) { this.valorPromedioCanje = valorPromedioCanje; }
    }
    
    class TopClienteReporte {
        private String clienteId;
        private String nombre;
        private int totalVisitas;
        private int totalCanjes;
        private double valorTotalCanjes;
        private String sucursalFavorita;
        
        // Getters y setters
        public String getClienteId() { return clienteId; }
        public void setClienteId(String clienteId) { this.clienteId = clienteId; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public int getTotalVisitas() { return totalVisitas; }
        public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
        
        public int getTotalCanjes() { return totalCanjes; }
        public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
        
        public double getValorTotalCanjes() { return valorTotalCanjes; }
        public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
        
        public String getSucursalFavorita() { return sucursalFavorita; }
        public void setSucursalFavorita(String sucursalFavorita) { this.sucursalFavorita = sucursalFavorita; }
    }
    
    class RendimientoSucursalReporte {
        private String sucursalId;
        private String nombre;
        private int totalVisitas;
        private int totalCanjes;
        private double valorTotalCanjes;
        private int clientesUnicos;
        private double tasaConversion;
        
        // Getters y setters
        public String getSucursalId() { return sucursalId; }
        public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public int getTotalVisitas() { return totalVisitas; }
        public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
        
        public int getTotalCanjes() { return totalCanjes; }
        public void setTotalCanjes(int totalCanjes) { this.totalCanjes = totalCanjes; }
        
        public double getValorTotalCanjes() { return valorTotalCanjes; }
        public void setValorTotalCanjes(double valorTotalCanjes) { this.valorTotalCanjes = valorTotalCanjes; }
        
        public int getClientesUnicos() { return clientesUnicos; }
        public void setClientesUnicos(int clientesUnicos) { this.clientesUnicos = clientesUnicos; }
        
        public double getTasaConversion() { return tasaConversion; }
        public void setTasaConversion(double tasaConversion) { this.tasaConversion = tasaConversion; }
    }
    
    class TendenciaTemporalReporte {
        private String fecha;
        private int visitas;
        private int canjes;
        private double valor;
        
        // Getters y setters
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        
        public int getVisitas() { return visitas; }
        public void setVisitas(int visitas) { this.visitas = visitas; }
        
        public int getCanjes() { return canjes; }
        public void setCanjes(int canjes) { this.canjes = canjes; }
        
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
    }
    
    class OpcionesReporte {
        private List<String> tipos;
        private List<SucursalOpcion> sucursales;
        private List<BeneficioOpcion> beneficios;
        
        // Getters y setters
        public List<String> getTipos() { return tipos; }
        public void setTipos(List<String> tipos) { this.tipos = tipos; }
        
        public List<SucursalOpcion> getSucursales() { return sucursales; }
        public void setSucursales(List<SucursalOpcion> sucursales) { this.sucursales = sucursales; }
        
        public List<BeneficioOpcion> getBeneficios() { return beneficios; }
        public void setBeneficios(List<BeneficioOpcion> beneficios) { this.beneficios = beneficios; }
        
        public static class SucursalOpcion {
            private String id;
            private String nombre;
            
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            
            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }
        }
        
        public static class BeneficioOpcion {
            private String id;
            private String nombre;
            
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            
            public String getNombre() { return nombre; }
            public void setNombre(String nombre) { this.nombre = nombre; }
        }
    }
    
    class ExportRequest {
        private String tipo;
        private String fechaInicio;
        private String fechaFin;
        private String sucursalId;
        private String beneficioId;
        private String formato;
        private String nombreArchivo;
        
        public ExportRequest(String tipo, String fechaInicio, String fechaFin, String sucursalId, String beneficioId, String formato, String nombreArchivo) {
            this.tipo = tipo;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.sucursalId = sucursalId;
            this.beneficioId = beneficioId;
            this.formato = formato;
            this.nombreArchivo = nombreArchivo;
        }
        
        // Getters y setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public String getFechaFin() { return fechaFin; }
        public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
        
        public String getSucursalId() { return sucursalId; }
        public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
        
        public String getBeneficioId() { return beneficioId; }
        public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
        
        public String getFormato() { return formato; }
        public void setFormato(String formato) { this.formato = formato; }
        
        public String getNombreArchivo() { return nombreArchivo; }
        public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    }
    
    class ExportResponse {
        private boolean success;
        private String message;
        private String downloadUrl;
        private String fileName;
        private long fileSize;
        
        // Getters y setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getFileSize() { return fileSize; }
         public void setFileSize(long fileSize) { this.fileSize = fileSize; }
     }
     
     // Clases para tablero cliente
     class TableroCliente {
         private String clienteId;
         private int totalVisitas;
         private int beneficiosDisponibles;
         private int canjesRecientes;
         private int puntosActuales;
         private int puntosProximoNivel;
         private String nivelActual;
         private String proximoNivel;
         private double progreso;
         private long ultimaActualizacion;
         
         // Getters y setters
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public int getTotalVisitas() { return totalVisitas; }
         public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
         
         public int getBeneficiosDisponibles() { return beneficiosDisponibles; }
         public void setBeneficiosDisponibles(int beneficiosDisponibles) { this.beneficiosDisponibles = beneficiosDisponibles; }
         
         public int getCanjesRecientes() { return canjesRecientes; }
         public void setCanjesRecientes(int canjesRecientes) { this.canjesRecientes = canjesRecientes; }
         
         public int getPuntosActuales() { return puntosActuales; }
         public void setPuntosActuales(int puntosActuales) { this.puntosActuales = puntosActuales; }
         
         public int getPuntosProximoNivel() { return puntosProximoNivel; }
         public void setPuntosProximoNivel(int puntosProximoNivel) { this.puntosProximoNivel = puntosProximoNivel; }
         
         public String getNivelActual() { return nivelActual; }
         public void setNivelActual(String nivelActual) { this.nivelActual = nivelActual; }
         
         public String getProximoNivel() { return proximoNivel; }
         public void setProximoNivel(String proximoNivel) { this.proximoNivel = proximoNivel; }
         
         public double getProgreso() { return progreso; }
         public void setProgreso(double progreso) { this.progreso = progreso; }
         
         public long getUltimaActualizacion() { return ultimaActualizacion; }
         public void setUltimaActualizacion(long ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
     }
     
     class CanjeReciente {
         private String id;
         private String beneficioId;
         private String beneficioNombre;
         private String sucursalId;
         private String sucursalNombre;
         private long fechaCanje;
         private int puntosUtilizados;
         private String estado;
         
         // Getters y setters
         public String getId() { return id; }
         public void setId(String id) { this.id = id; }
         
         public String getBeneficioId() { return beneficioId; }
         public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
         
         public String getBeneficioNombre() { return beneficioNombre; }
         public void setBeneficioNombre(String beneficioNombre) { this.beneficioNombre = beneficioNombre; }
         
         public String getSucursalId() { return sucursalId; }
         public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
         
         public String getSucursalNombre() { return sucursalNombre; }
         public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
         
         public long getFechaCanje() { return fechaCanje; }
         public void setFechaCanje(long fechaCanje) { this.fechaCanje = fechaCanje; }
         
         public int getPuntosUtilizados() { return puntosUtilizados; }
         public void setPuntosUtilizados(int puntosUtilizados) { this.puntosUtilizados = puntosUtilizados; }
         
         public String getEstado() { return estado; }
         public void setEstado(String estado) { this.estado = estado; }
     }
     
     class VisitaReciente {
         private String id;
         private String sucursalId;
         private String sucursalNombre;
         private long fechaVisita;
         private int puntosGanados;
         private String tipoVisita;
         
         // Getters y setters
         public String getId() { return id; }
         public void setId(String id) { this.id = id; }
         
         public String getSucursalId() { return sucursalId; }
         public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
         
         public String getSucursalNombre() { return sucursalNombre; }
         public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
         
         public long getFechaVisita() { return fechaVisita; }
         public void setFechaVisita(long fechaVisita) { this.fechaVisita = fechaVisita; }
         
         public int getPuntosGanados() { return puntosGanados; }
         public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }
         
         public String getTipoVisita() { return tipoVisita; }
         public void setTipoVisita(String tipoVisita) { this.tipoVisita = tipoVisita; }
     }
     
     class BeneficioRecomendado {
         private String id;
         private String nombre;
         private String descripcion;
         private int puntosRequeridos;
         private String categoria;
         private String imagenUrl;
         private boolean disponible;
         private String razonRecomendacion;
         private double puntuacionRecomendacion;
         
         // Getters y setters
         public String getId() { return id; }
         public void setId(String id) { this.id = id; }
         
         public String getNombre() { return nombre; }
         public void setNombre(String nombre) { this.nombre = nombre; }
         
         public String getDescripcion() { return descripcion; }
         public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
         
         public int getPuntosRequeridos() { return puntosRequeridos; }
         public void setPuntosRequeridos(int puntosRequeridos) { this.puntosRequeridos = puntosRequeridos; }
         
         public String getCategoria() { return categoria; }
         public void setCategoria(String categoria) { this.categoria = categoria; }
         
         public String getImagenUrl() { return imagenUrl; }
         public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
         
         public boolean isDisponible() { return disponible; }
         public void setDisponible(boolean disponible) { this.disponible = disponible; }
         
         public String getRazonRecomendacion() { return razonRecomendacion; }
         public void setRazonRecomendacion(String razonRecomendacion) { this.razonRecomendacion = razonRecomendacion; }
         
         public double getPuntuacionRecomendacion() { return puntuacionRecomendacion; }
         public void setPuntuacionRecomendacion(double puntuacionRecomendacion) { this.puntuacionRecomendacion = puntuacionRecomendacion; }
     }
     
     class CanjeRequest {
         private String clienteId;
         private String beneficioId;
         private String sucursalId;
         private int puntosUtilizar;
         private String metodoCanje;
         
         public CanjeRequest(String clienteId, String beneficioId, String sucursalId, int puntosUtilizar, String metodoCanje) {
             this.clienteId = clienteId;
             this.beneficioId = beneficioId;
             this.sucursalId = sucursalId;
             this.puntosUtilizar = puntosUtilizar;
             this.metodoCanje = metodoCanje;
         }
         
         // Getters y setters
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public String getBeneficioId() { return beneficioId; }
         public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
         
         public String getSucursalId() { return sucursalId; }
         public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
         
         public int getPuntosUtilizar() { return puntosUtilizar; }
         public void setPuntosUtilizar(int puntosUtilizar) { this.puntosUtilizar = puntosUtilizar; }
         
         public String getMetodoCanje() { return metodoCanje; }
         public void setMetodoCanje(String metodoCanje) { this.metodoCanje = metodoCanje; }
     }
     
     class MetaRequest {
         private String clienteId;
         private String tipoMeta;
         private int valorObjetivo;
         private String periodo;
         private String descripcion;
         
         public MetaRequest(String clienteId, String tipoMeta, int valorObjetivo, String periodo, String descripcion) {
             this.clienteId = clienteId;
             this.tipoMeta = tipoMeta;
             this.valorObjetivo = valorObjetivo;
             this.periodo = periodo;
             this.descripcion = descripcion;
         }
         
         // Getters y setters
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public String getTipoMeta() { return tipoMeta; }
         public void setTipoMeta(String tipoMeta) { this.tipoMeta = tipoMeta; }
         
         public int getValorObjetivo() { return valorObjetivo; }
         public void setValorObjetivo(int valorObjetivo) { this.valorObjetivo = valorObjetivo; }
         
         public String getPeriodo() { return periodo; }
         public void setPeriodo(String periodo) { this.periodo = periodo; }
         
         public String getDescripcion() { return descripcion; }
         public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
     }
     
     class VisitaRegistroResponse {
         private boolean success;
         private String message;
         private String visitaId;
         private int puntosGanados;
         
         public VisitaRegistroResponse(boolean success, String message, String visitaId, int puntosGanados) {
             this.success = success;
             this.message = message;
             this.visitaId = visitaId;
             this.puntosGanados = puntosGanados;
         }
         
         public boolean isSuccess() { return success; }
         public void setSuccess(boolean success) { this.success = success; }
         
         public String getMessage() { return message; }
         public void setMessage(String message) { this.message = message; }
         
         public String getVisitaId() { return visitaId; }
         public void setVisitaId(String visitaId) { this.visitaId = visitaId; }
         
         public int getPuntosGanados() { return puntosGanados; }
         public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }
     }
     
     class MetaResponse {
         private boolean success;
         private String message;
         private String metaId;
         private int progreso;
         
         public MetaResponse(boolean success, String message, String metaId, int progreso) {
             this.success = success;
             this.message = message;
             this.metaId = metaId;
             this.progreso = progreso;
         }
         
         public boolean isSuccess() { return success; }
         public void setSuccess(boolean success) { this.success = success; }
         
         public String getMessage() { return message; }
         public void setMessage(String message) { this.message = message; }
         
         public String getMetaId() { return metaId; }
         public void setMetaId(String metaId) { this.metaId = metaId; }
         
         public int getProgreso() { return progreso; }
         public void setProgreso(int progreso) { this.progreso = progreso; }
     }
     
     class MetaVisitasRequest {
         private String clienteId;
         private int metaVisitas;
         private String periodo;
         private String descripcion;
         
         public MetaVisitasRequest(String clienteId, int metaVisitas, String periodo, String descripcion) {
             this.clienteId = clienteId;
             this.metaVisitas = metaVisitas;
             this.periodo = periodo;
             this.descripcion = descripcion;
         }
         
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public int getMetaVisitas() { return metaVisitas; }
         public void setMetaVisitas(int metaVisitas) { this.metaVisitas = metaVisitas; }
         
         public String getPeriodo() { return periodo; }
         public void setPeriodo(String periodo) { this.periodo = periodo; }
         
         public String getDescripcion() { return descripcion; }
         public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
     }
     
     class CanjeBeneficioRequest {
         private String clienteId;
         private String beneficioId;
         private int puntosUtilizados;
         private String codigoQR;
         
         public CanjeBeneficioRequest(String clienteId, String beneficioId, int puntosUtilizados, String codigoQR) {
             this.clienteId = clienteId;
             this.beneficioId = beneficioId;
             this.puntosUtilizados = puntosUtilizados;
             this.codigoQR = codigoQR;
         }
         
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public String getBeneficioId() { return beneficioId; }
         public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
         
         public int getPuntosUtilizados() { return puntosUtilizados; }
         public void setPuntosUtilizados(int puntosUtilizados) { this.puntosUtilizados = puntosUtilizados; }
         
         public String getCodigoQR() { return codigoQR; }
         public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
     }
     
     class CanjeResponse {
         private boolean success;
         private String message;
         private String canjeId;
         private int puntosRestantes;
         
         public CanjeResponse(boolean success, String message, String canjeId, int puntosRestantes) {
             this.success = success;
             this.message = message;
             this.canjeId = canjeId;
             this.puntosRestantes = puntosRestantes;
         }
         
         public boolean isSuccess() { return success; }
         public void setSuccess(boolean success) { this.success = success; }
         
         public String getMessage() { return message; }
         public void setMessage(String message) { this.message = message; }
         
         public String getCanjeId() { return canjeId; }
         public void setCanjeId(String canjeId) { this.canjeId = canjeId; }
         
         public int getPuntosRestantes() { return puntosRestantes; }
         public void setPuntosRestantes(int puntosRestantes) { this.puntosRestantes = puntosRestantes; }
     }
     
     class VisitaRegistroRequest {
         private String clienteId;
         private String sucursalId;
         private String codigoQR;
         private String timestamp;
         private String ubicacion;
         
         public VisitaRegistroRequest(String clienteId, String sucursalId, String codigoQR, String timestamp, String ubicacion) {
             this.clienteId = clienteId;
             this.sucursalId = sucursalId;
             this.codigoQR = codigoQR;
             this.timestamp = timestamp;
             this.ubicacion = ubicacion;
         }
         
         public String getClienteId() { return clienteId; }
         public void setClienteId(String clienteId) { this.clienteId = clienteId; }
         
         public String getSucursalId() { return sucursalId; }
         public void setSucursalId(String sucursalId) { this.sucursalId = sucursalId; }
         
         public String getCodigoQR() { return codigoQR; }
         public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
         
         public String getTimestamp() { return timestamp; }
         public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
         
         public String getUbicacion() { return ubicacion; }
         public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
     }
     
     class CanjeRecienteCliente {
         private String canjeId;
         private String beneficioNombre;
         private String fechaCanje;
         private int puntosUtilizados;
         private String estado;
         
         public CanjeRecienteCliente(String canjeId, String beneficioNombre, String fechaCanje, int puntosUtilizados, String estado) {
             this.canjeId = canjeId;
             this.beneficioNombre = beneficioNombre;
             this.fechaCanje = fechaCanje;
             this.puntosUtilizados = puntosUtilizados;
             this.estado = estado;
         }
         
         public String getCanjeId() { return canjeId; }
         public void setCanjeId(String canjeId) { this.canjeId = canjeId; }
         
         public String getBeneficioNombre() { return beneficioNombre; }
         public void setBeneficioNombre(String beneficioNombre) { this.beneficioNombre = beneficioNombre; }
         
         public String getFechaCanje() { return fechaCanje; }
         public void setFechaCanje(String fechaCanje) { this.fechaCanje = fechaCanje; }
         
         public int getPuntosUtilizados() { return puntosUtilizados; }
         public void setPuntosUtilizados(int puntosUtilizados) { this.puntosUtilizados = puntosUtilizados; }
         
         public String getEstado() { return estado; }
         public void setEstado(String estado) { this.estado = estado; }
     }
     
     class VisitaRecienteCliente {
         private String visitaId;
         private String sucursalNombre;
         private String fechaVisita;
         private int puntosGanados;
         private String ubicacion;
         
         public VisitaRecienteCliente(String visitaId, String sucursalNombre, String fechaVisita, int puntosGanados, String ubicacion) {
             this.visitaId = visitaId;
             this.sucursalNombre = sucursalNombre;
             this.fechaVisita = fechaVisita;
             this.puntosGanados = puntosGanados;
             this.ubicacion = ubicacion;
         }
         
         public String getVisitaId() { return visitaId; }
         public void setVisitaId(String visitaId) { this.visitaId = visitaId; }
         
         public String getSucursalNombre() { return sucursalNombre; }
         public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
         
         public String getFechaVisita() { return fechaVisita; }
         public void setFechaVisita(String fechaVisita) { this.fechaVisita = fechaVisita; }
         
         public int getPuntosGanados() { return puntosGanados; }
         public void setPuntosGanados(int puntosGanados) { this.puntosGanados = puntosGanados; }
         
         public String getUbicacion() { return ubicacion; }
         public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
     }
     
     class BeneficioRecomendadoCliente {
         private String beneficioId;
         private String nombre;
         private String descripcion;
         private int puntosRequeridos;
         private String categoria;
         private boolean disponible;
         
         public BeneficioRecomendadoCliente(String beneficioId, String nombre, String descripcion, int puntosRequeridos, String categoria, boolean disponible) {
             this.beneficioId = beneficioId;
             this.nombre = nombre;
             this.descripcion = descripcion;
             this.puntosRequeridos = puntosRequeridos;
             this.categoria = categoria;
             this.disponible = disponible;
         }
         
         public String getBeneficioId() { return beneficioId; }
         public void setBeneficioId(String beneficioId) { this.beneficioId = beneficioId; }
         
         public String getNombre() { return nombre; }
         public void setNombre(String nombre) { this.nombre = nombre; }
         
         public String getDescripcion() { return descripcion; }
         public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
         
         public int getPuntosRequeridos() { return puntosRequeridos; }
         public void setPuntosRequeridos(int puntosRequeridos) { this.puntosRequeridos = puntosRequeridos; }
         
         public String getCategoria() { return categoria; }
         public void setCategoria(String categoria) { this.categoria = categoria; }
         
         public boolean isDisponible() { return disponible; }
         public void setDisponible(boolean disponible) { this.disponible = disponible; }
     }
 }