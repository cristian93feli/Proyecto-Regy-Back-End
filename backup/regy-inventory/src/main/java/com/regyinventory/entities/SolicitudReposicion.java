package com.regyinventory.entities;

import com.regyinventory.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solicitudes_reposicion" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudReposicion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false)
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadSolicitud prioridad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_destino_id", nullable = false)
    private ZonaEmpaque zonaDestino;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_solicitante_id", nullable = false)
    private Usuario usuarioSolicitante;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id" )
    private Usuario usuarioResponsable;
    @Column(length = 500)
    private String observaciones;
}
