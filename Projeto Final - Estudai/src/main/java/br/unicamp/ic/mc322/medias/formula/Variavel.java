package br.unicamp.ic.mc322.medias.formula;

import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Folha: a sigla de uma avaliação, como P1 ou T2. */
public class Variavel extends Expressao {
    private final String sigla;

    public Variavel(String sigla) {
        this.sigla = sigla.toUpperCase(Locale.ROOT);
    }

    @Override
    public double avaliar(Map<String, Double> valores) throws FormulaInvalidaException {
        Double v = valores.get(sigla);
        if (v == null) {
            throw new FormulaInvalidaException("A sigla " + sigla + " não existe nesta disciplina.");
        }
        return v;
    }

    @Override
    public Set<String> getVariaveis() {
        return Set.of(sigla);
    }

    public String getSigla() {
        return sigla;
    }

    @Override
    public String toString() {
        return sigla;
    }
}
