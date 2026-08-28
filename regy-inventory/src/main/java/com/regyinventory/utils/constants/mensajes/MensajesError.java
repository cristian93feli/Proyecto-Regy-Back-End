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

        public static final String AUTOELIMINACION = "No puedes eliminar tu propio usuario";
        public static final String RESPONSABLE_UNICO_ZONA = "El usuario no puede eliminarse porque es el único responsable de una zona de empaque";
        public static final String CON_HISTORIAL_NO_ELIMINABLE = "El usuario tiene historial operativo asociado y no puede eliminarse físicamente";

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

    public static final class Producto {

        public static final String NO_EXISTE =
                "No existe el producto con id %d";

        public static final String NUMERO_DUPLICADO =
                "Ya existe un producto con ese número";

        public static final String SKU_DUPLICADO =
                "Ya existe un producto con ese SKU";

        public static final String CODIGO_BARRAS_DUPLICADO =
                "Ya existe un producto con ese código de barras";

        public static final String MARCA_INACTIVA =
                "La marca seleccionada está inactiva";

        public static final String CATEGORIA_INACTIVA =
                "La categoría seleccionada está inactiva";

        public static final String TIENE_STOCK =
                "No se puede eliminar un producto con stock";

        public static final String TIENE_SOLICITUDES_PENDIENTES =
                "No se puede eliminar un producto con solicitudes pendientes";

        public static final String TIENE_HISTORIAL_ASOCIADO =
                "No se puede eliminar el producto porque tiene ingresos, movimientos u otros registros de inventario asociados";
        public static final String ARCHIVO_IMPORTACION_VACIO = "El archivo de importación está vacío";
        public static final String ARCHIVO_IMPORTACION_INVALIDO = "No fue posible procesar el archivo Excel";
        public static final String COLUMNAS_IMPORTACION_INVALIDAS = "El Excel no contiene las columnas requeridas";
        public static final String CAMPOS_OBLIGATORIOS_IMPORTACION = "Número de producto y nombre del producto son obligatorios";

        private Producto() {
        }
    }
    public static final class Ubicacion {

        public static final String NO_ENCONTRADA = "Ubicación no encontrada";
        public static final String CODIGO_DUPLICADO = "Ya existe una ubicación con el código indicado";
        public static final String INACTIVA = "La ubicación está inactiva";
        public static final String CON_HIJAS_NO_DESACTIVABLE =
                "No se puede desactivar una ubicación que tiene ubicaciones hijas";
        public static final String CON_STOCK_NO_DESACTIVABLE =
                "No se puede desactivar una ubicación que contiene stock";
        public static final String CON_HIJAS_NO_ELIMINABLE =
                "No se puede eliminar una ubicación que tiene ubicaciones hijas";
        public static final String CON_STOCK_NO_ELIMINABLE =
                "No se puede eliminar una ubicación que contiene stock";
        public static final String TIENE_REGISTROS_ASOCIADOS =
                "No se puede eliminar la ubicación porque tiene ingresos, movimientos, solicitudes u otros registros asociados";
        public static final String RAIZ_CON_PADRE =
                "Los depósitos y zonas de empaque no pueden tener ubicación padre";
        public static final String HIJA_SIN_PADRE =
                "El estante o la caja deben tener una ubicación padre";
        public static final String ESTANTE_PADRE_INVALIDO =
                "Un estante únicamente puede pertenecer a un depósito";
        public static final String CAJA_PADRE_INVALIDO =
                "Una caja únicamente puede pertenecer a un estante";
        public static final String USUARIO_SOLO_ZONA =
                "Solo una zona de empaque puede tener un usuario asignado";
        public static final String USUARIO_ASIGNADO_NO_ENCONTRADO =
                "Usuario asignado no encontrado";
        public static final String USUARIO_ASIGNADO_INACTIVO =
                "El usuario asignado está inactivo";
        public static final String JERARQUIA_INACTIVA =
                "La ubicación pertenece a una jerarquía inactiva";
        public static final String NO_ADMITE_INVENTARIO =
                "El inventario únicamente puede existir en cajas o zonas de empaque";
        public static final String ZONA_REQUIERE_RESPONSABLE = "Una zona de empaque debe tener al menos un usuario empaquetador responsable";
        public static final String RESPONSABLE_DEBE_SER_EMPAQUETADOR = "Solo usuarios con rol empaquetador pueden ser responsables de una zona";
        public static final String INGRESO_REQUIERE_CAJA =
                "Los ingresos de mercancía deben registrarse en una caja o en una zona de empaque";

        private Ubicacion() {
        }
    }

    public static final class Inventario {

        public static final String ORIGEN_DESTINO_IGUALES =
                "El origen y el destino no pueden ser iguales";
        public static final String STOCK_INSUFICIENTE =
                "Stock insuficiente. Disponible: %d";
        public static final String PRODUCTO_NO_ENCONTRADO =
                "Producto no encontrado";
        public static final String PRODUCTO_INACTIVO =
                "El producto está inactivo";
        public static final String PRODUCTO_SIN_STOCK =
                "El producto seleccionado no tiene stock disponible";
        public static final String CODIGO_BARRAS_NO_COINCIDE =
                "El código de barras no corresponde al producto solicitado";
        public static final String AJUSTE_SIN_CAMBIOS =
                "La nueva cantidad es igual a la cantidad actual del inventario";

        private Inventario() {
        }
    }

    public static final class SolicitudReposicion {

        public static final String NO_ENCONTRADA = "Solicitud no encontrada";
        public static final String NO_PENDIENTE = "La solicitud no está pendiente";
        public static final String STOCK_INSUFICIENTE_DEPOSITOS =
                "Stock insuficiente en las cajas de los depósitos";
        public static final String ZONA_NO_ENCONTRADA = "Zona de empaque no encontrada";
        public static final String DESTINO_INVALIDO =
                "La ubicación destino debe ser una zona de empaque activa";
        public static final String ORIGEN_REQUERIDO =
                "Debe seleccionar una caja de origen para completar la solicitud";
        public static final String ORIGEN_INVALIDO =
                "La ubicación de origen debe ser una caja activa con stock del producto solicitado";
        public static final String CODIGO_BARRAS_INVALIDO =
                "El código de barras validado no corresponde al producto de la solicitud";
        public static final String CANTIDAD_SUPERA_SOLICITUD =
                "La cantidad a enviar no puede superar la cantidad pendiente de la solicitud";
        public static final String CANTIDAD_SUPERA_STOCK_CAJA =
                "La cantidad a enviar no puede superar el stock disponible de la caja seleccionada";

        private SolicitudReposicion() {
        }
    }

    public static final class Auditoria {

        public static final String MOTIVO_OTRO_REQUERIDO =
                "Debe especificar el motivo cuando selecciona OTRO";
        public static final String TIPO_UBICACION_INCOMPATIBLE =
                "El tipo de auditoría no coincide con el tipo de ubicación";

        private Auditoria() {
        }
    }

    public static final class Configuracion {

        public static final String NO_ENCONTRADA = "Configuración no encontrada";

        private Configuracion() {
        }
    }

    public static final class Persistencia {

        public static final String REGISTRO_CON_ASOCIACIONES =
                "No se puede eliminar el registro porque tiene información asociada";

        private Persistencia() {
        }
    }

    public static final class Operacion {

        public static final String USUARIO_AUTENTICADO_NO_ENCONTRADO =
                "Usuario autenticado no encontrado";
        public static final String UBICACION_NO_ASIGNADA =
                "La ubicación no está asignada al usuario empaquetador autenticado";

        private Operacion() {
        }
    }

}
