package com.regyinventory.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_sistema" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSistema extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String clave;
    @Column(nullable = false, length = 500)
    private String valor;
    @Column(length = 250)
    private String descripcion;
}
