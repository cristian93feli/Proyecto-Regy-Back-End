package com.regyinventory.entities;

import com.regyinventory.enums.UnidadMedida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos", uniqueConstraints = {@UniqueConstraint(name = "uk_producto_numero", columnNames = "numero" ), @UniqueConstraint(name = "uk_producto_sku", columnNames = "sku" ), @UniqueConstraint(name = "uk_producto_codigo_barras", columnNames = "codigo_barras" )})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String numero;
    @Column(nullable = false, length = 150)
    private String nombre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_id" )
    private Marca marca;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id" )
    private Categoria categoria;
    @Column(length = 80)
    private String sku;
    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;
    @Column(length = 500)
    private String imagenUrl;
    @Column(precision = 14, scale = 2)
    private BigDecimal precioCompra;
    @Column(precision = 14, scale = 2)
    private BigDecimal precioVenta;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;
    @Column(nullable = false)
    @Builder.Default
    private Integer stockMinimo = 0;
}
