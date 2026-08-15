package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.ActualizarUbicacionRequestDTO;
import com.regyinventory.dto.request.CrearUbicacionRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UbicacionResponseDTO;
import com.regyinventory.entities.Ubicacion;
import com.regyinventory.entities.Usuario;
import com.regyinventory.enums.TipoAccionLog;
import com.regyinventory.enums.TipoUbicacion;
import com.regyinventory.enums.NombreRol;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.regyinventory.exceptions.BusinessException;
import com.regyinventory.exceptions.ResourceNotFoundException;
import com.regyinventory.repository.ILoteInventarioRepository;
import com.regyinventory.repository.IUbicacionRepository;
import com.regyinventory.repository.IUsuarioRepository;
import com.regyinventory.service.contracts.IUbicacionService;
import com.regyinventory.utils.PageableUtil;
import com.regyinventory.utils.constants.log.ConstantesLog;
import com.regyinventory.utils.constants.mensajes.MensajesError;
import com.regyinventory.utils.constants.numeros.Numeros;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        Set<Usuario> usuariosResponsables = resolverUsuariosResponsables(solicitudCreacion.getTipo(), solicitudCreacion.getUsuariosResponsablesIds());

        Ubicacion nuevaUbicacion = Ubicacion.builder()
                .codigo(codigoNormalizado)
                .nombre(solicitudCreacion.getNombre().trim())
                .descripcion(limpiarTextoOpcional(solicitudCreacion.getDescripcion()))
                .tipo(solicitudCreacion.getTipo())
                .ubicacionPadre(ubicacionPadre)
                .usuariosResponsables(usuariosResponsables)
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
        ubicacionExistente.setUsuariosResponsables(
                resolverUsuariosResponsables(ubicacionExistente.getTipo(), solicitudActualizacion.getUsuariosResponsablesIds())
        );

        return convertirRespuesta(ubicacionRepository.save(ubicacionExistente));
    }

    @Override
    public UbicacionResponseDTO buscar(Long ubicacionId) {
        Ubicacion ubicacion = obtenerUbicacion(ubicacionId);
        validarLecturaEmpaquetador(ubicacion);
        return convertirRespuesta(ubicacion);
    }

    @Override
    public PageResponseDTO<UbicacionResponseDTO> listar(
            Integer pagina,
            Integer tamano,
            String ordenarPor,
            String direccion
    ) {
        Pageable pageable = PageableUtil.create(pagina, tamano, ordenarPor, direccion);
        if (!operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return PageResponseDTO.fromPage(ubicacionRepository.findAll(pageable), this::convertirRespuesta);
        }

        List<Ubicacion> zonasAsignadas = ubicacionRepository.findDistinctByUsuariosResponsablesIdAndTipo(
                operacionSupport.obtenerUsuarioAutenticado().getId(),
                TipoUbicacion.ZONA_EMPAQUE
        );
        int desde = Math.min((int) pageable.getOffset(), zonasAsignadas.size());
        int hasta = Math.min(desde + pageable.getPageSize(), zonasAsignadas.size());
        return PageResponseDTO.fromPage(
                new PageImpl<>(zonasAsignadas.subList(desde, hasta), pageable, zonasAsignadas.size()),
                this::convertirRespuesta
        );
    }

    @Override
    public List<UbicacionResponseDTO> listarPorTipo(TipoUbicacion tipoUbicacion) {
        if (operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            if (tipoUbicacion != TipoUbicacion.ZONA_EMPAQUE) {
                return List.of();
            }
            return ubicacionRepository.findDistinctByUsuariosResponsablesIdAndTipo(
                            operacionSupport.obtenerUsuarioAutenticado().getId(),
                            TipoUbicacion.ZONA_EMPAQUE
                    ).stream()
                    .map(this::convertirRespuesta)
                    .toList();
        }
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
        if (!nuevoEstadoActivo && loteInventarioRepository.existsByUbicacionIdAndCantidadGreaterThan(ubicacionId, Numeros.CERO)) {
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
        if (loteInventarioRepository.existsByUbicacionIdAndCantidadGreaterThan(ubicacionId, Numeros.CERO)) {
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

    /** Valida y resuelve los empaquetadores responsables asociados a una zona de empaque. */
    private Set<Usuario> resolverUsuariosResponsables(TipoUbicacion tipoUbicacion, Set<Long> usuariosResponsablesIds) {
        if (tipoUbicacion != TipoUbicacion.ZONA_EMPAQUE) {
            if (usuariosResponsablesIds != null && !usuariosResponsablesIds.isEmpty()) {
                throw new BusinessException(MensajesError.Ubicacion.USUARIO_SOLO_ZONA);
            }
            return new HashSet<>();
        }
        if (usuariosResponsablesIds == null || usuariosResponsablesIds.isEmpty()) {
            throw new BusinessException(MensajesError.Ubicacion.ZONA_REQUIERE_RESPONSABLE);
        }
        Set<Usuario> responsables = usuariosResponsablesIds.stream().map(usuarioId -> {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException(MensajesError.Ubicacion.USUARIO_ASIGNADO_NO_ENCONTRADO));
            if (!usuario.getActivo()) throw new BusinessException(MensajesError.Ubicacion.USUARIO_ASIGNADO_INACTIVO);
            boolean empaquetador = usuario.getRoles().stream().anyMatch(rol -> rol.getNombre() == NombreRol.ROLE_PACKER);
            if (!empaquetador) throw new BusinessException(MensajesError.Ubicacion.RESPONSABLE_DEBE_SER_EMPAQUETADOR);
            return usuario;
        }).collect(Collectors.toSet());
        return responsables;
    }

    /** Restringe a los empaquetadores para que solo consulten las zonas que tienen asignadas. */
    private void validarLecturaEmpaquetador(Ubicacion ubicacion) {
        if (!operacionSupport.usuarioAutenticadoEsEmpaquetador()) {
            return;
        }
        if (ubicacion.getTipo() != TipoUbicacion.ZONA_EMPAQUE
                || !operacionSupport.obtenerZonasAsignadasIds().contains(ubicacion.getId())) {
            throw new BusinessException(MensajesError.Operacion.UBICACION_NO_ASIGNADA);
        }
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
                .usuariosResponsablesIds(ubicacion.getUsuariosResponsables().stream().map(Usuario::getId).collect(Collectors.toSet()))
                .usuariosResponsablesNombres(ubicacion.getUsuariosResponsables().stream().map(Usuario::getUsername).collect(Collectors.toSet()))
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
