package br.unicamp.ic.mc322.medias;

import estudai.atividades.Avaliavel;
import estudai.excecoes.FormulaInvalidaException;
import estudai.modelo.Bonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy do cálculo da média (F27). Cada disciplina usa uma regra, que pode ser trocada a qualquer momento.
 * Avaliação sem nota vale 0. Bônus com alvo somam na nota da avaliação; sem alvo, na média. Tudo limitado a 10.
 */
public interface RegraMedia {

    double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) throws FormulaInvalidaException;

    /** Padrão: média entre a média do semestre e a nota do exame. */
    default double calcularMediaFinalComExame(double media, double exame) {
        return limitar((media + exame) / 2);
    }

    /**
     * Nota que o aluno precisa tirar em cada avaliação ainda sem nota para chegar a "min".
     * 0 = já alcançou; Double.POSITIVE_INFINITY = impossível mesmo com 10 em tudo.
     */
    double calcularNotaNecessaria(List<Avaliavel> avaliacoes, List<Bonus> bonus, double min) throws FormulaInvalidaException;

    String getDescricaoRegra();

    // ------------------------------------------------------------ utilitários comuns às regras

    static double limitar(double v) {
        return Math.max(0, Math.min(10, v));
    }

    /** Nota da avaliação (0 se não lançada) somada aos bônus que apontam para ela. */
    static double notaComBonus(Avaliavel a, List<Bonus> bonus) {
        if (!a.possuiNotaLancada()) {
            return 0;
        }
        double nota = a.getNota();
        for (Bonus b : bonus) {
            if (!b.aplicaNaNotaFinal() && b.getAlvo().getSigla().equalsIgnoreCase(a.getSigla())) {
                nota += b.getValor();
            }
        }
        return limitar(nota);
    }

    static double aplicarBonusFinal(double media, List<Bonus> bonus) {
        double total = media;
        for (Bonus b : bonus) {
            if (b.aplicaNaNotaFinal()) {
                total += b.getValor();
            }
        }
        return limitar(total);
    }

    /**
     * Busca binária que funciona para qualquer regra, inclusive a fórmula livre:
     * testa notas iguais em todas as avaliações pendentes até achar a menor que alcança "min".
     */
    static double buscarNotaNecessaria(RegraMedia regra, List<Avaliavel> avaliacoes, List<Bonus> bonus, double min)
            throws FormulaInvalidaException {
        if (regra.calcularMedia(avaliacoes, bonus) >= min) {
            return 0;
        }
        boolean temPendente = avaliacoes.stream().anyMatch(a -> !a.possuiNotaLancada());
        if (!temPendente || simular(regra, avaliacoes, bonus, 10) < min) {
            return Double.POSITIVE_INFINITY;
        }
        double baixo = 0;
        double alto = 10;
        for (int i = 0; i < 40; i++) {
            double meio = (baixo + alto) / 2;
            if (simular(regra, avaliacoes, bonus, meio) >= min) {
                alto = meio;
            } else {
                baixo = meio;
            }
        }
        return Math.ceil(alto * 100 - 1e-6) / 100;   // arredonda para cima em 2 casas
    }

    private static double simular(RegraMedia regra, List<Avaliavel> avaliacoes, List<Bonus> bonus, double nota)
            throws FormulaInvalidaException {
        List<Avaliavel> simuladas = new ArrayList<>();
        for (Avaliavel a : avaliacoes) {
            simuladas.add(a.possuiNotaLancada() ? a : new NotaSimulada(a.getSigla(), a.getPeso(), nota));
        }
        return regra.calcularMedia(simuladas, bonus);
    }
}
