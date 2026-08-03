package com.regyinventory.dto.request;

import com.regyinventory.enums.TipoUbicacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos necesarios para crear una ubicación física")
public class CrearUbicacionRequestDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 80, message = "El código no puede superar 80 caracteres")
    @Schema(example = "CAJA-A01-01")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    @Schema(example = "Caja A01-01")
    private String nombre;

    @Size(max = 250, message = "La descripción no puede superar 250 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de ubicación es obligatorio")
    @Schema(example = "CAJA", allowableValues = {"DEPOSITO", "ESTANTE", "CAJA", "ZONA_EMPAQUE"})
    private TipoUbicacion tipo;

    @Schema(description = "Obligatorio para ESTANTE y CAJA; nulo para DEPÓSITO y ZONA_EMPAQUE")
    private Long ubicacionPadreId;

    @Schema(description = "Usuario responsable; únicamente aplica a ZONA_EMPAQUE")
    private Long usuarioAsignadoId;
}
