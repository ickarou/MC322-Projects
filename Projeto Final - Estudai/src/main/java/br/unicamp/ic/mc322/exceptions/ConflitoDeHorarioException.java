package br.unicamp.ic.mc322.exceptions;

/** Horário de aula sobreposto a outro do mesmo semestre (F10, F11). */
public class ConflitoDeHorarioException extends EstudaiException {
    private static final long serialVersionUID = 1L;

    public ConflitoDeHorarioException(String msg) {
        super(msg);
    }
}
