package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;
import com.regyinventory.entities.*;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.exceptions.*;
import com.regyinventory.repository.*;
import com.regyinventory.service.contracts.IProductoService;
import com.regyinventory.utils.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService implements IProductoService {
    private final IProductoRepository repo;
    private final IMarcaRepository marcaRepo;
    private final ICategoriaRepository categoriaRepo;
    private final ILoteInventarioRepository loteRepo;
    private final ISolicitudReposicionRepository solicitudRepo;
    private final OperacionSupport op;

    @Transactional
    public ProductoResponseDTO crear(CrearProductoRequestDTO r) {
        validar(r, null);
        Producto p = Producto.builder().numero(n(r.getNumero())).nombre(t(r.getNombre())).marca(marca(r.getMarcaId())).categoria(categoria(r.getCategoriaId())).sku(nulo(r.getSku())).codigoBarras(nulo(r.getCodigoBarras())).imagenUrl(nulo(r.getImagenUrl())).precioCompra(r.getPrecioCompra()).precioVenta(r.getPrecioVenta()).unidadMedida(r.getUnidadMedida()).stockMinimo(r.getStockMinimo()).build();
        p = repo.save(p);
        op.log(TipoAccionLog.CREAR, "Producto", p.getId(), "Producto creado: " + p.getNumero());
        return dto(p);
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ActualizarProductoRequestDTO r) {
        Producto p = get(id);
        validar(r, id);
        p.setNumero(n(r.getNumero()));
        p.setNombre(t(r.getNombre()));
        p.setMarca(marca(r.getMarcaId()));
        p.setCategoria(categoria(r.getCategoriaId()));
        p.setSku(nulo(r.getSku()));
        p.setCodigoBarras(nulo(r.getCodigoBarras()));
        p.setImagenUrl(nulo(r.getImagenUrl()));
        p.setPrecioCompra(r.getPrecioCompra());
        p.setPrecioVenta(r.getPrecioVenta());
        p.setUnidadMedida(r.getUnidadMedida());
        p.setStockMinimo(r.getStockMinimo());
        op.log(TipoAccionLog.ACTUALIZAR, "Producto", id, "Producto actualizado" );
        return dto(repo.save(p));
    }

    public ProductoResponseDTO buscar(Long id) {
        return dto(get(id));
    }

    public PageResponseDTO<ProductoResponseDTO> listar(Integer p, Integer s, String o, String d) {
        return PageResponseDTO.fromPage(repo.findAll(PageableUtil.create(p, s, o, d)), this::dto);
    }

    @Transactional
    public ProductoResponseDTO estado(Long id, boolean a) {
        Producto p = get(id);
        p.setActivo(a);
        op.log(a ? TipoAccionLog.ACTIVAR : TipoAccionLog.DESACTIVAR, "Producto", id, "Estado cambiado" );
        return dto(repo.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = get(id);
        if (loteRepo.existsByProductoIdAndCantidadGreaterThan(id, 0))
            throw new BusinessException("No se puede eliminar un producto con stock" );
        if (solicitudRepo.existsByProductoIdAndEstado(id, com.regyinventory.enums.EstadoSolicitud.PENDIENTE))
            throw new BusinessException("No se puede eliminar un producto con solicitudes pendientes" );
        repo.delete(p);
        op.log(TipoAccionLog.ELIMINAR, "Producto", id, "Producto eliminado" );
    }

    private void validar(CrearProductoRequestDTO r, Long id) {
        String num = n(r.getNumero());
        if (id == null ? repo.existsByNumeroIgnoreCase(num) : repo.existsByNumeroIgnoreCaseAndIdNot(num, id))
            throw new BusinessException("Ya existe un producto con ese número" );
        if (r.getSku() != null && !r.getSku().isBlank() && (id == null ? repo.existsBySkuIgnoreCase(r.getSku().trim()) : repo.existsBySkuIgnoreCaseAndIdNot(r.getSku().trim(), id)))
            throw new BusinessException("Ya existe un producto con ese SKU" );
        if (r.getCodigoBarras() != null && !r.getCodigoBarras().isBlank() && (id == null ? repo.existsByCodigoBarras(r.getCodigoBarras().trim()) : repo.existsByCodigoBarrasAndIdNot(r.getCodigoBarras().trim(), id)))
            throw new BusinessException("Ya existe un producto con ese código de barras" );
    }

    private Producto get(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
    }

    private Marca marca(Long id) {
        if (id == null) return null;
        Marca x = marcaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada" ));
        if (!x.getActivo()) throw new BusinessException("La marca está inactiva" );
        return x;
    }

    private Categoria categoria(Long id) {
        if (id == null) return null;
        Categoria x = categoriaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada" ));
        if (!x.getActivo()) throw new BusinessException("La categoría está inactiva" );
        return x;
    }

    private ProductoResponseDTO dto(Producto x) {
        return ProductoResponseDTO.builder().id(x.getId()).numero(x.getNumero()).nombre(x.getNombre()).marcaId(x.getMarca() == null ? null : x.getMarca().getId()).marcaNombre(x.getMarca() == null ? null : x.getMarca().getNombre()).categoriaId(x.getCategoria() == null ? null : x.getCategoria().getId()).categoriaNombre(x.getCategoria() == null ? null : x.getCategoria().getNombre()).sku(x.getSku()).codigoBarras(x.getCodigoBarras()).imagenUrl(x.getImagenUrl()).precioCompra(x.getPrecioCompra()).precioVenta(x.getPrecioVenta()).unidadMedida(x.getUnidadMedida()).stockMinimo(x.getStockMinimo()).stockTotal(loteRepo.stockTotalProducto(x.getId())).activo(x.getActivo()).build();
    }

    private String n(String s) {
        return s.trim().toUpperCase();
    }

    private String t(String s) {
        return s.trim();
    }

    private String nulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
