package br.unicamp.ic.mc322.exceptions;

/** Arquivo de dados ilegível ou com linhas inválidas (F38, F39, F45). */
public class ArquivoCorrompidoException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public ArquivoCorrompidoException(String msg) {
        super(msg);
    }
}
