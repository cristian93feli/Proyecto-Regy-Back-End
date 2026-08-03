package com.regyinventory.utils.constants.mensajes;

public final class MensajesValidacion {

    private MensajesValidacion() {
    }

    public static final class Comun {

        public static final String DESCRIPCION_MAXIMA =
                "La descripción no puede superar 250 caracteres";

        private Comun() {
        }
    }

    public static final class Login {

        public static final String USUARIO_OBLIGATORIO =
                "El usuario es obligatorio";

        public static final String CONTRASENA_OBLIGATORIA =
                "La contraseña es obligatoria";

        private Login() {
        }
    }

    public static final class Usuario {

        public static final String IDENTIFICACION_OBLIGATORIA =
                "La identificación es obligatoria";

        public static final String IDENTIFICACION_MAXIMA =
                "La identificación no puede superar 20 caracteres";

        public static final String NOMBRE_OBLIGATORIO =
                "El nombre es obligatorio";

        public static final String NOMBRE_MAXIMO =
                "El nombre no puede superar 80 caracteres";

        public static final String APELLIDO_OBLIGATORIO =
                "El apellido es obligatorio";

        public static final String APELLIDO_MAXIMO =
                "El apellido no puede superar 80 caracteres";

        public static final String CORREO_OBLIGATORIO =
                "El correo es obligatorio";

        public static final String CORREO_INVALIDO =
                "El correo no tiene un formato válido";

        public static final String USERNAME_OBLIGATORIO =
                "El nombre de usuario es obligatorio";

        public static final String USERNAME_LONGITUD =
                "El usuario debe tener entre 4 y 40 caracteres";

        public static final String CONTRASENA_OBLIGATORIA =
                "La contraseña es obligatoria";

        public static final String CONTRASENA_LONGITUD =
                "La contraseña debe tener entre 8 y 100 caracteres";

        public static final String NUEVA_CONTRASENA_OBLIGATORIA =
                "La nueva contraseña es obligatoria";

        public static final String ROL_OBLIGATORIO =
                "Debes asignar al menos un rol";

        private Usuario() {
        }
    }

    public static final class Marca {

        public static final String NOMBRE_OBLIGATORIO =
                "El nombre de la marca es obligatorio";

        public static final String NOMBRE_MAXIMO =
                "El nombre de la marca no puede superar 100 caracteres";

        private Marca() {
        }
    }

    public static final class Categoria {

        public static final String NOMBRE_OBLIGATORIO =
                "El nombre de la categoría es obligatorio";

        public static final String NOMBRE_MAXIMO =
                "El nombre de la categoría no puede superar 100 caracteres";

        private Categoria() {
        }
    }
}