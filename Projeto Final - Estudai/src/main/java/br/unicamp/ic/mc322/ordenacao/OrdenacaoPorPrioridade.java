package br.unicamp.ic.mc322.ordenacao;

import estudai.atividades.Atividade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Prioridade alta primeiro; empate decidido pelo prazo. */
public class OrdenacaoPorPrioridade implements EstrategiaOrdenacaoAtividade {

    public OrdenacaoPorPrioridade() {
    }

    @Override
    public List<Atividade> ordenar(List<Atividade> atividades) {
        List<Atividade> copia = new ArrayList<>(atividades);
        copia.sort(Comparator.comparingInt((Atividade a) -> -a.getPrioridade().getPeso())
                .thenComparing(Atividade::getPrazo));
        return copia;
    }

    @Override
    public String toString() {
        return "Prioridade";
    }
}
