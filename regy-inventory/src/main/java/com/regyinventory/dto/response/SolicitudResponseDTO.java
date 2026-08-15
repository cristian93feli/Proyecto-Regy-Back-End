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
    private String productoNumero;
    private String productoNombre;
    private Integer cantidad;
    private Integer cantidadPendiente;
    private com.regyinventory.enums.PrioridadSolicitud prioridad;
    private com.regyinventory.enums.EstadoSolicitud estado;
    private Long zonaDestinoId;
    private String zonaDestinoNombre;
    private String usuarioSolicitante;
    private String usuarioResponsable;
    private String observaciones;
    private java.time.LocalDateTime fechaCreacion;
    private Long sugerenciaUbicacionId;
    private String sugerenciaUbicacionCodigo;
    private String sugerenciaUbicacionNombre;
    private Integer sugerenciaCantidadDisponible;
    private java.time.LocalDate sugerenciaFechaVencimiento;
    private String sugerenciaCriterio;
}
