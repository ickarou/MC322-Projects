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
 */
public class ParserDeFormula {
    private String texto;
    private int posicao;

    public Expressao interpretar(String formula) throws FormulaInvalidaException {
        if (formula == null || formula.isBlank()) {
            throw new FormulaInvalidaException("A fórmula está vazia.");
        }
        this.texto = formula;
        this.posicao = 0;
        Expressao e = lerSoma();
        pularEspacos();
        if (posicao < texto.length()) {
            throw erro("caractere inesperado '" + texto.charAt(posicao) + "'");
        }
        return e;
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
            e = new OperacaoBinaria(c, e, lerFator());
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
