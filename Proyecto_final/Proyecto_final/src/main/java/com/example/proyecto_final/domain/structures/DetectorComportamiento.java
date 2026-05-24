package com.example.proyecto_final.domain.structures;

import com.example.proyecto_final.domain.enums.NivelRiesgo;
import com.example.proyecto_final.domain.enums.TipoEvento;
import com.example.proyecto_final.domain.model.EventoAuditoria;
import com.example.proyecto_final.domain.model.Transaccion;
import com.example.proyecto_final.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Analiza patrones de transacciones usando HashMap (promedios por usuario),
 * Lista (historial del gestor) y genera EventoAuditoria ante montos inusuales.
 */
public class DetectorComportamiento {

    private static final double MONTO_ALTO_ABSOLUTO = 1_000_000.0;
    private static final double MONTO_CRITICO_ABSOLUTO = 5_000_000.0;
    private static final double FACTOR_INUSUAL = 3.0;

    private final Map<String, Double> promedioPorUsuario;
    private final List<EventoAuditoria> eventosDetectados;

    public DetectorComportamiento() {
        this.promedioPorUsuario = new HashMap<>();
        this.eventosDetectados = new ArrayList<>();
    }

    /**
     * Calcula y almacena el promedio historico del usuario en el HashMap.
     */
    public void indexarHistorial(String usuarioId, List<Transaccion> historial) {
        double promedio = historial.stream()
                .mapToDouble(Transaccion::getValor)
                .average()
                .orElse(0.0);
        promedioPorUsuario.put(usuarioId, promedio);
        imprimir("indexar historial usuario " + usuarioId, promedio);
    }

    /**
     * Evalua una transaccion contra el historial del {@link GestorTransacciones}.
     */
    public Optional<EventoAuditoria> analizar(Usuario usuario, Transaccion transaccion, GestorTransacciones gestor) {
        List<Transaccion> historial = gestor.obtenerHistorial();
        double promedio = promedioPorUsuario.getOrDefault(
                usuario.getId(),
                historial.stream().mapToDouble(Transaccion::getValor).average().orElse(0.0)
        );

        boolean montoAbsolutoAlto = transaccion.getValor() >= MONTO_ALTO_ABSOLUTO;
        boolean superaPromedio = promedio > 0 && transaccion.getValor() >= promedio * FACTOR_INUSUAL;

        if (!montoAbsolutoAlto && !superaPromedio) {
            imprimir("analizar transaccion " + transaccion.getId() + " -> sin alerta", promedio);
            return Optional.empty();
        }

        NivelRiesgo riesgo = transaccion.getValor() >= MONTO_CRITICO_ABSOLUTO
                ? NivelRiesgo.CRITICO
                : NivelRiesgo.ALTO;

        EventoAuditoria evento = EventoAuditoria.builder()
                .id("aud-" + System.currentTimeMillis())
                .transaccionId(transaccion.getId())
                .usuarioId(usuario.getId())
                .tipoEvento(TipoEvento.ALERTA_GENERADA)
                .nivelRiesgo(riesgo)
                .descripcion(String.format(
                        "Monto inusual detectado: $%,.0f (promedio historico: $%,.0f)",
                        transaccion.getValor(),
                        promedio
                ))
                .fecha(LocalDateTime.now())
                .revisado(false)
                .build();

        eventosDetectados.add(evento);
        imprimir("analizar transaccion " + transaccion.getId() + " -> alerta generada", promedio);
        return Optional.of(evento);
    }

    public List<EventoAuditoria> obtenerEventosDetectados() {
        return new ArrayList<>(eventosDetectados);
    }

    public Map<String, Double> obtenerPromediosIndexados() {
        return new HashMap<>(promedioPorUsuario);
    }

    private void imprimir(String accion, double promedioReferencia) {
        StringBuilder estado = new StringBuilder();
        estado.append("Tipo: HashMap + Lista").append(System.lineSeparator());
        estado.append("Promedio evaluado: ").append(promedioReferencia).append(System.lineSeparator());
        estado.append("Entradas HashMap (usuario -> promedio):").append(System.lineSeparator());
        promedioPorUsuario.forEach((usuarioId, promedio) ->
                estado.append("  ").append(usuarioId).append(" -> ").append(promedio).append(System.lineSeparator())
        );
        estado.append("Eventos detectados: ").append(eventosDetectados.size()).append(System.lineSeparator());
        for (int i = 0; i < eventosDetectados.size(); i++) {
            estado.append("lista[").append(i).append("] -> ").append(eventosDetectados.get(i)).append(System.lineSeparator());
        }
        EstructuraConsolePrinter.imprimir("DetectorComportamiento", accion, estado.toString());
    }
}
