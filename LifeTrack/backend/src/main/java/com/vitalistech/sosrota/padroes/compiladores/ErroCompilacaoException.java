package com.vitalistech.sosrota.padroes.compiladores;

public class ErroCompilacaoException extends RuntimeException {
    public ErroCompilacaoException(String mensagem) {
        super(mensagem);
    }
}
