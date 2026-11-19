package br.unicamp.ic.mc322.ordenacao;

import estudai.atividades.Atividade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Prazo mais próximo primeiro. */
public class OrdenacaoPorPrazo implements EstrategiaOrdenacaoAtividade {

    public OrdenacaoPorPrazo() {
    }

    @Override
    public List<Atividade> ordenar(List<Atividade> atividades) {
        List<Atividade> copia = new ArrayList<>(atividades);
        copia.sort(Comparator.comparing(Atividade::getPrazo).thenComparing(Atividade::getTitulo));
        return copia;
    }

    @Override
    public String toString() {
        return "Prazo";
    }
}

