package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.List;

/** Σ(nota × peso) ÷ Σ(pesos). O peso de cada avaliação é definido no cadastro (F14). */
public class MediaPonderada implements RegraMedia {

    public MediaPonderada() {
    }

    @Override
    public double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) {
        double somaPesos = 0;
        double soma = 0;
        for (Avaliavel a : avaliacoes) {
            somaPesos += a.getPeso();
            soma += RegraMedia.notaComBonus(a, bonus) * a.getPeso();
        }
        if (somaPesos == 0) {
            // Nenhum peso definido: cai na média aritmética para não dividir por zero.
            return new MediaAritmetica().calcularMedia(avaliacoes, bonus);
        }
        return RegraMedia.aplicarBonusFinal(soma / somaPesos, bonus);
    }

    @Override
    public double calcularNotaNecessaria(List<Avaliavel> avaliacoes, List<Bonus> bonus, double min)
            throws FormulaInvalidaException {
        return RegraMedia.buscarNotaNecessaria(this, avaliacoes, bonus, min);
    }

    @Override
    public String getDescricaoRegra() {
        return "Média ponderada";
    }

    @Override
    public String toString() {
        return getDescricaoRegra();
    }
}
