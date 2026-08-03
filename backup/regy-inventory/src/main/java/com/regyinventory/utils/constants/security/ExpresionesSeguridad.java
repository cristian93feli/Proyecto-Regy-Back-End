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

    public static final String PRODUCTO_CREAR =
            "hasAuthority('PRODUCT_CREATE')";

    public static final String PRODUCTO_CONSULTAR =
            "hasAuthority('PRODUCT_READ')";

    public static final String PRODUCTO_ACTUALIZAR =
            "hasAuthority('PRODUCT_UPDATE')";

    public static final String PRODUCTO_ELIMINAR =
            "hasAuthority('PRODUCT_DELETE')";
}