package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponseDTO {
    private Long id;
    private com.regyinventory.enums.TipoAccionLog accion;
    private String entidad;
    private Long entidadId;
    private String detalle;
    private String usuario;
    private java.time.LocalDateTime fechaCreacion;
}
