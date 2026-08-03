package com.regyinventory.entities;

import com.regyinventory.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movimientos_inventario" )
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDestino tipoOrigen;
    private Long origenId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDestino tipoDestino;
    private Long destinoId;
    @Column(length = 500)
    private String observaciones;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
