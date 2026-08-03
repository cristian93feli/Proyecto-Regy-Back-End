package com.regyinventory.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "marcas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marca extends BaseEntity {

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String nombre;

    @Column(length = 250)
    private String descripcion;
}