# Managers Layer - Capa de Gestores

## Descripción General

El directorio `managers` contiene las **clases gestoras** de reglas de negocio que no pertenecen directamente a UI ni a servicios del sistema. Actualmente, esta capa implementa la lógica de beneficios de fidelización mediante `BeneficioManager`.

## Estructura Real

```
managers/
└── BeneficioManager.java
```

## BeneficioManager

- **Propósito**: Centraliza la lógica para evaluar, activar, obtener y aplicar beneficios de fidelidad basados en visitas y reglas de negocio.
- **Dependencias**: Modelos `Beneficio` y `Visita`. No depende de UI ni de servicios.

### Responsabilidades
- Evaluar beneficios automáticos según visitas recientes.
- Aplicar beneficios disponibles a un monto de compra, con opción de acumular.
- Filtrar y obtener beneficios disponibles por cliente, marcando expirados.
- Crear beneficios estándar (porcentaje, monto fijo, producto gratis, 2x1).

### Reglas de negocio implementadas
- Cada 5 visitas: `10%` de descuento.
- Cada 10 visitas: `Café americano` gratis.
- Cada 20 visitas: `2x1` en cualquier bebida.
- Cada 50 visitas: `25%` de descuento.
- Cliente frecuente: `≥3` visitas en la última semana → `15%` de descuento.
- Racha diaria: `5` días consecutivos de visita → `Postre` gratis.

### Métodos clave
- `List<Beneficio> evaluarBeneficiosAutomaticos(String clienteId, List<Visita> visitasRecientes)`
- `double aplicarBeneficios(String clienteId, double montoCompra, List<Beneficio> beneficiosDisponibles, boolean acumularBeneficios)`
- `List<Beneficio> obtenerBeneficiosDisponibles(String clienteId, List<Beneficio> todosBeneficios)`

### Uso de ejemplo
```java
BeneficioManager manager = new BeneficioManager(context);
List<Beneficio> nuevos = manager.evaluarBeneficiosAutomaticos(clienteId, visitas);
List<Beneficio> disponibles = manager.obtenerBeneficiosDisponibles(clienteId, todos);
double descuento = manager.aplicarBeneficios(clienteId, montoCompra, disponibles, false);
```

## Estado del Proyecto

### ✅ Implementado
- `BeneficioManager` con reglas básicas y temporales, aplicación y obtención de beneficios.

### 🔄 En Desarrollo
- Refinar reglas de negocio y parametrización desde backend.
- Integración con repositorios/servicios para persistencia de canjes.

### 📋 Futuras Mejoras
- Migración a Kotlin y coroutines para operaciones asíncronas.
- Inyección de dependencias (DI) para facilitar testing y extensibilidad.
- Métricas/analytics de uso de beneficios.

## Mejores Prácticas

- Mantener una única responsabilidad por manager.
- Evitar dependencias con UI; exponer APIs puras de negocio.
- Validar entradas y manejar expiración/estado de beneficios.
- Registrar errores y mantener consistencia en estados (`disponible`, `usado`, `expirado`).

---

Nota: Actualmente no existen `BaseManager`, `AuthManager`, `DataManager` ni otros managers referenciados previamente. La documentación se alinea al código real en `managers/`.