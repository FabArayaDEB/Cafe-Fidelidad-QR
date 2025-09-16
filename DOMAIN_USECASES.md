# Domain Use Cases - Casos de Uso del Dominio

## Descripción General

El directorio `domain/usecases` implementa la **capa de lógica de negocio** en la arquitectura Clean Architecture/MVVM del proyecto CafeFidelidaQRDemo. Los Use Cases encapsulan las reglas de negocio específicas de la aplicación, proporcionando una separación clara entre la lógica de presentación, acceso a datos e interfaz de usuario.

## Estructura del Directorio

```
domain/usecases/
├── AuthUseCase.java           # Lógica de autenticación y registro
├── PuntosUseCase.java         # Sistema de puntos y beneficios
└── TransaccionQRUseCase.java  # Procesamiento de transacciones QR
```

## Casos de Uso Implementados

### 1. AuthUseCase

**Ubicación**: `domain/usecases/AuthUseCase.java`

**Propósito**: Gestiona toda la lógica de negocio relacionada con autenticación y registro de usuarios.

**Responsabilidades**:
- Validación de credenciales de usuario
- Manejo y traducción de errores de autenticación
- Coordinación entre AuthRepository y ClienteRepository
- Creación de perfiles de usuario completos
- Gestión de sesiones de usuario

**Métodos Principales**:
```java
// Autenticación de usuario
void loginUser(String email, String password, AuthCallback callback)

// Registro de nuevo usuario
void registerUser(ClienteEntity nuevoCliente, String password, AuthCallback callback)

// Validación de credenciales
boolean validateCredentials(String email, String password)

// Traducción de errores
String translateAuthError(String error)
```

**Callbacks**:
- `AuthCallback`: Para operaciones de autenticación
- `RegistrationCallback`: Para registro de usuarios

### 2. PuntosUseCase

**Ubicación**: `domain/usecases/PuntosUseCase.java`

**Propósito**: Implementa toda la lógica del sistema de puntos, beneficios y recompensas de fidelidad.

**Responsabilidades**:
- Cálculo de puntos por transacciones
- Gestión de beneficios disponibles
- Validación de canjes de recompensas
- Determinación de niveles de cliente
- Aplicación de reglas de negocio de fidelidad

**Constantes de Negocio**:
```java
private static final int PUNTOS_CAFE_GRATIS = 100;
private static final int PUNTOS_DESCUENTO_10 = 50;
private static final int PUNTOS_DESCUENTO_20 = 150;
private static final int PUNTOS_POSTRE_GRATIS = 80;
private static final int PUNTOS_BEBIDA_PREMIUM = 120;
```

**Métodos Principales**:
```java
// Obtener beneficios disponibles
void getBeneficiosDisponibles(String clienteId, BeneficiosCallback callback)

// Canjear beneficio
void canjearBeneficio(String clienteId, String beneficioId, PuntosCallback callback)

// Calcular puntos por compra
int calcularPuntosPorCompra(double montoCompra)

// Verificar elegibilidad para beneficio
boolean esElegibleParaBeneficio(int puntosCliente, String beneficioId)
```

**Modelo de Beneficio**:
```java
public static class Beneficio {
    private String id;
    private String nombre;
    private String descripcion;
    private int puntosRequeridos;
    private String icono;
    private boolean disponible;
}
```

**Callbacks**:
- `PuntosCallback`: Para operaciones de puntos
- `BeneficiosCallback`: Para lista de beneficios

### 3. TransaccionQRUseCase

**Ubicación**: `domain/usecases/TransaccionQRUseCase.java`

**Propósito**: Maneja toda la lógica de procesamiento de transacciones mediante códigos QR.

**Responsabilidades**:
- Validación de códigos QR escaneados
- Procesamiento de transacciones
- Actualización automática de puntos del cliente
- Registro de historial de transacciones
- Coordinación entre múltiples repositorios

**Métodos Principales**:
```java
// Procesar código QR escaneado
void procesarCodigoQR(String qrData, TransaccionCallback callback)

// Validar formato de QR
boolean validarFormatoQR(String qrData)

// Registrar transacción
void registrarTransaccion(TransaccionEntity transaccion, TransaccionCallback callback)

// Actualizar puntos del cliente
void actualizarPuntosCliente(String clienteId, int puntosGanados, PuntosCallback callback)
```

**Callbacks**:
- `TransaccionCallback`: Para operaciones de transacción
- `QRValidationCallback`: Para validación de QR

## Arquitectura y Flujo de Datos

### Posición en la Arquitectura MVVM

```
UI Layer (Activities/Fragments)
        ↕️
Presentation Layer (ViewModels)
        ↕️
Domain Layer (Use Cases) ← ESTA CAPA
        ↕️
Data Layer (Repositories)
        ↕️
Database/Network (Room/Retrofit)
```

### Principios de Diseño

1. **Single Responsibility**: Cada Use Case tiene una responsabilidad específica
2. **Dependency Inversion**: Dependen de abstracciones, no de implementaciones
3. **Clean Architecture**: Separación clara de capas
4. **Testabilidad**: Fácil testing unitario de reglas de negocio

### Patrón de Implementación

Cada Use Case sigue este patrón:

```java
public class ExampleUseCase {
    // Dependencias (Repositories)
    private final Repository repository;
    
    // Constructor con inyección de dependencias
    public ExampleUseCase() {
        this.repository = Repository.getInstance();
    }
    
    // Método principal del caso de uso
    public void executeUseCase(InputParams params, Callback callback) {
        // 1. Validar entrada
        if (!validateInput(params)) {
            callback.onError("Invalid input");
            return;
        }
        
        // 2. Aplicar lógica de negocio
        ProcessedData result = applyBusinessLogic(params);
        
        // 3. Interactuar con repositorios
        repository.performOperation(result, new Repository.Callback() {
            @Override
            public void onSuccess(Data data) {
                callback.onSuccess(data);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(translateError(error));
            }
        });
    }
    
    // Métodos privados para lógica interna
    private boolean validateInput(InputParams params) { /* ... */ }
    private ProcessedData applyBusinessLogic(InputParams params) { /* ... */ }
    private String translateError(String error) { /* ... */ }
}
```

## Beneficios de esta Arquitectura

### 1. Separación de Responsabilidades
- **ViewModels**: Solo manejan estado de UI y eventos
- **Use Cases**: Solo contienen lógica de negocio
- **Repositories**: Solo manejan acceso a datos


## Integración con ViewModels

### Ejemplo de Uso en ViewModel

```java
public class LoginViewModel extends ViewModel {
    private final AuthUseCase authUseCase;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();
    
    public LoginViewModel() {
        this.authUseCase = new AuthUseCase(getApplication());
    }
    
    public void login(String email, String password) {
        authState.setValue(AuthState.LOADING);
        
        authUseCase.loginUser(email, password, new AuthUseCase.AuthCallback() {
            @Override
            public void onSuccess(ClienteEntity cliente) {
                authState.setValue(AuthState.SUCCESS);
            }
            
            @Override
            public void onError(String error) {
                authState.setValue(AuthState.ERROR(error));
            }
        });
    }
}
```

## Mejores Prácticas

### 1. Manejo de Errores
- Siempre proporcionar callbacks para éxito y error
- Traducir errores técnicos a mensajes de usuario
- Logging apropiado para debugging

### 2. Validación
- Validar todas las entradas antes del procesamiento
- Aplicar reglas de negocio consistentemente
- Proporcionar mensajes de error descriptivos

### 3. Callbacks
- Usar interfaces específicas para cada tipo de operación
- Manejar casos de éxito y error explícitamente
- Evitar callbacks anidados (callback hell)

### 4. Dependencias
- Inyectar dependencias a través del constructor
- Usar singletons para repositorios cuando sea apropiado
- Mantener acoplamiento bajo entre componentes

## Estado del Proyecto

### ✅ Implementado
- AuthUseCase: Autenticación y registro completos
- PuntosUseCase: Sistema de puntos y beneficios funcional
- TransaccionQRUseCase: Procesamiento de QR implementado
- Integración con ViewModels existentes
- Callbacks y manejo de errores

### 🔄 En Desarrollo
- Testing unitario de Use Cases
- Documentación de APIs internas
- Optimizaciones de rendimiento

### 📋 Futuras Mejoras
- Implementación de más Use Cases según necesidades
- Migración a Coroutines para operaciones asíncronas
- Implementación de caché en Use Cases
- Métricas y analytics de uso
