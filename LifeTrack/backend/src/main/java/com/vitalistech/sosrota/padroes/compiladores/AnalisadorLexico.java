package com.vitalistech.sosrota.padroes.compiladores;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador Léxico (Scanner)
 * Transforma uma String bruta de consulta em uma lista de Tokens.
 */
public class AnalisadorLexico {

    private final String entrada;
    private int posicao = 0;

    public AnalisadorLexico(String entrada) {
        this.entrada = entrada;
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

        while (posicao < entrada.length()) {
            char atual = entrada.charAt(posicao);

            if (Character.isWhitespace(atual)) {
                posicao++;
            } else if (atual == '=') {
                tokens.add(new Token(TipoToken.IGUAL, "="));
                posicao++;
            } else if (atual == '"') {
                tokens.add(lerString());
            } else if (Character.isLetter(atual)) {
                tokens.add(lerIdentificadorOuPalavraReservada());
            } else {
                throw new ErroCompilacaoException("Erro léxico: Caractere inesperado '" + atual + "' na posição " + posicao);
            }
        }

        tokens.add(new Token(TipoToken.EOF, ""));
        return tokens;
    }

    private Token lerString() {
        StringBuilder sb = new StringBuilder();
        posicao++; // Pula aspas iniciais
        while (posicao < entrada.length() && entrada.charAt(posicao) != '"') {
            sb.append(entrada.charAt(posicao));
            posicao++;
        }
        if (posicao >= entrada.length()) {
            throw new ErroCompilacaoException("Erro léxico: String literal não fechada com aspas duplas.");
        }
        posicao++; // Pula aspas finais
        return new Token(TipoToken.STRING, sb.toString());
    }

    private Token lerIdentificadorOuPalavraReservada() {
        StringBuilder sb = new StringBuilder();
        while (posicao < entrada.length() && (Character.isLetterOrDigit(entrada.charAt(posicao)) || entrada.charAt(posicao) == '.')) {
            sb.append(entrada.charAt(posicao));
            posicao++;
        }
        String lexema = sb.toString();
        if (lexema.equalsIgnoreCase("AND")) {
            return new Token(TipoToken.AND, lexema);
        } else if (lexema.equalsIgnoreCase("OR")) {
            return new Token(TipoToken.OR, lexema);
        } else {
            return new Token(TipoToken.IDENTIFICADOR, lexema);
        }
    }
}
