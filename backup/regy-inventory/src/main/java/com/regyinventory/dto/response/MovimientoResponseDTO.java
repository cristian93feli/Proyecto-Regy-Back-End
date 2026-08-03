package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private com.regyinventory.enums.TipoMovimiento tipoMovimiento;
    private com.regyinventory.enums.TipoDestino tipoOrigen;
    private Long origenId;
    private String origenNombre;
    private com.regyinventory.enums.TipoDestino tipoDestino;
    private Long destinoId;
    private String destinoNombre;
    private String observaciones;
    private String usuario;
    private java.time.LocalDateTime fechaCreacion;
}
