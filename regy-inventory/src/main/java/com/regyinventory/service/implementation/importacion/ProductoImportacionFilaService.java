package com.regyinventory.service.implementation.importacion;

import com.regyinventory.entities.Categoria;
import com.regyinventory.entities.Marca;
import com.regyinventory.entities.Producto;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.UnidadMedida;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.repository.ICategoriaRepository;
import com.regyinventory.repository.IMarcaRepository;
import com.regyinventory.repository.IProductoRepository;
import com.regyinventory.service.implementation.OperacionSupport;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.mensajes.MensajesExito;
import com.regyinventory.utils.constants.numeros.Numeros;
import java.text.Normalizer;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoImportacionFilaService {

    private final IProductoRepository productoRepository;
    private final IMarcaRepository marcaRepository;
    private final ICategoriaRepository categoriaRepository;
    private final OperacionSupport operacionSupport;

    /**
     * Importa una sola fila en una transacción independiente para que un error no revierta
     * las filas válidas procesadas anteriormente ni impida continuar con las siguientes.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultadoImportacionFila importarFila(DatosImportacionProducto datos) {
        validarCamposObligatorios(datos);
        validarDatosUnicos(datos);

        ResultadoEntidad<Marca> resultadoMarca = obtenerOCrearMarca(datos.marca());
        ResultadoEntidad<Categoria> resultadoCategoria = obtenerOCrearCategoria(datos.categoria());
        String skuFinal = resolverSku(datos, resultadoMarca.entidad(), resultadoCategoria.entidad());
        validarSkuFinalUnico(skuFinal);

        Producto producto = Producto.builder()
                .numero(normalizarNumero(datos.numero()))
                .nombre(datos.nombre().trim())
                .marca(resultadoMarca.entidad())
                .categoria(resultadoCategoria.entidad())
                .sku(normalizarTextoOpcional(skuFinal))
                .codigoBarras(normalizarTextoOpcional(datos.codigoBarras()))
                .unidadMedida(UnidadMedida.UNIDAD)
                .stockMinimo(Numeros.CERO)
                .build();

        Producto productoCreado = productoRepository.saveAndFlush(producto);
        operacionSupport.registrarLog(
                TipoAccionLog.CREAR,
                MensajesExito.Producto.NOMBRE_ENTIDAD,
                productoCreado.getId(),
                String.format(MensajesExito.Producto.LOG_CREADO, productoCreado.getNumero())
        );

        return new ResultadoImportacionFila(
                resultadoMarca.creada(),
                resultadoCategoria.creada()
        );
    }

    /** Valida los campos mínimos requeridos antes de persistir la fila. */
    private void validarCamposObligatorios(DatosImportacionProducto datos) {
        if (datos.numero() == null || datos.numero().isBlank()
                || datos.nombre() == null || datos.nombre().isBlank()) {
            throw new BusinessException(MensajesError.Producto.CAMPOS_OBLIGATORIOS_IMPORTACION);
        }
    }

    /** Valida número, SKU y código de barras antes de crear entidades asociadas. */
    private void validarDatosUnicos(DatosImportacionProducto datos) {
        if (productoRepository.existsByNumeroIgnoreCase(normalizarNumero(datos.numero()))) {
            throw new BusinessException(MensajesError.Producto.NUMERO_DUPLICADO);
        }

        String sku = normalizarTextoOpcional(datos.sku());
        if (sku != null && productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new BusinessException(MensajesError.Producto.SKU_DUPLICADO);
        }

        String codigoBarras = normalizarTextoOpcional(datos.codigoBarras());
        if (codigoBarras != null && productoRepository.existsByCodigoBarras(codigoBarras)) {
            throw new BusinessException(MensajesError.Producto.CODIGO_BARRAS_DUPLICADO);
        }
    }


    /** Valida el SKU final, incluido el generado automáticamente cuando el Excel no lo suministra. */
    private void validarSkuFinalUnico(String skuFinal) {
        String skuNormalizado = normalizarTextoOpcional(skuFinal);
        if (skuNormalizado != null && productoRepository.existsBySkuIgnoreCase(skuNormalizado)) {
            throw new BusinessException(MensajesError.Producto.SKU_DUPLICADO);
        }
    }

    /** Busca la marca de la fila o la crea dentro de la misma transacción. */
    private ResultadoEntidad<Marca> obtenerOCrearMarca(String nombreMarca) {
        if (nombreMarca == null || nombreMarca.isBlank()) {
            return new ResultadoEntidad<>(null, false);
        }

        String nombreNormalizado = nombreMarca.trim();
        return marcaRepository.findByNombreIgnoreCase(nombreNormalizado)
                .map(marca -> new ResultadoEntidad<>(marca, false))
                .orElseGet(() -> new ResultadoEntidad<>(
                        marcaRepository.save(Marca.builder().nombre(nombreNormalizado).descripcion(null).build()),
                        true
                ));
    }

    /** Busca la categoría de la fila o la crea dentro de la misma transacción. */
    private ResultadoEntidad<Categoria> obtenerOCrearCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return new ResultadoEntidad<>(null, false);
        }

        String nombreNormalizado = nombreCategoria.trim();
        return categoriaRepository.findByNombreIgnoreCase(nombreNormalizado)
                .map(categoria -> new ResultadoEntidad<>(categoria, false))
                .orElseGet(() -> new ResultadoEntidad<>(
                        categoriaRepository.save(Categoria.builder().nombre(nombreNormalizado).descripcion(null).build()),
                        true
                ));
    }

    /** Conserva el SKU enviado por el usuario o genera la sugerencia corporativa cuando viene vacío. */
    private String resolverSku(DatosImportacionProducto datos, Marca marca, Categoria categoria) {
        if (datos.sku() != null && !datos.sku().isBlank()) {
            return datos.sku().trim().toUpperCase(Locale.ROOT);
        }

        return generarSku(
                datos.numero(),
                marca == null ? datos.marca() : marca.getNombre(),
                categoria == null ? datos.categoria() : categoria.getNombre(),
                datos.codigoBarras()
        );
    }

    /** Genera el SKU sugerido con número, marca, categoría y los últimos seis dígitos del código de barras. */
    private String generarSku(String numero, String marca, String categoria, String codigoBarras) {
        String marcaParte = abreviar(marca, Numeros.CINCO);
        String categoriaParte = abreviar(categoria, Numeros.TRES);
        String barras = codigoBarras == null ? "" : codigoBarras.replaceAll("\\s+", "");
        String barrasParte = barras.length() <= Numeros.SEIS
                ? barras
                : barras.substring(barras.length() - Numeros.SEIS);

        return String.join(
                "-",
                normalizarNumero(numero),
                marcaParte,
                categoriaParte,
                barrasParte
        ).replaceAll("-+$", "");
    }

    /** Normaliza una palabra para utilizarla dentro del SKU. */
    private String abreviar(String valor, int longitud) {
        String normalizado = valor == null ? "" : Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
        return normalizado.substring(Numeros.CERO, Math.min(longitud, normalizado.length()));
    }

    /** Normaliza el número de producto para mantener el mismo formato del catálogo manual. */
    private String normalizarNumero(String numero) {
        return numero == null ? null : numero.trim().toUpperCase(Locale.ROOT);
    }

    /** Limpia valores opcionales y evita persistir cadenas vacías. */
    private String normalizarTextoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase(Locale.ROOT);
    }

    private record ResultadoEntidad<T>(T entidad, boolean creada) {
    }
}
