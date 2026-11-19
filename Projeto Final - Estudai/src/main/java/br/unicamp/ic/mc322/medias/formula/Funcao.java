package br.unicamp.ic.mc322.medias.formula;

import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Nó composto: função com um ou mais argumentos. Disponíveis: min, max e media. */
public class Funcao extends Expressao {
    public static final Set<String> NOMES = Set.of("min", "max", "media");

    private final String nome;
    private final List<Expressao> argumentos;

    public Funcao(String nome, List<Expressao> args) throws FormulaInvalidaException {
        String n = nome.toLowerCase(Locale.ROOT);
        if (!NOMES.contains(n)) {
            throw new FormulaInvalidaException("Função desconhecida: " + nome + ". Use min, max ou media.");
        }
        if (args == null || args.isEmpty()) {
            throw new FormulaInvalidaException("A função " + n + " precisa de pelo menos um argumento.");
        }
        this.nome = n;
        this.argumentos = new ArrayList<>(args);
    }

    @Override
    public double avaliar(Map<String, Double> valores) throws FormulaInvalidaException {
        double primeiro = argumentos.get(0).avaliar(valores);
        double resultado = primeiro;
        double soma = primeiro;
        for (int i = 1; i < argumentos.size(); i++) {
            double v = argumentos.get(i).avaliar(valores);
            soma += v;
            if (nome.equals("min")) {
                resultado = Math.min(resultado, v);
            } else if (nome.equals("max")) {
                resultado = Math.max(resultado, v);
            }
        }
        return nome.equals("media") ? soma / argumentos.size() : resultado;
    }

    @Override
    public Set<String> getVariaveis() {
        Set<String> vars = new HashSet<>();
        for (Expressao e : argumentos) {
            vars.addAll(e.getVariaveis());
        }
        return vars;
    }

    @Override
    public String toString() {
        return nome + argumentos;
    }
}
