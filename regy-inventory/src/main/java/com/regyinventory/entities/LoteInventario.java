package com.regyinventory.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lotes_inventario", indexes = {
        @Index(name = "idx_lote_producto", columnList = "producto_id"),
        @Index(name = "idx_lote_ubicacion", columnList = "ubicacion_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteInventario extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;
    @Column(nullable = false)
    private Integer cantidad;
    @Column(nullable = false)
    private LocalDateTime fechaIngreso;
    private LocalDate fechaVencimiento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingreso_stock_id")
    private IngresoStock ingresoStock;
}
