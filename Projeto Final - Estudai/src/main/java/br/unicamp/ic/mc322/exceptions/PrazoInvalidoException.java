package br.unicamp.ic.mc322.exceptions;

/** Prazo ou data inválida (F1, F2, F9, F14, F15). */
public class PrazoInvalidoException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public PrazoInvalidoException(String msg) {
        super(msg);
    }
}
