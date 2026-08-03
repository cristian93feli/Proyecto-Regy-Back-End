package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAuditoriaResponseDTO {
    private Long productoId;
    private String productoNombre;
    private Integer cantidadSistema;
    private Integer cantidadContada;
    private Integer diferencia;
}
