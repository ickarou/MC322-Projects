package br.unicamp.ic.mc322.medias.formula;

import estudai.excecoes.FormulaInvalidaException;

import java.util.Map;
import java.util.Set;

/**
 * Nó da árvore de uma fórmula (padrões Interpreter + Composite).
 * Folhas: Numero e Variavel. Nós compostos: OperacaoBinaria e Funcao.
 */
public abstract class Expressao {

    /** Calcula o valor usando as notas de cada sigla. */
    public abstract double avaliar(Map<String, Double> valores) throws FormulaInvalidaException;

    /** Siglas usadas nesta expressão e nas filhas. */
    public abstract Set<String> getVariaveis();
}