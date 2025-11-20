# Domain Use Cases - Casos de Uso del Dominio

## Descripción General

El directorio `domain/usecases` implementa la **lógica de negocio** desacoplada de UI y acceso a datos. Actualmente existe un único caso de uso: `AuthUseCase`.

## Estructura Real del Directorio

```
domain/usecases/
└── AuthUseCase.java
```

## AuthUseCase

- **Ubicación**: `domain/usecases/AuthUseCase.java`.
- **Propósito**: Encapsula la lógica de autenticación, registro y obtención del usuario actual.
- **Dependencias**: `AuthRepository` y `ClienteRepository`.

### Responsabilidades
- Validar entradas básicas (email y contraseña) en login/registro.
- Registrar usuarios y crear el perfil `Cliente` asociado.
- Autenticar usuarios y resolver el perfil desde `ClienteRepository`.
- Cerrar sesión y exponer estado mediante callbacks.
- Traducir errores técnicos a mensajes amigables (`translateAuthError`).

### Métodos principales (firma real)
- `void loginUser(String email, String password, AuthCallback callback)`.
- `void registerUser(String email, String password, String nombre, String telefono, AuthCallback callback)`.
- `void logout(AuthRepository.AuthCallback<Void> callback)`.
- `void getCurrentUser(AuthCallback callback)`.

### Ejemplo de uso
```java
AuthUseCase auth = new AuthUseCase(context);
auth.loginUser(email, password, new AuthUseCase.AuthCallback() {
    @Override public void onSuccess(Cliente cliente) { /* actualizar estado UI */ }
    @Override public void onError(String error) { /* mostrar mensaje de error */ }
});
```

## Casos de uso planificados

- `PuntosUseCase`: Reglas de puntos y beneficios (parte de la lógica actual vive en `BeneficioManager`).
// Eliminado: `TransaccionQRUseCase` (se descartan flujos de transacciones)

## Arquitectura y Flujo

```
UI (Activities/Fragments)
 ↕️
ViewModels
 ↕️
Use Cases (AuthUseCase)
 ↕️
Repositories
```

## Mejores Prácticas

- Mantener validación de entrada clara y temprana.
- Proveer callbacks de éxito/error con mensajes traducidos.
- Evitar acoplamiento con UI; depender de repositorios/abstracciones.
- Facilitar testing unitario con dependencias inyectables.

## Estado del Proyecto

### ✅ Implementado
- `AuthUseCase`: login, registro, logout y usuario actual.

### 🔄 En Desarrollo
- Documentación y pruebas unitarias de use cases.
- Diseño de `PuntosUseCase`.

### 📋 Futuras Mejoras
- Migración a coroutines/Kotlin para operaciones asíncronas.
- Inyección de dependencias (DI) en use cases para testabilidad.
