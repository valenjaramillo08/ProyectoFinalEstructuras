package com.example.proyecto_final.api.dto;

import com.example.proyecto_final.domain.enums.EstadoBilletera;
import com.example.proyecto_final.domain.enums.TipoBilletera;
import lombok.*;

/**
 * DTO for Billetera requests/responses
 */
/**
 * DTO usado para crear, actualizar y listar billeteras.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilleteraDTO {
    private String id;
    private String nombre;
    private TipoBilletera tipo;
    private Double saldo;
    private EstadoBilletera estado;
    private String usuarioId;
}

