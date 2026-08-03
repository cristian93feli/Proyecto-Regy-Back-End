package com.regyinventory.service.contracts;

import com.regyinventory.dto.response.DashboardResponseDTO;

/** Proporciona indicadores operativos consolidados para el dashboard. */
public interface IDashboardService {

    /** @return resumen actual del inventario y solicitudes */
    DashboardResponseDTO resumen();
}
