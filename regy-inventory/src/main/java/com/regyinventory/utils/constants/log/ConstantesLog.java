package com.regyinventory.utils.constants.log;

/**
 * Nombres de entidades y descripciones estandarizadas para la trazabilidad.
 */
public final class ConstantesLog {

    private ConstantesLog() {
    }

    public static final class Entidad {
        public static final String UBICACION = "Ubicacion";
        public static final String INGRESO_STOCK = "IngresoStock";
        public static final String MOVIMIENTO_INVENTARIO = "MovimientoInventario";
        public static final String SOLICITUD_REPOSICION = "SolicitudReposicion";
        public static final String AUDITORIA_INVENTARIO = "AuditoriaInventario";
        public static final String CONFIGURACION_SISTEMA = "ConfiguracionSistema";

        private Entidad() {
        }
    }

    public static final class Detalle {
        public static final String UBICACION_CREADA = "Ubicación creada";
        public static final String INGRESO_STOCK_REGISTRADO = "Ingreso de stock registrado";
        public static final String MOVIMIENTO_STOCK_REGISTRADO = "Movimiento de stock registrado";
        public static final String AJUSTE_INVENTARIO_REGISTRADO = "Ajuste administrativo de inventario registrado";
        public static final String SOLICITUD_CREADA = "Solicitud creada";
        public static final String SOLICITUD_COMPLETADA = "Solicitud completada";
        public static final String SOLICITUD_COMPLETADA_PARCIAL = "Solicitud atendida parcialmente";
        public static final String SOLICITUD_CANCELADA = "Solicitud cancelada";
        public static final String AUDITORIA_REALIZADA = "Auditoría realizada en %s";
        public static final String CONFIGURACION_ACTUALIZADA = "Configuración actualizada: %s";

        private Detalle() {
        }
    }
}
