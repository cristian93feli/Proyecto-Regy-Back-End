package com.regyinventory.entities;

import com.regyinventory.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "auditorias_inventario" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaInventario extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAuditoria tipoAuditoria;

    private Long destinoId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MotivoAjuste motivo;
    @Column(length = 500)
    private String motivoOtro;
    @Column(length = 500)
    private String observaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @OneToMany(mappedBy = "auditoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleAuditoria> detalles = new ArrayList<>();
}
