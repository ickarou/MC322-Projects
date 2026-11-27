package br.unicamp.ic.mc322.atividades;

/**
 * O que entra no cálculo da média: sigla, peso e nota. Só leitura, porque é isso que as regras
 * de média precisam. Lançar nota e mudar peso são operações da AtividadeAvaliativa.
 */
public interface Avaliavel {
    String getSigla();

    /** Nota de 0 a 10, ou null se ainda não foi lançada. */
    Double getNota();

    double getPeso();

    default boolean possuiNotaLancada() {
        return getNota() != null;
    }
}
