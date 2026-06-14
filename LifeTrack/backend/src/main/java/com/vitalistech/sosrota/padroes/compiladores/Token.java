package com.vitalistech.sosrota.padroes.compiladores;

public class Token {
    private TipoToken tipo;
    private String valor;

    public Token(TipoToken tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "Token{" + "tipo=" + tipo + ", valor='" + valor + '\'' + '}';
    }
}
