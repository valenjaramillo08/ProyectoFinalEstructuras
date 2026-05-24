package com.example.proyecto_final.infrastructure.repository;

import com.example.proyecto_final.domain.model.EventoAuditoria;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
/**
 * Repositorio MongoDB para eventos de auditoria.
 */

public interface EventoAuditoriaRepository extends MongoRepository<EventoAuditoria, String> {
    /**
     * Lista eventos de auditoria por usuario.
     *
     * @param usuarioId identificador del usuario.
     * @return eventos ordenados cronologicamente.
     */
    List<EventoAuditoria> findByUsuarioIdOrderByFechaAsc(String usuarioId);
}
