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
}