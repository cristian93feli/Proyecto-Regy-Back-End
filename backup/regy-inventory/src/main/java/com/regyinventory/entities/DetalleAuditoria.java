package com.regyinventory.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_auditoria" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAuditoria extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditoria_id", nullable = false)
    private AuditoriaInventario auditoria;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false)
    private Integer cantidadSistema;
    @Column(nullable = false)
    private Integer cantidadContada;
    @Column(nullable = false)
    private Integer diferencia;
}
