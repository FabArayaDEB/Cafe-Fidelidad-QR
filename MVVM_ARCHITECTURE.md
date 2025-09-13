# Arquitectura MVVM con StateFlow y LiveData

## Resumen

Este proyecto implementa una arquitectura MVVM (Model-View-ViewModel) moderna utilizando StateFlow para manejo de estado reactivo y LiveData para compatibilidad con Data Binding.

## Componentes Principales

### 1. ViewModels

Los ViewModels han sido refactorizados para seguir patrones MVVM estrictos:

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

### 2. Repository Layer

Capa de repositorios implementada para abstracción de datos:

#### AuthRepository
- **Ubicación**: `data/repositories/AuthRepository.java`
- **Funciones**: Login, logout, gestión de sesiones

#### ClienteRepository
- **Ubicación**: `data/repositories/ClienteRepository.java`
- **Funciones**: CRUD de clientes, sincronización

### 3. Use Cases

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

### Ejemplo de Implementación

```java
// StateFlow para estado interno
private final MutableStateFlow<String> _toolbarTitle = new MutableStateFlow<>("Mi Perfil");
public StateFlow<String> toolbarTitle = _toolbarTitle.asStateFlow();

// LiveData para Data Binding
public LiveData<String> toolbarTitleLiveData = toolbarTitle.asLiveData();

// Getters para ambos tipos
public StateFlow<String> getToolbarTitle() {
    return toolbarTitle;
}

public LiveData<String> getToolbarTitleLiveData() {
    return toolbarTitleLiveData;
}
```

## Beneficios de la Arquitectura

### 1. Separación de Responsabilidades
- **View**: Solo maneja UI y eventos de usuario
- **ViewModel**: Lógica de presentación y estado
- **Repository**: Abstracción de fuentes de datos
- **Use Cases**: Lógica de negocio específica

### 2. Testabilidad
- ViewModels independientes de Android Framework
- Use Cases con lógica de negocio aislada
- Repositorios con interfaces mockeable

### 3. Mantenibilidad
- Código organizado por responsabilidades
- Fácil localización de bugs
- Escalabilidad mejorada

### 4. Reactividad
- Estado reactivo con StateFlow
- Actualizaciones automáticas de UI
- Manejo eficiente de cambios de estado

## Estructura de Directorios

```
app/src/main/java/com/example/cafefidelidaqrdemo/
├── data/
│   ├── entities/          # Entidades de datos
│   └── repositories/      # Capa de repositorios
├── domain/
│   └── usecases/         # Casos de uso
├── viewmodels/           # ViewModels
├── ui/
│   ├── admin/           # UI de administrador
│   └── cliente/         # UI de cliente
└── utils/               # Utilidades
```

## Próximos Pasos

1. **Migración Completa**: Migrar todos los ViewModels restantes a StateFlow
2. **Testing**: Implementar tests unitarios para ViewModels y Use Cases
3. **Optimización**: Optimizar rendimiento con StateFlow avanzado
4. **Documentación**: Expandir documentación de componentes específicos

## Convenciones de Código

### Naming
- StateFlow privados: `_nombreVariable`
- StateFlow públicos: `nombreVariable`
- LiveData para binding: `nombreVariableLiveData`
- Getters StateFlow: `getNombreVariable()`
- Getters LiveData: `getNombreVariableLiveData()`

### Estructura de ViewModel
1. Dependencias
2. StateFlow privados
3. StateFlow públicos
4. LiveData para binding
5. Constructor
6. Métodos públicos
7. Métodos privados
8. Getters

Esta arquitectura proporciona una base sólida para el desarrollo escalable y mantenible de la aplicación de fidelización con códigos QR.