package com.regyinventory.entities;

import com.regyinventory.enums.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(nullable = false)
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipoMovimiento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_origen_id")
    private Ubicacion ubicacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_destino_id")
    private Ubicacion ubicacionDestino;
    @Column(length = 500)
    private String observaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
