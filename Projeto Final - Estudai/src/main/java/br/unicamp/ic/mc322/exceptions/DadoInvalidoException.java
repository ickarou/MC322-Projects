package br.unicamp.ic.mc322.exceptions;

/** Dado de cadastro inválido (F6, F7, F10, F19, F32, F41). */
public class DadoInvalidoException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public DadoInvalidoException(String msg) {
        super(msg);
    }
}
