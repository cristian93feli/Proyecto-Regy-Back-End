package com.regyinventory.service.contracts;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import com.regyinventory.dto.request.ActualizarProductoRequestDTO;
import com.regyinventory.dto.request.CrearProductoRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.ProductoResponseDTO;
import com.regyinventory.dto.response.ImportacionProductosResponseDTO;

/**
 * Define las operaciones del catálogo maestro de productos.
 *
 * <p>Los productos representan información comercial y descriptiva. El stock no se almacena
 * directamente en el producto, sino que se calcula a partir de los lotes ubicados en cajas y
 * zonas de empaque.</p>
 */
public interface IProductoService {

    /**
     * Registra un producto nuevo en el catálogo.
     *
     * <p>Valida la unicidad del número, SKU y código de barras. Cuando se indican marca o
     * categoría, ambas deben existir y estar activas. Esta operación no crea inventario.</p>
     *
     * @param solicitudCreacion información del producto que se desea registrar
     * @return producto registrado
     */
    ProductoResponseDTO crear(CrearProductoRequestDTO solicitudCreacion);

    /**
     * Actualiza los datos maestros de un producto existente.
     *
     * @param id identificador del producto
     * @param solicitudActualizacion nueva información del producto
     * @return producto actualizado
     */
    ProductoResponseDTO actualizar(
            Long id,
            ActualizarProductoRequestDTO solicitudActualizacion
    );

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador del producto
     * @return producto encontrado, incluyendo su stock total calculado
     */
    ProductoResponseDTO buscarPorId(Long id);

    /**
     * Consulta productos con paginación y ordenamiento.
     *
     * @param pagina número de página, iniciando en cero
     * @param tamanoPagina cantidad de registros por página
     * @param ordenarPor campo utilizado para ordenar
     * @param direccionOrdenamiento dirección ASC o DESC
     * @return página de productos
     */
    PageResponseDTO<ProductoResponseDTO> listar(
            Integer pagina,
            Integer tamanoPagina,
            String ordenarPor,
            String direccionOrdenamiento,
            String termino,
            Long marcaId,
            Long categoriaId,
            Boolean activo
    );

    /**
     * Activa o desactiva un producto sin eliminar su historial.
     *
     * @param id identificador del producto
     * @param activo estado que se desea asignar
     * @return producto con el estado actualizado
     */
    ProductoResponseDTO cambiarEstado(Long id, boolean activo);

    /**
     * Elimina definitivamente un producto cuando no tiene stock ni solicitudes pendientes.
     *
     * @param id identificador del producto
     */
    void eliminar(Long id);

    /** Busca productos activos por nombre, número o código de barras respetando el alcance del usuario. */
    List<ProductoResponseDTO> buscar(String termino);

    /** Procesa un Excel de productos, creando marcas y categorías inexistentes antes del producto. */
    ImportacionProductosResponseDTO importar(MultipartFile archivo);
}

