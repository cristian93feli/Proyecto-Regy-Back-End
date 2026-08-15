package com.regyinventory.dto.response;

import com.regyinventory.enums.TipoUbicacion;
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
public class InventarioResponseDTO {
    private Long productoId;
    private String numeroProducto;
    private String nombreProducto;
    private Long ubicacionId;
    private String ubicacionNombre;
    private TipoUbicacion tipoUbicacion;
    private Long depositoId;
    private String depositoNombre;
    private java.time.LocalDate fechaVencimiento;
    private Integer cantidad;
    private Integer stockMinimo;
    private Boolean stockBajo;
}
