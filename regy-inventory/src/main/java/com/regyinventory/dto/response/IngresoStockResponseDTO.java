package com.regyinventory.dto.response;

import com.regyinventory.enums.TipoUbicacion;
import java.time.LocalDate;
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
public class IngresoStockResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private Long ubicacionDestinoId;
    private String ubicacionDestinoNombre;
    private TipoUbicacion tipoUbicacionDestino;
    private LocalDate fechaVencimiento;
    private String observaciones;
    private String usuario;
    private LocalDateTime fechaCreacion;
}
