package com.example.proyecto_final.domain.model;

import com.example.proyecto_final.domain.enums.NivelUsuario;
import com.example.proyecto_final.domain.enums.TipoBeneficio;
import lombok.*;
/**
 * Beneficio canjeable del sistema de recompensas.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficio {
    private String id;
    private String descripcion;
    private NivelUsuario nivelRequerido;
    private int puntosNecesarios;
    private TipoBeneficio tipo;
    private boolean activo;

    @Override
    public String toString() {
        return String.format("Beneficio{id='%s', descripcion='%s', nivelRequerido=%s, puntosNecesarios=%d, tipo=%s}",
                id, descripcion, nivelRequerido, puntosNecesarios, tipo);
    }
}

