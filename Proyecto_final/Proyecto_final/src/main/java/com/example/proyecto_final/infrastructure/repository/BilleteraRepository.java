package com.example.proyecto_final.infrastructure.repository;

import com.example.proyecto_final.domain.model.Billetera;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
/**
 * Repositorio MongoDB para billeteras.
 */

public interface BilleteraRepository extends MongoRepository<Billetera, String> {
    /**
     * Lista billeteras por propietario.
     *
     * @param usuarioId identificador del usuario.
     * @return billeteras asociadas.
     */
    List<Billetera> findByUsuarioId(String usuarioId);
}
