package com.example.proyecto_final.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
/**
 * DTO raiz del grafo de analitica formado por nodos y enlaces.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrafoAnaliticaDTO {
    private String tipo;
    private String titulo;
    private List<NodoGrafoDTO> nodos;
    private List<EnlaceGrafoDTO> enlaces;
}
