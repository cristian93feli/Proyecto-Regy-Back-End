package com.regyinventory.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "depositos" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deposito extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
    @Column(length = 250)
    private String descripcion;
}
