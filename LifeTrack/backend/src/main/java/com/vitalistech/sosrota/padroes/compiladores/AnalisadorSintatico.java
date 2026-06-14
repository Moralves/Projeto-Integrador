package com.vitalistech.sosrota.padroes.compiladores;

import java.util.List;

/**
 * Analisador Sintático (Parser) usando a técnica de Descida Recursiva Preditiva.
 * 
 * Gramática Livre de Contexto (GLC):
 * Expressao -> Condicao ExpressaoLinha
 * ExpressaoLinha -> AND Condicao ExpressaoLinha | OR Condicao ExpressaoLinha | vazio
 * Condicao -> IDENTIFICADOR IGUAL STRING
 */
public class AnalisadorSintatico {

    private final List<Token> tokens;
    private int posicaoAtual = 0;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void analisar() {
        expressao();
        if (tokenAtual().getTipo() != TipoToken.EOF) {
            throw new ErroCompilacaoException("Erro sintático: Tokens remanescentes após fim da expressão válida.");
        }
    }

    private Token tokenAtual() {
        if (posicaoAtual < tokens.size()) {
            return tokens.get(posicaoAtual);
        }
        return tokens.get(tokens.size() - 1); // Retorna EOF
    }

    private void consumir(TipoToken tipoEsperado) {
        if (tokenAtual().getTipo() == tipoEsperado) {
            posicaoAtual++;
        } else {
            throw new ErroCompilacaoException("Erro sintático: Esperado " + tipoEsperado + ", mas encontrado " + tokenAtual().getTipo());
        }
    }

    private void expressao() {
        condicao();
        expressaoLinha();
    }

    private void expressaoLinha() {
        if (tokenAtual().getTipo() == TipoToken.AND) {
            consumir(TipoToken.AND);
            condicao();
            expressaoLinha();
        } else if (tokenAtual().getTipo() == TipoToken.OR) {
            consumir(TipoToken.OR);
            condicao();
            expressaoLinha();
        }
        // Vazio (epsilon)
    }

    private void condicao() {
        consumir(TipoToken.IDENTIFICADOR);
        consumir(TipoToken.IGUAL);
        consumir(TipoToken.STRING);
    }
}
