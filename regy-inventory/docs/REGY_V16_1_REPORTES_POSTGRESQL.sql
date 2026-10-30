-- REGY V16.1. Ejecutar solo si la instalación gestiona el esquema manualmente.
-- Si spring.jpa.hibernate.ddl-auto=update está habilitado, Hibernate puede crear esta tabla.
CREATE TABLE IF NOT EXISTS reportes_temporales (
    id VARCHAR(36) PRIMARY KEY,
    importacion_id VARCHAR(36) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    archivo_origen VARCHAR(255),
    nombre_reporte VARCHAR(255) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    vence_en TIMESTAMP WITH TIME ZONE NOT NULL,
    descargado_en TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_reportes_usuario_vigencia ON reportes_temporales(usuario_id, vence_en);
CREATE INDEX IF NOT EXISTS idx_reportes_vencimiento ON reportes_temporales(vence_en);
