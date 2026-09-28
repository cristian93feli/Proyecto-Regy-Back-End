package com.regyinventory.dto.response;

import com.regyinventory.enums.EstadoImportacionProducto;
import java.util.ArrayList;
import java.util.List;
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
public class ImportacionProductosProgresoDTO {
    private String id;
    private EstadoImportacionProducto estado;
    private Integer total;
    private Integer procesados;
    private Integer exitosos;
    private Integer erroresCantidad;
    private Integer porcentaje;
    private Integer marcasCreadas;
    private Integer categoriasCreadas;
    private String archivoErroresNombre;
    private String archivoErroresBase64;
    private String mensaje;
    @Builder.Default
    private List<ErrorImportacionProductoDTO> errores = new ArrayList<>();
}
