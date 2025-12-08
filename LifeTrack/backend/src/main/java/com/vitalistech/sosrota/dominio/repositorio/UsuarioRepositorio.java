package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);
    
    /**
     * Verifica se existe usuário com o telefone especificado.
     * @param telefone Número de telefone
     * @return Optional com o usuário se encontrado
     */
    Optional<Usuario> findByTelefone(String telefone);
    
    /**
     * Verifica se existe usuário com o telefone especificado, excluindo um ID específico.
     * Útil para validação ao editar.
     * @param telefone Número de telefone
     * @param id ID do usuário a excluir da busca
     * @return Optional com o usuário se encontrado
     */
    Optional<Usuario> findByTelefoneAndIdNot(String telefone, Long id);
}
