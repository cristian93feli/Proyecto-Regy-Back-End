package com.regyinventory.utils.constants.swagger;

public final class DocumentacionApi {

    private DocumentacionApi() {
    }

    public static final String ESQUEMA_SEGURIDAD =
            "bearerAuth";

    public static final class Autenticacion {

        public static final String TAG =
                "Autenticación";

        public static final String DESCRIPCION_TAG =
                "Operaciones públicas para iniciar sesión";

        public static final String LOGIN =
                "Iniciar sesión";

        public static final String DESCRIPCION_LOGIN =
                "Valida las credenciales y devuelve un token JWT";

        private Autenticacion() {
        }
    }

    public static final class Usuario {

        public static final String TAG =
                "Usuarios";

        public static final String DESCRIPCION_TAG =
                "Administración de usuarios del sistema";

        public static final String CREAR =
                "Crear usuario";

        public static final String DESCRIPCION_CREAR =
                "Crea un usuario y le asigna uno o varios roles";

        public static final String CONSULTAR =
                "Consultar usuario";

        public static final String DESCRIPCION_CONSULTAR =
                "Obtiene un usuario por su identificador";

        public static final String LISTAR =
                "Listar usuarios";

        public static final String DESCRIPCION_LISTAR =
                "Obtiene los usuarios utilizando paginación y ordenamiento";

        public static final String ACTUALIZAR =
                "Actualizar usuario";

        public static final String DESCRIPCION_ACTUALIZAR =
                "Actualiza los datos y roles de un usuario";

        public static final String CAMBIAR_CONTRASENA =
                "Cambiar contraseña";

        public static final String DESCRIPCION_CAMBIAR_CONTRASENA =
                "Asigna una nueva contraseña cifrada al usuario";

        public static final String ACTIVAR =
                "Activar usuario";

        public static final String DESCRIPCION_ACTIVAR =
                "Habilita el acceso de un usuario";

        public static final String DESACTIVAR =
                "Desactivar usuario";

        public static final String DESCRIPCION_DESACTIVAR =
                "Inhabilita el acceso de un usuario";

        private Usuario() {
        }
    }

    public static final class Rol {

        public static final String TAG =
                "Roles";

        public static final String DESCRIPCION_TAG =
                "Consulta del catálogo de roles";

        public static final String LISTAR =
                "Listar roles";

        public static final String DESCRIPCION_LISTAR =
                "Obtiene el catálogo de roles disponibles";

        public static final String CONSULTAR =
                "Consultar rol";

        public static final String DESCRIPCION_CONSULTAR =
                "Obtiene un rol por su identificador";

        private Rol() {
        }
    }

    public static final class Marca {

        public static final String TAG =
                "Marcas";

        public static final String DESCRIPCION_TAG =
                "Administración del catálogo de marcas";

        public static final String CREAR =
                "Crear marca";

        public static final String DESCRIPCION_CREAR =
                "Registra una nueva marca";

        public static final String CONSULTAR =
                "Consultar marca";

        public static final String DESCRIPCION_CONSULTAR =
                "Obtiene una marca por su identificador";

        public static final String LISTAR =
                "Listar marcas";

        public static final String DESCRIPCION_LISTAR =
                "Obtiene las marcas con paginación y ordenamiento";

        public static final String ACTUALIZAR =
                "Actualizar marca";

        public static final String DESCRIPCION_ACTUALIZAR =
                "Actualiza el nombre y la descripción de una marca";

        public static final String ACTIVAR =
                "Activar marca";

        public static final String DESCRIPCION_ACTIVAR =
                "Activa una marca previamente inactiva";

        public static final String DESACTIVAR =
                "Desactivar marca";

        public static final String DESCRIPCION_DESACTIVAR =
                "Desactiva una marca sin eliminarla";

        public static final String ELIMINAR =
                "Eliminar marca";

        public static final String DESCRIPCION_ELIMINAR =
                "Elimina definitivamente una marca si no tiene registros asociados";

        private Marca() {
        }
    }

    public static final class Categoria {

        public static final String TAG =
                "Categorías";

        public static final String DESCRIPCION_TAG =
                "Administración del catálogo de categorías";

        public static final String CREAR =
                "Crear categoría";

        public static final String DESCRIPCION_CREAR =
                "Registra una nueva categoría";

        public static final String CONSULTAR =
                "Consultar categoría";

        public static final String DESCRIPCION_CONSULTAR =
                "Obtiene una categoría por su identificador";

        public static final String LISTAR =
                "Listar categorías";

        public static final String DESCRIPCION_LISTAR =
                "Obtiene las categorías con paginación y ordenamiento";

        public static final String ACTUALIZAR =
                "Actualizar categoría";

        public static final String DESCRIPCION_ACTUALIZAR =
                "Actualiza el nombre y la descripción de una categoría";

        public static final String ACTIVAR =
                "Activar categoría";

        public static final String DESCRIPCION_ACTIVAR =
                "Activa una categoría previamente inactiva";

        public static final String DESACTIVAR =
                "Desactivar categoría";

        public static final String DESCRIPCION_DESACTIVAR =
                "Desactiva una categoría sin eliminarla";

        public static final String ELIMINAR =
                "Eliminar categoría";

        public static final String DESCRIPCION_ELIMINAR =
                "Elimina definitivamente una categoría si no tiene registros asociados";

        private Categoria() {
        }
    }
}