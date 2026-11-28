package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.List;

/**
 * Σ(nota × peso) ÷ Σ(pesos). O peso de cada avaliação é definido no cadastro (F14).
 * Avaliações com peso 0 não influenciam a média.
 */
public class MediaPonderada implements RegraMedia {

    public MediaPonderada() {
    }

    @Override
    public double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) {
        double somaPesos = 0;
        double soma = 0;
        for (Avaliavel a : avaliacoes) {
            somaPesos += a.getPeso();
            soma += CalculoMedia.notaComBonus(a, bonus) * a.getPeso();
        }
        if (somaPesos == 0) {
            // Nenhum peso definido: cai na média aritmética para não dividir por zero.
            return new MediaAritmetica().calcularMedia(avaliacoes, bonus);
        }
        return CalculoMedia.aplicarBonusFinal(soma / somaPesos, bonus);
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
