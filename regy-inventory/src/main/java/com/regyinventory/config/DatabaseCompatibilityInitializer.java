package com.regyinventory.config;

import com.regyinventory.utils.constants.database.ConstantesBaseDatos;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajusta de forma idempotente restricciones de bases creadas por versiones anteriores de REGY.
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseCompatibilityInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Permite movimientos de entrada/salida sin origen o destino y evita CHECK obsoletos al ampliar enums.
     */
    @Override
    public void run(String... args) {
        jdbcTemplate.execute(ConstantesBaseDatos.MOVIMIENTO_ORIGEN_OPCIONAL);
        jdbcTemplate.execute(ConstantesBaseDatos.MOVIMIENTO_DESTINO_OPCIONAL);
        jdbcTemplate.execute(ConstantesBaseDatos.ELIMINAR_CHECK_PERMISOS);
        jdbcTemplate.execute(ConstantesBaseDatos.ELIMINAR_CHECK_TIPO_MOVIMIENTO);
    }
}
