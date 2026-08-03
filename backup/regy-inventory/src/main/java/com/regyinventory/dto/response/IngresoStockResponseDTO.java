package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoStockResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private com.regyinventory.enums.TipoDestino tipoDestino;
    private Long destinoId;
    private String destinoNombre;
    private java.time.LocalDate fechaVencimiento;
    private String observaciones;
    private String usuario;
    private java.time.LocalDateTime fechaCreacion;
}
