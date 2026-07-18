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

        validarNombre(request.getNombre());

        Marca marca = modelMapper.map(request, Marca.class);

        Marca guardada = marcaRepository.save(marca);

        return convertir(guardada);
    }

    @Override
    public MarcaResponseDTO buscarPorId(Long id) {

        return convertir(buscarEntidad(id));
    }

    @Override
    public PageResponseDTO<MarcaResponseDTO> listar(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    ) {

        Pageable pageable =
                PageableUtil.create(
                        page,
                        size,
                        sortBy,
                        direction
                );

        Page<Marca> resultado =
                marcaRepository.findAll(pageable);

        return PageResponseDTO.fromPage(
                resultado,
                this::convertir

        );
    }

    private Marca buscarEntidad(Long id) {

        return marcaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la marca con id " + id
                        ));
    }

    private void validarNombre(String nombre) {

        if (marcaRepository.existsByNombreIgnoreCase(
                nombre.trim()
        )) {

            throw new BusinessException(
                    "Ya existe una marca con ese nombre"
            );
        }

    }

    private MarcaResponseDTO convertir(
            Marca marca
    ) {

        return modelMapper.map(
                marca,
                MarcaResponseDTO.class
        );

    }

    @Override
    @Transactional
    public MarcaResponseDTO actualizar(
            Long id,
            ActualizarMarcaRequestDTO request
    ) {

        Marca marca = buscarEntidad(id);

        validarActualizacion(
                id,
                request.getNombre()
        );

        marca.setNombre(
                request.getNombre().trim()
        );

        marca.setDescripcion(
                request.getDescripcion()
        );

        Marca actualizada =
                marcaRepository.save(marca);

        return convertir(actualizada);

    }

    @Override
    @Transactional
    public MarcaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    ) {

        Marca marca = buscarEntidad(id);

        if (Boolean.TRUE.equals(marca.getActivo()) == activo) {

            throw new BusinessException(
                    activo
                            ? "La marca ya se encuentra activa"
                            : "La marca ya se encuentra inactiva"
            );
        }

        marca.setActivo(activo);

        Marca actualizada =
                marcaRepository.save(marca);

        return convertir(actualizada);
    }

    private void validarActualizacion(
            Long id,
            String nombre
    ) {

        if (marcaRepository
                .existsByNombreIgnoreCaseAndIdNot(
                        nombre.trim(),
                        id
                )) {

            throw new BusinessException(
                    "Ya existe otra marca con ese nombre"
            );
        }
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Marca marca = buscarEntidad(id);

        try {
            marcaRepository.delete(marca);
            marcaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    "No se puede eliminar la marca porque tiene registros asociados"
            );
        }
    }
}