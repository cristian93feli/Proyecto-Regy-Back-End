package com.regyinventory.dto.response;

import com.regyinventory.enums.EstadoImportacionProducto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Estado consultable de la importación masiva, independiente de la de productos. */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ImportacionStockProgresoDTO {
    private String id;
    private EstadoImportacionProducto estado;
    private Integer total;
    private Integer procesados;
    private Integer exitosos;
    private Integer rechazados;
    private Integer erroresCantidad;
    private Integer unidadesIngresadas;
    private Integer ubicacionesAfectadas;
    private Integer porcentaje;
    private String mensaje;
    private String reporteId;
    private String archivoErroresNombre;
    private String archivoErroresBase64;
    @Builder.Default
    private List<ErrorImportacionStockDTO> errores = new ArrayList<>();
}
