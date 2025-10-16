# Repository Layer - Capa de Repositorios

## Descripción General

El directorio `repository` implementa la **capa de acceso a datos** (local y remoto) y sirve de puente entre ViewModels y las fuentes de información: `CafeFidelidadDB` (SQLite) y `ApiService` (Retrofit).

Características clave:
- Cada repositorio extiende `BaseRepository`, que provee estados compartidos vía `LiveData`: `isLoading`, `errorMessage`, `successMessage`, `isOffline`.
- Operaciones asíncronas mediante `ExecutorService` para no bloquear la UI.
- Callbacks de resultado para operaciones de larga duración.
- Integración con SQLite (`CafeFidelidadDB`) para CRUD y consultas.
- Integración con API (`ApiService`) para sincronización y operaciones remotas.

Repositorios existentes:
- `AuthRepository`, `ProductoRepository`, `SucursalRepository`, `BeneficioRepository`, `ClienteRepository`, `CanjeRepository`, `VisitaRepository`, `VisitaAdminRepository`, `AdminRepository`.

## Estado del Proyecto

### ✅ Implementado
- BaseRepository con `LiveData` de estados y `ExecutorService`
- Repositorios principales conectados a `CafeFidelidadDB` y `ApiService`
- CRUD de productos, clientes, sucursales, beneficios, visitas y canjes
- Búsqueda y filtrado de productos (categoría, disponibilidad, activo)
- Autenticación local con `SessionManager` y roles
- Interfaces y callbacks para operaciones asíncronas

### 🔄 En Desarrollo
- Estrategias de sincronización con servidor (selectiva)
- Métricas de rendimiento y profiling de consultas
- Testing automatizado de repositorios
- Documentación detallada de endpoints y flujos


## Mejores Prácticas

### 1. Separación de Responsabilidades
- **Repository**: Solo acceso a datos
- **ViewModel**: Solo lógica de presentación
- **UseCase**: Solo lógica de negocio

### 2. Manejo de Threading
- Ejecutar operaciones en `ExecutorService`
- Publicar resultados/estados vía `LiveData` (main thread)
- Evitar fugas con cancelación y limpieza adecuada

### 3. Gestión de Memoria
- Cleanup de recursos en onDestroy
- Weak references para callbacks
- Caché con límites de tamaño

### 4. Seguridad
- Validación de datos de entrada
- Sanitización de queries
- Manejo seguro de credenciales

## Ejemplos de Flujos

- Productos: `ProductosViewModel` → `ProductoRepository` → `CafeFidelidadDB` (carga inicial) y `ApiService` (sync opcional). Estados expuestos por `BaseRepository`.
- Login: `LoginViewModel` → `AuthRepository` → validación local y `SessionManager`. `LiveData` de usuario actual y `isLoading`.

## Conclusión

La capa de repositorios ofrece una abstracción clara y reactiva sobre fuentes de datos locales (SQLite) y remotas (Retrofit). El uso de `BaseRepository` estandariza estados y threading, facilitando mantenimiento y escalabilidad.