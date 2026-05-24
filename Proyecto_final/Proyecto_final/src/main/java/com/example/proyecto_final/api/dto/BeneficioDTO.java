package com.example.proyecto_final.api.dto;

import com.example.proyecto_final.domain.enums.NivelUsuario;
import com.example.proyecto_final.domain.enums.TipoBeneficio;
import lombok.*;

/**
 * DTO for Beneficio requests/responses
 */
/**
 * DTO para beneficios disponibles o canjeados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficioDTO {
    private String id;
    private String descripcion;
    private NivelUsuario nivelRequerido;
    private Integer puntosNecesarios;
    private TipoBeneficio tipo;
    private Boolean activo;
}

