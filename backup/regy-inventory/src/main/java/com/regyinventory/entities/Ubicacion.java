package com.regyinventory.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubicaciones", uniqueConstraints = @UniqueConstraint(name = "uk_ubicacion_deposito_codigo", columnNames = {"deposito_id", "codigo"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String codigo;
    @Column(nullable = false, length = 120)
    private String nombre;
    @Column(length = 250)
    private String descripcion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", nullable = false, updatable = false)
    private Deposito deposito;
}
