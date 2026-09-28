# REGY Backend V14 — Importación y Productos

## Alcance implementado

- Importación de Excel validada completamente antes de iniciar.
- Procesamiento de archivos grandes fila por fila, con transacción independiente por fila (`REQUIRES_NEW`).
- Progreso asíncrono real: total, procesados, exitosos, errores y porcentaje.
- Excel final con hoja `Resumen` y hoja de filas `Rechazados`.
- Lectura estable de Número de Producto, SKU y Código de Barras como identificadores de texto.
- Expansión de valores en notación científica a su representación decimal completa cuando Excel conserva precisión.
- Bloqueo preventivo de identificadores numéricos de más de 15 dígitos cuando Excel pudo perder precisión.
- Normalización de marcas por mayúsculas, tildes y espacios para evitar nuevas duplicidades lógicas.
- Creación automática de marcas y categorías faltantes durante la importación.
- Búsqueda global de Productos por nombre, SKU y código de barras.
- Orden predeterminado de Número de Producto de forma numérica ascendente antes de paginar.
- Conservación del endpoint síncrono de importación para compatibilidad; la interfaz usa el flujo asíncrono.

## Nuevos endpoints

- `POST /api/productos/importar/iniciar`
- `GET /api/productos/importar/{importacionId}`

## Notación científica

Los identificadores no se almacenan intencionalmente como `double`. Si Excel entrega un valor como `8.80997E+12` sin pérdida de precisión, el backend lo transforma a su representación decimal completa antes de persistirlo. Si Excel almacenó como número un identificador de más de 15 dígitos, la validación previa bloquea la importación y solicita formatear esa columna como Texto, porque Excel puede haber perdido dígitos y no sería seguro reconstruirlos.

## Validación realizada en el entorno de entrega

- Revisión estructural de 173 clases Java.
- Sin marcadores de conflicto ni desbalance de llaves detectado.
- No fue posible ejecutar Maven completo porque Maven no está instalado en el contenedor y el wrapper requiere descarga externa.

En el equipo de desarrollo ejecutar:

```powershell
.\mvnw.cmd clean test
```
