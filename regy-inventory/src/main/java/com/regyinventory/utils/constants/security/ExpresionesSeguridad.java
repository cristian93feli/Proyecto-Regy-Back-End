package com.regyinventory.utils.constants.security;

public final class ExpresionesSeguridad {

    private ExpresionesSeguridad() {
    }

    public static final String ROL_ADMIN =
            "hasRole('ADMIN')";

    public static final String USUARIO_CREAR =
            "hasAuthority('USER_CREATE')";

    public static final String USUARIO_CONSULTAR =
            "hasAuthority('USER_READ')";

    public static final String USUARIO_ACTUALIZAR =
            "hasAuthority('USER_UPDATE')";

    public static final String USUARIO_CAMBIAR_ESTADO =
            "hasAuthority('USER_ENABLE_DISABLE')";

    public static final String USUARIO_ELIMINAR =
            "hasAuthority('USER_DELETE')";

    public static final String PRODUCTO_CREAR =
            "hasAuthority('PRODUCT_CREATE')";

    public static final String PRODUCTO_CONSULTAR =
            "hasAuthority('PRODUCT_READ')";

    public static final String PRODUCTO_ACTUALIZAR =
            "hasAuthority('PRODUCT_UPDATE')";

    public static final String PRODUCTO_ELIMINAR =
            "hasAuthority('PRODUCT_DELETE')";

    public static final String UBICACION_CREAR = "hasAuthority('LOCATION_CREATE')";
    public static final String UBICACION_CONSULTAR = "hasAnyAuthority('LOCATION_READ','WAREHOUSE_READ','PACKING_ZONE_READ')";
    public static final String UBICACION_ACTUALIZAR = "hasAuthority('LOCATION_UPDATE')";
    public static final String UBICACION_ELIMINAR = "hasAuthority('LOCATION_DELETE')";
    public static final String STOCK_RECIBIR = "hasAuthority('STOCK_RECEIVE')";
    public static final String STOCK_MOVER = "hasAuthority('STOCK_MOVE')";
    public static final String STOCK_VENDER = "hasAuthority('STOCK_SELL')";
    public static final String STOCK_CONSULTAR = "hasAuthority('STOCK_READ')";
    public static final String SOLICITUD_CREAR = "hasAuthority('REPLENISHMENT_REQUEST_CREATE')";
    public static final String SOLICITUD_CONSULTAR = "hasAuthority('REPLENISHMENT_REQUEST_READ')";
    public static final String SOLICITUD_COMPLETAR = "hasAuthority('REPLENISHMENT_REQUEST_COMPLETE')";
    public static final String SOLICITUD_CANCELAR = "hasAuthority('REPLENISHMENT_REQUEST_CANCEL')";
    public static final String AUDITORIA_GESTIONAR = "hasAnyAuthority('WAREHOUSE_AUDIT','LOCATION_AUDIT','PACKING_ZONE_AUDIT')";
    public static final String CONFIGURACION_CONSULTAR = "hasAuthority('SYSTEM_CONFIGURATION_READ')";
    public static final String CONFIGURACION_ACTUALIZAR = "hasAuthority('SYSTEM_CONFIGURATION_UPDATE')";
    public static final String LOG_CONSULTAR = "hasAuthority('LOG_READ')";

}