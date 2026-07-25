package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarMarcaRequestDTO;
import com.regyinventory.dto.request.CrearMarcaRequestDTO;
import com.regyinventory.dto.response.MarcaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.entities.Marca;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.IMarcaRepository;
import com.regyinventory.service.contracts.IMarcaService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarcaService implements IMarcaService {

    private final IMarcaRepository marcaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public MarcaResponseDTO crear(
            CrearMarcaRequestDTO request
    ) {

        String nombre =
                request.getNombre().trim();

        validarNombreCreacion(nombre);

        Marca marca =
                modelMapper.map(
                        request,
                        Marca.class
                );

        marca.setNombre(nombre);
        marca.setDescripcion(
                normalizarDescripcion(
                        request.getDescripcion()
                )
        );

        Marca marcaGuardada =
                marcaRepository.save(marca);

        return convertirRespuesta(marcaGuardada);
    }

    @Override
    public MarcaResponseDTO buscarPorId(Long id) {

        return convertirRespuesta(
                buscarEntidad(id)
        );
    }

    @Override
    public PageResponseDTO<MarcaResponseDTO> listar(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    ) {

        Pageable pageable = PageableUtil.create(
                page,
                size,
                sortBy,
                direction
        );

        Page<Marca> resultado =
                marcaRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                resultado,
                this::convertirRespuesta
        );
    }

    @Override
    @Transactional
    public MarcaResponseDTO actualizar(
            Long id,
            ActualizarMarcaRequestDTO request
    ) {

        Marca marca =
                buscarEntidad(id);

        String nombre =
                request.getNombre().trim();

        validarNombreActualizacion(
                id,
                nombre
        );

        marca.setNombre(nombre);
        marca.setDescripcion(
                normalizarDescripcion(
                        request.getDescripcion()
                )
        );

        Marca marcaActualizada =
                marcaRepository.save(marca);

        return convertirRespuesta(
                marcaActualizada
        );
    }

    @Override
    @Transactional
    public MarcaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    ) {

        Marca marca =
                buscarEntidad(id);

        if (Boolean.TRUE.equals(
                marca.getActivo()
        ) == activo) {

            throw new BusinessException(
                    activo
                            ? MensajesError.Marca.YA_ACTIVA
                            : MensajesError.Marca.YA_INACTIVA
            );
        }

        marca.setActivo(activo);

        Marca marcaActualizada =
                marcaRepository.save(marca);

        return convertirRespuesta(
                marcaActualizada
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Marca marca =
                buscarEntidad(id);

        try {
            marcaRepository.delete(marca);
            marcaRepository.flush();

        } catch (DataIntegrityViolationException exception) {

            throw new BusinessException(
                    MensajesError.Marca
                            .TIENE_REGISTROS_ASOCIADOS
            );
        }
    }

    private Marca buscarEntidad(Long id) {

        return marcaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format(
                                        MensajesError.Marca.NO_EXISTE,
                                        id
                                )
                        )
                );
    }

    private void validarNombreCreacion(
            String nombre
    ) {

        if (marcaRepository
                .existsByNombreIgnoreCase(nombre)) {

            throw new BusinessException(
                    MensajesError.Marca.NOMBRE_DUPLICADO
            );
        }
    }

    private void validarNombreActualizacion(
            Long id,
            String nombre
    ) {

        if (marcaRepository
                .existsByNombreIgnoreCaseAndIdNot(
                        nombre,
                        id
                )) {

            throw new BusinessException(
                    MensajesError.Marca
                            .OTRO_NOMBRE_DUPLICADO
            );
        }
    }

    private MarcaResponseDTO convertirRespuesta(
            Marca marca
    ) {

        return modelMapper.map(
                marca,
                MarcaResponseDTO.class
        );
    }

    private String normalizarDescripcion(
            String descripcion
    ) {

        if (descripcion == null
                || descripcion.isBlank()) {

            return null;
        }

        return descripcion.trim();
    }
}