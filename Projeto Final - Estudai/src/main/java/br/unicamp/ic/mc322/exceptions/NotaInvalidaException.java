package br.unicamp.ic.mc322.exceptions;

/** Nota, peso ou bônus fora dos limites (F14, F24, F25, F26, F29). */
public class NotaInvalidaException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public NotaInvalidaException(String msg) {
        super(msg);
    }
}
