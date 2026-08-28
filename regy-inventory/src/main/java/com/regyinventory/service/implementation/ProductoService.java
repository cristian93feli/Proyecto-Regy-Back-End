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
import com.regyinventory.repository.IMovimientoInventarioRepository;
import com.regyinventory.repository.IIngresoStockRepository;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IMarcaRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.repository.ISolicitudReposicionRepository;
import com.regyinventory.service.contracts.IProductoService;
import com.regyinventory.service.implementation.importacion.DatosImportacionProducto;
import com.regyinventory.service.implementation.importacion.ProductoImportacionFilaService;
import com.regyinventory.service.implementation.importacion.ResultadoImportacionFila;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.text.Normalizer;
import java.util.Locale;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final ProductoImportacionFilaService productoImportacionFilaService;
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
        solicitud.setSku(generarSku(
                solicitud.getNumero(),
                marca == null ? null : marca.getNombre(),
                categoria == null ? null : categoria.getNombre(),
                solicitud.getCodigoBarras()
        ));
    }

    /**
     * Procesa el archivo sin una transacción global y delega cada fila a un servicio con REQUIRES_NEW.
     * De esta forma una fila inválida se revierte de manera aislada y la importación continúa.
     */
    @Override
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
            Map<String, Integer> columnasImportacion = obtenerColumnasImportacion(
                    hoja.getRow(Numeros.CERO),
                    formatter
            );

            for (int indiceFila = Numeros.UNO; indiceFila <= hoja.getLastRowNum(); indiceFila++) {
                Row fila = hoja.getRow(indiceFila);
                if (fila == null || filaVacia(fila, formatter)) {
                    continue;
                }

                resultado.setFilasProcesadas(resultado.getFilasProcesadas() + Numeros.UNO);
                procesarFilaImportacion(
                        fila,
                        indiceFila + Numeros.UNO,
                        formatter,
                        columnasImportacion,
                        resultado
                );
            }

            adjuntarArchivoErrores(resultado);
            return resultado;
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Procesa una fila y actualiza contadores únicamente después de confirmar su transacción. */
    private void procesarFilaImportacion(
            Row fila,
            int numeroFila,
            DataFormatter formatter,
            Map<String, Integer> columnasImportacion,
            ImportacionProductosResponseDTO resultado
    ) {
        DatosImportacionProducto datos = construirDatosImportacion(fila, formatter, columnasImportacion);

        try {
            ResultadoImportacionFila resultadoFila = productoImportacionFilaService.importarFila(datos);
            resultado.setProductosCreados(resultado.getProductosCreados() + Numeros.UNO);
            if (resultadoFila.marcaCreada()) {
                resultado.setMarcasCreadas(resultado.getMarcasCreadas() + Numeros.UNO);
            }
            if (resultadoFila.categoriaCreada()) {
                resultado.setCategoriasCreadas(resultado.getCategoriasCreadas() + Numeros.UNO);
            }
        } catch (RuntimeException excepcion) {
            resultado.getErrores().add(ErrorImportacionProductoDTO.builder()
                    .fila(numeroFila)
                    .numeroProducto(datos.numero())
                    .marca(datos.marca())
                    .categoria(datos.categoria())
                    .codigoBarras(datos.codigoBarras())
                    .nombreProducto(datos.nombre())
                    .sku(datos.sku())
                    .mensaje(obtenerCausaReal(excepcion))
                    .build());
        }
    }

    /** Convierte una fila del Excel en datos tipados usando las columnas detectadas por cabecera. */
    private DatosImportacionProducto construirDatosImportacion(
            Row fila,
            DataFormatter formatter,
            Map<String, Integer> columnasImportacion
    ) {
        return new DatosImportacionProducto(
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_NUMERO), formatter),
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_MARCA), formatter),
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_CATEGORIA), formatter),
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS), formatter),
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_NOMBRE), formatter),
                valorCelda(fila, columna(columnasImportacion, ConstantesImportacionProducto.ENCABEZADO_SKU), formatter)
        );
    }

    /** Obtiene la posición de una columna requerida a partir de su encabezado normalizado. */
    private int columna(Map<String, Integer> columnasImportacion, String encabezado) {
        return columnasImportacion.get(normalizarEncabezado(encabezado));
    }

    /** Extrae el mensaje funcional más específico disponible dentro de la cadena de excepciones. */
    private String obtenerCausaReal(RuntimeException excepcion) {
        Throwable causa = excepcion;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        if (causa.getMessage() != null && !causa.getMessage().isBlank()) {
            return causa.getMessage();
        }
        return excepcion.getMessage() == null
                ? MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO
                : excepcion.getMessage();
    }

    /** Genera un Excel descargable con las filas rechazadas y la causa exacta de cada rechazo. */
    private void adjuntarArchivoErrores(ImportacionProductosResponseDTO resultado) {
        if (resultado.getErrores() == null || resultado.getErrores().isEmpty()) {
            return;
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet hoja = workbook.createSheet(ConstantesImportacionProducto.HOJA_RECHAZADOS);
            Row cabecera = hoja.createRow(Numeros.CERO);
            String[] encabezados = {
                    ConstantesImportacionProducto.ENCABEZADO_FILA,
                    ConstantesImportacionProducto.ENCABEZADO_NUMERO,
                    ConstantesImportacionProducto.ENCABEZADO_MARCA,
                    ConstantesImportacionProducto.ENCABEZADO_CATEGORIA,
                    ConstantesImportacionProducto.ENCABEZADO_CODIGO_BARRAS,
                    ConstantesImportacionProducto.ENCABEZADO_NOMBRE,
                    ConstantesImportacionProducto.ENCABEZADO_SKU,
                    ConstantesImportacionProducto.ENCABEZADO_MOTIVO
            };
            for (int indice = Numeros.CERO; indice < encabezados.length; indice++) {
                cabecera.createCell(indice).setCellValue(encabezados[indice]);
            }

            for (int indice = Numeros.CERO; indice < resultado.getErrores().size(); indice++) {
                ErrorImportacionProductoDTO error = resultado.getErrores().get(indice);
                Row fila = hoja.createRow(indice + Numeros.UNO);
                fila.createCell(Numeros.CERO).setCellValue(error.getFila());
                fila.createCell(Numeros.UNO).setCellValue(valorSeguro(error.getNumeroProducto()));
                fila.createCell(Numeros.DOS).setCellValue(valorSeguro(error.getMarca()));
                fila.createCell(Numeros.TRES).setCellValue(valorSeguro(error.getCategoria()));
                fila.createCell(Numeros.CUATRO).setCellValue(valorSeguro(error.getCodigoBarras()));
                fila.createCell(Numeros.CINCO).setCellValue(valorSeguro(error.getNombreProducto()));
                fila.createCell(Numeros.SEIS).setCellValue(valorSeguro(error.getSku()));
                fila.createCell(Numeros.SIETE).setCellValue(valorSeguro(error.getMensaje()));
            }

            for (int indice = Numeros.CERO; indice < encabezados.length; indice++) {
                hoja.autoSizeColumn(indice);
            }

            workbook.write(outputStream);
            resultado.setArchivoErroresNombre(ConstantesImportacionProducto.ARCHIVO_ERRORES_NOMBRE);
            resultado.setArchivoErroresBase64(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        } catch (IOException excepcion) {
            throw new BusinessException(MensajesError.Producto.ARCHIVO_IMPORTACION_INVALIDO);
        }
    }

    /** Evita valores nulos al escribir el reporte Excel de errores. */
    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
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

    /**
     * Genera el SKU sugerido utilizando número de producto, marca,
     * categoría y los últimos seis caracteres del código de barras.
     */
    private String generarSku(
            String numeroProducto,
            String nombreMarca,
            String nombreCategoria,
            String codigoBarras
    ) {
        String numeroNormalizado = normalizarSegmentoSku(numeroProducto);
        String marcaNormalizada = limitarLongitud(
                normalizarSegmentoSku(nombreMarca),
                5
        );
        String categoriaNormalizada = limitarLongitud(
                normalizarSegmentoSku(nombreCategoria),
                3
        );
        String codigoBarrasNormalizado = normalizarSegmentoSku(codigoBarras);

        String ultimosDigitosCodigoBarras =
                codigoBarrasNormalizado.length() > 6
                        ? codigoBarrasNormalizado.substring(
                        codigoBarrasNormalizado.length() - 6
                )
                        : codigoBarrasNormalizado;

        return String.join(
                "-",
                numeroNormalizado,
                marcaNormalizada,
                categoriaNormalizada,
                ultimosDigitosCodigoBarras
        );
    }

    /**
     * Normaliza un valor utilizado para construir el SKU,
     * eliminando tildes, espacios y caracteres especiales.
     */
    private String normalizarSegmentoSku(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }

        String valorSinTildes = Normalizer.normalize(
                valor.trim(),
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return valorSinTildes
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Limita un segmento del SKU a la longitud máxima indicada.
     */
    private String limitarLongitud(String valor, int longitudMaxima) {
        if (valor.length() <= longitudMaxima) {
            return valor;
        }

        return valor.substring(0, longitudMaxima);
    }
}
