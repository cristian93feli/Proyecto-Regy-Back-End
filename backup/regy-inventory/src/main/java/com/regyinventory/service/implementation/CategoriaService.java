package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarCategoriaRequestDTO;
import com.regyinventory.dto.request.CrearCategoriaRequestDTO;
import com.regyinventory.dto.response.CategoriaResponseDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.entities.Categoria;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.ICategoriaRepository;
import com.regyinventory.service.contracts.ICategoriaService;
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
public class CategoriaService
        implements ICategoriaService {

    private final ICategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(
            CrearCategoriaRequestDTO request
    ) {

        String nombre =
                request.getNombre().trim();

        validarNombreCreacion(nombre);

        Categoria categoria =
                modelMapper.map(
                        request,
                        Categoria.class
                );

        categoria.setNombre(nombre);
        categoria.setDescripcion(
                normalizarDescripcion(
                        request.getDescripcion()
                )
        );

        Categoria categoriaGuardada =
                categoriaRepository.save(categoria);

        return convertirRespuesta(
                categoriaGuardada
        );
    }

    @Override
    public CategoriaResponseDTO buscarPorId(
            Long id
    ) {

        return convertirRespuesta(
                buscarEntidad(id)
        );
    }

    @Override
    public PageResponseDTO<CategoriaResponseDTO> listar(
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

        Page<Categoria> resultado =
                categoriaRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                resultado,
                this::convertirRespuesta
        );
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(
            Long id,
            ActualizarCategoriaRequestDTO request
    ) {

        Categoria categoria =
                buscarEntidad(id);

        String nombre =
                request.getNombre().trim();

        validarNombreActualizacion(
                id,
                nombre
        );

        categoria.setNombre(nombre);
        categoria.setDescripcion(
                normalizarDescripcion(
                        request.getDescripcion()
                )
        );

        Categoria categoriaActualizada =
                categoriaRepository.save(categoria);

        return convertirRespuesta(
                categoriaActualizada
        );
    }

    @Override
    @Transactional
    public CategoriaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    ) {

        Categoria categoria =
                buscarEntidad(id);

        if (Boolean.TRUE.equals(
                categoria.getActivo()
        ) == activo) {

            throw new BusinessException(
                    activo
                            ? MensajesError.Categoria.YA_ACTIVA
                            : MensajesError.Categoria.YA_INACTIVA
            );
        }

        categoria.setActivo(activo);

        Categoria categoriaActualizada =
                categoriaRepository.save(categoria);

        return convertirRespuesta(
                categoriaActualizada
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria =
                buscarEntidad(id);

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();

        } catch (DataIntegrityViolationException exception) {

            throw new BusinessException(
                    MensajesError.Categoria
                            .TIENE_REGISTROS_ASOCIADOS
            );
        }
    }

    private Categoria buscarEntidad(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                String.format(
                                        MensajesError.Categoria.NO_EXISTE,
                                        id
                                )
                        )
                );
    }

    private void validarNombreCreacion(
            String nombre
    ) {

        if (categoriaRepository
                .existsByNombreIgnoreCase(nombre)) {

            throw new BusinessException(
                    MensajesError.Categoria
                            .NOMBRE_DUPLICADO
            );
        }
    }

    private void validarNombreActualizacion(
            Long id,
            String nombre
    ) {

        if (categoriaRepository
                .existsByNombreIgnoreCaseAndIdNot(
                        nombre,
                        id
                )) {

            throw new BusinessException(
                    MensajesError.Categoria
                            .OTRO_NOMBRE_DUPLICADO
            );
        }
    }

    private CategoriaResponseDTO convertirRespuesta(
            Categoria categoria
    ) {

        return modelMapper.map(
                categoria,
                CategoriaResponseDTO.class
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