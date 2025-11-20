# Funcionalidades de Ubicación (Prototipo)

## Resumen
- Objetivo: asociar coordenadas básicas a una sucursal al crear/editar.
- Alcance: adquisición puntual de ubicación desde la UI; sin servicios en segundo plano.

## Permisos
- `ACCESS_COARSE_LOCATION`: ubicación aproximada.
- `ACCESS_FINE_LOCATION`: ubicación precisa.
- Eliminados para el prototipo: `ACCESS_BACKGROUND_LOCATION` y cualquier servicio de ubicación.

## Flujo de Ubicación en Sucursales
- La UI (por ejemplo, `FragmentSucursalesAdmin.java`) solicita permisos de forma progresiva.
- Usa `FusedLocationProviderClient` para obtener una ubicación una sola vez.
- Si se obtiene latitud/longitud, se rellenan los campos del formulario de sucursal.
- Si falla o no hay permisos, se permite ingresar coordenadas manualmente.

### Ejemplo mínimo
```java
fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
    if (location != null) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        // Prefill en el formulario de sucursal
    } else {
        // Fallback: permitir ingreso manual
    }
});
```

## Decisiones de Arquitectura
- Sin `services/LocationService.java` ni gestores dedicados en `utils`.
- No se mantiene tracking continuo; solo adquisición puntual desde la UI.
- Se favorece simplicidad y permisos mínimos para el prototipo.

## Archivos Clave
- `AndroidManifest.xml`: solo permisos `COARSE` y `FINE` de ubicación.
- `FragmentSucursalesAdmin.java`: lógica de permisos y adquisición puntual.

## Próximos pasos (opcional)
- Añadir botón "Usar mi ubicación" en el formulario de sucursal.
- Validar formato de coordenadas y rango permitido.
- Mostrar el mapa para verificar la posición antes de guardar.