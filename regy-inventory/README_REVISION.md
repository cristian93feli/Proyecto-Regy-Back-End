# REGY Inventory - Versión candidata

Esta entrega reemplaza el modelo separado de depósitos/ubicaciones/zonas por una jerarquía unificada.

## Prueba inicial

```bash
mvn clean test
mvn spring-boot:run
```

Swagger: `http://localhost:8080/swagger-ui.html`

## Orden mínimo de prueba

1. Autenticación.
2. Crear depósito.
3. Crear estante con padre depósito.
4. Crear caja con padre estante.
5. Crear zona de empaque sin padre.
6. Crear producto.
7. Ingresar stock en caja.
8. Mover stock de caja a zona de empaque.
9. Crear y completar solicitud de reposición.
10. Auditar caja y zona de empaque.
11. Consultar dashboard y logs.
