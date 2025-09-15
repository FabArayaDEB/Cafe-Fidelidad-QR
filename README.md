# Café Fidelidad QR 

Aplicación Android de sistema de fidelidad para cafeterías que utiliza códigos QR para el registro de compras y acumulación de puntos.

## Características Principales

- **Sistema de Autenticación**: Registro e inicio de sesión con Firebase Authentication
- **Perfil de Usuario**: Gestión de datos personales y visualización de información
- **Sistema de Puntos**: Acumulación y seguimiento de puntos de fidelidad
- **Escaneo QR**: Lectura de códigos QR para registrar compras
- **Historial de Transacciones**: Registro completo de compras y puntos ganados
- **Panel de Administración**: Gestión de productos, beneficios, sucursales y clientes
- **Beneficios**: Sistema de recompensas basado en puntos acumulados

## Estructura del Proyecto

```
cafeFidelidaQRdemo/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cafefidelidaqrdemo/
│   │   │   │   ├── activities/          # Actividades principales
│   │   │   │   │   ├── QRScannerActivity.java
│   │   │   │   │   ├── BeneficiosActivity.java
│   │   │   │   │   ├── DatosPersonalesActivity.java
│   │   │   │   │   ├── EditarPerfilActivity.java
│   │   │   │   │   ├── HistorialActivity.java
│   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   ├── OpcionesLoginActivity.java
│   │   │   │   │   ├── RecuperarPassActivity.java
│   │   │   │   │   └── RegistroActivity.java
│   │   │   │   ├── fragments/           # Fragmentos de UI
│   │   │   │   │   ├── FragmentHistorial.java
│   │   │   │   │   ├── FragmentPerfil.java
│   │   │   │   │   ├── FragmentPuntos.java
│   │   │   │   │   └── FragmentQR.java
│   │   │   │   ├── adapters/            # Adaptadores RecyclerView
│   │   │   │   │   ├── BeneficioAdapter.java
│   │   │   │   │   ├── HistorialAdapter.java
│   │   │   │   │   ├── ProductoAdapter.java
│   │   │   │   │   └── SucursalAdapter.java
│   │   │   │   ├── viewmodels/          # ViewModels MVVM
│   │   │   │   ├── database/            # Capa de base de datos
│   │   │   │   │   ├── entities/        # Entidades Room
│   │   │   │   │   ├── dao/             # Data Access Objects
│   │   │   │   │   ├── models/          # Modelos auxiliares
│   │   │   │   │   │   ├── Beneficio.java
│   │   │   │   │   │   ├── Cliente.java
│   │   │   │   │   │   ├── Producto.java
│   │   │   │   │   │   ├── Sucursal.java
│   │   │   │   │   │   ├── TopCliente.java
│   │   │   │   │   │   └── Transaccion.java
│   │   │   │   │   └── CafeFidelidadDatabase.java
│   │   │   │   ├── repository/          # Capa de repositorios
│   │   │   │   │   ├── base/            # Repositorio base
│   │   │   │   │   └── interfaces/      # Contratos
│   │   │   │   ├── domain/              # Lógica de negocio
│   │   │   │   │   └── usecases/        # Casos de uso
│   │   │   │   ├── network/             # Capa de red
│   │   │   │   ├── sync/                # Sincronización
│   │   │   │   ├── security/            # Seguridad y validación
│   │   │   │   ├── offline/             # Gestión offline
│   │   │   │   ├── managers/            # Gestores especializados
│   │   │   │   ├── workers/             # WorkManager tasks
│   │   │   │   ├── ui/                  # Componentes UI
│   │   │   │   │   └── admin/           # Panel de administración
│   │   │   │   │       ├── FragmentAdminDashboard.java
│   │   │   │   │       ├── FragmentProductosAdmin.java
│   │   │   │   │       └── FragmentSucursalesAdmin.java
│   │   │   │   └── utils/               # Utilidades
│   │   │   │       ├── QRCodeGenerator.java
│   │   │   │       └── QRScanResult.java
│   │   │   ├── res/
│   │   │   │   ├── drawable/            # Recursos gráficos
│   │   │   │   ├── layout/              # Layouts XML
│   │   │   │   ├── values/              # Valores (colores, strings, etc.)
│   │   │   │   └── xml/                 # Configuraciones XML
│   │   │   └── AndroidManifest.xml
│   │   └── androidTest/                 # Tests de instrumentación
│   ├── build.gradle                     # Configuración de build del módulo
│   ├── google-services.json            # Configuración de Firebase
│   └── proguard-rules.pro              # Reglas ProGuard

```
## Arquitectura

### Patrón de Arquitectura
La aplicación sigue una arquitectura **MVVM (Model-View-ViewModel)** con **Clean Architecture**, organizando el código en capas bien definidas:

#### **Capas Principales**

**1. Database Layer (Room)**
- **`database/entities/`**: Entidades de base de datos (ClienteEntity, ProductoEntity, etc.)
- **`database/dao/`**: Data Access Objects para operaciones CRUD (Data Access Objects)
- **`database/models/`**: Modelos auxiliares para consultas complejas
- **`CafeFidelidadDatabase.java`**: Configuración principal de Room

**2. Repository Layer**
- **`repository/`**: Abstrae las fuentes de datos (local/remota)
- **`repository/base/`**: Repositorio base con funcionalidades comunes
- **`repository/interfaces/`**: Contratos de repositorios

**3. Domain Layer**
- **`domain/usecases/`**: Lógica de negocio encapsulada
- **Use Cases**: AuthUseCase, PuntosUseCase, TransaccionQRUseCase

**4. Presentation Layer**
- **`viewmodels/`**: Maneja el estado de la UI y lógica de presentación
- **`ui/`**: Componentes de UI organizados por funcionalidad
- **`fragments/`**: Fragmentos principales de la aplicación

#### **Organización de Directorios**

**Funcionalidades Especializadas:**
- **`network/`**: Capa de red (Retrofit, API services)
- **`sync/`**: Sincronización offline con WorkManager
- **`security/`**: Validación de QR y comunicación segura
- **`offline/`**: Gestión de estado offline
- **`adapters/`**: Adaptadores RecyclerView
- **`utils/`**: Utilidades reutilizables
- **`managers/`**: Gestores especializados

#### 🔄 **Flujo de Datos**
```
UI (Activities/Fragments) 
    ↕️
ViewModels 
    ↕️
Use Cases (Domain) 
    ↕️
Repositories 
    ↕️
DAOs ↔️ Network
    ↕️
Database (Room)
```

#### **Beneficios de la Arquitectura**
- **Separación de Responsabilidades**: Cada capa tiene un propósito específico
- **Testabilidad**: Fácil mockeo de dependencias
- **Mantenibilidad**: Código organizado y fácil de localizar
- **Escalabilidad**: Estructura preparada para crecimiento
- **Reactividad**: StateFlow + LiveData para actualizaciones automáticas

### Tecnologías Utilizadas

#### **Base de Datos y Persistencia**
- **Room Database**: Base de datos local SQLite con ORM
- **Firebase Realtime Database**: Base de datos en tiempo real (legacy)
- **Firebase Storage**: Almacenamiento de archivos
- **SharedPreferences**: Almacenamiento de configuraciones

#### **Autenticación y Seguridad**
- **Firebase Authentication**: Autenticación de usuarios
- **Custom Security**: Validación de QR y comunicación segura
- **Session Management**: Gestión de sesiones de usuario

#### **Networking y Sincronización**
- **Retrofit**: Cliente HTTP para APIs REST
- **WorkManager**: Tareas en background y sincronización
- **OkHttp**: Cliente HTTP con interceptores
- **Gson**: Serialización/deserialización JSON

#### **UI y UX**
- **Material Design**: Componentes de UI modernos
- **Data Binding**: Vinculación reactiva de datos
- **ViewBinding**: Acceso seguro a vistas
- **Navigation Component**: Navegación entre fragmentos

#### **Funcionalidades Específicas**
- **ZXing**: Librería para escaneo de códigos QR
- **Glide**: Carga y cache de imágenes
- **CameraX**: API moderna de cámara

#### **Arquitectura y Patrones**
- **MVVM**: Patrón Model-View-ViewModel
- **StateFlow**: Manejo de estado reactivo
- **LiveData**: Observación lifecycle-aware
- **Coroutines**: Programación asíncrona
- **Dependency Injection**: Inyección de dependencias manual

## Funcionalidades Detalladas

### 1. Sistema de Autenticación
- Registro de nuevos usuarios con email y contraseña
- Inicio de sesión seguro
- Recuperación de contraseña
- Validación de datos de entrada

### 2. Perfil de Usuario
- Visualización de información personal
- Código QR personal para identificación
- Acceso a "Mi Cuenta" para editar datos
- Navegación a historial y configuraciones

### 3. Mi Cuenta (Datos Personales)
- Edición de nombre y apellido
- Actualización de número de teléfono
- Modificación de fecha de nacimiento
- Sincronización automática con Firebase

### 4. Sistema de Puntos
- Visualización de puntos actuales
- Nivel de fidelidad (bajo, medio, alto)
- Historial de transacciones
- Puntos necesarios para siguiente nivel

### 5. Escaneo QR
- Escáner de códigos QR en tiempo real
- Validación de códigos de cliente
- Registro de compras (para administradores)
- Control de flash y entrada manual

### 6. Beneficios
- Lista de beneficios disponibles
- Filtros por estado (Disponibles, Usados, Expirados)
- Sistema de canje por puntos
- Actualización automática de estado

### 7. Panel de Administración
- Gestión de productos del catálogo
- Administración de sucursales
- Control de beneficios y promociones
- Estadísticas y reportes

## Requisitos del Sistema

- **Android API Level**: Mínimo 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Permisos requeridos**:
  - `CAMERA`: Para escaneo QR
  - `INTERNET`: Para conexión a Firebase
  - `ACCESS_NETWORK_STATE`: Para verificar conectividad

## Configuración del Proyecto


## Flujo de Usuario

### Cliente
1. **Registro/Login** → Crear cuenta o iniciar sesión
2. **Perfil** → Ver información personal y QR
3. **Mi Cuenta** → Editar datos personales
4. **Puntos** → Consultar saldo y historial
5. **Beneficios** → Ver y canjear recompensas
6. **QR** → Mostrar código para escaneo

### Administrador
1. **Login** → Acceso con credenciales de admin
2. **Dashboard** → Panel de control principal
3. **Productos** → Gestionar catálogo
4. **Sucursales** → Administrar ubicaciones
5. **Escáner** → Registrar compras de clientes

## Seguridad

- Autenticación segura con Firebase
- Validación de datos en cliente y servidor
- Reglas de seguridad en Firebase Database
- Encriptación de comunicaciones HTTPS


**Versión**: 1.0.0  
**Última actualización**: Enero 2025