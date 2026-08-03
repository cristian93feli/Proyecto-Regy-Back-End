package com.regyinventory.entities;

import com.regyinventory.enums.TipoDestino;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "ingresos_stock" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngresoStock extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false)
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDestino tipoDestino;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id" )
    private Ubicacion ubicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_empaque_id" )
    private ZonaEmpaque zonaEmpaque;
    private LocalDate fechaVencimiento;
    @Column(length = 500)
    private String observaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
