# Reporte de refactor técnico — Productos

## Alcance

Esta entrega aplica la primera fase de limpieza técnica al módulo Productos, sin modificar su contrato HTTP ni su modelo de base de datos.

## Archivos modificados

- `ProductoController.java`
- `IProductoService.java`
- `ProductoService.java`
- `CrearProductoRequestDTO.java`
- `ActualizarProductoRequestDTO.java`
- `ProductoResponseDTO.java`
- `MensajesError.java`
- `MensajesExito.java`
- `DocumentacionApi.java`
- `CHANGELOG.md`

## Cambios principales

- Variables, dependencias y métodos con nombres descriptivos.
- Separación de validación, construcción, actualización, búsqueda y conversión de respuestas.
- Swagger con operaciones, seguridad y respuestas HTTP documentadas.
- JavaDoc de negocio en todos los métodos del contrato.
- Mensajes y textos Swagger centralizados.
- DTO de entrada y salida documentados con `@Schema`.

## Reglas preservadas

- Número, SKU y código de barras deben ser únicos cuando se informan.
- Marca y categoría son opcionales, pero deben estar activas cuando se asignan.
- El producto no almacena stock directamente.
- No se elimina un producto con stock.
- No se elimina un producto con solicitudes pendientes.

## Validación pendiente en ambiente local

Ejecutar:

```bash
mvn clean test
```

Este entorno no dispone del comando Maven instalado, por lo que la compilación debe confirmarse en el ambiente donde el proyecto ya levantó.
