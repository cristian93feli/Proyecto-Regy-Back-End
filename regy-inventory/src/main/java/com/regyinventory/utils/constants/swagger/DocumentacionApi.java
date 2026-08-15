package com.regyinventory.utils.constants.swagger;

public final class DocumentacionApi {

    public static final String SEGURIDAD_BEARER = "bearerAuth";

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

        public static final String ELIMINAR = "Eliminar usuario";
        public static final String DESCRIPCION_ELIMINAR = "Elimina un usuario únicamente cuando no rompe responsabilidades de zona ni integridad histórica.";

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

    public static final class CodigoRespuesta {
        public static final String EXITO = "200";
        public static final String CREADO = "201";
        public static final String SOLICITUD_INVALIDA = "400";
        public static final String NO_AUTENTICADO = "401";
        public static final String SIN_PERMISO = "403";
        public static final String NO_ENCONTRADO = "404";
        public static final String CONFLICTO = "409";

        private CodigoRespuesta() {
        }
    }

    public static final class Respuesta {

        public static final String CREADO = "Recurso creado correctamente";
        public static final String ACTUALIZADO = "Recurso actualizado correctamente";
        public static final String CONSULTA_EXITOSA = "Consulta realizada correctamente";
        public static final String SOLICITUD_INVALIDA = "La solicitud contiene datos inválidos";
        public static final String NO_AUTENTICADO = "Token ausente, inválido o vencido";
        public static final String SIN_PERMISO = "El usuario no tiene el permiso requerido";
        public static final String NO_ENCONTRADO = "El recurso solicitado no existe";
        public static final String CONFLICTO_NEGOCIO = "La operación incumple una regla de negocio";

        private Respuesta() {
        }
    }

    public static final class Producto {

        public static final String TAG = "Productos";
        public static final String DESCRIPCION_TAG =
                "Administración del catálogo maestro de productos";
        public static final String CREAR = "Crear producto";
        public static final String DESCRIPCION_CREAR =
                "Registra un producto sin generar inventario. Valida número, SKU, código de barras y relaciones activas.";
        public static final String CONSULTAR = "Consultar producto";
        public static final String DESCRIPCION_CONSULTAR =
                "Obtiene un producto por su identificador e incluye el stock total calculado desde sus lotes.";
        public static final String LISTAR = "Listar productos";
        public static final String DESCRIPCION_LISTAR =
                "Obtiene el catálogo de productos utilizando paginación y ordenamiento.";
        public static final String ACTUALIZAR = "Actualizar producto";
        public static final String DESCRIPCION_ACTUALIZAR =
                "Actualiza los datos maestros de un producto sin modificar directamente su inventario.";
        public static final String ACTIVAR = "Activar producto";
        public static final String DESCRIPCION_ACTIVAR =
                "Habilita un producto para nuevas operaciones del sistema.";
        public static final String DESACTIVAR = "Desactivar producto";
        public static final String DESCRIPCION_DESACTIVAR =
                "Inhabilita el producto para nuevas operaciones sin eliminar su historial.";
        public static final String ELIMINAR = "Eliminar producto";
        public static final String DESCRIPCION_ELIMINAR =
                "Elimina el producto únicamente cuando no tiene stock ni solicitudes pendientes.";

        public static final String NUMERO = "Número interno único del producto";
        public static final String NOMBRE = "Nombre comercial del producto";
        public static final String MARCA_ID = "Identificador opcional de una marca activa";
        public static final String CATEGORIA_ID = "Identificador opcional de una categoría activa";
        public static final String SKU = "Código SKU opcional y único";
        public static final String CODIGO_BARRAS = "Código de barras opcional y único";
        public static final String IMAGEN_URL = "Dirección opcional de la imagen del producto";
        public static final String PRECIO_COMPRA = "Precio de compra no negativo";
        public static final String PRECIO_VENTA = "Precio de venta no negativo";
        public static final String UNIDAD_MEDIDA = "Unidad utilizada para controlar el producto";
        public static final String STOCK_MINIMO = "Cantidad mínima esperada antes de generar alerta";
        public static final String STOCK_TOTAL = "Stock calculado a partir de los lotes existentes";
        public static final String ACTIVO = "Indica si el producto está habilitado";

        public static final String BUSCAR = "Buscar productos";
        public static final String DESCRIPCION_BUSCAR = "Busca productos activos por número, nombre o código de barras respetando el alcance del usuario autenticado.";
        public static final String IMPORTAR = "Importar productos desde Excel";
        public static final String DESCRIPCION_IMPORTAR = "Procesa el archivo Excel de productos y crea previamente marcas y categorías que no existan.";

        private Producto() {
        }
    }

    public static final class Ubicacion {
        public static final String TAG = "Ubicaciones físicas";
        public static final String DESCRIPCION_TAG = "Administración de depósitos, estantes, cajas y zonas de empaque";
        public static final String CREAR = "Crear ubicación";
        public static final String DESCRIPCION_CREAR = "Crea una ubicación respetando la jerarquía depósito, estante y caja, o una zona de empaque independiente.";
        public static final String CONSULTAR = "Consultar ubicación";
        public static final String DESCRIPCION_CONSULTAR = "Obtiene el detalle y la jerarquía de una ubicación.";
        public static final String LISTAR = "Listar ubicaciones";
        public static final String DESCRIPCION_LISTAR = "Lista ubicaciones con paginación y ordenamiento.";
        public static final String LISTAR_TIPO = "Listar ubicaciones por tipo";
        public static final String DESCRIPCION_LISTAR_TIPO =
                "Lista depósitos, estantes, cajas o zonas de empaque según el tipo indicado.";
        public static final String LISTAR_HIJAS = "Listar ubicaciones hijas";
        public static final String DESCRIPCION_LISTAR_HIJAS =
                "Obtiene los estantes de un depósito o las cajas de un estante.";
        public static final String ACTUALIZAR = "Actualizar ubicación";
        public static final String DESCRIPCION_ACTUALIZAR =
                "Actualiza el código, nombre, descripción y usuario asignado sin cambiar el tipo ni la jerarquía.";
        public static final String ACTIVAR = "Activar ubicación";
        public static final String DESCRIPCION_ACTIVAR =
                "Habilita una ubicación para nuevas operaciones.";
        public static final String DESACTIVAR = "Desactivar ubicación";
        public static final String DESCRIPCION_DESACTIVAR =
                "Desactiva la ubicación únicamente cuando no tiene ubicaciones hijas ni stock.";
        public static final String ELIMINAR = "Eliminar ubicación";
        public static final String DESCRIPCION_ELIMINAR =
                "Elimina definitivamente una ubicación únicamente cuando no tiene hijas ni existencias.";
        private Ubicacion() { }
    }

    public static final class Inventario {
        public static final String TAG = "Inventario";
        public static final String DESCRIPCION_TAG = "Ingresos, movimientos y consultas de existencias por producto y ubicación";
        public static final String INGRESAR = "Registrar ingreso de stock";
        public static final String DESCRIPCION_INGRESAR = "Registra mercancía en una caja activa y crea los lotes correspondientes.";
        public static final String MOVER = "Mover stock";
        public static final String DESCRIPCION_MOVER = "Transfiere stock entre cajas o zonas de empaque aplicando FEFO y FIFO.";
        public static final String EXISTENCIAS = "Consultar existencias";
        public static final String STOCK_BAJO = "Consultar productos con stock bajo";
        public static final String VENTA = "Registrar salida por venta";
        public static final String DESCRIPCION_VENTA = "Descuenta inventario aplicando FEFO/FIFO sin crear una entidad comercial de venta";
        public static final String DESCRIPCION_VENTA_REQUEST = "Salida de inventario por venta, sin registrar una entidad comercial de venta";
        public static final String ORIGENES = "Consultar orígenes con stock del producto";
        public static final String PRODUCTOS_DISPONIBLES = "Consultar productos con stock disponible para venta";
        public static final String AJUSTAR = "Ajustar inventario";
        public static final String DESCRIPCION_AJUSTAR = "Permite al administrador establecer una nueva cantidad y registra la diferencia como ajuste positivo o negativo.";
        public static final String DESCRIPCION_AJUSTAR_REQUEST = "Ajuste administrativo de la cantidad de un producto en una ubicación";
        private Inventario() { }
    }

    public static final class SolicitudReposicion {
        public static final String TAG = "Solicitudes de reposición";
        public static final String DESCRIPCION_TAG = "Gestión del abastecimiento de zonas de empaque desde cajas";
        public static final String CREAR = "Crear solicitud de reposición";
        public static final String LISTAR = "Listar solicitudes de reposición";
        public static final String COMPLETAR = "Completar solicitud de reposición";
        public static final String CANCELAR = "Cancelar solicitud de reposición";
        public static final String SUGERENCIAS = "Consultar cajas sugeridas por FEFO/FIFO";
        private SolicitudReposicion() { }
    }

    public static final class Auditoria {
        public static final String TAG = "Auditorías de inventario";
        public static final String DESCRIPCION_TAG = "Registro y consulta de conteos físicos y ajustes de inventario";
        public static final String CREAR = "Registrar auditoría";
        public static final String LISTAR = "Listar auditorías";
        private Auditoria() { }
    }

    public static final class Configuracion {
        public static final String TAG = "Configuración";
        public static final String DESCRIPCION_TAG = "Consulta y actualización de parámetros funcionales del sistema";
        public static final String LISTAR = "Consultar configuración";
        public static final String ACTUALIZAR = "Actualizar configuración";
        private Configuracion() { }
    }

    public static final class Dashboard {
        public static final String TAG = "Dashboard";
        public static final String DESCRIPCION_TAG = "Indicadores resumidos del inventario y la operación";
        public static final String CONSULTAR = "Consultar resumen del dashboard";
        private Dashboard() { }
    }

    public static final class Log {
        public static final String TAG = "Logs";
        public static final String DESCRIPCION_TAG = "Consulta paginada de la trazabilidad de acciones del sistema";
        public static final String LISTAR = "Listar logs";
        private Log() { }
    }

}
