package br.unicamp.ic.mc322.ordenacao;

import br.unicamp.ic.mc322.atividades.Atividade;

import java.util.List;

/** Strategy de ordenação da lista de atividades (F21). Devolve uma lista nova, sem alterar a original. */
public interface EstrategiaOrdenacaoAtividade {
    List<Atividade> ordenar(List<Atividade> atividades);
}

