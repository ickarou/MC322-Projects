package br.unicamp.ic.mc322.ordenacao;

import estudai.atividades.Atividade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Agrupa por nome da disciplina; dentro dela, prazo mais próximo primeiro. */
public class OrdenacaoPorDisciplina implements EstrategiaOrdenacaoAtividade {

    public OrdenacaoPorDisciplina() {
    }

    @Override
    public List<Atividade> ordenar(List<Atividade> atividades) {
        List<Atividade> copia = new ArrayList<>(atividades);
        copia.sort(Comparator.comparing((Atividade a) -> a.getDisciplina().getNome(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Atividade::getPrazo));
        return copia;
    }

    @Override
    public String toString() {
        return "Disciplina";
    }
}

