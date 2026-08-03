package com.regyinventory.entities;

import com.regyinventory.enums.NombrePermiso;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private NombrePermiso nombre;

    @Column(length = 250)
    private String descripcion;

    @ManyToMany(mappedBy = "permisos")
    @Builder.Default
    private Set<Rol> roles  = new HashSet<>();

}