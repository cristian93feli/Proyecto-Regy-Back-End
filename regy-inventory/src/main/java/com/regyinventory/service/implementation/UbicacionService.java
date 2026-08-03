package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarUbicacionRequestDTO;
import com.regyinventory.dto.request.CrearUbicacionRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.entities.Usuario;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.service.contracts.IUbicacionService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UbicacionService implements IUbicacionService {

    private final IUbicacionRepository ubicacionRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ILoteInventarioRepository loteInventarioRepository;
    private final OperacionSupport operacionSupport;

    @Override
    @Transactional
    public UbicacionResponseDTO crear(CrearUbicacionRequestDTO solicitudCreacion) {
        String codigoNormalizado = normalizarCodigo(solicitudCreacion.getCodigo());
        validarCodigoDisponible(codigoNormalizado, null);

        Ubicacion ubicacionPadre = resolverYValidarPadre(solicitudCreacion.getTipo(), solicitudCreacion.getUbicacionPadreId());
        Usuario usuarioAsignado = resolverUsuarioAsignado(solicitudCreacion.getTipo(), solicitudCreacion.getUsuarioAsignadoId());

        Ubicacion nuevaUbicacion = Ubicacion.builder()
                .codigo(codigoNormalizado)
                .nombre(solicitudCreacion.getNombre().trim())
                .descripcion(limpiarTextoOpcional(solicitudCreacion.getDescripcion()))
                .tipo(solicitudCreacion.getTipo())
                .ubicacionPadre(ubicacionPadre)
                .usuarioAsignado(usuarioAsignado)
                .build();

        Ubicacion ubicacionCreada = ubicacionRepository.save(nuevaUbicacion);
        operacionSupport.registrarLog(
                TipoAccionLog.CREAR,
                ConstantesLog.Entidad.UBICACION,
                ubicacionCreada.getId(),
                ConstantesLog.Detalle.UBICACION_CREADA
        );
        return convertirRespuesta(ubicacionCreada);
    }

    @Override
    @Transactional
    public UbicacionResponseDTO actualizar(Long ubicacionId, ActualizarUbicacionRequestDTO solicitudActualizacion) {
        Ubicacion ubicacionExistente = obtenerUbicacion(ubicacionId);
        String codigoNormalizado = normalizarCodigo(solicitudActualizacion.getCodigo());
        validarCodigoDisponible(codigoNormalizado, ubicacionId);

        ubicacionExistente.setCodigo(codigoNormalizado);
        ubicacionExistente.setNombre(solicitudActualizacion.getNombre().trim());
        ubicacionExistente.setDescripcion(limpiarTextoOpcional(solicitudActualizacion.getDescripcion()));
        ubicacionExistente.setUsuarioAsignado(
                resolverUsuarioAsignado(ubicacionExistente.getTipo(), solicitudActualizacion.getUsuarioAsignadoId())
        );

        return convertirRespuesta(ubicacionRepository.save(ubicacionExistente));
    }

    @Override
    public UbicacionResponseDTO buscar(Long ubicacionId) {
        return convertirRespuesta(obtenerUbicacion(ubicacionId));
    }

    @Override
    public PageResponseDTO<UbicacionResponseDTO> listar(
            Integer pagina,
            Integer tamano,
            String ordenarPor,
            String direccion
    ) {
        return PageResponseDTO.fromPage(
                ubicacionRepository.findAll(PageableUtil.create(pagina, tamano, ordenarPor, direccion)),
                this::convertirRespuesta
        );
    }

    @Override
    public List<UbicacionResponseDTO> listarPorTipo(TipoUbicacion tipoUbicacion) {
        return ubicacionRepository.findByTipo(tipoUbicacion).stream().map(this::convertirRespuesta).toList();
    }

    @Override
    public List<UbicacionResponseDTO> listarHijas(Long ubicacionPadreId) {
        obtenerUbicacion(ubicacionPadreId);
        return ubicacionRepository.findByUbicacionPadreId(ubicacionPadreId).stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public UbicacionResponseDTO cambiarEstado(Long ubicacionId, boolean nuevoEstadoActivo) {
        Ubicacion ubicacion = obtenerUbicacion(ubicacionId);
        if (!nuevoEstadoActivo && ubicacionRepository.existsByUbicacionPadreId(ubicacionId)) {
            throw new BusinessException(MensajesError.Ubicacion.CON_HIJAS_NO_DESACTIVABLE);
        }
        if (!nuevoEstadoActivo && loteInventarioRepository.existsByUbicacionIdAndCantidadGreaterThan(ubicacionId, 0)) {
            throw new BusinessException(MensajesError.Ubicacion.CON_STOCK_NO_DESACTIVABLE);
        }
        ubicacion.setActivo(nuevoEstadoActivo);
        return convertirRespuesta(ubicacionRepository.save(ubicacion));
    }

    @Override
    @Transactional
    public void eliminar(Long ubicacionId) {
        Ubicacion ubicacion = obtenerUbicacion(ubicacionId);
        if (ubicacionRepository.existsByUbicacionPadreId(ubicacionId)) {
            throw new BusinessException(MensajesError.Ubicacion.CON_HIJAS_NO_ELIMINABLE);
        }
        if (loteInventarioRepository.existsByUbicacionIdAndCantidadGreaterThan(ubicacionId, 0)) {
            throw new BusinessException(MensajesError.Ubicacion.CON_STOCK_NO_ELIMINABLE);
        }
        ubicacionRepository.delete(ubicacion);
    }

    private Ubicacion resolverYValidarPadre(TipoUbicacion tipoUbicacion, Long ubicacionPadreId) {
        if (tipoUbicacion == TipoUbicacion.DEPOSITO || tipoUbicacion == TipoUbicacion.ZONA_EMPAQUE) {
            if (ubicacionPadreId != null) {
                throw new BusinessException(MensajesError.Ubicacion.RAIZ_CON_PADRE);
            }
            return null;
        }
        if (ubicacionPadreId == null) {
            throw new BusinessException(MensajesError.Ubicacion.HIJA_SIN_PADRE);
        }
        Ubicacion ubicacionPadre = obtenerUbicacionActiva(ubicacionPadreId);
        if (tipoUbicacion == TipoUbicacion.ESTANTE && ubicacionPadre.getTipo() != TipoUbicacion.DEPOSITO) {
            throw new BusinessException(MensajesError.Ubicacion.ESTANTE_PADRE_INVALIDO);
        }
        if (tipoUbicacion == TipoUbicacion.CAJA && ubicacionPadre.getTipo() != TipoUbicacion.ESTANTE) {
            throw new BusinessException(MensajesError.Ubicacion.CAJA_PADRE_INVALIDO);
        }
        return ubicacionPadre;
    }

    private Usuario resolverUsuarioAsignado(TipoUbicacion tipoUbicacion, Long usuarioAsignadoId) {
        if (tipoUbicacion != TipoUbicacion.ZONA_EMPAQUE) {
            if (usuarioAsignadoId != null) {
                throw new BusinessException(MensajesError.Ubicacion.USUARIO_SOLO_ZONA);
            }
            return null;
        }
        if (usuarioAsignadoId == null) {
            return null;
        }
        Usuario usuarioAsignado = usuarioRepository.findById(usuarioAsignadoId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Ubicacion.USUARIO_ASIGNADO_NO_ENCONTRADO));
        if (!usuarioAsignado.getActivo()) {
            throw new BusinessException(MensajesError.Ubicacion.USUARIO_ASIGNADO_INACTIVO);
        }
        return usuarioAsignado;
    }

    private void validarCodigoDisponible(String codigo, Long ubicacionIdExcluida) {
        boolean codigoExiste = ubicacionIdExcluida == null
                ? ubicacionRepository.existsByCodigoIgnoreCase(codigo)
                : ubicacionRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, ubicacionIdExcluida);
        if (codigoExiste) {
            throw new BusinessException(MensajesError.Ubicacion.CODIGO_DUPLICADO);
        }
    }

    private Ubicacion obtenerUbicacion(Long ubicacionId) {
        return ubicacionRepository.findById(ubicacionId)
                .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Ubicacion.NO_ENCONTRADA));
    }

    private Ubicacion obtenerUbicacionActiva(Long ubicacionId) {
        Ubicacion ubicacion = obtenerUbicacion(ubicacionId);
        if (!ubicacion.getActivo()) {
            throw new BusinessException(MensajesError.Ubicacion.INACTIVA);
        }
        return ubicacion;
    }

    private UbicacionResponseDTO convertirRespuesta(Ubicacion ubicacion) {
        Ubicacion deposito = obtenerDepositoRaiz(ubicacion);
        return UbicacionResponseDTO.builder()
                .id(ubicacion.getId())
                .codigo(ubicacion.getCodigo())
                .nombre(ubicacion.getNombre())
                .descripcion(ubicacion.getDescripcion())
                .tipo(ubicacion.getTipo())
                .ubicacionPadreId(ubicacion.getUbicacionPadre() == null ? null : ubicacion.getUbicacionPadre().getId())
                .ubicacionPadreNombre(ubicacion.getUbicacionPadre() == null ? null : ubicacion.getUbicacionPadre().getNombre())
                .depositoId(deposito == null ? null : deposito.getId())
                .depositoNombre(deposito == null ? null : deposito.getNombre())
                .usuarioAsignadoId(ubicacion.getUsuarioAsignado() == null ? null : ubicacion.getUsuarioAsignado().getId())
                .usuarioAsignadoNombre(ubicacion.getUsuarioAsignado() == null ? null : ubicacion.getUsuarioAsignado().getUsername())
                .admiteInventario(admiteInventario(ubicacion))
                .activo(ubicacion.getActivo())
                .build();
    }

    private Ubicacion obtenerDepositoRaiz(Ubicacion ubicacion) {
        Ubicacion ubicacionActual = ubicacion;
        while (ubicacionActual != null && ubicacionActual.getTipo() != TipoUbicacion.DEPOSITO) {
            ubicacionActual = ubicacionActual.getUbicacionPadre();
        }
        return ubicacionActual;
    }

    private boolean admiteInventario(Ubicacion ubicacion) {
        return ubicacion.getTipo() == TipoUbicacion.CAJA || ubicacion.getTipo() == TipoUbicacion.ZONA_EMPAQUE;
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private String limpiarTextoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
