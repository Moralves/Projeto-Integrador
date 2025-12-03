package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.*;
import com.vitalistech.sosrota.util.AlgoritmoDijkstra;
import com.vitalistech.sosrota.util.ResultadoRota;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável por registrar ocorrências e despachar ambulâncias.
 */
@Service
public class OcorrenciaServico {

    private final OcorrenciaRepositorio ocorrenciaRepositorio;
    private final AmbulanciaRepositorio ambulanciaRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;
    private final RuaConexaoRepositorio ruaConexaoRepositorio;
    private final EquipeRepositorio equipeRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public OcorrenciaServico(OcorrenciaRepositorio ocorrenciaRepositorio,
                             AmbulanciaRepositorio ambulanciaRepositorio,
                             AtendimentoRepositorio atendimentoRepositorio,
                             RuaConexaoRepositorio ruaConexaoRepositorio,
                             EquipeRepositorio equipeRepositorio,
                             UsuarioRepositorio usuarioRepositorio) {
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
        this.ruaConexaoRepositorio = ruaConexaoRepositorio;
        this.equipeRepositorio = equipeRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Ocorrencia registrarOcorrencia(Bairro bairroLocal,
                                          String tipoOcorrencia,
                                          Gravidade gravidade,
                                          String observacoes,
                                          Usuario usuarioRegistro) {

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setBairroLocal(bairroLocal);
        ocorrencia.setTipoOcorrencia(tipoOcorrencia);
        ocorrencia.setGravidade(gravidade);
        ocorrencia.setObservacoes(observacoes);
        ocorrencia.setDataHoraAbertura(LocalDateTime.now());
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.ABERTA);
        ocorrencia.setUsuarioRegistro(usuarioRegistro);

        return ocorrenciaRepositorio.save(ocorrencia);
    }

    /**
     * Despacha uma ambulância para a ocorrência considerando regras de SLA e equipe.
     */
    @Transactional
    public Atendimento despacharOcorrencia(Long idOcorrencia, Usuario usuarioDespacho) {

        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {
            throw new IllegalStateException("Somente ocorrências ABERTAS podem ser despachadas");
        }

        TipoAmbulancia tipoNecessario;
        int slaMinutos;

        if (ocorrencia.getGravidade() == Gravidade.ALTA) {
            tipoNecessario = TipoAmbulancia.UTI;
            slaMinutos = 8;
        } else if (ocorrencia.getGravidade() == Gravidade.MEDIA) {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 15;
        } else {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 30;
        }

        List<Ambulancia> ambulanciasDisponiveis =
                ambulanciaRepositorio.findByStatusAndAtivaTrue(StatusAmbulancia.DISPONIVEL);

        Ambulancia melhorAmbulancia = null;
        double menorDistancia = Double.POSITIVE_INFINITY;

        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        for (Ambulancia a : ambulanciasDisponiveis) {

            if (!tipoCompativel(a.getTipo(), tipoNecessario)) {
                continue;
            }

            if (!ambulanciaPossuiEquipeCompleta(a)) {
                continue;
            }

            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
                    a.getBairroBase(),
                    ocorrencia.getBairroLocal(),
                    todasConexoes
            );

            double distKm = rota.getDistanciaKm();
            if (Double.isInfinite(distKm)) continue;

            if (distKm <= slaMinutos && distKm < menorDistancia) {
                menorDistancia = distKm;
                melhorAmbulancia = a;
            }
        }

        if (melhorAmbulancia == null) {
            throw new IllegalStateException("Nenhuma ambulância apta encontrada dentro do SLA.");
        }

        melhorAmbulancia.setStatus(StatusAmbulancia.EM_ATENDIMENTO);
        ambulanciaRepositorio.save(melhorAmbulancia);

        ocorrencia.setStatusOcorrencia(StatusOcorrencia.DESPACHADA);
        ocorrenciaRepositorio.save(ocorrencia);

        Atendimento atendimento = new Atendimento();
        atendimento.setOcorrencia(ocorrencia);
        atendimento.setAmbulancia(melhorAmbulancia);
        atendimento.setDataHoraDespacho(LocalDateTime.now());
        atendimento.setDistanciaKm(menorDistancia);
        atendimento.setUsuarioDespacho(usuarioDespacho);

        return atendimentoRepositorio.save(atendimento);
    }

    private boolean tipoCompativel(TipoAmbulancia tipoAmbulancia, TipoAmbulancia tipoNecessario) {
        if (tipoNecessario == TipoAmbulancia.UTI) {
            return tipoAmbulancia == TipoAmbulancia.UTI;
        }
        return tipoAmbulancia == TipoAmbulancia.BASICA || tipoAmbulancia == TipoAmbulancia.UTI;
    }

    private boolean ambulanciaPossuiEquipeCompleta(Ambulancia ambulancia) {
        Equipe equipe = equipeRepositorio.findByAmbulanciaAndAtivaTrue(ambulancia).orElse(null);
        if (equipe == null) return false;

        boolean condutor = false;
        boolean enfermeiro = false;
        boolean medico = false;

        for (EquipeProfissional ep : equipe.getProfissionais()) {
            FuncaoProfissional f = ep.getProfissional().getFuncao();
            if (f == FuncaoProfissional.CONDUTOR) condutor = true;
            if (f == FuncaoProfissional.ENFERMEIRO) enfermeiro = true;
            if (f == FuncaoProfissional.MEDICO) medico = true;
        }

        if (ambulancia.getTipo() == TipoAmbulancia.UTI) {
            return condutor && enfermeiro && medico;
        } else {
            return condutor && enfermeiro;
        }
    }
}
