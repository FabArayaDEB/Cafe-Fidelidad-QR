# ViewModels Layer - Capa de ViewModels

## Descripción General

El directorio `viewmodels` contiene todos los **ViewModels** del proyecto CafeFidelidaQRDemo, implementando el patrón **MVVM (Model-View-ViewModel)**. Los ViewModels actúan como intermediarios entre la UI (Activities/Fragments) y la lógica de negocio (UseCases/Repositories), gestionando el estado de la interfaz de usuario y sobreviviendo a cambios de configuración.

Los ViewModels en esta capa proporcionan:
- **Gestión de Estado**: Manejo del estado de la UI con LiveData/Observable
- **Supervivencia a Configuración**: Mantienen datos durante rotaciones de pantalla
- **Separación de Responsabilidades**: Aíslan la lógica de presentación de la UI
- **Comunicación Reactiva**: Observación de cambios de datos en tiempo real
- **Validación de Formularios**: Validación en tiempo real de campos de entrada
- **Manejo de Errores**: Gestión centralizada de errores y estados de carga
- **Navegación**: Control de flujos de navegación entre pantallas
- **Caché de Datos**: Almacenamiento temporal de datos para mejor rendimiento

Cada ViewModel extiende de `BaseViewModel` para heredar funcionalidades comunes como manejo de errores, estados de carga, validaciones y comunicación con repositorios. La arquitectura sigue el patrón Factory para la creación de ViewModels con dependencias inyectadas.

Los ViewModels se organizan por funcionalidad (auth, admin, cliente, shared) y utilizan LiveData para la comunicación reactiva con la UI, garantizando que los datos se mantengan sincronizados y la interfaz responda automáticamente a los cambios.

## Estado del Proyecto

### ✅ Implementado
- BaseViewModel con funcionalidad común
- ViewModels principales (Login, Admin, Cliente)
- Factory pattern para creación de ViewModels
- Manejo de estados de carga y errores
- Validación de formularios
- Integración con Use Cases

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

La capa de ViewModels proporciona una separación clara entre la UI y la lógica de negocio, implementando el patrón MVVM de forma robusta. Los ViewModels manejan el estado de la UI, sobreviven a cambios de configuración y proporcionan una interfaz limpia para la comunicación con las capas de datos.

La implementación con LiveData permite una programación reactiva que mantiene la UI sincronizada con los datos, mientras que el patrón Factory facilita la inyección de dependencias y la creación de ViewModels.

---

**Nota**: Esta documentación describe la arquitectura y componentes de la capa de ViewModels del proyecto CafeFidelidaQRDemo. Para implementación específica, consultar los archivos de código correspondientes en el directorio `viewmodels/`.