package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;

/** O que recebe nota e peso e entra no cálculo da média (provas e trabalhos). */
public interface Avaliavel {
    String getSigla();

    /** Nota de 0 a 10, ou null se ainda não foi lançada. */
    Double getNota();

    void lancarNota(double nota) throws NotaInvalidaException;

    void removerNota();

    double getPeso();

    void setPeso(double peso) throws NotaInvalidaException;

    boolean possuiNotaLancada();
}

