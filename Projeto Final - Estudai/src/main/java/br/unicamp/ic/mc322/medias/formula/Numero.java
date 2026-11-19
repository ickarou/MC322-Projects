package br.unicamp.ic.mc322.medias.formula;

import java.util.Map;
import java.util.Set;

/** Folha: um número fixo, como 2 ou 0.5. */
public class Numero extends Expressao {
    private final double valor;

    public Numero(double valor) {
        this.valor = valor;
    }

    @Override
    public double avaliar(Map<String, Double> valores) {
        return valor;
    }

    @Override
    public Set<String> getVariaveis() {
        return Set.of();
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
