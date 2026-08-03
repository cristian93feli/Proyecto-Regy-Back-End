package com.regyinventory.entities;

import com.regyinventory.enums.TipoDestino;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "lotes_inventario", indexes = {@Index(name = "idx_lote_producto", columnList = "producto_id" ), @Index(name = "idx_lote_ubicacion", columnList = "ubicacion_id" ), @Index(name = "idx_lote_zona", columnList = "zona_empaque_id" )})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteInventario extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDestino tipoDestino;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id" )
    private Ubicacion ubicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_empaque_id" )
    private ZonaEmpaque zonaEmpaque;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(nullable = false)
    private LocalDateTime fechaIngreso;
    private LocalDate fechaVencimiento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingreso_stock_id" )
    private IngresoStock ingresoStock;
}
