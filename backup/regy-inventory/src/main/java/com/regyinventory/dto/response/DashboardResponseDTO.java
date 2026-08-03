package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private Integer stockTotal;
    private Integer stockDepositos;
    private Integer stockZonasEmpaque;
    private Long productosActivos;
    private Long productosStockBajo;
    private Long solicitudesPendientes;
    private java.util.List<InventarioResponseDTO> stockBajo;
}
