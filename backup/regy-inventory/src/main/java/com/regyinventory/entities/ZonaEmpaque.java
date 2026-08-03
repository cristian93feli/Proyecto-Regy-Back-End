package com.regyinventory.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zonas_empaque" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaEmpaque extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
    @Column(length = 250)
    private String descripcion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado_id" )
    private Usuario usuarioAsignado;
}
