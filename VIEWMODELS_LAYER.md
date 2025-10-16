# ViewModels Layer - Capa de ViewModels

## Descripción General

El directorio `viewmodels` contiene los **ViewModels** del proyecto, implementando el patrón **MVVM (Model-View-ViewModel)**. Los ViewModels actúan como intermediarios entre la UI (Activities/Fragments) y los repositorios, gestionando el estado de la interfaz y sobreviviendo a cambios de configuración.

Los ViewModels en esta capa proporcionan:
- **Gestión de Estado**: Manejo del estado de la UI con LiveData/Observable
- **Supervivencia a Configuración**: Mantienen datos durante rotaciones de pantalla
- **Separación de Responsabilidades**: Aíslan la lógica de presentación de la UI
- **Comunicación Reactiva**: Observación de cambios de datos en tiempo real
- **Validación de Formularios**: Validación en tiempo real de campos de entrada
- **Manejo de Errores**: Gestión centralizada de errores y estados de carga
- **Navegación**: Control de flujos de navegación entre pantallas
- **Caché de Datos**: Almacenamiento temporal de datos para mejor rendimiento

Los ViewModels usan `LiveData` y, cuando corresponde, `AndroidViewModel` para acceder a `Application`. Los estados de carga y error provienen, en su mayoría, de los repositorios (`BaseRepository`) para evitar duplicaciones.

ViewModels existentes:
- `LoginViewModel`: validación de email/contraseña, login local vía `AuthRepository`, expone `isLoading`, errores y éxito.
- `ProductosViewModel` (AndroidViewModel): gestión de productos, búsqueda y filtros; observa `ProductoRepository` (`isLoading`, errores, offline, empty state).
- `SucursalesViewModel`: gestión y listado de sucursales.
- `MisBeneficiosViewModel`: combina estados de múltiples repositorios con `MediatorLiveData`.
- `MainViewModel`: estados propios para navegación y carga general.
- `ClienteQRViewModel`: estados propios para operaciones de QR.

## Estado del Proyecto

### ✅ Implementado
- ViewModels principales (Login, Productos, Sucursales, Beneficios, Main, ClienteQR)
- Manejo de estados de carga y errores con `LiveData`
- Validación sencilla de formularios (login)
- Observación de estados de repositorios (`isLoading`, errores)

### 🔄 En Desarrollo
- ViewModels de reportes avanzados
- Paginación en listas grandes
- Caché local de datos
- Sincronización offline

### 📋 Futuras Mejoras
- Migración a Kotlin y Coroutines
- StateFlow en lugar de LiveData
- Compose State Management
- Testing unitario completo
- Inyección de dependencias con Hilt
- ViewModels compartidos entre pantallas
- Estado persistente entre sesiones

## Mejores Prácticas

### 1. Separación de Responsabilidades
- **ViewModel**: Solo lógica de presentación
- **Repository**: Acceso a datos
- **UseCase**: Lógica de negocio
- **UI**: Solo presentación

### 2. Gestión de Estado
- **Inmutabilidad**: No exponer MutableLiveData
- **Estados Claros**: Definir estados específicos
- **Error Handling**: Manejo consistente de errores
- **Loading States**: Indicadores de carga apropiados
 - **Estados del Repositorio**: Preferir `repository.getIsLoading()` y evitar duplicación

### 3. Performance
- **Lazy Loading**: Cargar datos solo cuando se necesiten
- **Caché**: Cachear datos frecuentemente accedidos
- **Paginación**: Para listas grandes
- **Debouncing**: Para búsquedas en tiempo real

### 4. Testing
- **Unit Tests**: Para lógica de ViewModels
- **Mock Dependencies**: Usar mocks para repositorios
- **LiveData Testing**: Usar InstantTaskExecutorRule
- **Edge Cases**: Probar casos límite

## Conclusión

La capa de ViewModels proporciona una separación clara entre la UI y el acceso a datos, implementando MVVM con `LiveData`. Los estados de los repositorios se exponen a la UI de forma reactiva, y se evita duplicar `isLoading` cuando ya lo gestiona el repositorio.
