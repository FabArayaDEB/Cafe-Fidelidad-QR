# Café Fidelidad QR — Documentación Actualizada

Aplicación Android para fidelización en cafeterías con registro de compras y canje de beneficios mediante QR, arquitectura MVVM en Java y almacenamiento local en SQLite.

## Características Principales

- Autenticación local con roles (cliente y administrador) y gestión de sesión.
- Catálogo de productos con búsqueda, filtros y estados de disponibilidad.
- Beneficios y canjes basados en puntos por visitas y compras.
- Escaneo de códigos QR para registro y validación de transacciones.
- Módulo de administración: productos, sucursales, beneficios y reportes.
- Servicio de ubicación en primer plano para funcionalidades basadas en localización.

## Estructura del Proyecto

```
caffeFidelidaDemo/
├── app/
│   └── src/main/java/com/example/cafefidelidaqrdemo/
│       ├── AdminMainActivity.java
│       ├── BeneficiosActivity.java
│       ├── CatalogoActivity.java
│       ├── ClienteMainActivity.java
│       ├── DatosPersonalesActivity.java
│       ├── DetalleProductoActivity.java
│       ├── EditarPerfilActivity.java
│       ├── HistorialActivity.java
│       ├── LoginActivity.java
│       ├── MainActivity.java
│       ├── OpcionesLoginActivity.java
│       ├── RecuperarPassActivity.java
│       ├── RegistroActivity.java
│       ├── adapters/
│       │   ├── BeneficioAdapter.java
│       │   ├── BeneficiosAdminAdapter.java
│       │   ├── BeneficiosDisponiblesAdapter.java
│       │   ├── ClientesAdminAdapter.java
│       │   ├── HistorialAdapter.java
│       │   ├── ProductosAdapter.java
│       │   └── SucursalesAdapter.java
│       ├── database/
│       │   └── CafeFidelidadDB.java            # SQLiteOpenHelper con tablas y CRUD
│       ├── fragments/
│       │   ├── FragmentMisBeneficios.java
│       │   ├── FragmentPerfil.java
│       │   ├── FragmentProductos.java
│       │   └── FragmentSucursales.java
│       ├── managers/
│       │   └── BeneficioManager.java
│       ├── models/
│       │   ├── Beneficio.java, Canje.java, Cliente.java, Producto.java,
│       │   │   Sucursal.java, Visita.java, RecentActivity.java
│       ├── network/
│       │   ├── ApiClient.java, ApiService.java, RetrofitClient.java
│       ├── repository/
│       │   ├── AuthRepository.java, ProductoRepository.java, SucursalRepository.java,
│       │   │   BeneficioRepository.java, ClienteRepository.java, CanjeRepository.java,
│       │   │   AdminRepository.java, VisitaRepository.java, VisitaAdminRepository.java
│       │   ├── base/ (BaseRepository)
│       │   └── interfaces/
│       ├── services/
│       │   └── LocationService.java
│       ├── ui/
│       │   ├── admin/ (panel administración)
│       │   ├── cliente/
│       │   ├── adapters/
│       │   └── dialogs/
│       ├── utils/
│       │   ├── SessionManager.java, NetworkUtils.java, QRGenerator.java, QRValidator.java,
│       │   │   LocationManager.java, LocationUtils.java, SearchManager.java
│       └── viewmodels/
│           ├── LoginViewModel.java, ProductosViewModel.java, SucursalesViewModel.java,
│           │   MisBeneficiosViewModel.java, MainViewModel.java, ClienteQRViewModel.java
└── app/src/main/res/ (layouts, drawables, menus, values, xml)
```

## Arquitectura

- MVVM en Java con `LiveData` y `AndroidViewModel`.
- Repositorios con `ExecutorService` para operaciones asíncronas y estados compartidos.
- Base de datos local con `SQLiteOpenHelper` (`CafeFidelidadDB`) y CRUD manual.
- Comunicación HTTP con Retrofit y modelos JSON via Gson.
- Capa de utilidades para sesión, red, QR y ubicación.

### Flujo de Datos

```
UI (Activities/Fragments)
  ↕
ViewModels (LiveData)
  ↕
Repositories (ExecutorService + LiveData)
  ↕
SQLite (CafeFidelidadDB) ↔ Retrofit (ApiService)
```

### Ejemplos de Flujos

- Login: `LoginActivity` → `LoginViewModel` → `AuthRepository` → `SessionManager` (login local, roles).
- Productos: `FragmentProductos` → `ProductosViewModel` → `ProductoRepository` → `CafeFidelidadDB`/`ApiService` (carga, búsqueda y filtros).

## Librerías Utilizadas (build.gradle)

- UI: `viewBinding`, `dataBinding`, `material`, `constraintlayout`, `circleimageview`, `glide`.
- QR: `zxing-android-embedded`, `zxing-core`.
- Red: `retrofit`, `converter-gson`, `okhttp`, `logging-interceptor`, `gson`.
- Cámara y navegación: `camera-core/camera2/lifecycle/view`, `navigation-fragment/ui`.
- Ubicación y mapas: `play-services-location`, `play-services-maps`.
- Testing: `junit`, `mockito`, `arch-core-testing`.
- Nota: Dependencias de `room` y `workmanager` están presentes pero no se usan en la implementación actual; la base de datos real es `SQLiteOpenHelper`.

## Base de Datos (Resumen)

- Archivo: `cafe_fidelidad.db` via `CafeFidelidadDB`.
- Tablas: `clientes`, `productos`, `sucursales`, `beneficios`, `visitas`, `canjes`.
- Relaciones: claves foráneas entre `visitas`/`canjes` y `clientes`/`productos`/`beneficios`/`sucursales`.
- Operaciones: CRUD completas para modelos principales con ejecución en background.

## Servicios y Utilidades

- `LocationService`: servicio en primer plano con notificación y actualización continua de ubicación.
- `SessionManager`: sesión de usuario con `SharedPreferences` (id, email, nombre, estado login).
- `NetworkUtils`: verificación de conectividad (`ConnectivityManager`).
- `QRGenerator`/`QRValidator`: utilidades para generar/validar QR (apoya uso de `jjwt`).

## Adapters y UI

- `ProductosAdapter`: `ListAdapter` con `DiffUtil`, soporta `ClientViewHolder` y `AdminViewHolder` (clicks, editar/eliminar, activar/desactivar).
- `BeneficioAdapter`, `BeneficiosAdminAdapter`, `SucursalesAdapter`, `ClientesAdminAdapter`, `HistorialAdapter`: listas especializadas con listeners.
- Fragments y Activities usan `ViewBinding` y estados de carga/error mediante `LiveData`.

## Requisitos del Sistema

- Min SDK: `26`, Target SDK: `36`.
- Permisos: `CAMERA`, `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `FOREGROUND_SERVICE`.

## Configuración

- Base URL: configurada en `RetrofitClient` (`https://api.cafefidelidad.com/`).
- Habilitado `viewBinding` y `dataBinding` en `app/build.gradle`.

## Flujos de Usuario

- Cliente: Login → Perfil/QR → Productos → Beneficios/Canjes → Sucursales.
- Administrador: Login → Dashboard → Productos/Sucursales/Beneficios → Reportes.

## Seguridad

- Comunicación HTTPS mediante Retrofit/OkHttp.
- Validación de QR y soporte de tokens (JWT libs disponibles).
- Gestión de sesión local segura.

**Versión**: 1.0.0  
**Última actualización**: Octubre 2025