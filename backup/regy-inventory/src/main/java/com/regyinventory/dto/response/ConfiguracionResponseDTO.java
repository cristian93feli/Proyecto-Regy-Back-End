package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionResponseDTO {
    private Long id;
    private String clave;
    private String valor;
    private String descripcion;
}
