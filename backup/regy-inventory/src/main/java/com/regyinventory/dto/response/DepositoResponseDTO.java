package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
