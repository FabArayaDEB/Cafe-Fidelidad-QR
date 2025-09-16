# Models Layer - Capa de Modelos

## Descripción General

El directorio `models` contiene las **clases de modelo de datos** que representan la estructura de información intercambiada con APIs externas y servicios web en el proyecto CafeFidelidaQRDemo. Estos modelos actúan como **DTOs (Data Transfer Objects)** y definen el contrato de datos entre la aplicación móvil y los servicios backend.

## Modelos Principales

### 1. Cliente

**Propósito**: Representa la información de un cliente en el sistema de fidelización.

**Características**:
- **Identificación**: ID único, email, teléfono
- **Información Personal**: nombre, apellido, fecha nacimiento, género
- **Sistema de Puntos**: puntos acumulados, nivel de fidelización
- **Preferencias**: mapa de preferencias personalizadas
- **Referidos**: sistema de códigos de referido
- **Actividad**: última visita, sucursal preferida

### 2. Producto

**Propósito**: Representa un producto del catálogo de la cafetería.

**Características**:
- **Información Básica**: nombre, descripción, precio, categoría
- **Multimedia**: lista de imágenes del producto
- **Disponibilidad**: stock, disponibilidad, popularidad
- **Sistema de Puntos**: puntos requeridos para canje
- **Información Nutricional**: calorías, ingredientes, alérgenos
- **Valoraciones**: rating promedio, número de reviews
- **Descuentos**: porcentaje de descuento aplicable

### 3. Transaccion

**Características**:
- **Identificación**: ID único, número de ticket, QR code
- **Items**: lista de productos con cantidades y precios
- **Montos**: total, descuentos, monto final
- **Puntos**: puntos ganados y utilizados
- **Estado**: flujo de estados de la transacción
- **Metadatos**: información adicional flexible
- **Ubicación**: sucursal y empleado que procesó

### 4. ItemTransaccion

**Propósito**: Representa un item individual dentro de una transacción.

### 5. Beneficio

**Propósito**: Representa un beneficio o recompensa disponible para canje.

### 6. ClienteQRData

**Propósito**: Representa los datos codificados en códigos QR para clientes.

## Modelos de Request

### 1. AuthRequest

**Propósito**: Modelo para solicitudes de autenticación.

### 2. ClienteRequest

**Propósito**: Modelo para solicitudes relacionadas con clientes.

### 3. TransaccionRequest

**Propósito**: Modelo para solicitudes de transacciones.

## Modelos de Response

### 1. ApiResponse

**Propósito**: Modelo base para todas las respuestas de API.

### 2. AuthResponse

**Propósito**: Modelo para respuestas de autenticación.

### 3. ErrorResponse

**Propósito**: Modelo para respuestas de error estandarizadas.

## DTOs (Data Transfer Objects)

### 1. ClienteDTO

**Propósito**: DTO optimizado para transferencia de datos de cliente.

### 2. ProductoDTO

**Propósito**: DTO optimizado para transferencia de datos de producto.

### Implementado
- Modelos principales (Cliente, Producto, Transaccion, Beneficio)
- Modelos de Request y Response
- DTOs para transferencia optimizada
- ClienteQRData para códigos QR
- Validaciones básicas
- Serialización JSON con Gson
- Estructura de errores estandarizada

### En Desarrollo
- Validaciones avanzadas con anotaciones
- Modelos de reportes y analytics
- Optimizaciones de serialización
- Documentación de APIs

### Futuras Mejoras
- Implementación de Sealed Classes para estados
- Validaciones con Bean Validation
- Serialización con Moshi o Kotlinx.serialization
- Generación automática de DTOs
- Versionado de modelos para compatibilidad
- Compresión de payloads grandes
- Caché de objetos serializados

## Mejores Prácticas

### 1. Diseño de Modelos
- **Inmutabilidad**: Usar final fields cuando sea posible
- **Validaciones**: Implementar validaciones en constructores
- **Null Safety**: Manejar valores nulos apropiadamente
- **Documentación**: Documentar campos y métodos importantes

### 2. Serialización
- **Anotaciones**: Usar @SerializedName para mapeo de campos
- **Exclusiones**: Excluir campos sensibles de serialización
- **Formatos**: Usar formatos consistentes para fechas y números
- **Versionado**: Mantener compatibilidad hacia atrás

### 3. Validaciones
- **Tempranas**: Validar datos en el punto de entrada
- **Específicas**: Proporcionar mensajes de error claros
- **Consistentes**: Usar las mismas reglas en cliente y servidor
- **Performance**: Optimizar validaciones frecuentes

### 4. DTOs
- **Propósito Específico**: Crear DTOs para casos de uso específicos
- **Ligeros**: Incluir solo campos necesarios
- **Conversión**: Implementar métodos de conversión claros
- **Reutilización**: Reutilizar DTOs cuando sea apropiado
