package com.regyinventory.dto.request;

import com.regyinventory.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
public class CrearAuditoriaRequestDTO {
    @NotNull
    private TipoAuditoria tipoAuditoria;
    @NotNull
    private Long destinoId;
    @NotNull
    private MotivoAjuste motivo;
    @Size(max = 500)
    private String motivoOtro;
    @Size(max = 500)
    private String observaciones;
    @NotEmpty
    private List<@Valid ConteoAuditoriaRequestDTO> conteos;
}
