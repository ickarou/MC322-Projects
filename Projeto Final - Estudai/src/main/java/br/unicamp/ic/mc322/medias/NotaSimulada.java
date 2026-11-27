package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;

/**
 * Avaliação "de mentira", só para cálculo: usada para simular notas (F30)
 * e para pôr a nota da substitutiva no lugar da prova substituída. Imutável.
 */
public record NotaSimulada(String sigla, double peso, Double nota) implements Avaliavel {

    @Override
    public String getSigla() {
        return sigla;
    }

    @Override
    public Double getNota() {
        return nota;
    }

    @Override
    public double getPeso() {
        return peso;
    }
}
