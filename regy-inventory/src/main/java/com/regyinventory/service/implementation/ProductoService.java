package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarProductoRequestDTO;
import com.regyinventory.dto.request.CrearProductoRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;
import com.regyinventory.dto.response.ErrorImportacionProductoDTO;
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
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IMarcaRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.ISolicitudReposicionRepository;
import com.regyinventory.service.contracts.IProductoService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.numeros.Numeros;
import com.regyinventory.utils.texto.TextoUtil;
import com.regyinventory.utils.constants.importacion.ConstantesImportacionProducto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService implements IProductoService {

    private final IProductoRepository productoRepository;
    private final IMarcaRepository marcaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final ISolicitudReposicionRepository solicitudReposicionRepository;
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
    public ProductoResponseDTO buscarPorId(Long id) {
        return convertirAProductoResponse(obtenerProductoPorId(id));
    }

    @Override
    public PageResponseDTO<ProductoResponseDTO> listar(
            Integer pagina,
            Integer tamanoPagina,
            String ordenarPor,
            String direccionOrdenamiento
    ) {
        Pageable pageable = PageableUtil.create(
                pagina,
                tamanoPagina,
                ordenarPor,
                direccionOrdenamiento
        );
        Page<Producto> productos = operacionSupport.usuarioAutenticadoEsEmpaquetador()
                ? productoRepository.listarVisiblesEnUbicaciones(
                        operacionSupport.obtenerZonasAsignadasIds(),
                        pageable
                )
                : productoRepository.findAll(pageable);

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

        productoRepository.delete(productoExistente);

        operacionSupport.registrarLog(
                TipoAccionLog.ELIMINAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                id,
                MensajesExito.Producto.LOG_ELIMINADO
        );
    }

    /** Busca productos activos por nombre, número o código de barras y respeta el alcance del empaquetador. */
    @Override
    public List<ProductoResponseDTO> buscar(String termino) {
        String terminoNormalizado = TextoUtil.normalizarBusqueda(termino);
        List<Producto> encontrados = productoRepository.buscarActivos(
                terminoNormalizado,
                PageRequest.of(Numeros.CERO, Numeros.CINCUENTA)
        );
        if (!operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return encontrados.stream().map(this::convertirAProductoResponse).toList();
        }
        java.util.Set<Long> zonasPermitidas = operacionSupport.obtenerZonasAsignadasIds();
        return encontrados.stream()
                .filter(producto -> loteInventarioRepository.buscarUbicacionesConStock(producto.getId()).stream()
                        .anyMatch(ubicacion -> zonasPermitidas.contains(ubicacion.getId())))
                .map(this::convertirAProductoResponse)
                .toList();
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
        solicitud.setSku(generarSku(
                solicitud.getNumero(),
                marca == null ? null : marca.getNombre(),
                categoria == null ? null : categoria.getNombre(),
                solicitud.getCodigoBarras()
        ));
    }

    /** Importa productos desde Excel y crea primero las marcas y categorías que aún no existan. */
    @Override
    @Transactional
    public ImportacionProductosResponseDTO importar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_VACIO);
        }

        ImportacionProductosResponseDTO resultado = ImportacionProductosResponseDTO.builder()
                .filasProcesadas(Numeros.CERO)
                .productosCreados(Numeros.CERO)
                .marcasCreadas(Numeros.CERO)
                .categoriasCreadas(Numeros.CERO)
                .errores(new ArrayList<>())
                .build();

        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        try (InputStream inputStream = archivo.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet hoja = workbook.getSheetAt(Numeros.CERO);
            Map<String, Integer> columnasImportacion = obtenerColumnasImportacion(hoja.getRow(Numeros.CERO), formatter);
            for (int indiceFila = Numeros.UNO; indiceFila <= hoja.getLastRowNum(); indiceFila++) {
                Row fila = hoja.getRow(indiceFila);
                if (fila == null || filaVacia(fila, formatter)) {
                    continue;
                }
                resultado.setFilasProcesadas(resultado.getFilasProcesadas() + Numeros.UNO);
                procesarFilaImportacion(fila, indiceFila + Numeros.UNO, formatter, resultado, columnasImportacion);
            }
            return resultado;
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Procesa una fila del Excel usando las posiciones detectadas desde la cabecera normalizada. */
    private void procesarFilaImportacion(
            Row fila,
            int numeroFila,
            DataFormatter formatter,
            ImportacionProductosResponseDTO resultado,
            Map<String, Integer> columnasImportacion
    ) {
        String numero = valorCelda(
                fila,
                columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_NUMERO)),
                formatter
        );
        try {
            String marcaNombre = valorCelda(fila, columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_MARCA)), formatter);
            String categoriaNombre = valorCelda(fila, columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_CATEGORIA)), formatter);
            String codigoBarras = valorCelda(fila, columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS)), formatter);
            String nombre = valorCelda(fila, columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_NOMBRE)), formatter);
            String sku = valorCelda(fila, columnasImportacion.get(normalizarEncabezado(ConstantesImportacionProducto.ENCABEZADO_SKU)), formatter);

            if (numero.isBlank() || nombre.isBlank()) {
                throw new BusinessException(MensajesError.Producto.CAMPOS_OBLIGATORIOS_IMPORTACION);
            }
            if (productoRepository.existsByNumeroIgnoreCase(normalizarNumero(numero))) {
                throw new BusinessException(MensajesError.Producto.NUMERO_DUPLICADO);
            }

            Marca marca = obtenerOCrearMarca(marcaNombre, resultado);
            Categoria categoria = obtenerOCrearCategoria(categoriaNombre, resultado);
            String skuFinal = sku.isBlank()
                    ? generarSku(numero, marcaNombre, categoriaNombre, codigoBarras)
                    : sku.trim().toUpperCase();

            CrearProductoRequestDTO solicitud = new CrearProductoRequestDTO();
            solicitud.setNumero(numero);
            solicitud.setNombre(nombre);
            solicitud.setMarcaId(marca == null ? null : marca.getId());
            solicitud.setCategoriaId(categoria == null ? null : categoria.getId());
            solicitud.setCodigoBarras(codigoBarras.isBlank() ? null : codigoBarras);
            solicitud.setSku(skuFinal);
            solicitud.setUnidadMedida(UnidadMedida.UNIDAD);
            solicitud.setStockMinimo(Numeros.CERO);
            crear(solicitud);
            resultado.setProductosCreados(resultado.getProductosCreados() + Numeros.UNO);
        } catch (RuntimeException excepcion) {
            resultado.getErrores().add(ErrorImportacionProductoDTO.builder()
                    .fila(numeroFila)
                    .numeroProducto(numero)
                    .mensaje(excepcion.getMessage())
                    .build());
        }
    }

    /** Crea una marca del Excel únicamente cuando todavía no existe. */
    private Marca obtenerOCrearMarca(String nombreMarca, ImportacionProductosResponseDTO resultado) {
        if (nombreMarca == null || nombreMarca.isBlank()) {
            return null;
        }
        return marcaRepository.findByNombreIgnoreCase(nombreMarca.trim()).orElseGet(() -> {
            Marca marca = Marca.builder().nombre(nombreMarca.trim()).descripcion(null).build();
            resultado.setMarcasCreadas(resultado.getMarcasCreadas() + Numeros.UNO);
            return marcaRepository.save(marca);
        });
    }

    /** Crea una categoría del Excel únicamente cuando todavía no existe. */
    private Categoria obtenerOCrearCategoria(String nombreCategoria, ImportacionProductosResponseDTO resultado) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return null;
        }
        return categoriaRepository.findByNombreIgnoreCase(nombreCategoria.trim()).orElseGet(() -> {
            Categoria categoria = Categoria.builder().nombre(nombreCategoria.trim()).descripcion(null).build();
            resultado.setCategoriasCreadas(resultado.getCategoriasCreadas() + Numeros.UNO);
            return categoriaRepository.save(categoria);
        });
    }

    /** Genera el SKU sugerido: número-marca(5)-categoría(3)-últimos6 del código de barras. */
    private String generarSku(String numero, String marca, String categoria, String codigoBarras) {
        String marcaParte = abreviar(marca, Numeros.CINCO);
        String categoriaParte = abreviar(categoria, Numeros.TRES);
        String barras = codigoBarras == null ? "" : codigoBarras.replaceAll("\\s+", "");
        String barrasParte = barras.length() <= Numeros.SEIS ? barras : barras.substring(barras.length() - Numeros.SEIS);
        return String.join(
                "-",
                normalizarNumero(numero),
                marcaParte,
                categoriaParte,
                barrasParte
        ).replaceAll("-+$", "");
    }

    /** Normaliza una palabra y toma como máximo la cantidad de caracteres requerida para el SKU. */
    private String abreviar(String valor, int longitud) {
        String normalizado = valor == null ? "" : java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        return normalizado.substring(Numeros.CERO, Math.min(longitud, normalizado.length()));
    }

    /**
     * Detecta las columnas requeridas sin depender de tildes, mayúsculas, espacios extra ni del orden del Excel.
     */
    private Map<String, Integer> obtenerColumnasImportacion(Row cabecera, DataFormatter formatter) {
        if (cabecera == null) {
            throw new BusinessException(MensajesError.Producto.COLUMNAS_IMPORTACION_INVALIDAS);
        }

        Map<String, Integer> columnas = new HashMap<>();
        for (int indice = Numeros.CERO; indice < cabecera.getLastCellNum(); indice++) {
            String encabezado = normalizarEncabezado(valorCelda(cabecera, indice, formatter));
            if (!encabezado.isBlank()) {
                columnas.put(encabezado, indice);
            }
        }

        for (String encabezadoRequerido : ConstantesImportacionProducto.ENCABEZADOS) {
            if (!columnas.containsKey(normalizarEncabezado(encabezadoRequerido))) {
                throw new BusinessException(MensajesError.Producto.COLUMNAS_IMPORTACION_INVALIDAS);
            }
        }
        return columnas;
    }

    /** Normaliza una cabecera para tolerar diferencias de tildes, espacios y capitalización. */
    private String normalizarEncabezado(String encabezado) {
        if (encabezado == null) {
            return "";
        }
        return java.text.Normalizer.normalize(encabezado.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    /** Indica si una fila no contiene información útil, incluso si las columnas fueron reordenadas. */
    private boolean filaVacia(Row fila, DataFormatter formatter) {
        for (int columna = Numeros.CERO; columna < fila.getLastCellNum(); columna++) {
            if (!valorCelda(fila, columna, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    /** Obtiene el texto visible de una celda sin depender de su tipo interno de Excel. */
    private String valorCelda(Row fila, int columna, DataFormatter formatter) {
        Cell celda = fila.getCell(columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return celda == null ? "" : formatter.formatCellValue(celda).trim();
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
