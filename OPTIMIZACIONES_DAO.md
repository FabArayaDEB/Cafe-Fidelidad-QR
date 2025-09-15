# Optimizaciones de Base de Datos - DAOs

## Problemas Identificados

### 1. DAOs Duplicados
- `ReporteDao.java` y `ReporteAdminDao.java` son idénticos
- `TableroDao.java` existe en dos ubicaciones: `/data/dao/` y `/database/dao/`

### 2. Consultas Sin Índices
- Consultas frecuentes por `fecha_hora`, `id_cliente`, `id_sucursal` sin índices
- Consultas de rango de fechas sin optimización
- Consultas con LIKE sin índices de texto

### 3. Consultas Complejas Sin Optimizar
- Consultas agregadas con múltiples GROUP BY
- Subconsultas que pueden convertirse en JOINs
- Consultas con múltiples condiciones WHERE sin índices compuestos

### 4. Consultas Redundantes
- Métodos duplicados con y sin LiveData
- Consultas similares que pueden unificarse

## Optimizaciones Implementadas

### 1. Eliminación de Duplicados
- Eliminar `ReporteAdminDao.java` (duplicado de `ReporteDao.java`)
- Consolidar `TableroDao.java` en una sola ubicación

### 2. Optimización de Consultas
- Agregar índices para campos frecuentemente consultados
- Optimizar consultas de rango de fechas
- Mejorar consultas agregadas
- Consolidar métodos redundantes

### 3. Mejoras de Rendimiento
- Usar LIMIT en consultas que pueden retornar muchos resultados
- Optimizar consultas de sincronización
- Mejorar consultas de búsqueda con LIKE

## 2. Consolidación de Repositorios Duplicados

### Problemas Identificados:
- [x] Repositorios duplicados en diferentes directorios
- [x] Inconsistencias en patrones de implementación
- [x] Código redundante entre repositorios similares

### Optimizaciones Implementadas:
- [x] Eliminar repositorios duplicados (ClienteRepository duplicado)
- [x] Eliminar DAOs duplicados en data/dao/
- [x] Consolidar repositorios en directorio principal repository/
- [x] Mover AuthRepository y TransaccionRepository a estructura principal
- [ ] Estandarizar patrones de implementación en todos los repositorios

## Estado: EN PROGRESO
Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')