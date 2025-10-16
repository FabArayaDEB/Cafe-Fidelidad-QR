# Services Layer - Capa de Servicios

## Descripción General

El directorio `services` contiene servicios del sistema que realizan operaciones en segundo plano, independientes del ciclo de vida de Activities/Fragments. Actualmente se implementa un servicio de ubicación para detectar proximidad a sucursales y facilitar experiencias basadas en localización.

## Servicios Existentes

- `LocationService` (Foreground Service):
  - Objetivo: obtener ubicación periódica del usuario y emitir actualizaciones/broadcasts para otras capas.
  - Notificación: crea canal `LocationServiceChannel` y muestra notificación persistente requerida para foreground.
  - Integración: usa `utils.LocationManager` (LiveData de ubicación y errores) para observar cambios.
  - Comunicación: emite `Intent` broadcast `LOCATION_UPDATE` y `LOCATION_ERROR` con datos relevantes.
  - Control: métodos estáticos `startLocationService(context)` y `stopLocationService(context)`.

## Estado del Proyecto

### ✅ Implementado
- Servicio de ubicación en primer plano (`LocationService`).
- Observadores `LiveData` para ubicación y errores vía `LocationManager`.
- Canal de notificación y `NotificationCompat` configurados.

### 🔄 En Desarrollo
- Lógica de proximidad a sucursales y notificaciones contextuales.
- Persistencia de ubicaciones y sincronización con backend.
- Manejo granular de intervalos/accuracy acorde a batería.

### 📋 Futuras Mejoras
- Integración con `WorkManager` para tareas periódicas o diferidas.
- Servicios adicionales según necesidades (sync, notificaciones, escáner continuo).
- Métricas/telemetría de servicio y resiliencia (reintentos, backoff).

## Mejores Prácticas

### 1. Gestión de Recursos
- Respetar ciclo de vida del servicio (startForeground/stop, cleanup en `onDestroy`).
- Minimizar uso de batería ajustando intervalos y precisión.
- Evitar trabajo pesado en el hilo principal.

### 2. Seguridad y Privacidad
- Solicitar y validar permisos de ubicación antes de iniciar actualizaciones.
- No almacenar datos sensibles sin consentimiento y protección.
- Usar canales de notificación con descripciones claras.

### 3. Comunicación
- Emitir broadcasts bien definidos y documentados.
- Considerar `PendingIntent` seguro (`FLAG_IMMUTABLE` en API recientes).

## Conclusión

La capa de servicios se centra actualmente en `LocationService` para experiencias basadas en ubicación. Su implementación como servicio en primer plano garantiza continuidad y cumplimiento de políticas de Android, mientras que su integración con `LocationManager` simplifica el flujo de datos hacia la UI/Repositorios. Se prevé expandir esta capa con servicios de sincronización y notificaciones conforme evolucione el proyecto.