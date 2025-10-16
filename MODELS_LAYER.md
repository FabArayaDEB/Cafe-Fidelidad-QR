# Models Layer - Capa de Modelos

## Descripción General

El directorio `models` contiene las **clases de modelo de datos** utilizadas por la aplicación y sus capas (UI, managers, repositorios). Esta documentación refleja únicamente los modelos existentes en el código actual.

## Modelos presentes

### Cliente
- Identificación y contacto: `id`, `nombre`, `email`, `telefono`.
- Estado y tiempos: `estado`, `fechaCreacion`, `fechaActualizacion`, `activo`.
- Fidelización: `totalVisitas`, `puntosAcumulados`, utilidades para calcular `nivel` y actualizar puntos/visitas.

### Producto
- Información básica: `id`, `nombre`, `descripcion`, `precio`, `categoria`, `imagenUrl`.
- Disponibilidad: `estado`, `disponible`, `stock`, `puntosRequeridos`.
- Tiempos: `fechaCreacion`, `fechaActualizacion`.

### Sucursal
- Información básica: `id`, `nombre`, `direccion`, `telefono`.
- Ubicación: `latitud`, `longitud`.
- Operación: `horarioApertura`, `horarioCierre`, `abierto`, `activa`.
- Tiempos: `fechaCreacion`, `fechaActualizacion`.

### Beneficio
- Tipo/estado: `tipo` (porcentaje, fijo, producto_gratis, dos_por_uno), `estado` (disponible, usado, expirado), `activo`.
- Reglas: `visitasRequeridas`, `valorDescuento`, `valorDescuentoPorcentaje`, `valorDescuentoFijo`, `productoId`.
- Alcance y vigencia: `clienteId`, `fechaVencimiento`, `fechaCreacion`, `fechaInicioVigencia`, `fechaFinVigencia`.
- Contadores: `vecesCanjeado`, `cantidadMaximaUsos`, `cantidadUsosActuales`.
- Utilidades: `esValido()`, `estaVencido()`, `marcarComoUsado()`.

### Visita
- Datos de la visita: `id`, `userId`, `sucursal`, `direccionSucursal`, `fechaVisita`.
- Compra y puntos: `montoCompra`, `puntosGanados`, `productos` (JSON), `metodoPago`, `qrCode`.

### Canje
- Identificación y tipo: `id`, `tipo` (descuento_porcentaje, monto_fijo, 2x1, producto_gratis), `descripcion`.
- Valores: `valor`, `productoGratis` (si aplica), `codigoVerificacion`, `usado`.
- Tiempos y lugar: `fechaCanje`, `fechaExpiracion`, `sucursal`.
- Campos de compatibilidad BD: `clienteId`, `beneficioId`, `puntosUtilizados`, `estado`, `getPuntosUsados()`.

### RecentActivity
- Resumen de actividad: `id`, `tipo`, `descripcion`, `timestamp`, `usuario`.

## Estado del Proyecto

### ✅ Implementado
- Modelos: `Cliente`, `Producto`, `Sucursal`, `Beneficio`, `Visita`, `Canje`, `RecentActivity`.
- Métodos de utilidad en modelos para estados, cálculos y representación.

### 🔄 En Desarrollo
- Validaciones adicionales y documentación puntual por modelo.
- Compatibilidad con repositorios/servicios donde aplique.

### 📋 Futuras Mejoras
- Migración progresiva a Kotlin (data classes) y null-safety.
- Anotaciones para serialización/validación cuando se integren APIs.
- Versionado de modelos si se conectan servicios externos.

## Mejores Prácticas

- Mantener modelos centrados en datos y utilidades simples.
- Evitar lógica de negocio compleja en modelos; delegar a managers/use cases.
- Usar timestamps (`fechaCreacion`, `fechaActualizacion`) coherentes para auditoría.
- Documentar campos críticos y estados aceptados (`estado`, `activo`).
