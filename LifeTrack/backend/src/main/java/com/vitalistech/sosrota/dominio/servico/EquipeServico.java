package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.AmbulanciaRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.EquipeRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.ProfissionalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela criação e manutenção de equipes.
 */
@Service
public class EquipeServico {

    private final EquipeRepositorio equipeRepositorio;
    private final ProfissionalRepositorio profissionalRepositorio;
    private final AmbulanciaRepositorio ambulanciaRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;

    public EquipeServico(EquipeRepositorio equipeRepositorio,
                         ProfissionalRepositorio profissionalRepositorio,
                         AmbulanciaRepositorio ambulanciaRepositorio,
                         AtendimentoRepositorio atendimentoRepositorio) {
        this.equipeRepositorio = equipeRepositorio;
        this.profissionalRepositorio = profissionalRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
    }

    @Transactional
    public Equipe criarEquipe(Long idAmbulancia, String descricao, List<Long> idsProfissionais) {

        Ambulancia ambulancia = ambulanciaRepositorio.findById(idAmbulancia)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));

        // Verificar se a ambulância já possui uma equipe ativa
        if (equipeRepositorio.findEquipeAtivaPorAmbulancia(idAmbulancia).isPresent()) {
            throw new IllegalStateException("A ambulância selecionada já possui uma equipe ativa vinculada");
        }

        TipoAmbulancia tipo = ambulancia.getTipo();

        boolean temCondutor = false;
        boolean temEnfermeiro = false;
        boolean temMedico = false;

        // Validar profissionais e verificar turnos
        Turno turnoEquipe = null;
        
        for (Long idProf : idsProfissionais) {
            Profissional p = profissionalRepositorio.findById(idProf)
                    .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

            // Validar se profissional está ativo
            if (!p.isAtivo()) {
                throw new IllegalStateException("Profissional " + p.getNome() + " está inativo.");
            }

            // Validar status: não pode estar em atendimento, em folga ou inativo
            if (p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
                throw new IllegalStateException("Profissional " + p.getNome() + " está em atendimento e não pode ser adicionado a uma equipe.");
            }
            
            if (p.getStatus() == StatusProfissional.EM_FOLGA) {
                throw new IllegalStateException("Profissional " + p.getNome() + " está em folga e não pode ser adicionado a uma equipe.");
            }
            
            if (p.getStatus() == StatusProfissional.INATIVO) {
                throw new IllegalStateException("Profissional " + p.getNome() + " está inativo e não pode ser adicionado a uma equipe.");
            }

            if (equipeRepositorio.profissionalEmEquipeAtiva(idProf) > 0) {
                throw new IllegalStateException("Profissional " + p.getNome() + " já está em uma equipe ativa.");
            }

            // Validar turno: todos os profissionais devem estar no mesmo turno
            if (turnoEquipe == null) {
                turnoEquipe = p.getTurno();
            } else if (turnoEquipe != p.getTurno()) {
                throw new IllegalStateException("Todos os profissionais devem estar no mesmo turno. " + 
                        p.getNome() + " está no turno " + p.getTurno().name() + 
                        ", mas outros estão no turno " + turnoEquipe.name() + ".");
            }

            if (p.getFuncao() == FuncaoProfissional.CONDUTOR) temCondutor = true;
            if (p.getFuncao() == FuncaoProfissional.ENFERMEIRO) temEnfermeiro = true;
            if (p.getFuncao() == FuncaoProfissional.MEDICO) temMedico = true;
        }

        if (tipo == TipoAmbulancia.BASICA) {
            if (!temCondutor || !temEnfermeiro) {
                throw new IllegalStateException("Equipe de ambulância BÁSICA deve ter Condutor e Enfermeiro.");
            }
        }

        if (tipo == TipoAmbulancia.UTI) {
            if (!temCondutor || !temEnfermeiro || !temMedico) {
                throw new IllegalStateException("Equipe de ambulância UTI deve ter Condutor, Enfermeiro e Médico.");
            }
        }

        Equipe equipe = new Equipe();
        equipe.setAmbulancia(ambulancia);
        equipe.setDescricao(descricao);
        equipe.setAtiva(true);
        equipeRepositorio.save(equipe);

        // Ativar a ambulância quando uma equipe é criada
        ambulancia.setAtiva(true);
        if (ambulancia.getStatus() == com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.INATIVA) {
            ambulancia.setStatus(com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.DISPONIVEL);
        }
        ambulanciaRepositorio.save(ambulancia);

        for (Long idProf : idsProfissionais) {
            Profissional p = profissionalRepositorio.findById(idProf)
                    .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

            EquipeProfissional ep = new EquipeProfissional();
            ep.setEquipe(equipe);
            ep.setProfissional(p);
            equipe.getProfissionais().add(ep);
            
            // Marcar profissional como disponível (já está na equipe, mas ainda não em atendimento)
            // O status será alterado para EM_ATENDIMENTO quando a equipe for despachada
            if (p.getStatus() != StatusProfissional.DISPONIVEL) {
                p.setStatus(StatusProfissional.DISPONIVEL);
                profissionalRepositorio.save(p);
            }
        }

        return equipeRepositorio.save(equipe);
    }

    /**
     * Verifica se uma equipe está em atendimento.
     * Uma equipe está em atendimento se há um atendimento ativo (ocorrência DESPACHADA ou EM_ATENDIMENTO).
     */
    public boolean equipeEmAtendimento(Long idEquipe) {
        return atendimentoRepositorio.findAtendimentosAtivosPorEquipe(idEquipe).size() > 0;
    }

    /**
     * Atualiza uma equipe existente.
     * Permite alterar descrição, profissionais e ambulância (se equipe inativa).
     * Se equipe inativa receber uma ambulância, ela será reativada.
     */
    @Transactional
    public Equipe atualizarEquipe(Long idEquipe, String descricao, List<Long> idsProfissionais, Long idAmbulancia) {
        Equipe equipe = equipeRepositorio.findById(idEquipe)
                .orElseThrow(() -> new IllegalArgumentException("Equipe não encontrada"));

        // Verificar se a equipe está em atendimento (só para equipes ativas)
        if (equipe.isAtiva() && equipeEmAtendimento(idEquipe)) {
            throw new IllegalStateException("Não é possível editar uma equipe que está em atendimento");
        }
        
        // Verificar se equipe está inativa e se está recebendo uma ambulância para reativar
        boolean equipeEraInativa = !equipe.isAtiva();
        boolean seraReativada = equipeEraInativa && idAmbulancia != null;
        
        // Permitir alterar ambulância apenas se equipe estava inativa
        if (equipeEraInativa && idAmbulancia != null) {
            Ambulancia novaAmbulancia = ambulanciaRepositorio.findById(idAmbulancia)
                    .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
            
            // Verificar se a nova ambulância já tem uma equipe ativa
            if (equipeRepositorio.findEquipeAtivaPorAmbulancia(idAmbulancia).isPresent()) {
                throw new IllegalStateException("A ambulância selecionada já possui uma equipe ativa vinculada");
            }
            
            // Desvincular da ambulância antiga se houver
            Ambulancia ambulanciaAntiga = equipe.getAmbulancia();
            if (ambulanciaAntiga != null && !ambulanciaAntiga.getId().equals(idAmbulancia)) {
                // Verificar se a ambulância antiga não tem mais equipes ativas
                if (!equipeRepositorio.findEquipeAtivaPorAmbulancia(ambulanciaAntiga.getId()).isPresent()) {
                    ambulanciaAntiga.setAtiva(false);
                    ambulanciaAntiga.setStatus(com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.INATIVA);
                    ambulanciaRepositorio.save(ambulanciaAntiga);
                }
            }
            
            equipe.setAmbulancia(novaAmbulancia);
            
            // Ativar a nova ambulância
            novaAmbulancia.setAtiva(true);
            if (novaAmbulancia.getStatus() == com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.INATIVA) {
                novaAmbulancia.setStatus(com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.DISPONIVEL);
            }
            ambulanciaRepositorio.save(novaAmbulancia);
            
            // Reativar a equipe após vincular a ambulância
            equipe.setAtiva(true);
        }

        // Atualizar descrição
        if (descricao != null && !descricao.trim().isEmpty()) {
            equipe.setDescricao(descricao);
        }

        // Remover profissionais antigos que não estão na nova lista
        equipe.getProfissionais().removeIf(ep -> {
            Long idProf = ep.getProfissional().getId();
            if (!idsProfissionais.contains(idProf)) {
                // Resetar status do profissional se não estiver mais na equipe
                Profissional p = ep.getProfissional();
                if (p.getStatus() == StatusProfissional.DISPONIVEL) {
                    // Profissional pode ser removido da equipe
                }
                return true;
            }
            return false;
        });

        // Validar turno: todos os profissionais devem estar no mesmo turno
        Turno turnoEquipe = null;
        
        // Adicionar novos profissionais
        for (Long idProf : idsProfissionais) {
            Profissional p = profissionalRepositorio.findById(idProf)
                    .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

            // Verificar turno: todos os profissionais devem estar no mesmo turno
            if (turnoEquipe == null) {
                turnoEquipe = p.getTurno();
            } else if (turnoEquipe != p.getTurno()) {
                throw new IllegalStateException("Todos os profissionais devem estar no mesmo turno. " + 
                        p.getNome() + " está no turno " + p.getTurno().name() + 
                        ", mas outros estão no turno " + turnoEquipe.name() + ".");
            }

            // Verificar se o profissional já está na equipe
            boolean jaEstaNaEquipe = equipe.getProfissionais().stream()
                    .anyMatch(ep -> ep.getProfissional().getId().equals(idProf));

            if (!jaEstaNaEquipe) {
                // Validar se profissional está ativo
                if (!p.isAtivo()) {
                    throw new IllegalStateException("Profissional " + p.getNome() + " está inativo.");
                }

                // Validar status
                if (p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
                    throw new IllegalStateException("Profissional " + p.getNome() + " está em atendimento e não pode ser adicionado a uma equipe.");
                }

                if (p.getStatus() == StatusProfissional.EM_FOLGA) {
                    throw new IllegalStateException("Profissional " + p.getNome() + " está em folga e não pode ser adicionado a uma equipe.");
                }

                if (p.getStatus() == StatusProfissional.INATIVO) {
                    throw new IllegalStateException("Profissional " + p.getNome() + " está inativo e não pode ser adicionado a uma equipe.");
                }

                // Verificar se profissional já está em outra equipe ativa
                // Se a equipe será reativada, verificar antes de marcar como ativa
                // Se a equipe já está ativa, verificar normalmente
                if (seraReativada || equipe.isAtiva()) {
                    // Verificar se não é da mesma equipe
                    boolean estaNestaEquipe = equipe.getProfissionais().stream()
                            .anyMatch(ep -> ep.getProfissional().getId().equals(idProf));
                    
                    // Se não está nesta equipe, verificar se está em outra equipe ativa
                    if (!estaNestaEquipe) {
                        // Contar quantas equipes ativas têm esse profissional
                        // Se a equipe atual será reativada, precisamos contar ela também
                        long contagem = equipeRepositorio.profissionalEmEquipeAtiva(idProf);
                        if (contagem > 0) {
                            throw new IllegalStateException("Profissional " + p.getNome() + " já está em outra equipe ativa.");
                        }
                    }
                }

                EquipeProfissional ep = new EquipeProfissional();
                ep.setEquipe(equipe);
                ep.setProfissional(p);
                equipe.getProfissionais().add(ep);

                // Marcar profissional como disponível
                if (p.getStatus() != StatusProfissional.DISPONIVEL) {
                    p.setStatus(StatusProfissional.DISPONIVEL);
                    profissionalRepositorio.save(p);
                }
            }
        }

        // Validar composição da equipe após atualização (só se tiver ambulância)
        Ambulancia ambulancia = equipe.getAmbulancia();
        if (ambulancia != null) {
            TipoAmbulancia tipo = ambulancia.getTipo();
            boolean temCondutor = false;
            boolean temEnfermeiro = false;
            boolean temMedico = false;

            for (EquipeProfissional ep : equipe.getProfissionais()) {
                Profissional p = ep.getProfissional();
                if (p.getFuncao() == FuncaoProfissional.CONDUTOR) temCondutor = true;
                if (p.getFuncao() == FuncaoProfissional.ENFERMEIRO) temEnfermeiro = true;
                if (p.getFuncao() == FuncaoProfissional.MEDICO) temMedico = true;
            }

            if (tipo == TipoAmbulancia.BASICA) {
                if (!temCondutor || !temEnfermeiro) {
                    throw new IllegalStateException("Equipe de ambulância BÁSICA deve ter Condutor e Enfermeiro.");
                }
            }

            if (tipo == TipoAmbulancia.UTI) {
                if (!temCondutor || !temEnfermeiro || !temMedico) {
                    throw new IllegalStateException("Equipe de ambulância UTI deve ter Condutor, Enfermeiro e Médico.");
                }
            }
        }

        return equipeRepositorio.save(equipe);
    }

    /**
     * Remove (desativa) uma equipe vinculada a uma ambulância.
     * Atualiza o status dos profissionais para DISPONIVEL.
     */
    @Transactional
    public void removerEquipePorAmbulancia(Long idAmbulancia) {
        Equipe equipe = equipeRepositorio.findEquipeAtivaPorAmbulancia(idAmbulancia)
                .orElseThrow(() -> new IllegalStateException("Não há equipe ativa vinculada a esta ambulância"));

        // Verificar se a equipe está em atendimento
        if (equipeEmAtendimento(equipe.getId())) {
            throw new IllegalStateException("Não é possível remover uma equipe que está em atendimento. Finalize o atendimento antes.");
        }

        // Atualizar status dos profissionais para DISPONIVEL
        for (EquipeProfissional ep : equipe.getProfissionais()) {
            Profissional p = ep.getProfissional();
            if (p.getStatus() == StatusProfissional.DISPONIVEL || p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
                p.setStatus(StatusProfissional.DISPONIVEL);
                profissionalRepositorio.save(p);
            }
        }

        // Desativar a equipe (soft delete)
        equipe.setAtiva(false);
        equipeRepositorio.save(equipe);

        // Desativar a ambulância quando a equipe é removida
        Ambulancia ambulancia = equipe.getAmbulancia();
        ambulancia.setAtiva(false);
        ambulancia.setStatus(com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.INATIVA);
        ambulanciaRepositorio.save(ambulancia);
    }
}
