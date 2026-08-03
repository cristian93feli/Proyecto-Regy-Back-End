# CHANGELOG - REGY Inventory RC Ubicaciones Jerárquicas

## Cambios principales

- Se unificaron depósito, estante, caja y zona de empaque en la entidad `Ubicacion`.
- Se agregó `TipoUbicacion`: `DEPOSITO`, `ESTANTE`, `CAJA`, `ZONA_EMPAQUE`.
- Jerarquía obligatoria: depósito -> estante -> caja.
- Las zonas de empaque son nodos raíz y no admiten hijos.
- El inventario solo puede existir en cajas o zonas de empaque.
- Los ingresos de mercancía solo se registran en cajas.
- Los movimientos se realizan entre cajas y zonas de empaque.
- Solicitudes de reposición tienen como destino una zona de empaque.
- Auditorías solo se realizan sobre cajas o zonas de empaque.
- Se eliminaron los módulos separados `Deposito` y `ZonaEmpaque` para evitar duplicidad de reglas.
- Se agregaron nombres descriptivos en la lógica nueva de ubicaciones, inventario, solicitudes, auditorías y dashboard.
- Se agregó documentación Swagger al controlador unificado de ubicaciones y esquemas de sus DTO.
- Se aplicó formato de cuatro espacios a los archivos Java compactados.

## Validación pendiente

El entorno de generación no dispone de Maven instalado ni acceso garantizado para descargar dependencias. La compilación final debe ejecutarse localmente con `mvn clean test`.

## 2026-07-26 - Refactor técnico del módulo Productos

- Se formateó y documentó `ProductoController` con Swagger completo.
- Se reemplazaron variables y métodos abreviados por nombres descriptivos.
- Se centralizaron mensajes de éxito, errores y documentación Swagger.
- Se agregó JavaDoc detallado al contrato `IProductoService`.
- Se documentaron los DTO de producto con `@Schema`.
- Se preservaron las reglas existentes de creación, actualización, estado y eliminación.
- No se realizaron cambios de esquema de base de datos ni de rutas públicas.

## Refactor integral de módulos operativos

- Se centralizaron rutas, permisos y mensajes de los módulos de ubicaciones, inventario, solicitudes de reposición, auditorías, configuración, dashboard y logs.
- Se normalizaron nombres de dependencias, parámetros y métodos en controladores y servicios compactos.
- Se agregó documentación Swagger a los controladores operativos.
- Se agregó JavaDoc a los contratos de servicios operativos.
- Se preservaron las rutas y reglas de negocio existentes.

## Refactor integral v3

- Eliminados literales restantes del controlador de ubicaciones.
- Centralizados mensajes de negocio de los servicios nuevos.
- Centralizados nombres y detalles de logs operativos.
- Mejorados nombres de variables, parámetros, repositorios y métodos auxiliares.
- Centralizados valores de paginación, ordenamiento y separadores.
- Swagger de ubicaciones migrado completamente a constantes reutilizables.
- Sin cambios en reglas de negocio, entidades, tablas o endpoints públicos.
