# REGY Inventory - Ajustes de calidad v3

## Alcance

Esta versión parte de `REGY_Inventory_Refactor_Integral_v2` y conserva la lógica de negocio y los contratos HTTP existentes.

## Correcciones realizadas

- Se eliminó texto literal del `UbicacionController`.
- Se centralizaron rutas específicas de ubicaciones.
- Se centralizaron valores predeterminados de paginación, ordenamiento y separadores internos.
- Se agregó documentación Swagger reutilizable para todas las operaciones de ubicaciones.
- Se agregaron códigos de respuesta Swagger centralizados.
- Se centralizaron mensajes de error de ubicaciones, inventario, solicitudes, auditorías, configuración y autenticación operativa.
- Se centralizaron nombres de entidades y descripciones de logs operativos.
- Se reemplazó `OperacionSupport.log(...)` por `registrarLog(...)`.
- Se reemplazó `usuarioActual()` por `obtenerUsuarioAutenticado()`.
- Se reemplazó la variable `u` por `nombreUsuarioAutenticado`.
- Se mejoraron nombres de solicitudes, repositorios y parámetros de paginación en los servicios nuevos.
- Se eliminaron imports comodín de `OperacionSupport`.
- Se mantuvieron intactas las reglas de jerarquía, inventario, reposición y auditoría.

## Archivos principales modificados

- `UbicacionController.java`
- `UbicacionService.java`
- `InventarioService.java`
- `SolicitudReposicionService.java`
- `AuditoriaService.java`
- `ConfiguracionService.java`
- `LogService.java`
- `OperacionSupport.java`
- `RutasApi.java`
- `ValoresApi.java`
- `MensajesError.java`
- `DocumentacionApi.java`
- `ConstantesLog.java`

## Validación local requerida

```bash
mvn clean test
```

El entorno de generación no contiene Maven ni las dependencias del proyecto. Se realizó una validación estructural de llaves, imports duplicados y errores sintácticos básicos con `javac`; los errores restantes de esa ejecución corresponden a dependencias externas no disponibles en el entorno.
