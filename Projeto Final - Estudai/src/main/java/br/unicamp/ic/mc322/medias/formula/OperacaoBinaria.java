package br.unicamp.ic.mc322.medias.formula;

import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Nó composto: uma operação (+, -, *, /) entre duas expressões filhas. */
public class OperacaoBinaria extends Expressao {
    private final char operador;
    private final Expressao esquerda;
    private final Expressao direita;

    public OperacaoBinaria(char op, Expressao esq, Expressao dir) {
        if ("+-*/".indexOf(op) < 0) {
            throw new IllegalArgumentException("Operador desconhecido: " + op);
        }
        this.operador = op;
        this.esquerda = esq;
        this.direita = dir;
    }

    @Override
    public double avaliar(Map<String, Double> valores) throws FormulaInvalidaException {
        double a = esquerda.avaliar(valores);
        double b = direita.avaliar(valores);
        switch (operador) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            default:
                if (b == 0) {
                    // 0/0 vale 0: em fórmulas como 2*P*T/(P+T), as notas ainda não lançadas valem 0
                    // e a média deve dar 0, não erro. Já x/0 com x diferente de 0 continua sendo erro.
                    if (a == 0) {
                        return 0;
                    }
                    throw new FormulaInvalidaException("A fórmula tem uma divisão por zero.");
                }
                return a / b;
        }
    }

    @Override
    public Set<String> getVariaveis() {
        Set<String> vars = new HashSet<>(esquerda.getVariaveis());
        vars.addAll(direita.getVariaveis());
        return vars;
    }

    @Override
    public String toString() {
        return "(" + esquerda + " " + operador + " " + direita + ")";
    }
}
