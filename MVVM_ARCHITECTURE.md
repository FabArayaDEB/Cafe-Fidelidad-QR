# Arquitectura MVVM con StateFlow y LiveData

## Componentes Principales

### 1. Database Layer (Room)

Arquitectura de base de datos consolidada utilizando Room:

#### Entidades Principales
- **Ubicación**: `database/entities/`
- **Entidades**:
  - `ClienteEntity.java` - Gestión de clientes y puntos de fidelidad
  - `ProductoEntity.java` - Catálogo de productos
  - `VisitaEntity.java` - Registro de visitas de clientes
  - `TransaccionEntity.java` - Historial de transacciones
  - `CanjeEntity.java` - Registro de canjes de beneficios
  - `BeneficioEntity.java` - Catálogo de beneficios disponibles
  - `SucursalEntity.java` - Información de sucursales
  - `TableroEntity.java` - Dashboard de métricas
  - `ReporteEntity.java` - Datos para reportes administrativos

#### DAOs (Data Access Objects)
- **Ubicación**: `database/dao/`
- **Funcionalidades**:
  - Operaciones CRUD optimizadas
  - Consultas específicas por entidad
  - Soporte para sincronización offline
  - Métodos de agregación para reportes

#### Modelos de Vista
- **Ubicación**: `database/models/`
- **Propósito**: Clases auxiliares para consultas complejas y métricas

### 2. ViewModels

Los ViewModels implementan patrones MVVM estrictos:

#### MainViewModel
- **Ubicación**: `viewmodels/MainViewModel.java`
- **Responsabilidades**: 
  - Manejo de navegación
  - Estado de autenticación
  - Título de toolbar
- **Características**:
  - Usa StateFlow para estado reactivo
  - Proporciona LiveData para Data Binding
  - Manejo de errores centralizado

#### LoginViewModel
- **Ubicación**: `viewmodels/LoginViewModel.java`
- **Responsabilidades**:
  - Lógica de autenticación
  - Validación de formularios
  - Manejo de estados de carga
- **Características**:
  - StateFlow para estado interno
  - LiveData para UI binding
  - Validación reactiva de campos

### 3. Repository Layer

Capa de repositorios implementada para abstracción de datos:

#### AuthRepository
- **Ubicación**: `repository/AuthRepository.java`
- **Funciones**: Login, logout, gestión de sesiones

#### ClienteRepository
- **Ubicación**: `repository/ClienteRepository.java`
- **Funciones**: CRUD de clientes, sincronización

#### ProductoRepository
- **Ubicación**: `repository/ProductoRepository.java`
- **Funciones**: Gestión de productos, conversión entre entidades y modelos

#### AdminRepository
- **Ubicación**: `repository/AdminRepository.java`
- **Funciones**: Operaciones administrativas, reportes, métricas

### 4. Use Cases

Lógica de negocio encapsulada en Use Cases:

#### AuthUseCase
- **Ubicación**: `domain/usecases/AuthUseCase.java`
- **Funciones**: 
  - Validación de credenciales
  - Manejo de errores de autenticación
  - Traducción de errores a mensajes de usuario

#### TransaccionQRUseCase
- **Ubicación**: `domain/usecases/TransaccionQRUseCase.java`
- **Funciones**:
  - Validación de códigos QR
  - Registro de transacciones
  - Actualización de puntos

#### PuntosUseCase
- **Ubicación**: `domain/usecases/PuntosUseCase.java`
- **Funciones**:
  - Cálculo de puntos
  - Gestión de beneficios
  - Canje de recompensas

### 4. Data Binding

Implementación de Data Binding para vinculación reactiva:

#### MainActivity
- Data Binding configurado con MainViewModel
- Observación automática de cambios de estado
- Título de toolbar reactivo

#### LoginActivity
- Data Binding con LoginViewModel
- Validación de formularios en tiempo real
- Manejo de estados de carga

## Patrones Implementados

### StateFlow vs LiveData

**StateFlow**:
- Usado para estado interno del ViewModel
- Manejo reactivo de estado
- Mejor rendimiento para operaciones complejas
- Compatibilidad con Coroutines

**LiveData**:
- Usado para compatibilidad con Data Binding
- Observación lifecycle-aware
- Integración con componentes de Android

## Beneficios de la Arquitectura

### 1. Separación de Responsabilidades
- **View**: Solo maneja UI y eventos de usuario
- **ViewModel**: Lógica de presentación y estado
- **Repository**: Abstracción de fuentes de datos
- **Use Cases**: Lógica de negocio específica

## Estado del Proyecto

### ✅ Completado
- Migración de ViewModels principales (MainViewModel, MisBeneficiosViewModel, PerfilViewModel)
- Migración completa de ViewModels de administrador
- Implementación de BaseRepository para funcionalidad común
- Configuración de interfaces de repositorio (IUserRepository, IProductoRepository)
- Estructura de directorios consolidada
- Optimización completa de consultas de base de datos en DAOs
- Consolidación de repositorios duplicados
- Eliminación de DAOs duplicados
- Implementación de índices de base de datos para optimización

### 🔄 En Progreso
- Actualización de documentación técnica
- Implementación de casos de uso (UseCases)

### 📋 Pendiente
- Tests unitarios
- Documentación completa de la API
- Implementación de cache avanzado
- Métricas de rendimiento

## Estructura de Directorios

```
app/src/main/java/com/example/cafefidelidaqr/
├── database/
│   ├── entities/         # Entidades Room consolidadas
│   ├── dao/             # Data Access Objects
│   ├── models/          # Modelos auxiliares para consultas
│   └── AppDatabase.java # Configuración de base de datos
├── repository/          # Capa de repositorios
├── data/
│   ├── dao/            # DAOs adicionales
│   ├── converter/      # Convertidores de datos
│   └── repositories/   # Repositorios específicos
├── domain/
│   └── usecases/       # Casos de uso
├── viewmodels/         # ViewModels
├── ui/
│   ├── admin/         # UI de administrador
│   └── cliente/       # UI de cliente
├── network/
│   └── models/        # Modelos de red
├── adapters/          # Adaptadores RecyclerView
└── utils/             # Utilidades
```

