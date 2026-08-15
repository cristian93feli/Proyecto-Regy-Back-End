package com.regyinventory.utils.constants.database;

/**
 * Sentencias idempotentes requeridas para compatibilizar bases existentes con nuevas reglas del dominio.
 */
public final class ConstantesBaseDatos {

    private ConstantesBaseDatos() {
    }

    public static final String MOVIMIENTO_ORIGEN_OPCIONAL =
            "ALTER TABLE movimientos_inventario ALTER COLUMN ubicacion_origen_id DROP NOT NULL";

    public static final String MOVIMIENTO_DESTINO_OPCIONAL =
            "ALTER TABLE movimientos_inventario ALTER COLUMN ubicacion_destino_id DROP NOT NULL";

    public static final String ELIMINAR_CHECK_PERMISOS =
            "ALTER TABLE permisos DROP CONSTRAINT IF EXISTS permisos_nombre_check";

    public static final String ELIMINAR_CHECK_TIPO_MOVIMIENTO =
            "ALTER TABLE movimientos_inventario DROP CONSTRAINT IF EXISTS movimientos_inventario_tipo_movimiento_check";
}
