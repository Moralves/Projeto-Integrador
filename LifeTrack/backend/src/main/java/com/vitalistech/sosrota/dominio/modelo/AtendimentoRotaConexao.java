package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

/**
 * Representa uma conexão de rua utilizada no caminho calculado pelo Dijkstra
 * para um atendimento específico. Armazena a sequência do caminho.
 */
@Entity
@Table(name = "atendimento_rota_conexao")
public class AtendimentoRotaConexao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_atendimento")
    private Atendimento atendimento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rua_conexao")
    private RuaConexao ruaConexao;

    /**
     * Ordem sequencial da conexão no caminho (1 = primeira conexão, 2 = segunda, etc.)
     */
    @Column(nullable = false)
    private Integer ordem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(Atendimento atendimento) {
        this.atendimento = atendimento;
    }

    public RuaConexao getRuaConexao() {
        return ruaConexao;
    }

    public void setRuaConexao(RuaConexao ruaConexao) {
        this.ruaConexao = ruaConexao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}







