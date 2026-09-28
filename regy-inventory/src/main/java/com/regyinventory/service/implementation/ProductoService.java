package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarProductoRequestDTO;
import com.regyinventory.dto.request.CrearProductoRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.dto.response.ProductoResponseDTO;
import com.regyinventory.entities.Categoria;
import com.regyinventory.entities.Marca;
import com.regyinventory.entities.Producto;
import com.regyinventory.enums.EstadoSolicitud;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.UnidadMedida;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.ICategoriaRepository;
import com.regyinventory.repository.IMovimientoInventarioRepository;
import com.regyinventory.repository.IIngresoStockRepository;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IMarcaRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.ISolicitudReposicionRepository;
import com.regyinventory.service.contracts.IProductoService;
import com.regyinventory.service.implementation.importacion.ProductoImportacionArchivoService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.numeros.Numeros;
import com.regyinventory.utils.texto.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final IProductoRepository productoRepository;
    private final IMarcaRepository marcaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final IIngresoStockRepository ingresoStockRepository;
    private final IMovimientoInventarioRepository movimientoInventarioRepository;
    private final ISolicitudReposicionRepository solicitudReposicionRepository;
    private final ProductoImportacionArchivoService productoImportacionArchivoService;
    private final ProductoSkuService productoSkuService;
    private final OperacionSupport operacionSupport;

    @Override
    @Transactional
    public ProductoResponseDTO crear(CrearProductoRequestDTO solicitudCreacion) {
        completarSkuSugeridoSiEsNecesario(solicitudCreacion);
        validarDatosUnicos(solicitudCreacion, null);

        Producto nuevoProducto = construirProducto(solicitudCreacion);
        Producto productoCreado = productoRepository.save(nuevoProducto);

        operacionSupport.registrarLog(
                TipoAccionLog.CREAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                productoCreado.getId(),
                String.format(MensajesExito.Producto.LOG_CREADO, productoCreado.getNumero())
        );

        return convertirAProductoResponse(productoCreado);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(
            Long id,
            ActualizarProductoRequestDTO solicitudActualizacion
    ) {
        Producto productoExistente = obtenerProductoPorId(id);
        completarSkuSugeridoSiEsNecesario(solicitudActualizacion);
        validarDatosUnicos(solicitudActualizacion, id);
        actualizarDatosProducto(productoExistente, solicitudActualizacion);

        Producto productoActualizado = productoRepository.save(productoExistente);

        operacionSupport.registrarLog(
                TipoAccionLog.ACTUALIZAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                id,
                MensajesExito.Producto.LOG_ACTUALIZADO
        );

        return convertirAProductoResponse(productoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        return convertirAProductoResponse(obtenerProductoPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductoResponseDTO> listar(
            Integer pagina,
            Integer tamanoPagina,
            String ordenarPor,
            String direccionOrdenamiento,
            String termino,
            Long marcaId,
            Long categoriaId,
            Boolean activo
    ) {
        String terminoNormalizado = TextoUtil.normalizarBusqueda(termino);
        boolean usaVistaProductos = !terminoNormalizado.isBlank()
                || marcaId != null
                || categoriaId != null
                || activo != null
                || "numero".equalsIgnoreCase(ordenarPor);

        Page<Producto> productos;
        if (usaVistaProductos) {
            Pageable pageable = PageRequest.of(pagina, tamanoPagina);
            productos = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                    ? productoRepository.filtrarPaginadoVisibles(
                            terminoNormalizado,
                            marcaId,
                            categoriaId,
                            activo,
                            operacionSupport.obtenerZonasAsignadasIds(),
                            pageable
                    )
                    : productoRepository.filtrarPaginado(
                            terminoNormalizado,
                            marcaId,
                            categoriaId,
                            activo,
                            pageable
                    );
        } else {
            Pageable pageable = PageableUtil.create(
                    pagina,
                    tamanoPagina,
                    ordenarPor,
                    direccionOrdenamiento
            );
            productos = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                    ? productoRepository.listarVisiblesEnUbicaciones(
                            operacionSupport.obtenerZonasAsignadasIds(),
                            pageable
                    )
                    : productoRepository.findAll(pageable);
        }

        return PageResponseDTO.fromPage(productos, this::convertirAProductoResponse);
    }

    @Override
    @Transactional
    public ProductoResponseDTO cambiarEstado(Long id, boolean activo) {
        Producto productoExistente = obtenerProductoPorId(id);
        productoExistente.setActivo(activo);

        Producto productoActualizado = productoRepository.save(productoExistente);

        operacionSupport.registrarLog(
                activo ? TipoAccionLog.ACTIVAR : TipoAccionLog.DESACTIVAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                id,
                activo
                        ? MensajesExito.Producto.LOG_ACTIVADO
                        : MensajesExito.Producto.LOG_DESACTIVADO
        );

        return convertirAProductoResponse(productoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto productoExistente = obtenerProductoPorId(id);

        validarProductoSinStock(id);
        validarProductoSinSolicitudesPendientes(id);
        validarProductoSinHistorialAsociado(id);

        try {
            productoRepository.delete(productoExistente);
            productoRepository.flush();
        } catch (DataIntegrityViolationException excepcion) {
            throw new BusinessException(MensajesError.Producto.TIENE_HISTORIAL_ASOCIADO);
        }

        operacionSupport.registrarLog(
                TipoAccionLog.ELIMINAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                id,
                MensajesExito.Producto.LOG_ELIMINADO
        );
    }

    /** Impide eliminar productos con historial operativo aunque actualmente no tengan stock. */
    private void validarProductoSinHistorialAsociado(Long productoId) {
        boolean tieneHistorial = loteInventarioRepository.existsByProductoId(productoId)
                || ingresoStockRepository.existsByProductoId(productoId)
                || movimientoInventarioRepository.existsByProductoId(productoId)
                || solicitudReposicionRepository.existsByProductoId(productoId);
        if (tieneHistorial) {
            throw new BusinessException(MensajesError.Producto.TIENE_HISTORIAL_ASOCIADO);
        }
    }

    /** Busca productos activos por nombre, número o código de barras y respeta el alcance del empaquetador. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscar(String termino) {
        String terminoNormalizado = TextoUtil.normalizarBusqueda(termino);
        List<Producto> encontrados = productoRepository.buscarActivos(
                terminoNormalizado,
                PageRequest.of(Numeros.CERO, Numeros.CINCUENTA)
        );
        List<Producto> visibles = encontrados;
        if (operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            java.util.Set<Long> zonasPermitidas = operacionSupport.obtenerZonasAsignadasIds();
            visibles = encontrados.stream()
                    .filter(producto -> loteInventarioRepository.buscarUbicacionesConStock(producto.getId()).stream()
                            .anyMatch(ubicacion -> zonasPermitidas.contains(ubicacion.getId())))
                    .toList();
        }

        return ordenarResultadosBusqueda(visibles, termino)
                .stream()
                .map(this::convertirAProductoResponse)
                .toList();
    }

    /** Ordena por número cuando el criterio es numérico y por nombre para búsquedas textuales. */
    private List<Producto> ordenarResultadosBusqueda(List<Producto> productos, String termino) {
        boolean busquedaNumerica = termino != null && termino.trim().matches("\\d+");
        java.util.Comparator<Producto> comparador = busquedaNumerica
                ? java.util.Comparator.comparing(this::numeroProductoComparable)
                        .thenComparing(producto -> producto.getNumero().toLowerCase(Locale.ROOT))
                : java.util.Comparator.comparing(
                        producto -> TextoUtil.normalizarBusqueda(producto.getNombre())
                );
        return productos.stream().sorted(comparador).toList();
    }

    /** Extrae la parte numérica del número de producto para evitar ordenamientos lexicográficos como 1, 10, 2. */
    private BigInteger numeroProductoComparable(Producto producto) {
        String digitos = producto.getNumero() == null
                ? ""
                : producto.getNumero().replaceAll("[^0-9]", "");
        return digitos.isBlank() ? BigInteger.ZERO : new BigInteger(digitos);
    }

    /**
     * Completa el SKU con la regla corporativa cuando el cliente no envía uno, manteniéndolo editable si ya viene informado.
     */
    private void completarSkuSugeridoSiEsNecesario(CrearProductoRequestDTO solicitud) {
        if (solicitud.getSku() != null && !solicitud.getSku().isBlank()) {
            return;
        }
        Marca marca = obtenerMarcaActivaOpcional(solicitud.getMarcaId());
        Categoria categoria = obtenerCategoriaActivaOpcional(solicitud.getCategoriaId());
        solicitud.setSku(productoSkuService.generar(
                solicitud.getNumero(),
                marca == null ? null : marca.getNombre(),
                categoria == null ? null : categoria.getNombre(),
                solicitud.getCodigoBarras()
        ));
    }

    /** Procesa el Excel completo sin una transacción global y mantiene una transacción aislada por fila. */
    @Override
    public ImportacionProductosResponseDTO importar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_VACIO);
        }
        try {
            byte[] contenido = archivo.getBytes();
            productoImportacionArchivoService.validar(contenido);
            return productoImportacionArchivoService.procesar(contenido, resultado -> { });
        } catch (java.io.IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    private Producto construirProducto(CrearProductoRequestDTO solicitudCreacion) {
        return Producto.builder()
                .numero(normalizarNumero(solicitudCreacion.getNumero()))
                .nombre(normalizarTextoObligatorio(solicitudCreacion.getNombre()))
                .marca(obtenerMarcaActivaOpcional(solicitudCreacion.getMarcaId()))
                .categoria(obtenerCategoriaActivaOpcional(solicitudCreacion.getCategoriaId()))
                .sku(normalizarTextoOpcional(solicitudCreacion.getSku()))
                .codigoBarras(normalizarTextoOpcional(solicitudCreacion.getCodigoBarras()))
                .imagenUrl(normalizarTextoOpcional(solicitudCreacion.getImagenUrl()))
                .precioCompra(solicitudCreacion.getPrecioCompra())
                .precioVenta(solicitudCreacion.getPrecioVenta())
                .unidadMedida(solicitudCreacion.getUnidadMedida())
                .stockMinimo(solicitudCreacion.getStockMinimo())
                .build();
    }

    private void actualizarDatosProducto(
            Producto productoExistente,
            CrearProductoRequestDTO solicitudActualizacion
    ) {
        productoExistente.setNumero(normalizarNumero(solicitudActualizacion.getNumero()));
        productoExistente.setNombre(normalizarTextoObligatorio(solicitudActualizacion.getNombre()));
        productoExistente.setMarca(obtenerMarcaActivaOpcional(solicitudActualizacion.getMarcaId()));
        productoExistente.setCategoria(
                obtenerCategoriaActivaOpcional(solicitudActualizacion.getCategoriaId())
        );
        productoExistente.setSku(normalizarTextoOpcional(solicitudActualizacion.getSku()));
        productoExistente.setCodigoBarras(
                normalizarTextoOpcional(solicitudActualizacion.getCodigoBarras())
        );
        productoExistente.setImagenUrl(
                normalizarTextoOpcional(solicitudActualizacion.getImagenUrl())
        );
        productoExistente.setPrecioCompra(solicitudActualizacion.getPrecioCompra());
        productoExistente.setPrecioVenta(solicitudActualizacion.getPrecioVenta());
        productoExistente.setUnidadMedida(solicitudActualizacion.getUnidadMedida());
        productoExistente.setStockMinimo(solicitudActualizacion.getStockMinimo());
    }

    private void validarDatosUnicos(
            CrearProductoRequestDTO solicitud,
            Long productoIdExcluido
    ) {
        String numeroNormalizado = normalizarNumero(solicitud.getNumero());
        String skuNormalizado = normalizarTextoOpcional(solicitud.getSku());
        String codigoBarrasNormalizado = normalizarTextoOpcional(solicitud.getCodigoBarras());

        boolean numeroDuplicado = productoIdExcluido == null
                ? productoRepository.existsByNumeroIgnoreCase(numeroNormalizado)
                : productoRepository.existsByNumeroIgnoreCaseAndIdNot(
                        numeroNormalizado,
                        productoIdExcluido
                );

        if (numeroDuplicado) {
            throw new BusinessException(MensajesError.Producto.NUMERO_DUPLICADO);
        }

        if (skuNormalizado != null) {
            boolean skuDuplicado = productoIdExcluido == null
                    ? productoRepository.existsBySkuIgnoreCase(skuNormalizado)
                    : productoRepository.existsBySkuIgnoreCaseAndIdNot(
                            skuNormalizado,
                            productoIdExcluido
                    );

            if (skuDuplicado) {
                throw new BusinessException(MensajesError.Producto.SKU_DUPLICADO);
            }
        }

        if (codigoBarrasNormalizado != null) {
            boolean codigoBarrasDuplicado = productoIdExcluido == null
                    ? productoRepository.existsByCodigoBarras(codigoBarrasNormalizado)
                    : productoRepository.existsByCodigoBarrasAndIdNot(
                            codigoBarrasNormalizado,
                            productoIdExcluido
                    );

            if (codigoBarrasDuplicado) {
                throw new BusinessException(MensajesError.Producto.CODIGO_BARRAS_DUPLICADO);
            }
        }
    }

    private void validarProductoSinStock(Long productoId) {
        if (loteInventarioRepository.existsByProductoIdAndCantidadGreaterThan(productoId, 0)) {
            throw new BusinessException(MensajesError.Producto.TIENE_STOCK);
        }
    }

    private void validarProductoSinSolicitudesPendientes(Long productoId) {
        if (solicitudReposicionRepository.existsByProductoIdAndEstado(
                productoId,
                EstadoSolicitud.PENDIENTE
        )) {
            throw new BusinessException(MensajesError.Producto.TIENE_SOLICITUDES_PENDIENTES);
        }
    }

    private Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                String.format(MensajesError.Producto.NO_EXISTE, id)
                        )
                );
    }

    private Marca obtenerMarcaActivaOpcional(Long marcaId) {
        if (marcaId == null) {
            return null;
        }

        Marca marcaEncontrada = marcaRepository.findById(marcaId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                String.format(MensajesError.Marca.NO_EXISTE, marcaId)
                        )
                );

        if (!marcaEncontrada.getActivo()) {
            throw new BusinessException(MensajesError.Producto.MARCA_INACTIVA);
        }

        return marcaEncontrada;
    }

    private Categoria obtenerCategoriaActivaOpcional(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }

        Categoria categoriaEncontrada = categoriaRepository.findById(categoriaId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                String.format(MensajesError.Categoria.NO_EXISTE, categoriaId)
                        )
                );

        if (!categoriaEncontrada.getActivo()) {
            throw new BusinessException(MensajesError.Producto.CATEGORIA_INACTIVA);
        }

        return categoriaEncontrada;
    }

    private ProductoResponseDTO convertirAProductoResponse(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .numero(producto.getNumero())
                .nombre(producto.getNombre())
                .marcaId(producto.getMarca() == null ? null : producto.getMarca().getId())
                .marcaNombre(producto.getMarca() == null ? null : producto.getMarca().getNombre())
                .categoriaId(
                        producto.getCategoria() == null ? null : producto.getCategoria().getId()
                )
                .categoriaNombre(
                        producto.getCategoria() == null ? null : producto.getCategoria().getNombre()
                )
                .sku(producto.getSku())
                .codigoBarras(producto.getCodigoBarras())
                .imagenUrl(producto.getImagenUrl())
                .precioCompra(producto.getPrecioCompra())
                .precioVenta(producto.getPrecioVenta())
                .unidadMedida(producto.getUnidadMedida())
                .stockMinimo(producto.getStockMinimo())
                .stockTotal(calcularStockVisible(producto.getId()))
                .activo(producto.getActivo())
                .build();
    }

    /** Calcula el stock total visible para el usuario autenticado. */
    private Integer calcularStockVisible(Long productoId) {
        if (!operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return loteInventarioRepository.stockTotalProducto(productoId);
        }
        return operacionSupport.obtenerZonasAsignadasIds().stream()
                .mapToInt(ubicacionId -> loteInventarioRepository.stockUbicacion(productoId, ubicacionId))
                .sum();
    }

    private String normalizarNumero(String numero) {
        return numero.trim().toUpperCase();
    }

    private String normalizarTextoObligatorio(String texto) {
        return texto.trim();
    }

    private String normalizarTextoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
