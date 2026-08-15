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

    public static final class Inventario {

        public static final String PRODUCTO_OBLIGATORIO =
                "El producto es obligatorio";

        public static final String UBICACION_OBLIGATORIA =
                "La ubicación es obligatoria";

        public static final String NUEVA_CANTIDAD_OBLIGATORIA =
                "La nueva cantidad es obligatoria";

        public static final String NUEVA_CANTIDAD_INVALIDA =
                "La nueva cantidad no puede ser negativa";

        public static final String OBSERVACIONES_MAXIMAS =
                "Las observaciones no pueden superar 500 caracteres";

        private Inventario() {
        }
    }

    public static final class SolicitudReposicion {

        public static final String CANTIDAD_ENVIO_OBLIGATORIA =
                "La cantidad a enviar es obligatoria";

        public static final String CANTIDAD_ENVIO_INVALIDA =
                "La cantidad a enviar debe ser mayor a cero";

        private SolicitudReposicion() {
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