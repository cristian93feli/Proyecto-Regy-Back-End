package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaResponseDTO {
    private Long id;
    private com.regyinventory.enums.TipoAuditoria tipoAuditoria;
    private Long destinoId;
    private String destinoNombre;
    private com.regyinventory.enums.MotivoAjuste motivo;
    private String motivoOtro;
    private String observaciones;
    private String usuario;
    private java.time.LocalDateTime fechaCreacion;
    private java.util.List<DetalleAuditoriaResponseDTO> detalles;
}
