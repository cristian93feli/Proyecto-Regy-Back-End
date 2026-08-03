# REGY Inventory Backend Completo

Incluye autenticación JWT, usuarios/roles/permisos, productos, marcas, categorías, depósitos, ubicaciones, zonas de empaque, inventario por lote y destino, ingresos, movimientos atómicos FEFO/FIFO, solicitudes de reposición, auditorías/ajustes, logs, configuración y dashboard.

## Ejecutar
1. Crear PostgreSQL `regy_inventory`.
2. Configurar `DB_PASSWORD` y `JWT_SECRET` o usar valores locales predeterminados.
3. `mvn clean test`
4. `mvn spring-boot:run`
5. Swagger: `http://localhost:8080/swagger-ui.html`

Usuario inicial: `admin` / `Admin123*` (cambiar al primer ingreso).

## Flujo de prueba
Crear depósito -> ubicación -> zona de empaque -> marca/categoría -> producto -> ingreso de stock -> mover stock -> crear/completar solicitud -> auditar -> revisar dashboard y logs.
