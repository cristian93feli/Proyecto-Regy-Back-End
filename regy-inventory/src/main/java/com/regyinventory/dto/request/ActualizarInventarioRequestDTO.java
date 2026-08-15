package com.regyinventory.dto.request;

import com.regyinventory.utils.constants.mensajes.MensajesValidacion;
import com.regyinventory.utils.constants.numeros.Numeros;
import com.regyinventory.utils.constants.swagger.DocumentacionApi;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = DocumentacionApi.Inventario.DESCRIPCION_AJUSTAR_REQUEST)
public class ActualizarInventarioRequestDTO {

    @NotNull(message = MensajesValidacion.Inventario.PRODUCTO_OBLIGATORIO)
    private Long productoId;

    @NotNull(message = MensajesValidacion.Inventario.UBICACION_OBLIGATORIA)
    private Long ubicacionId;

    @NotNull(message = MensajesValidacion.Inventario.NUEVA_CANTIDAD_OBLIGATORIA)
    @PositiveOrZero(message = MensajesValidacion.Inventario.NUEVA_CANTIDAD_INVALIDA)
    private Integer nuevaCantidad;

    @Size(max = Numeros.QUINIENTOS, message = MensajesValidacion.Inventario.OBSERVACIONES_MAXIMAS)
    private String observaciones;
}
