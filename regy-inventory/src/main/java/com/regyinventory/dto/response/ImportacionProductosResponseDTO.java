package com.regyinventory.dto.response;

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
public class ImportacionProductosResponseDTO {
    private Integer filasProcesadas;
    private Integer productosCreados;
    private Integer marcasCreadas;
    private Integer categoriasCreadas;
    @Builder.Default
    private List<ErrorImportacionProductoDTO> errores = new ArrayList<>();
}
