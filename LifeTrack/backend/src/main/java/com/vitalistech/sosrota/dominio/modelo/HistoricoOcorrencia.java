package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Representa um registro no histórico de ações realizadas em ocorrências.
 * Armazena informações completas para auditoria e rastreabilidade.
 */
@Entity
@Table(name = "historico_ocorrencias")
public class HistoricoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ocorrencia", nullable = false)
    private Ocorrencia ocorrencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AcaoHistorico acao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusOcorrencia statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusOcorrencia statusNovo;

    @Column(name = "descricao_acao", columnDefinition = "TEXT")
    private String descricaoAcao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    // Snapshot das informações da ocorrência no momento da ação
    @Column(name = "tipo_ocorrencia", length = 255)
    private String tipoOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gravidade gravidade;

    @Column(name = "bairro_origem_nome", length = 255)
    private String bairroOrigemNome;

    @Column(length = 1000)
    private String observacoes;

    // Snapshot das informações do usuário no momento da ação
    @Column(name = "usuario_nome", length = 255)
    private String usuarioNome;

    @Column(name = "usuario_login", length = 100)
    private String usuarioLogin;

    @Column(name = "usuario_perfil", length = 50)
    private String usuarioPerfil;

    // Informações da ambulância no momento da ação
    @Column(name = "placa_ambulancia", length = 20)
    private String placaAmbulancia;

    @Column(name = "acao_ambulancia", length = 100)
    private String acaoAmbulancia; // Ex: "Indo até o local", "Retornando para base"

    public HistoricoOcorrencia() {
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public AcaoHistorico getAcao() {
        return acao;
    }

    public void setAcao(AcaoHistorico acao) {
        this.acao = acao;
    }

    public StatusOcorrencia getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusOcorrencia statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusOcorrencia getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusOcorrencia statusNovo) {
        this.statusNovo = statusNovo;
    }

    public String getDescricaoAcao() {
        return descricaoAcao;
    }

    public void setDescricaoAcao(String descricaoAcao) {
        this.descricaoAcao = descricaoAcao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(String tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public Gravidade getGravidade() {
        return gravidade;
    }

    public void setGravidade(Gravidade gravidade) {
        this.gravidade = gravidade;
    }

    public String getBairroOrigemNome() {
        return bairroOrigemNome;
    }

    public void setBairroOrigemNome(String bairroOrigemNome) {
        this.bairroOrigemNome = bairroOrigemNome;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }

    public void setUsuarioLogin(String usuarioLogin) {
        this.usuarioLogin = usuarioLogin;
    }

    public String getUsuarioPerfil() {
        return usuarioPerfil;
    }

    public void setUsuarioPerfil(String usuarioPerfil) {
        this.usuarioPerfil = usuarioPerfil;
    }

    public String getPlacaAmbulancia() {
        return placaAmbulancia;
    }

    public void setPlacaAmbulancia(String placaAmbulancia) {
        this.placaAmbulancia = placaAmbulancia;
    }

    public String getAcaoAmbulancia() {
        return acaoAmbulancia;
    }

    public void setAcaoAmbulancia(String acaoAmbulancia) {
        this.acaoAmbulancia = acaoAmbulancia;
    }
}

