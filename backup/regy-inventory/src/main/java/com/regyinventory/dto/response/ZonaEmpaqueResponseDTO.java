package com.regyinventory.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaEmpaqueResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long usuarioAsignadoId;
    private String usuarioAsignadoNombre;
    private Boolean activo;
}
