package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.List;

/**
 * Strategy do cálculo da média (F27). Cada disciplina usa uma regra, que pode ser trocada a qualquer momento.
 * Avaliação sem nota vale 0. Bônus com alvo somam na nota da avaliação; sem alvo, na média. Tudo limitado a 10.
 */
public interface RegraMedia {

    double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) throws FormulaInvalidaException;

    /**
     * Padrão: média entre a média do semestre e a nota do exame.
     * Lança FormulaInvalidaException se uma fórmula de exame personalizada não puder ser calculada.
     */
    default double calcularMediaFinalComExame(double media, double exame) throws FormulaInvalidaException {
        return CalculoMedia.limitar((media + exame) / 2);
    }

    /**
     * Nota que o aluno precisa tirar em cada avaliação ainda sem nota para chegar a "min".
     * 0 = já alcançou; Double.POSITIVE_INFINITY = impossível mesmo com 10 em tudo.
     */
    default double calcularNotaNecessaria(List<Avaliavel> avaliacoes, List<Bonus> bonus, double min)
            throws FormulaInvalidaException {
        return CalculoMedia.buscarNotaNecessaria(this, avaliacoes, bonus, min);
    }

    String getDescricaoRegra();
}
