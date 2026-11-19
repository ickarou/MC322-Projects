package br.unicamp.ic.mc322.exceptions;

/** Arquivo que não é PDF válido ou que sumiu do disco (F47, F49, F50). */
public class ArquivoPdfInvalidoException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public ArquivoPdfInvalidoException(String msg) {
        super(msg);
    }
}
