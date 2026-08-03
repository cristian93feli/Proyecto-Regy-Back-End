package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ActualizarUbicacionRequestDTO;
import com.regyinventory.dto.request.CrearUbicacionRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
import com.regyinventory.enums.TipoUbicacion;

import java.util.List;

/**
 * Gestiona la jerarquía física del inventario.
 *
 * <p>Jerarquía permitida: DEPÓSITO → ESTANTE → CAJA. Las zonas de empaque
 * son nodos raíz, no admiten hijos y pueden contener inventario temporal.</p>
 */
public interface IUbicacionService {
    UbicacionResponseDTO crear(CrearUbicacionRequestDTO solicitud);

    UbicacionResponseDTO actualizar(Long ubicacionId, ActualizarUbicacionRequestDTO solicitud);

    UbicacionResponseDTO buscar(Long ubicacionId);

    PageResponseDTO<UbicacionResponseDTO> listar(Integer pagina, Integer tamano, String ordenarPor, String direccion);

    List<UbicacionResponseDTO> listarPorTipo(TipoUbicacion tipoUbicacion);

    List<UbicacionResponseDTO> listarHijas(Long ubicacionPadreId);

    UbicacionResponseDTO cambiarEstado(Long ubicacionId, boolean activo);

    void eliminar(Long ubicacionId);
}
