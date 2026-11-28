package br.unicamp.ic.mc322.medias.formula;

import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforma o texto da fórmula numa árvore de Expressao (parser descendente recursivo).
 *
 * Gramática:
 *   soma    := produto (('+' | '-') produto)*
 *   produto := fator (('*' | '/') fator)*
 *   fator   := numero | sigla | funcao '(' soma (',' soma)* ')' | '(' soma ')' | '-' fator
 *
 * Casas decimais usam ponto (0.4); a vírgula separa os argumentos das funções.
 */
public class ParserDeFormula {
    private String texto;
    private int posicao;

    public Expressao interpretar(String formula) throws FormulaInvalidaException {
        if (formula == null || formula.isBlank()) {
            throw new FormulaInvalidaException("A fórmula está vazia.");
        }
        if (temVirgulaDecimal(formula)) {
            throw new FormulaInvalidaException("Use ponto nas casas decimais (ex.: 0.4 em vez de 0,4). "
                    + "A vírgula só separa os argumentos das funções; se for isso, ponha um espaço depois dela.");
        }
        this.texto = formula;
        this.posicao = 0;
        Expressao e = lerSoma();
        pularEspacos();
        if (posicao < texto.length()) {
            if (atual() == ',') {
                throw erro("vírgula fora de uma função (para casas decimais, use ponto: 0.4)");
            }
            throw erro("caractere inesperado '" + texto.charAt(posicao) + "'");
        }
        return e;
    }

    /**
     * Procura um dígito, uma vírgula e outro dígito colados, como em "0,4": quase sempre é uma
     * casa decimal escrita com vírgula. Em "max(P1, 2)" não acusa, porque depois da vírgula vem espaço.
     */
    private static boolean temVirgulaDecimal(String s) {
        for (int i = 1; i < s.length() - 1; i++) {
            if (s.charAt(i) == ','
                    && Character.isDigit(s.charAt(i - 1))
                    && Character.isDigit(s.charAt(i + 1))) {
                return true;
            }
        }
        return false;
    }

    private Expressao lerSoma() throws FormulaInvalidaException {
        Expressao e = lerProduto();
        while (true) {
            pularEspacos();
            char c = atual();
            if (c != '+' && c != '-') {
                return e;
            }
            posicao++;
            e = new OperacaoBinaria(c, e, lerProduto());
        }
    }

    private Expressao lerProduto() throws FormulaInvalidaException {
        Expressao e = lerFator();
        while (true) {
            pularEspacos();
            char c = atual();
            if (c != '*' && c != '/') {
                return e;
            }
            posicao++;
            int inicioDivisor = posicao;
            Expressao direita = lerFator();
            if (c == '/' && direita instanceof Numero n && n.getValor() == 0) {
                posicao = inicioDivisor;
                throw erro("divisão por zero");
            }
            e = new OperacaoBinaria(c, e, direita);
        }
    }

    private Expressao lerFator() throws FormulaInvalidaException {
        pularEspacos();
        if (posicao >= texto.length()) {
            throw erro("a fórmula terminou antes da hora");
        }
        char c = atual();
        if (c == '(') {
            posicao++;
            Expressao e = lerSoma();
            esperar(')');
            return e;
        }
        if (c == '-') {
            posicao++;
            return new OperacaoBinaria('-', new Numero(0), lerFator());
        }
        if (c == '+') {
            posicao++;
            return lerFator();
        }
        if (Character.isDigit(c) || c == '.') {
            return lerNumero();
        }
        if (Character.isLetter(c)) {
            String nome = lerIdentificador();
            pularEspacos();
            if (atual() == '(') {
                posicao++;
                List<Expressao> args = new ArrayList<>();
                args.add(lerSoma());
                pularEspacos();
                while (atual() == ',') {
                    posicao++;
                    args.add(lerSoma());
                    pularEspacos();
                }
                esperar(')');
                return new Funcao(nome, args);
            }
            return new Variavel(nome);
        }
        throw erro("caractere inesperado '" + c + "'");
    }

    private Expressao lerNumero() throws FormulaInvalidaException {
        int inicio = posicao;
        while (posicao < texto.length() && (Character.isDigit(atual()) || atual() == '.')) {
            posicao++;
        }
        String s = texto.substring(inicio, posicao);
        try {
            return new Numero(Double.parseDouble(s));
        } catch (NumberFormatException ex) {
            throw erro("número inválido '" + s + "'");
        }
    }

    private String lerIdentificador() {
        int inicio = posicao;
        while (posicao < texto.length() && (Character.isLetterOrDigit(atual()) || atual() == '_')) {
            posicao++;
        }
        return texto.substring(inicio, posicao);
    }

    private void esperar(char c) throws FormulaInvalidaException {
        pularEspacos();
        if (atual() != c) {
            throw erro("faltou '" + c + "'");
        }
        posicao++;
    }

    private void pularEspacos() {
        while (posicao < texto.length() && Character.isWhitespace(texto.charAt(posicao))) {
            posicao++;
        }
    }

    private char atual() {
        return posicao < texto.length() ? texto.charAt(posicao) : '\0';
    }

    private FormulaInvalidaException erro(String motivo) {
        return new FormulaInvalidaException("Erro na fórmula (posição " + (posicao + 1) + "): " + motivo + ".");
    }
}
