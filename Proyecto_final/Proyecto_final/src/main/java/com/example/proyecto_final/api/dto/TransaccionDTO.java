package com.example.proyecto_final.api.dto;

import com.example.proyecto_final.domain.enums.EstadoTransaccion;
import com.example.proyecto_final.domain.enums.NivelRiesgo;
import com.example.proyecto_final.domain.enums.TipoTransaccion;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Transaccion requests/responses
 */
/**
 * DTO de transacciones usado por los endpoints de movimientos financieros.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionDTO {
    private String id;
    private LocalDateTime fecha;
    private TipoTransaccion tipo;
    private Double valor;
    private String billeteraOrigenId;
    private String billeteraDestinoId;
    private EstadoTransaccion estado;
    private Integer puntosGenerados;
    private NivelRiesgo nivelRiesgo;
}

