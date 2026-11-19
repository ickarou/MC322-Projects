package br.unicamp.ic.mc322.medias;

import estudai.atividades.Avaliavel;

/**
 * Avaliação "de mentira", só para cálculo: usada para simular notas (F30)
 * e para pôr a nota da substitutiva no lugar da prova substituída. Imutável.
 */
public final class NotaSimulada implements Avaliavel {
    private final String sigla;
    private final double peso;
    private final Double nota;

    public NotaSimulada(String sigla, double peso, Double nota) {
        this.sigla = sigla;
        this.peso = peso;
        this.nota = nota;
    }

    @Override
    public String getSigla() {
        return sigla;
    }

    @Override
    public Double getNota() {
        return nota;
    }

    @Override
    public void lancarNota(double nota) {
        throw new UnsupportedOperationException("Nota simulada não pode ser alterada.");
    }

    @Override
    public void removerNota() {
        throw new UnsupportedOperationException("Nota simulada não pode ser alterada.");
    }

    @Override
    public double getPeso() {
        return peso;
    }

    @Override
    public void setPeso(double peso) {
        throw new UnsupportedOperationException("Nota simulada não pode ser alterada.");
    }

    @Override
    public boolean possuiNotaLancada() {
        return nota != null;
    }
}

