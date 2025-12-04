package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.AmbulanciaRepositorio;
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

    public EquipeServico(EquipeRepositorio equipeRepositorio,
                         ProfissionalRepositorio profissionalRepositorio,
                         AmbulanciaRepositorio ambulanciaRepositorio) {
        this.equipeRepositorio = equipeRepositorio;
        this.profissionalRepositorio = profissionalRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
    }

    @Transactional
    public Equipe criarEquipe(Long idAmbulancia, String descricao, List<Long> idsProfissionais) {

        Ambulancia ambulancia = ambulanciaRepositorio.findById(idAmbulancia)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));

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
}
