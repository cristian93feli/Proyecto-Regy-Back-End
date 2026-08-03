package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Long depositoId;
    private String depositoNombre;
    private Boolean activo;
}
