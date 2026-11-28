package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Contas comuns às regras de média. Fica visível só dentro do pacote: quem está fora usa
 * as regras pela interface RegraMedia.
 */
final class CalculoMedia {

    private CalculoMedia() {
    }

    static double limitar(double v) {
        return Math.max(0, Math.min(10, v));
    }

    /** Nota da avaliação (0 se não lançada) somada aos bônus que apontam para ela, limitada a 10. */
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

    /** Soma os bônus sem alvo à média, limitando a 10. */
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
     * Busca binária que funciona para qualquer regra, inclusive a fórmula livre: testa a mesma nota
     * em todas as avaliações pendentes até achar a menor que alcança "min".
     * Supõe que a média não diminui quando uma nota aumenta, o que vale para as regras aritmética
     * e ponderada e para fórmulas sem subtrair ou dividir por uma sigla.
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
