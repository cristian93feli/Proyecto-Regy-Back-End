package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private com.regyinventory.enums.PrioridadSolicitud prioridad;
    private com.regyinventory.enums.EstadoSolicitud estado;
    private Long zonaDestinoId;
    private String zonaDestinoNombre;
    private String usuarioSolicitante;
    private String usuarioResponsable;
    private String observaciones;
    private java.time.LocalDateTime fechaCreacion;
}
