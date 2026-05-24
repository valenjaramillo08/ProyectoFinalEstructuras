package com.example.proyecto_final.infrastructure.repository;

import com.example.proyecto_final.domain.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
/**
 * Repositorio MongoDB para usuarios.
 */

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    /**
     * Busca un usuario por su email normalizado.
     *
     * @param email correo electronico.
     * @return usuario encontrado, si existe.
     */
    Optional<Usuario> findByEmail(String email);
}
