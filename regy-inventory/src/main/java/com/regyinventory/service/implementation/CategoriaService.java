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
public class CategoriaService implements ICategoriaService {

    private final ICategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(
            CrearCategoriaRequestDTO request
    ) {

        String nombre = request.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException(
                    "Ya existe una categoría con ese nombre"
            );
        }

        Categoria categoria =
                modelMapper.map(request, Categoria.class);

        categoria.setNombre(nombre);
        categoria.setDescripcion(
                normalizarDescripcion(request.getDescripcion())
        );

        Categoria guardada =
                categoriaRepository.save(categoria);

        return convertir(guardada);
    }

    @Override
    public CategoriaResponseDTO buscarPorId(Long id) {
        return convertir(buscarEntidad(id));
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
                this::convertir
        );
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(
            Long id,
            ActualizarCategoriaRequestDTO request
    ) {

        Categoria categoria = buscarEntidad(id);
        String nombre = request.getNombre().trim();

        if (categoriaRepository
                .existsByNombreIgnoreCaseAndIdNot(nombre, id)) {

            throw new BusinessException(
                    "Ya existe otra categoría con ese nombre"
            );
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(
                normalizarDescripcion(request.getDescripcion())
        );

        Categoria actualizada =
                categoriaRepository.save(categoria);

        return convertir(actualizada);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO cambiarEstado(
            Long id,
            boolean activo
    ) {

        Categoria categoria = buscarEntidad(id);

        if (Boolean.TRUE.equals(categoria.getActivo()) == activo) {
            throw new BusinessException(
                    activo
                            ? "La categoría ya se encuentra activa"
                            : "La categoría ya se encuentra inactiva"
            );
        }

        categoria.setActivo(activo);

        Categoria actualizada =
                categoriaRepository.save(categoria);

        return convertir(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = buscarEntidad(id);

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene registros asociados"
            );
        }
    }

    private Categoria buscarEntidad(Long id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la categoría con id " + id
                        )
                );
    }

    private CategoriaResponseDTO convertir(
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
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        return descripcion.trim();
    }
}