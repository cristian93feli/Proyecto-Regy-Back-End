package com.regyinventory.entities;

import com.regyinventory.enums.TipoAccionLog;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "logs_operacion" )
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogOperacion extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoAccionLog accion;
    @Column(nullable = false, length = 80)
    private String entidad;
    private Long entidadId;
    @Column(nullable = false, length = 500)
    private String detalle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id" )
    private Usuario usuario;
}
