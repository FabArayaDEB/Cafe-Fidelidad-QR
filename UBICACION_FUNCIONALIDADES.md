# Funcionalidades de Ubicación - Café Fidelidad QR Demo

## Resumen

Se han implementado funcionalidades completas de ubicación en la aplicación Café Fidelidad QR Demo, incluyendo permisos, seguimiento, almacenamiento y gestión de datos de ubicación.

## Componentes Implementados

### 1. Permisos y Configuración

#### AndroidManifest.xml
- **ACCESS_FINE_LOCATION**: Ubicación precisa mediante GPS
- **ACCESS_COARSE_LOCATION**: Ubicación aproximada mediante red
- **ACCESS_BACKGROUND_LOCATION**: Ubicación en segundo plano (Android 10+)
- **FOREGROUND_SERVICE**: Para servicios de ubicación en primer plano
- **FOREGROUND_SERVICE_LOCATION**: Tipo específico de servicio de ubicación

#### Características declaradas
- **android.hardware.location**: Hardware de ubicación
- **android.hardware.location.gps**: GPS específico
- **android.hardware.location.network**: Ubicación por red

### 2. Clases Principales

#### LocationManager (utils/LocationManager.java)
**Funcionalidades:**
- Verificación y solicitud de permisos
- Inicio/detención de actualizaciones de ubicación
- Obtención de última ubicación conocida
- Cálculo de distancias entre ubicaciones
- Verificación de ubicaciones dentro de un radio
- Callbacks para manejo de ubicación y errores

**Métodos principales:**
```java
// Verificar permisos
boolean hasLocationPermissions()
boolean isLocationEnabled()

// Gestión de ubicación
void startLocationUpdates()
void stopLocationUpdates()
void getLastKnownLocation(LocationCallback callback)

// Utilidades
float calculateDistance(Location loc1, Location loc2)
boolean isLocationWithinRadius(Location center, Location target, float radius)
```

#### LocationService (services/LocationService.java)
**Funcionalidades:**
- Servicio en primer plano para seguimiento continuo
- Notificaciones persistentes
- Observadores de ubicación
- Manejo de errores y reconexión automática

#### UbicacionEntity (database/entities/UbicacionEntity.java)
**Campos de la base de datos:**
- `id`: Identificador único
- `usuarioId`: ID del usuario
- `latitud`: Coordenada de latitud
- `longitud`: Coordenada de longitud
- `precision`: Precisión en metros
- `fechaRegistro`: Timestamp del registro
- `direccion`: Dirección textual (opcional)
- `ciudad`: Ciudad (opcional)
- `esCercanaASucursal`: Booleano si está cerca de sucursal
- `sucursalId`: ID de sucursal cercana (opcional)
- `distanciaASucursal`: Distancia a sucursal en metros
- `sincronizado`: Estado de sincronización

#### UbicacionDao (database/dao/UbicacionDao.java)
**Operaciones de base de datos:**
- CRUD básico (insertar, actualizar, eliminar, consultar)
- Consultas por usuario, fecha, área geográfica
- Análisis de ubicaciones (conteos, estadísticas)
- Gestión de sincronización
- Limpieza de datos antiguos

#### UbicacionRepository (repository/UbicacionRepository.java)
**Capa de abstracción:**
- Integración entre LocationManager y base de datos
- Operaciones asíncronas con ExecutorService
- Verificación automática de sucursales cercanas
- Sincronización con servidor (preparado para API)
- Gestión de estados y notificaciones

#### UbicacionViewModel (viewmodel/UbicacionViewModel.java)
**Gestión de UI:**
- LiveData para observación reactiva
- Manejo de permisos y estados
- Control de seguimiento de ubicación
- Integración con repositorio
- Gestión del ciclo de vida

### 3. Interfaz de Usuario

#### UbicacionActivity (activities/UbicacionActivity.java)
**Características:**
- Gestión completa de permisos
- Control de seguimiento automático
- Visualización de estadísticas
- Lista de ubicaciones recientes
- Sincronización y limpieza de datos

#### UbicacionAdapter (adapters/UbicacionAdapter.java)
**Funcionalidades:**
- Visualización de ubicaciones en RecyclerView
- Indicadores visuales de precisión y sincronización
- Información de sucursales cercanas
- Click listeners para interacciones

### 4. Base de Datos

#### Migración 5 → 6
```sql
CREATE TABLE ubicaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    latitud REAL NOT NULL,
    longitud REAL NOT NULL,
    precision REAL NOT NULL,
    fecha_registro INTEGER NOT NULL,
    direccion TEXT,
    ciudad TEXT,
    es_cercana_a_sucursal INTEGER DEFAULT 0,
    sucursal_id INTEGER,
    distancia_a_sucursal REAL,
    sincronizado INTEGER DEFAULT 0
);
```

## Uso de las Funcionalidades

### 1. Inicialización Básica

```java
// En una Activity
UbicacionViewModel viewModel = new ViewModelProvider(this).get(UbicacionViewModel.class);
viewModel.setCurrentUserId(usuarioId);

// Observar estado de permisos
viewModel.getHasLocationPermissions().observe(this, hasPermissions -> {
    if (!hasPermissions) {
        // Solicitar permisos
        viewModel.requestLocationPermissions();
    }
});
```

### 2. Seguimiento de Ubicación

```java
// Iniciar seguimiento automático
viewModel.startLocationTracking();

// Obtener ubicación una sola vez
viewModel.getCurrentLocationOnce();

// Detener seguimiento
viewModel.stopLocationTracking();
```

### 3. Consulta de Datos

```java
// Obtener ubicaciones del usuario
viewModel.getUserLocations().observe(this, locations -> {
    // Actualizar UI con las ubicaciones
});

// Obtener estadísticas
viewModel.getLocationCount().observe(this, count -> {
    // Mostrar conteo de ubicaciones
});

viewModel.getBranchVisitCount().observe(this, visits -> {
    // Mostrar visitas a sucursales
});
```

### 4. Gestión de Datos

```java
// Sincronizar con servidor
viewModel.syncLocations();

// Limpiar ubicaciones antiguas (30 días)
viewModel.cleanOldLocations(30);

// Eliminar todas las ubicaciones del usuario
viewModel.deleteAllUserLocations();
```

## Configuración de Permisos

### 1. Solicitud de Permisos
La aplicación solicita permisos de forma progresiva:
1. ACCESS_COARSE_LOCATION (básico)
2. ACCESS_FINE_LOCATION (preciso)
3. ACCESS_BACKGROUND_LOCATION (segundo plano, solo si es necesario)

### 2. Manejo de Denegación
- Explicación clara del uso de permisos
- Redirección a configuración si es necesario
- Funcionalidad degradada sin permisos

## Consideraciones de Rendimiento

### 1. Batería
- Uso de LocationRequest con intervalos optimizados
- Detención automática en onPause
- Servicio en primer plano solo cuando es necesario

### 2. Almacenamiento
- Limpieza automática de datos antiguos
- Compresión de datos de ubicación
- Sincronización eficiente

### 3. Red
- Sincronización por lotes
- Retry automático en caso de fallo
- Almacenamiento local como respaldo

## Próximas Mejoras

1. **Integración con API**: Sincronización real con servidor
2. **Geofencing**: Notificaciones al entrar/salir de áreas
3. **Mapas**: Visualización de ubicaciones en mapa
4. **Analytics**: Análisis de patrones de ubicación
5. **Optimización**: Mejoras en precisión y consumo de batería

## Archivos Creados/Modificados

### Nuevos Archivos
- `utils/LocationManager.java`
- `services/LocationService.java`
- `database/entities/UbicacionEntity.java`
- `database/dao/UbicacionDao.java`
- `repository/UbicacionRepository.java`
- `viewmodel/UbicacionViewModel.java`
- `activities/UbicacionActivity.java`
- `adapters/UbicacionAdapter.java`
- `res/layout/activity_ubicacion.xml`
- `res/layout/item_ubicacion.xml`

### Archivos Modificados
- `AndroidManifest.xml`: Permisos, características y servicios
- `database/CafeFidelidadDatabase.java`: Nueva entidad y migración

## Conclusión

Las funcionalidades de ubicación están completamente implementadas y listas para usar. El sistema es robusto, eficiente y sigue las mejores prácticas de Android para el manejo de ubicación y permisos.