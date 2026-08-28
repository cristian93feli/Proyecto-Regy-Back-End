# REGY Inventory - Ajustes V12

Esta versión implementa el alcance aprobado del documento de cambios del cliente.

## Backend

- Mensajes funcionales al intentar eliminar productos o ubicaciones con historial/relaciones asociadas.
- Manejo global de violaciones de integridad con respuesta HTTP 409 y mensaje funcional.
- Importación Excel por fila mediante `ProductoImportacionFilaService` con `REQUIRES_NEW`.
- `ProductoService.importar(...)` se ejecuta sin transacción global; cada fila confirma o revierte de forma independiente.
- `saveAndFlush` fuerza los errores de persistencia dentro de la fila correspondiente.
- Los contadores se actualizan únicamente tras una fila exitosa.
- Se genera un Excel con filas rechazadas, datos originales y motivo de rechazo.
- Búsqueda de productos ordenada numéricamente cuando el término es numérico y alfabéticamente cuando es textual.
- Los métodos nuevos de negocio incluyen comentarios/Javadoc y los textos reutilizables permanecen centralizados en constantes.
