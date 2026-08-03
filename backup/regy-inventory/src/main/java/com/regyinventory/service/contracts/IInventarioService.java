package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface IInventarioService {
    IngresoStockResponseDTO ingresar(IngresoStockRequestDTO r);

    MovimientoResponseDTO mover(MoverStockRequestDTO r);

    java.util.List<InventarioResponseDTO> existencias();

    java.util.List<InventarioResponseDTO> stockBajo();
}
