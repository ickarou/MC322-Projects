package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.List;

/** Soma das notas ÷ quantidade de avaliações. Os pesos são ignorados. */
public class MediaAritmetica implements RegraMedia {

    public MediaAritmetica() {
    }

    @Override
    public double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) {
        if (avaliacoes.isEmpty()) {
            return CalculoMedia.aplicarBonusFinal(0, bonus);
        }
        double soma = 0;
        for (Avaliavel a : avaliacoes) {
            soma += CalculoMedia.notaComBonus(a, bonus);
        }
        return CalculoMedia.aplicarBonusFinal(soma / avaliacoes.size(), bonus);
    }

    @Override
    public String getDescricaoRegra() {
        return "Média aritmética";
    }

    @Override
    public String toString() {
        return getDescricaoRegra();
    }
}
