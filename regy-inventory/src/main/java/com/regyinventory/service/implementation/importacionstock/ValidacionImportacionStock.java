package com.regyinventory.service.implementation.importacionstock;

import com.regyinventory.dto.response.ErrorImportacionStockDTO;
import java.util.List;

public record ValidacionImportacionStock(int total, List<FilaImportacionStock> validas,
                                        List<ErrorImportacionStockDTO> errores) { }
