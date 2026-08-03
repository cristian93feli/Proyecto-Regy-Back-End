package com.regyinventory.service.implementation;

import com.regyinventory.dto.response.LogResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.entities.LogOperacion;
import com.regyinventory.repository.ILogOperacionRepository;
import com.regyinventory.service.contracts.ILogService;
import com.regyinventory.utils.constants.api.ValoresApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService implements ILogService {


    private final ILogOperacionRepository logOperacionRepository;

    @Override
    public PageResponseDTO<LogResponseDTO> listar(
            Integer numeroPagina,
            Integer tamanoPagina
    ) {
        PageRequest paginacion = PageRequest.of(
                numeroPagina,
                tamanoPagina,
                Sort.by(Sort.Direction.DESC, ValoresApi.CAMPO_FECHA_CREACION)
        );

        return PageResponseDTO.fromPage(
                logOperacionRepository.findAll(paginacion),
                this::convertirALogResponse
        );
    }

    private LogResponseDTO convertirALogResponse(LogOperacion logOperacion) {
        String nombreUsuario = logOperacion.getUsuario() == null
                ? null
                : logOperacion.getUsuario().getUsername();

        return LogResponseDTO.builder()
                .id(logOperacion.getId())
                .accion(logOperacion.getAccion())
                .entidad(logOperacion.getEntidad())
                .entidadId(logOperacion.getEntidadId())
                .detalle(logOperacion.getDetalle())
                .usuario(nombreUsuario)
                .fechaCreacion(logOperacion.getFechaCreacion())
                .build();
    }
}
