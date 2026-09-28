package com.regyinventory.utils.constants.mensajes;

public final class MensajesExito {

    private MensajesExito() {
    }

    public static final class Autenticacion {

        public static final String LOGIN =
                "Inicio de sesión exitoso";

        private Autenticacion() {
        }
    }

    public static final class Usuario {

        public static final String CREADO =
                "Usuario creado correctamente";

        public static final String ENCONTRADO =
                "Usuario encontrado";

        public static final String LISTADOS =
                "Usuarios consultados correctamente";

        public static final String ACTUALIZADO =
                "Usuario actualizado correctamente";

        public static final String CONTRASENA_ACTUALIZADA =
                "Contraseña actualizada correctamente";

        public static final String ACTIVADO =
                "Usuario activado correctamente";

        public static final String DESACTIVADO =
                "Usuario desactivado correctamente";

        public static final String ELIMINADO = "Usuario eliminado correctamente";

        private Usuario() {
        }
    }

    public static final class Rol {

        public static final String ENCONTRADO =
                "Rol encontrado";

        public static final String LISTADOS =
                "Roles consultados correctamente";

        private Rol() {
        }
    }

    public static final class Marca {

        public static final String CREADA =
                "Marca creada correctamente";

        public static final String ENCONTRADA =
                "Marca encontrada";

        public static final String LISTADAS =
                "Marcas consultadas correctamente";

        public static final String ACTUALIZADA =
                "Marca actualizada correctamente";

        public static final String ACTIVADA =
                "Marca activada correctamente";

        public static final String DESACTIVADA =
                "Marca desactivada correctamente";

        public static final String ELIMINADA =
                "Marca eliminada correctamente";

        private Marca() {
        }
    }

    public static final class Categoria {

        public static final String CREADA =
                "Categoría creada correctamente";

        public static final String ENCONTRADA =
                "Categoría encontrada";

        public static final String LISTADAS =
                "Categorías consultadas correctamente";

        public static final String ACTUALIZADA =
                "Categoría actualizada correctamente";

        public static final String ACTIVADA =
                "Categoría activada correctamente";

        public static final String DESACTIVADA =
                "Categoría desactivada correctamente";

        public static final String ELIMINADA =
                "Categoría eliminada correctamente";

        private Categoria() {
        }
    }

    public static final class Producto {

        public static final String CREADO =
                "Producto creado correctamente";

        public static final String ENCONTRADO =
                "Producto encontrado";

        public static final String LISTADOS =
                "Productos consultados correctamente";

        public static final String ACTUALIZADO =
                "Producto actualizado correctamente";

        public static final String ACTIVADO =
                "Producto activado correctamente";

        public static final String DESACTIVADO =
                "Producto desactivado correctamente";

        public static final String ELIMINADO =
                "Producto eliminado correctamente";
        public static final String BUSCADOS = "Productos encontrados correctamente";
        public static final String IMPORTADOS = "Importación de productos procesada correctamente";
        public static final String IMPORTACION_EN_PROCESO = "Importación en proceso";
        public static final String IMPORTACION_FINALIZADA = "Importación finalizada";

        public static final String NOMBRE_ENTIDAD =
                "Producto";

        public static final String LOG_CREADO =
                "Producto creado: %s";

        public static final String LOG_ACTUALIZADO =
                "Producto actualizado";

        public static final String LOG_ACTIVADO =
                "Producto activado";

        public static final String LOG_DESACTIVADO =
                "Producto desactivado";

        public static final String LOG_ELIMINADO =
                "Producto eliminado";

        private Producto() {
        }
    }

    public static final class Ubicacion {
        public static final String CREADA = "Ubicación creada correctamente";
        public static final String ENCONTRADA = "Ubicación consultada correctamente";
        public static final String LISTADAS = "Ubicaciones consultadas correctamente";
        public static final String HIJAS_LISTADAS = "Ubicaciones hijas consultadas correctamente";
        public static final String ACTUALIZADA = "Ubicación actualizada correctamente";
        public static final String ACTIVADA = "Ubicación activada correctamente";
        public static final String DESACTIVADA = "Ubicación desactivada correctamente";
        public static final String ELIMINADA = "Ubicación eliminada correctamente";
        private Ubicacion() { }
    }

    public static final class Inventario {
        public static final String INGRESADO = "Stock ingresado correctamente";
        public static final String MOVIDO = "Stock movido correctamente";
        public static final String EXISTENCIAS = "Existencias consultadas correctamente";
        public static final String STOCK_BAJO = "Stock bajo consultado correctamente";
        public static final String VENTA_REGISTRADA = "Salida por venta registrada correctamente";
        public static final String ORIGENES_LISTADOS = "Ubicaciones con stock consultadas correctamente";
        public static final String PRODUCTOS_DISPONIBLES = "Productos con stock consultados correctamente";
        public static final String AJUSTADO = "Inventario ajustado correctamente";
        private Inventario() { }
    }

    public static final class SolicitudReposicion {
        public static final String CREADA = "Solicitud creada correctamente";
        public static final String LISTADAS = "Solicitudes consultadas correctamente";
        public static final String COMPLETADA = "Solicitud completada correctamente";
        public static final String COMPLETADA_PARCIAL = "Solicitud atendida parcialmente; quedó cantidad pendiente";
        public static final String CANCELADA = "Solicitud cancelada correctamente";
        public static final String SUGERENCIAS = "Sugerencias de reposición consultadas correctamente";
        private SolicitudReposicion() { }
    }

    public static final class Auditoria {
        public static final String REGISTRADA = "Auditoría registrada correctamente";
        public static final String LISTADAS = "Auditorías consultadas correctamente";
        private Auditoria() { }
    }

    public static final class Configuracion {
        public static final String LISTADA = "Configuración consultada correctamente";
        public static final String ACTUALIZADA = "Configuración actualizada correctamente";
        private Configuracion() { }
    }

    public static final class Dashboard {
        public static final String CONSULTADO = "Dashboard consultado correctamente";
        private Dashboard() { }
    }

    public static final class Log {
        public static final String LISTADOS = "Logs consultados correctamente";
        private Log() { }
    }

}
