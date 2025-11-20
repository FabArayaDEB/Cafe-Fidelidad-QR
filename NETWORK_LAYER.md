# Network Layer - Capa de Red

## Descripción General

El directorio `network` contiene la **infraestructura HTTP** de la aplicación: definición de endpoints REST, creación del cliente Retrofit y utilidades de conectividad.

Componentes principales:
- `ApiService`: interfaz Retrofit con endpoints para auth y CRUD de clientes, productos, beneficios, sucursales, visitas y canjes.
- `RetrofitClient`: cliente configurado con `BASE_URL` (`https://api.cafefidelidad.com/`) y `GsonConverterFactory`; integra `OkHttp` con `HttpLoggingInterceptor`.
- `ApiClient`: fachada simple para obtener una instancia de `ApiService`.
- `NetworkUtils`: utilidades para verificar conectividad (`isNetworkAvailable`).

## Estado del Proyecto

### ✅ Implementado
- Cliente HTTP con Retrofit + Gson
- OkHttp con `logging-interceptor` para debugging
- Definición de endpoints REST en `ApiService`
- Utilidades de conectividad (`NetworkUtils.isNetworkAvailable`)

### 🔄 En Desarrollo
- Manejo de autenticación (tokens) y headers dinámicos
- Manejo centralizado de errores y reintentos
- Certificate pinning en producción
- (El sistema de reportes fue eliminado)

### 📋 Futuras Mejoras
- Compresión de requests/responses (GZIP)
- Estrategias de caché HTTP y offline-first
- Rate limiting y backoff exponencial
- Métricas y tracing de red

## Mejores Prácticas

### 1. Configuración
- **Timeouts Apropiados**: Configurar timeouts según el tipo de operación
- **Caché Inteligente**: Usar estrategias de caché apropiadas
- **Seguridad**: Implementar certificate pinning en producción
- **Logging**: Logging detallado en desarrollo, mínimo en producción

### 2. Manejo de Errores
- **Clasificación**: Distinguir entre errores de red, HTTP y de aplicación
- **Reintentos**: Implementar reintentos con backoff exponencial
- **Fallbacks**: Proporcionar fallbacks para operaciones críticas
- **Reporting**: Reportar errores a sistemas de monitoreo

### 3. Performance
- **Conexiones Persistentes**: Reutilizar conexiones HTTP
- **Compresión**: Usar GZIP para requests/responses grandes
- **Paginación**: Implementar paginación para listas grandes
- **Caché**: Cachear respuestas cuando sea apropiado

### 4. Seguridad
- **HTTPS**: Usar siempre HTTPS en producción
- **Certificate Pinning**: Implementar para prevenir MITM
- **Headers Sensibles**: No loggear headers con información sensible
- **Validación**: Validar todas las respuestas del servidor

## Conclusión

La capa de red, basada en Retrofit/OkHttp y Gson, centraliza la comunicación con la API y facilita el consumo de endpoints de forma tipada y mantenible. Con `NetworkUtils`, la aplicación decide cuándo realizar llamadas o trabajar en modo offline.

---

**Nota**: Esta documentación describe la arquitectura y componentes de la capa de red del proyecto CafeFidelidaQRDemo. Para implementación específica, consultar los archivos de código correspondientes en el directorio `network/`.
