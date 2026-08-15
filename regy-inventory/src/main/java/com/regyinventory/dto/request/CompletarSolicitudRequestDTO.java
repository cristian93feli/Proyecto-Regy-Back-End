package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import com.regyinventory.utils.constants.numeros.Numeros;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletarSolicitudRequestDTO {
    private Long ubicacionOrigenId;

    @NotNull(message = MensajesValidacion.SolicitudReposicion.CANTIDAD_ENVIO_OBLIGATORIA)
    @Positive(message = MensajesValidacion.SolicitudReposicion.CANTIDAD_ENVIO_INVALIDA)
    private Integer cantidadEnviar;

    @Size(max = Numeros.CIEN)
    private String codigoBarrasValidacion;

    @Size(max = Numeros.QUINIENTOS)
    private String observaciones;
}
