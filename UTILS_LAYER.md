# Utils Layer - Capa de Utilidades

## Descripción General

El directorio `utils` contiene **clases de utilidad y helpers** usados en toda la app. Centraliza lógica común como manejo de sesión, conectividad, QR, ubicación y búsqueda local para mejorar reutilización y consistencia.

## Componentes Existentes

- `SessionManager`: gestiona sesión de usuario con `SharedPreferences` (crear/cerrar sesión, `isLoggedIn`, getters de `userId`, `email`, `name`).
- `NetworkUtils`: verificación básica de conectividad (`isNetworkAvailable`), inicialización de contexto (`init`).
- `QRGenerator`: generación y validación de QR de cliente con ZXing; parseo a `ClienteQRData`.
- `QRValidator`: validaciones auxiliares de contenido QR (estructura y prefijos). 
- `LocationUtils`: utilidades de ubicación (permisos, cálculo de distancia Haversine, formatos de distancia, validación de coordenadas).
- `LocationManager`: envoltorio simplificado sobre `android.location.LocationManager` con `LiveData` para ubicación y errores, start/stop updates.
- `SearchManager`: búsqueda y filtrado local de `Sucursal` + ordenamiento por distancia, helpers `SucursalWithDistance`.

## Estado del Proyecto

### ✅ Implementado
- Gestión de sesión (`SessionManager`).
- Conectividad básica (`NetworkUtils`).
- QR de cliente (generación/validación/parseo) con ZXing (`QRGenerator`).
- Utilidades de ubicación (`LocationUtils`) y gestor simplificado (`LocationManager`).
- Búsqueda/filtrado local (`SearchManager`).

### 🔄 En Desarrollo
- Validaciones adicionales y normalización en `QRValidator`.
- Mejoras de precisión en ubicación (proveedor de red, tiempo, distance filter).
- Extender `NetworkUtils` con tipos de red y callbacks de cambios.

## Mejores Prácticas

### 1. Diseño
- Métodos puros y estáticos cuando no haya estado.
- Evitar dependencias fuertes con `Context`; usar inicialización explícita.
- Manejo de errores controlado y seguro (sin información sensible en logs).

### 2. Uso y Reutilización
- Utilidades pequeñas y enfocadas, una responsabilidad por clase.
- Documentar entradas/salidas y precondiciones.
- Evitar bloquear UI; delegar tareas costosas fuera del hilo principal.

### 3. Seguridad
- Validar entradas en utilidades que procesan datos externos (QR, red).
- Mantener privacidad al manejar sesión y datos de usuario.

## Conclusión

Las utilidades actuales cubren las necesidades clave del proyecto: sesión, red, QR, ubicación y búsqueda. Su uso consistente reduce duplicación de lógica en UI/ViewModels/Repositories y facilita mantenimiento. Se sugiere fortalecer pruebas y migrar gradualmente a Kotlin para mejorar expresividad y seguridad.