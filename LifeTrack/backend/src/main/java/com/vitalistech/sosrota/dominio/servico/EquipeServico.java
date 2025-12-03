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

        for (Long idProf : idsProfissionais) {
            Profissional p = profissionalRepositorio.findById(idProf)
                    .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

            if (equipeRepositorio.profissionalEmEquipeAtiva(idProf) > 0) {
                throw new IllegalStateException("Profissional " + p.getNome() + " já está em uma equipe ativa.");
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
        }

        return equipeRepositorio.save(equipe);
    }
}
