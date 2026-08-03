# Reporte de refactorización integral

Esta entrega toma como base `REGY_Inventory_Refactor_Productos_v1`.

## Alcance aplicado

- Producto: se conserva el refactor previamente aprobado.
- Ubicaciones: rutas, permisos y mensajes centralizados.
- Inventario: controlador formateado, nombres descriptivos, Swagger y contrato documentado.
- Solicitudes de reposición: controlador y contrato documentados, nombres descriptivos.
- Auditorías: controlador y contrato documentados.
- Configuración: controlador, contrato e implementación refactorizados.
- Dashboard: controlador y contrato documentados.
- Logs: controlador, contrato e implementación refactorizados.
- Constantes: ampliación de `RutasApi`, `ExpresionesSeguridad`, `MensajesExito` y `DocumentacionApi`.

## Reglas preservadas

- El inventario solo existe en cajas y zonas de empaque.
- Los ingresos directos se registran en cajas.
- La jerarquía es depósito > estante > caja.
- La zona de empaque es raíz y no tiene hijos.
- Las reposiciones mueven inventario de cajas a zonas de empaque.
- Los movimientos conservan la selección FEFO/FIFO existente.

## Validación requerida en ambiente local

Ejecutar:

```bash
mvn clean test
```

Luego validar Swagger y ejecutar el plan de pruebas entregado.
