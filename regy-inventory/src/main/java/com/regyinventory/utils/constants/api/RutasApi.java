package com.regyinventory.utils.constants.api;

public final class RutasApi {

    private RutasApi() {
    }

    public static final String AUTENTICACION =
            "/api/auth";

    public static final String LOGIN =
            "/login";

    public static final String USUARIOS =
            "/api/usuarios";

    public static final String ROLES =
            "/api/roles";

    public static final String MARCAS =
            "/api/marcas";

    public static final String CATEGORIAS =
            "/api/categorias";

    public static final String PRODUCTOS =
            "/api/productos";

    public static final String POR_ID =
            "/{id}";

    public static final String ACTIVAR =
            "/{id}/activar";

    public static final String DESACTIVAR =
            "/{id}/desactivar";

    public static final String CAMBIAR_CONTRASENA =
            "/{id}/password";

    public static final String UBICACIONES = "/api/ubicaciones";
    public static final String INVENTARIO = "/api/inventario";
    public static final String SOLICITUDES_REPOSICION = "/api/solicitudes";
    public static final String AUDITORIAS = "/api/auditorias";
    public static final String CONFIGURACION = "/api/configuracion";
    public static final String DASHBOARD = "/api/dashboard";
    public static final String LOGS = "/api/logs";
    public static final String INGRESOS = "/ingresos";
    public static final String MOVIMIENTOS = "/movimientos";
    public static final String VENTAS = "/salidas/venta";
    public static final String AJUSTAR_INVENTARIO = "/ajustes";
    public static final String ORIGENES_PRODUCTO = "/productos/{productoId}/origenes";
    public static final String EXISTENCIAS = "/existencias";
    public static final String STOCK_BAJO = "/stock-bajo";
    public static final String UBICACION_POR_ID = "/{ubicacionId}";
    public static final String UBICACION_ACTIVAR = "/{ubicacionId}/activar";
    public static final String UBICACION_DESACTIVAR = "/{ubicacionId}/desactivar";
    public static final String POR_TIPO = "/tipo/{tipoUbicacion}";
    public static final String UBICACIONES_HIJAS = "/{ubicacionPadreId}/hijas";
    public static final String COMPLETAR = "/{id}/completar";
    public static final String CANCELAR = "/{id}/cancelar";
    public static final String POR_CLAVE = "/{clave}";
    public static final String PRODUCTOS_DISPONIBLES = "/productos-disponibles";
    public static final String BUSCAR_PRODUCTOS = "/buscar";
    public static final String IMPORTAR_PRODUCTOS = "/importar";
    public static final String SUGERENCIAS_SOLICITUD = "/{id}/sugerencias";

}