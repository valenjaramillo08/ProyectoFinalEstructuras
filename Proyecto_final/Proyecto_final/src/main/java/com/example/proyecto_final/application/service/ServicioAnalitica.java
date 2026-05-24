package com.example.proyecto_final.application.service;

import com.example.proyecto_final.api.dto.ReporteUsoDTO;
import com.example.proyecto_final.api.dto.EnlaceGrafoDTO;
import com.example.proyecto_final.api.dto.GrafoAnaliticaDTO;
import com.example.proyecto_final.api.dto.NodoGrafoDTO;
import com.example.proyecto_final.api.dto.UsuarioActivoDTO;
import com.example.proyecto_final.domain.enums.EstadoTransaccion;
import com.example.proyecto_final.domain.enums.NivelRiesgo;
import com.example.proyecto_final.domain.enums.TipoBilletera;
import com.example.proyecto_final.domain.enums.TipoTransaccion;
import com.example.proyecto_final.domain.model.Billetera;
import com.example.proyecto_final.domain.model.EventoAuditoria;
import com.example.proyecto_final.domain.model.Transaccion;
import com.example.proyecto_final.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.proyecto_final.domain.structures.GrafoTransferencias;
/**
 * Servicio de consultas analiticas sobre usuarios, billeteras y transacciones.
 */

@Service
@RequiredArgsConstructor
public class ServicioAnalitica {
    private final PlataformaContext context;
    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;
    private final TransaccionService transaccionService;

    /**
     * Construye un reporte agregado de actividad para un usuario.
     *
     * @param usuarioId identificador del usuario.
     * @return reporte con cantidades, montos y fechas de actividad.
     */
    public ReporteUsoDTO generarReporte(String usuarioId) {
        usuarioService.obtenerObligatorio(usuarioId);
        List<Transaccion> transacciones = transaccionesUsuario(usuarioId);
        double montoTotal = transacciones.stream().mapToDouble(Transaccion::getValor).sum();
        long billeterasActivas = billeteraService.listarPorUsuario(usuarioId).stream()
                .filter(b -> b.getHistorialTransacciones() != null && !b.getHistorialTransacciones().isEmpty())
                .count();

        return ReporteUsoDTO.builder()
                .id(UUID.randomUUID().toString())
                .usuarioId(usuarioId)
                .totalTransacciones((long) transacciones.size())
                .montoTotalMovilizado(montoTotal)
                .fechaInicio(transacciones.stream().map(Transaccion::getFecha).min(LocalDateTime::compareTo).orElse(null))
                .fechaFin(transacciones.stream().map(Transaccion::getFecha).max(LocalDateTime::compareTo).orElse(null))
                .promedioTransaccion(transacciones.isEmpty() ? 0.0 : montoTotal / transacciones.size())
                .billeterasMasActivas(billeterasActivas)
                .build();
    }

    public List<Billetera> billeterasConMayorUso() {
        return billeteraService.listarTodas().stream()
                .sorted(Comparator.comparingInt(this::cantidadMovimientos).reversed())
                .limit(10)
                .toList();
    }

    public List<Usuario> usuariosConMasTransferencias() {
        return usuarioService.listarTodos().stream()
                .sorted(Comparator.comparingLong((Usuario u) -> transaccionService.contarTransferenciasUsuario(u.getId())).reversed())
                .limit(10)
                .toList();
    }

    /**
     * Devuelve usuarios activos en un formato plano para tablas del frontend.
     *
     * @return resumen de usuarios ordenados por cantidad de transacciones.
     */
    public List<UsuarioActivoDTO> usuariosActivosResumen() {
        return usuarioService.listarTodos().stream()
                .map(usuario -> UsuarioActivoDTO.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombre())
                        .totalTransacciones((long) transaccionesUsuario(usuario.getId()).size())
                        .puntosAcumulados(usuario.getPuntosAcumulados())
                        .nivel(usuario.getNivel())
                        .build())
                .sorted(Comparator.comparingLong(UsuarioActivoDTO::getTotalTransacciones).reversed())
                .limit(10)
                .toList();
    }

    public Map<String, Object> consultarAuditoria(String usuarioId) {
        usuarioService.obtenerObligatorio(usuarioId);
        List<EventoAuditoria> eventos = context.getEventosAuditoria().stream()
                .filter(evento -> usuarioId.equals(evento.getUsuarioId()))
                .toList();
        Map<String, Object> auditoria = new LinkedHashMap<>();
        auditoria.put("usuarioId", usuarioId);
        auditoria.put("totalEventos", eventos.size());
        auditoria.put("eventosCriticos", contarRiesgo(eventos, NivelRiesgo.CRITICO));
        auditoria.put("eventosAltos", contarRiesgo(eventos, NivelRiesgo.ALTO));
        auditoria.put("eventos", eventos);
        return auditoria;
    }

    public Map<TipoTransaccion, Long> frecuenciaPorTipo() {
        Map<TipoTransaccion, Long> frecuencia = new EnumMap<>(TipoTransaccion.class);
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            frecuencia.put(tipo, 0L);
        }

        Map<TipoTransaccion, Long> conteos = transaccionService.listarTodas().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .collect(Collectors.groupingBy(
                        Transaccion::getTipo,
                        () -> new EnumMap<>(TipoTransaccion.class),
                        Collectors.counting()
                ));
        frecuencia.putAll(conteos);
        return frecuencia;
    }

    public Map<TipoBilletera, Long> categoriasMasActivas() {
        Map<TipoBilletera, Long> categorias = new EnumMap<>(TipoBilletera.class);
        for (Billetera billetera : billeteraService.listarTodas()) {
            categorias.put(billetera.getTipo(), (long) cantidadMovimientos(billetera));
        }
        return categorias.entrySet().stream()
                .sorted(Map.Entry.<TipoBilletera, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Long::sum,
                        LinkedHashMap::new
                ));
    }

    public double montoTotalMovilizado(LocalDateTime inicio, LocalDateTime fin) {
        LocalDateTime desde = inicio == null ? LocalDateTime.MIN : inicio;
        LocalDateTime hasta = fin == null ? LocalDateTime.MAX : fin;
        return transaccionService.filtrarPorPeriodo(desde, hasta).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .mapToDouble(Transaccion::getValor)
                .sum();
    }

    public Map<String, Object> analizarGrafoUsuario(String usuarioId) {
        usuarioService.obtenerObligatorio(usuarioId);
        Map<String, Object> analisis = new LinkedHashMap<>(context.getGrafoTransferencias().analizarConexiones(usuarioId));
        Map<String, Double> destinos = context.getGrafoTransferencias().obtenerDestinosPonderados(usuarioId);
        analisis.put("destinosPonderados", destinos);
        analisis.put("hayCiclos", context.getGrafoTransferencias().detectarCiclos());
        analisis.put("vertices", context.getGrafoTransferencias().cantidadVertices());
        return analisis;
    }

    /**
     * Devuelve el grafo correspondiente al tipo solicitado por el frontend.
     */
    public GrafoAnaliticaDTO grafoPorTipo(String usuarioId, String tipo) {
        usuarioService.obtenerObligatorio(usuarioId);
        String normalizado = tipo == null ? "transferencias" : tipo.trim().toLowerCase();
        return switch (normalizado) {
            case "billeteras" -> grafoRelacionesBilleteras();
            case "rutas" -> grafoRutasFrecuentes(usuarioId);
            case "patrones" -> grafoPatronesInteraccion();
            default -> grafoTransferencias();
        };
    }

    /**
     * Grafo 1: transferencias entre usuarios (aristas ponderadas por monto).
     */
    public GrafoAnaliticaDTO grafoTransferencias() {
        GrafoTransferencias grafo = context.getGrafoTransferencias();
        List<String> vertices = grafo.obtenerVertices().stream().sorted().toList();

        List<NodoGrafoDTO> nodos = vertices.stream()
                .map(id -> NodoGrafoDTO.builder()
                        .id(id)
                        .nombre(nombreUsuario(id))
                        .build())
                .toList();

        List<EnlaceGrafoDTO> enlaces = vertices.stream()
                .flatMap(origen -> grafo.obtenerDestinosPonderados(origen).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(destino -> EnlaceGrafoDTO.builder()
                                .origen(origen)
                                .destino(destino.getKey())
                                .monto(destino.getValue())
                                .build()))
                .toList();

        return GrafoAnaliticaDTO.builder()
                .tipo("transferencias")
                .titulo("Transferencias entre usuarios")
                .nodos(nodos)
                .enlaces(enlaces)
                .build();
    }

    /**
     * Grafo 2: relaciones entre billeteras segun movimientos completados.
     */
    public GrafoAnaliticaDTO grafoRelacionesBilleteras() {
        Map<String, String> nombres = new HashMap<>();
        Map<String, Map<String, Double>> adyacencia = new HashMap<>();

        transaccionService.listarTodas().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> t.getBilleteraOrigenId() != null && t.getBilleteraDestinoId() != null)
                .forEach(t -> {
                    String origen = t.getBilleteraOrigenId();
                    String destino = t.getBilleteraDestinoId();
                    billeteraService.buscarPorId(origen).ifPresent(b -> nombres.put(origen, b.getNombre()));
                    billeteraService.buscarPorId(destino).ifPresent(b -> nombres.put(destino, b.getNombre()));
                    adyacencia
                            .computeIfAbsent(origen, k -> new HashMap<>())
                            .merge(destino, t.getValor(), Double::sum);
                });

        Set<String> idsBilleteras = new HashSet<>();
        adyacencia.forEach((origen, destinos) -> {
            idsBilleteras.add(origen);
            idsBilleteras.addAll(destinos.keySet());
        });

        List<NodoGrafoDTO> nodos = idsBilleteras.stream()
                .sorted()
                .map(id -> NodoGrafoDTO.builder()
                        .id(id)
                        .nombre(nombres.getOrDefault(id, id))
                        .build())
                .toList();

        List<EnlaceGrafoDTO> enlaces = adyacencia.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(destino -> EnlaceGrafoDTO.builder()
                                .origen(entry.getKey())
                                .destino(destino.getKey())
                                .monto(destino.getValue())
                                .build()))
                .toList();

        return GrafoAnaliticaDTO.builder()
                .tipo("billeteras")
                .titulo("Relaciones entre billeteras")
                .nodos(nodos)
                .enlaces(enlaces)
                .build();
    }

    /**
     * Grafo 3: rutas frecuentes de movimiento de dinero (caminos con intermediarios).
     */
    public GrafoAnaliticaDTO grafoRutasFrecuentes(String usuarioId) {
        GrafoTransferencias grafo = context.getGrafoTransferencias();
        Set<String> nodosIds = new HashSet<>();
        Map<String, Double> aristas = new LinkedHashMap<>();

        List<String> vertices = grafo.obtenerVertices().stream().sorted().toList();
        for (String origen : vertices) {
            for (String destino : vertices) {
                if (origen.equals(destino)) {
                    continue;
                }
                for (List<String> ruta : grafo.encontrarRutas(origen, destino)) {
                    if (ruta.size() < 3) {
                        continue;
                    }
                    for (int i = 0; i < ruta.size() - 1; i++) {
                        String o = ruta.get(i);
                        String d = ruta.get(i + 1);
                        nodosIds.add(o);
                        nodosIds.add(d);
                        String clave = o + "->" + d;
                        aristas.merge(clave, grafo.obtenerMonto(o, d), Double::sum);
                    }
                }
            }
        }

        if (aristas.isEmpty()) {
            for (String origen : vertices) {
                nodosIds.add(origen);
                grafo.obtenerDestinosPonderados(origen).entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .limit(5)
                        .forEach(destino -> {
                            nodosIds.add(destino.getKey());
                            aristas.put(origen + "->" + destino.getKey(), destino.getValue());
                        });
            }
            nodosIds.add(usuarioId);
        }

        List<NodoGrafoDTO> nodos = nodosIds.stream()
                .sorted()
                .map(id -> NodoGrafoDTO.builder()
                        .id(id)
                        .nombre(nombreUsuario(id))
                        .build())
                .toList();

        List<EnlaceGrafoDTO> enlaces = aristas.entrySet().stream()
                .map(entry -> {
                    String[] partes = entry.getKey().split("->", 2);
                    return EnlaceGrafoDTO.builder()
                            .origen(partes[0])
                            .destino(partes[1])
                            .monto(entry.getValue())
                            .build();
                })
                .toList();

        return GrafoAnaliticaDTO.builder()
                .tipo("rutas")
                .titulo("Rutas frecuentes de movimiento de dinero")
                .nodos(nodos)
                .enlaces(enlaces)
                .build();
    }

    /**
     * Grafo 4: patrones de interaccion financiera (volumen bidireccional entre pares).
     */
    public GrafoAnaliticaDTO grafoPatronesInteraccion() {
        GrafoTransferencias grafo = context.getGrafoTransferencias();
        List<String> vertices = grafo.obtenerVertices().stream().sorted().toList();
        Map<String, Double> interacciones = new LinkedHashMap<>();

        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                String a = vertices.get(i);
                String b = vertices.get(j);
                double volumen = grafo.obtenerMonto(a, b) + grafo.obtenerMonto(b, a);
                if (volumen > 0) {
                    interacciones.put(a + "->" + b, volumen);
                    interacciones.put(b + "->" + a, volumen);
                }
            }
        }

        List<NodoGrafoDTO> nodos = vertices.stream()
                .map(id -> NodoGrafoDTO.builder()
                        .id(id)
                        .nombre(nombreUsuario(id))
                        .build())
                .toList();

        List<EnlaceGrafoDTO> enlaces = interacciones.entrySet().stream()
                .map(entry -> {
                    String[] partes = entry.getKey().split("->", 2);
                    return EnlaceGrafoDTO.builder()
                            .origen(partes[0])
                            .destino(partes[1])
                            .monto(entry.getValue())
                            .build();
                })
                .toList();

        return GrafoAnaliticaDTO.builder()
                .tipo("patrones")
                .titulo("Patrones de interaccion financiera")
                .nodos(nodos)
                .enlaces(enlaces)
                .build();
    }

    public List<Transaccion> transaccionesMayorValor(int limite) {
        return transaccionService.mayoresValores(Math.max(1, limite));
    }

    public Map<String, Object> compararRendimiento(String usuarioId) {
        Map<String, Object> resultado = new HashMap<>();

        long inicioHash = System.nanoTime();
        usuarioService.buscarPorId(usuarioId);
        long finHash = System.nanoTime();

        long inicioLista = System.nanoTime();
        usuarioService.listarTodos().stream().filter(u -> u.getId().equals(usuarioId)).findFirst();
        long finLista = System.nanoTime();

        resultado.put("busquedaTablaHashNs", finHash - inicioHash);
        resultado.put("busquedaListaLinealNs", finLista - inicioLista);
        resultado.put("usuariosEvaluados", usuarioService.listarTodos().size());
        resultado.put("nota", "La tabla hash evita recorrer todos los usuarios cuando se busca por identificacion");
        return resultado;
    }

    private List<Transaccion> transaccionesUsuario(String usuarioId) {
        return transaccionService.listarTodas().stream()
                .filter(t -> usuarioId.equals(usuarioDeTransaccion(t)))
                .toList();
    }

    private String usuarioDeTransaccion(Transaccion transaccion) {
        String billeteraId = transaccion.getBilleteraOrigenId() != null
                ? transaccion.getBilleteraOrigenId()
                : transaccion.getBilleteraDestinoId();
        if (billeteraId == null) {
            return null;
        }
        return billeteraService.buscarPorId(billeteraId)
                .map(Billetera::getUsuarioId)
                .orElse(null);
    }

    private String nombreUsuario(String usuarioId) {
        return usuarioService.buscarPorId(usuarioId)
                .map(Usuario::getNombre)
                .orElse(usuarioId);
    }

    private int cantidadMovimientos(Billetera billetera) {
        return billetera.getHistorialTransacciones() == null ? 0 : billetera.getHistorialTransacciones().size();
    }

    private long contarRiesgo(List<EventoAuditoria> eventos, NivelRiesgo riesgo) {
        return eventos.stream().filter(evento -> evento.getNivelRiesgo() == riesgo).count();
    }
}
