package com.regyinventory.dto.response;

import com.regyinventory.enums.TipoUbicacion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ubicación física de inventario")
public class UbicacionResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoUbicacion tipo;
    private Long ubicacionPadreId;
    private String ubicacionPadreNombre;
    private Long depositoId;
    private String depositoNombre;
    private java.util.Set<Long> usuariosResponsablesIds;
    private java.util.Set<String> usuariosResponsablesNombres;
    private Boolean admiteInventario;
    private Boolean activo;
}
