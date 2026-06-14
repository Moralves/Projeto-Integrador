package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
import com.vitalistech.sosrota.web.dto.RelatorioOcorrenciaDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints para relatórios administrativos.
 */
@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioControlador {

    private final com.vitalistech.sosrota.padroes.template.GeradorRelatorioOcorrencia geradorRelatorioOcorrencia;

    public RelatorioControlador(com.vitalistech.sosrota.padroes.template.GeradorRelatorioOcorrencia geradorRelatorioOcorrencia) {
        this.geradorRelatorioOcorrencia = geradorRelatorioOcorrencia;
    }

    @GetMapping("/ocorrencias")
    public List<RelatorioOcorrenciaDTO> relatorioOcorrencias() {
        // Padrão Template Method: Delega a execução para o gerador que implementa o template
        return geradorRelatorioOcorrencia.gerarRelatorio();
    }

    /**
     * Endpoint da Consulta Avançada utilizando o Analisador Léxico e Sintático
     * Requisito: Linguagens Formais e Compiladores
     * Exemplo query: parametro.tipo="BASICA" AND parametro.status="DISPONIVEL"
     */
    @GetMapping("/consulta-avancada")
    public org.springframework.http.ResponseEntity<?> consultaAvancada(@RequestParam String query) {
        try {
            // 1. Análise Léxica (Scanner)
            com.vitalistech.sosrota.padroes.compiladores.AnalisadorLexico lexer = 
                new com.vitalistech.sosrota.padroes.compiladores.AnalisadorLexico(query);
            List<com.vitalistech.sosrota.padroes.compiladores.Token> tokens = lexer.analisar();

            // 2. Análise Sintática (Parser)
            com.vitalistech.sosrota.padroes.compiladores.AnalisadorSintatico parser = 
                new com.vitalistech.sosrota.padroes.compiladores.AnalisadorSintatico(tokens);
            parser.analisar();

            // Se chegou aqui, a query é gramaticalmente válida
            // (A execução semântica não é necessária pelo escopo estrito da disciplina, mas a sintática passou)
            return org.springframework.http.ResponseEntity.ok("Consulta analisada e compilada com sucesso! Tokens: " + tokens.size());

        } catch (com.vitalistech.sosrota.padroes.compiladores.ErroCompilacaoException e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Falha de Compilação: " + e.getMessage());
        }
    }
}

