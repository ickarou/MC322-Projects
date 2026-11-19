package br.unicamp.ic.mc322.exceptions;

/** Atividade inexistente na disciplina (F15 a F18). */
public class AtividadeNaoEncontradaException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public AtividadeNaoEncontradaException(String msg) {
        super(msg);
    }
}
