package com.regyinventory.dto.response;

import com.regyinventory.enums.TipoMovimiento;
import com.regyinventory.enums.TipoUbicacion;
import java.time.LocalDateTime;
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
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private TipoMovimiento tipoMovimiento;
    private Long ubicacionOrigenId;
    private String ubicacionOrigenNombre;
    private TipoUbicacion tipoUbicacionOrigen;
    private Long ubicacionDestinoId;
    private String ubicacionDestinoNombre;
    private TipoUbicacion tipoUbicacionDestino;
    private String observaciones;
    private String usuario;
    private LocalDateTime fechaCreacion;
}
