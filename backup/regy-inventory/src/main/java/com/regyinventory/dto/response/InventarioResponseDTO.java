package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDTO {
    private Long productoId;
    private String numeroProducto;
    private String nombreProducto;
    private com.regyinventory.enums.TipoDestino tipoDestino;
    private Long destinoId;
    private String destinoNombre;
    private Integer cantidad;
    private Integer stockMinimo;
    private Boolean stockBajo;
}
