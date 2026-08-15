package com.regyinventory.entities;

import com.regyinventory.enums.TipoUbicacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "ubicaciones",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ubicacion_codigo",
                columnNames = "codigo"
        )
)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUbicacion tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_padre_id")
    private Ubicacion ubicacionPadre;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ubicaciones_responsables", joinColumns = @JoinColumn(name = "ubicacion_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private Set<Usuario> usuariosResponsables = new HashSet<>();
}
