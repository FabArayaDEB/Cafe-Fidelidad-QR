# Database Layer - Capa de Base de Datos

## Descripción General

El directorio `database` implementa la **persistencia local** utilizando un `SQLiteOpenHelper` propio: `CafeFidelidadDB.java`. Esta capa gestiona el archivo `cafe_fidelidad.db`, crea el esquema de tablas, realiza operaciones CRUD y sirve como fuente de datos local para repositorios.

Características principales de la implementación:
- `SQLiteOpenHelper` con `onCreate` y `onUpgrade` para gestión de esquema.
- Tablas normalizadas con claves foráneas y constraints.
- Métodos CRUD para modelos: `Cliente`, `Producto`, `Sucursal`, `Beneficio`, `Visita`, `Canje`.
- Consultas específicas: búsqueda de productos, filtros por categoría/estado, listados de beneficios disponibles, visitas por cliente, canjes por estado.
- Uso de `ContentValues`, `Cursor` y consultas parametrizadas para seguridad y rendimiento.

Nota: Aunque el proyecto declara dependencias de Room en `build.gradle`, la implementación activa utiliza SQLite manual mediante `CafeFidelidadDB`.

## Estado del Proyecto

### ✅ Implementado
- Archivo `cafe_fidelidad.db` y `CafeFidelidadDB` funcional
- Esquema de tablas: clientes, productos, sucursales, beneficios, visitas, canjes
- Claves foráneas y restricciones de integridad
- CRUD completo para modelos principales
- Búsqueda y filtros de productos (categoría, disponibilidad, activo)
- Listado de beneficios por puntos requeridos y estado
- Visitas y canjes vinculados a clientes y productos
- Integración con repositorios mediante hilos de fondo (`ExecutorService`)

### 🔄 En Desarrollo
- Estrategias de migración en `onUpgrade`
- Consultas agregadas y reportes avanzados
- Índices adicionales para columnas de alta consulta
- Testing automatizado de CRUD y restricciones
- Exportación/backup del archivo SQLite

### 📋 Futuras Mejoras
- Full-Text Search (FTS) para búsqueda avanzada
- Encriptación de datos sensibles (SQLCipher)
- Vistas materializadas para reportes
- Triggers de auditoría y consistencia
- Índices compuestos y particionado lógico

## Mejores Prácticas

### 1. Diseño de Entidades
- **Primary Keys**: IDs string (UUID) para compatibilidad con API remota
- **Foreign Keys**: Definir relaciones con `ON DELETE/UPDATE` apropiados
- **Índices**: Crear índices en columnas de búsqueda (nombre, categoría, activo)
- **Constraints**: Validaciones a nivel de base (NOT NULL, UNIQUE)

### 2. Consultas Eficientes
- **Proyecciones**: Seleccionar solo columnas necesarias
- **Joins**: Consultas con `INNER/LEFT JOIN` según necesidad
- **Parámetros**: Usar `?` para evitar SQL injection
- **Índices**: Aprovechar índices para `WHERE` y `ORDER BY`

### 3. Manejo de Datos
- **Threading**: Ejecutar desde repositorios con `ExecutorService`
- **Transacciones**: Usar `beginTransaction()`/`setTransactionSuccessful()`/`endTransaction()`
- **Sincronización**: Campos de control (timestamps, flags) para sync futura
- **Cleanup**: Limpieza de datos antiguos/registros inactivos

## Esquema de Tablas (Resumen)

- `clientes`: id, nombre, apellido, email, telefono, fecha_nacimiento, fecha_registro, puntos, activo
- `productos`: id, nombre, descripcion, precio, categoria, activo, disponible, stock, imagen_url
- `sucursales`: id, nombre, direccion, ciudad, latitud, longitud, telefono
- `beneficios`: id, nombre, descripcion, puntos_requeridos, activo, fecha_inicio, fecha_fin
- `visitas`: id, cliente_id (FK), sucursal_id (FK), fecha_visita, puntos_ganados, notas
- `canjes`: id, cliente_id (FK), beneficio_id (FK), producto_id (FK), fecha_canje, puntos_usados, estado

Claves foráneas principales
- `visitas.cliente_id` → `clientes.id`
- `visitas.sucursal_id` → `sucursales.id`
- `canjes.cliente_id` → `clientes.id`
- `canjes.beneficio_id` → `beneficios.id`
- `canjes.producto_id` → `productos.id`

## Operaciones CRUD y Consultas

- Clientes: crear, obtener por id/email, actualizar datos, activar/desactivar, sumar puntos.
- Productos: crear, listar, buscar por nombre, filtrar por categoría/activo/disponible, actualizar stock y disponibilidad.
- Sucursales: crear, listar, obtener por ciudad, actualizar datos.
- Beneficios: crear, listar, filtrar por puntos requeridos/estado, activar/desactivar.
- Visitas: registrar visita, listar por cliente/sucursal, cálculo de puntos ganados.
- Canjes: registrar canje, listar por cliente/estado, validar puntos disponibles.

## Flujo de Datos

```
Repositories (Auth/Producto/Sucursal/Beneficio/Visita/Canje)
  ↕ (CRUD/consultas en hilos de fondo)
CafeFidelidadDB (SQLiteOpenHelper)
```

Los repositorios encapsulan threading, estados (`isLoading`, `error`, `success`) y coordinan lecturas/escrituras con la base local y, cuando aplique, con servicios de red (`ApiService`).

## Conclusión

La capa de base de datos, implementada con `SQLiteOpenHelper`, proporciona almacenamiento local robusto y operaciones CRUD eficientes para los módulos de clientes, productos, sucursales, beneficios, visitas y canjes. Su integración con los repositorios y el uso de hilos de fondo asegura buen rendimiento y una experiencia fluida.

---

**Nota**: Para detalles de implementación, consultar `app/src/main/java/com/example/cafefidelidaqrdemo/database/CafeFidelidadDB.java`.
