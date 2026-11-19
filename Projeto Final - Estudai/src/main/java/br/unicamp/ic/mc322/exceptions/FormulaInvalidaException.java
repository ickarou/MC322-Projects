package br.unicamp.ic.mc322.exceptions;

/** Fórmula de média com erro de sintaxe ou sigla inexistente (F27, F28). */
public class FormulaInvalidaException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public FormulaInvalidaException(String msg) {
        super(msg);
    }
}
