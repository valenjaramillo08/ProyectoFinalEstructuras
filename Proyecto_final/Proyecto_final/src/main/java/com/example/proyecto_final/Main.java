package com.example.proyecto_final;

import com.example.proyecto_final.domain.enums.EstadoTransaccion;
import com.example.proyecto_final.domain.enums.NivelUsuario;
import com.example.proyecto_final.domain.enums.TipoTransaccion;
import com.example.proyecto_final.domain.model.EventoAuditoria;
import com.example.proyecto_final.domain.model.Transaccion;
import com.example.proyecto_final.domain.model.Usuario;
import com.example.proyecto_final.domain.structures.DetectorComportamiento;
import com.example.proyecto_final.domain.structures.GestorTransacciones;
import com.example.proyecto_final.domain.structures.TablaUsuarios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Casos de prueba de demostracion para el informe tecnico.
 *
 * Ejecucion:
 *   mvn -q compile exec:java -Dexec.mainClass=com.example.proyecto_final.Main
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println(" CASOS DE PRUEBA - PLATAFORMA FINTECH");
        System.out.println("============================================================");

        caso1CreacionUsuarioBronce();
        caso2TransaccionPorGestor();
        caso3DeteccionMontoInusual();

        System.out.println("============================================================");
        System.out.println(" FIN DE CASOS DE PRUEBA");
        System.out.println("============================================================");
    }

    private static void caso1CreacionUsuarioBronce() {
        System.out.println();
        System.out.println("--- CASO 1: Creacion de usuario nivel BRONCE ---");

        TablaUsuarios tablaUsuarios = new TablaUsuarios();

        Usuario usuario = Usuario.builder()
                .id("usr-bronce-001")
                .nombre("Camila Ortiz")
                .email("camila.ortiz@fintech.local")
                .telefono("3001234567")
                .nivel(NivelUsuario.BRONCE)
                .puntosAcumulados(120)
                .fechaRegistro(LocalDateTime.now())
                .build();

        tablaUsuarios.insertar(usuario);

        Usuario recuperado = tablaUsuarios.buscar("usr-bronce-001");

        System.out.println("Usuario creado:");
        System.out.println("  id     = " + recuperado.getId());
        System.out.println("  nombre = " + recuperado.getNombre());
        System.out.println("  email  = " + recuperado.getEmail());
        System.out.println("  nivel  = " + recuperado.getNivel());
        System.out.println("  puntos = " + recuperado.getPuntosAcumulados());
        System.out.println("Estructura usada: TablaUsuarios (HashMap con encadenamiento)");
        System.out.println("Total usuarios en tabla: " + tablaUsuarios.size());
    }

    private static void caso2TransaccionPorGestor() {
        System.out.println();
        System.out.println("--- CASO 2: Transaccion procesada por GestorTransacciones ---");

        GestorTransacciones gestor = new GestorTransacciones();

        Transaccion recarga = Transaccion.builder()
                .id("tx-recarga-001")
                .fecha(LocalDateTime.now().minusMinutes(5))
                .tipo(TipoTransaccion.RECARGA)
                .valor(250_000)
                .billeteraDestinoId("wal-principal-001")
                .estado(EstadoTransaccion.COMPLETADA)
                .puntosGenerados(2_500)
                .build();

        Transaccion transferencia = Transaccion.builder()
                .id("tx-transfer-001")
                .fecha(LocalDateTime.now())
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .valor(180_000)
                .billeteraOrigenId("wal-principal-001")
                .billeteraDestinoId("wal-ahorro-002")
                .estado(EstadoTransaccion.COMPLETADA)
                .puntosGenerados(1_800)
                .build();

        gestor.agregar(recarga);
        gestor.agregar(transferencia);

        List<Transaccion> historial = gestor.obtenerHistorial();
        Transaccion encontrada = gestor.buscar("tx-transfer-001");
        Transaccion topePila = gestor.revertir();

        System.out.println("Historial registrado (Lista enlazada):");
        historial.forEach(tx -> System.out.println("  -> " + tx));

        System.out.println("Busqueda por id tx-transfer-001:");
        System.out.println("  -> " + encontrada);

        System.out.println("Reversion desde Pila:");
        System.out.println("  -> " + topePila);
        System.out.println("Tamano lista: " + gestor.size());
    }

    private static void caso3DeteccionMontoInusual() {
        System.out.println();
        System.out.println("--- CASO 3: DetectorComportamiento - monto inusual ---");

        Usuario usuario = Usuario.builder()
                .id("usr-bronce-001")
                .nombre("Camila Ortiz")
                .nivel(NivelUsuario.BRONCE)
                .build();

        GestorTransacciones gestor = new GestorTransacciones();
        DetectorComportamiento detector = new DetectorComportamiento();

        gestor.agregar(Transaccion.builder()
                .id("tx-normal-001")
                .fecha(LocalDateTime.now().minusDays(2))
                .tipo(TipoTransaccion.RECARGA)
                .valor(200_000)
                .billeteraDestinoId("wal-principal-001")
                .estado(EstadoTransaccion.COMPLETADA)
                .build());

        gestor.agregar(Transaccion.builder()
                .id("tx-normal-002")
                .fecha(LocalDateTime.now().minusDays(1))
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .valor(150_000)
                .billeteraOrigenId("wal-principal-001")
                .billeteraDestinoId("wal-ahorro-002")
                .estado(EstadoTransaccion.COMPLETADA)
                .build());

        detector.indexarHistorial(usuario.getId(), gestor.obtenerHistorial());

        Transaccion transaccionInusual = Transaccion.builder()
                .id("tx-inusual-001")
                .fecha(LocalDateTime.now())
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .valor(1_850_000)
                .billeteraOrigenId("wal-principal-001")
                .billeteraDestinoId("wal-externa-999")
                .estado(EstadoTransaccion.COMPLETADA)
                .build();

        gestor.agregar(transaccionInusual);

        Optional<EventoAuditoria> eventoOpt = detector.analizar(usuario, transaccionInusual, gestor);

        if (eventoOpt.isPresent()) {
            EventoAuditoria evento = eventoOpt.get();
            System.out.println("EventoAuditoria generado:");
            System.out.println("  id           = " + evento.getId());
            System.out.println("  transaccion  = " + evento.getTransaccionId());
            System.out.println("  usuario      = " + evento.getUsuarioId());
            System.out.println("  tipoEvento   = " + evento.getTipoEvento());
            System.out.println("  nivelRiesgo  = " + evento.getNivelRiesgo());
            System.out.println("  descripcion  = " + evento.getDescripcion());
            System.out.println("  revisado     = " + evento.isRevisado());
        } else {
            System.out.println("No se genero evento de auditoria.");
        }

        System.out.println("Promedios indexados (HashMap): " + detector.obtenerPromediosIndexados());
        System.out.println("Total eventos detectados (Lista): " + detector.obtenerEventosDetectados().size());
    }
}
