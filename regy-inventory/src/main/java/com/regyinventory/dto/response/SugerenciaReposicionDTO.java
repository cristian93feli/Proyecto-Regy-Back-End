package com.regyinventory.dto.response;

import java.time.LocalDate;
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
public class SugerenciaReposicionDTO {
    private Long ubicacionId;
    private String ubicacionCodigo;
    private String ubicacionNombre;
    private String estanteNombre;
    private String depositoNombre;
    private Integer cantidadDisponible;
    private LocalDate fechaVencimiento;
    private String criterio;
}
