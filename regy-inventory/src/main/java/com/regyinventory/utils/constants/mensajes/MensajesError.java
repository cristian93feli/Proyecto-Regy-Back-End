package com.regyinventory.utils.constants.mensajes;

public final class MensajesError {

    private MensajesError() {
    }

    public static final String CAMPOS_INVALIDOS =
            "Existen campos inválidos";

    public static final String ERROR_INTERNO =
            "Ocurrió un error interno en el servidor";

    public static final String CREDENCIALES_INCORRECTAS =
            "Usuario o contraseña incorrectos";

    public static final String NO_AUTENTICADO =
            "No estás autenticado o el token es inválido";

    public static final String ACCESO_DENEGADO =
            "No tienes permisos para realizar esta acción";

    public static final class Usuario {

        public static final String NO_EXISTE =
                "No se encontró el usuario con ID %d";

        public static final String NO_ENCONTRADO_USERNAME =
                "Usuario no encontrado: %s";

        public static final String IDENTIFICACION_DUPLICADA =
                "Ya existe un usuario con esa identificación";

        public static final String CORREO_DUPLICADO =
                "Ya existe un usuario con ese correo";

        public static final String USERNAME_DUPLICADO =
                "Ya existe un usuario con ese nombre de usuario";

        public static final String OTRA_IDENTIFICACION_DUPLICADA =
                "Ya existe otro usuario con esa identificación";

        public static final String OTRO_CORREO_DUPLICADO =
                "Ya existe otro usuario con ese correo";

        public static final String OTRO_USERNAME_DUPLICADO =
                "Ya existe otro usuario con ese nombre de usuario";

        public static final String ROLES_NO_EXISTEN =
                "No existen los roles con ID: %s";

        public static final String ROLES_INACTIVOS =
                "No se pueden asignar roles inactivos";

        public static final String YA_ACTIVO =
                "El usuario ya se encuentra activo";

        public static final String YA_INACTIVO =
                "El usuario ya se encuentra inactivo";

        public static final String AUTODESACTIVACION =
                "No puedes desactivar tu propio usuario";

        private Usuario() {
        }
    }

    public static final class Rol {

        public static final String NO_EXISTE =
                "No se encontró el rol con ID %d";

        private Rol() {
        }
    }

    public static final class Marca {

        public static final String NO_EXISTE =
                "No existe la marca con id %d";

        public static final String NOMBRE_DUPLICADO =
                "Ya existe una marca con ese nombre";

        public static final String OTRO_NOMBRE_DUPLICADO =
                "Ya existe otra marca con ese nombre";

        public static final String YA_ACTIVA =
                "La marca ya se encuentra activa";

        public static final String YA_INACTIVA =
                "La marca ya se encuentra inactiva";

        public static final String TIENE_REGISTROS_ASOCIADOS =
                "No se puede eliminar la marca porque tiene registros asociados";

        private Marca() {
        }
    }

    public static final class Categoria {

        public static final String NO_EXISTE =
                "No existe la categoría con id %d";

        public static final String NOMBRE_DUPLICADO =
                "Ya existe una categoría con ese nombre";

        public static final String OTRO_NOMBRE_DUPLICADO =
                "Ya existe otra categoría con ese nombre";

        public static final String YA_ACTIVA =
                "La categoría ya se encuentra activa";

        public static final String YA_INACTIVA =
                "La categoría ya se encuentra inactiva";

        public static final String TIENE_REGISTROS_ASOCIADOS =
                "No se puede eliminar la categoría porque tiene registros asociados";

        private Categoria() {
        }
    }
}