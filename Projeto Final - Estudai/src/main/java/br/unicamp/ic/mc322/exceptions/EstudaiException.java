package br.unicamp.ic.mc322.exceptions;

/**
 * Base de todas as exceções próprias do Estudaí.
 * Permite que a interface trate qualquer erro do sistema com um único catch.
 */
public abstract class EstudaiException extends Exception {
    private static final long serialVersionUID = 1L;

    public EstudaiException(String msg) {
        super(msg);
    }
}

