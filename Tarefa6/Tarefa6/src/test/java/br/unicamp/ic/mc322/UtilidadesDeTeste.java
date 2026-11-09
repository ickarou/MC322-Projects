package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.model.maquina.Maquina;

/**
 * Apoio para os testes: operacoes repetidas em varias classes de teste.
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public final class UtilidadesDeTeste {

    private static final int LIMITE_DE_DESGASTES = 100_000;

    /** Classe utilitaria, nao e instanciada. */
    private UtilidadesDeTeste() {
    }

    /**
     * Desgasta a maquina repetidamente ate que alguma parte quebre.
     * O desgaste e sorteado, mas cada aplicacao tem chance de remover saude,
     * entao a maquina sempre quebra depois de algumas dezenas de usos.
     *
     * @param maquina maquina a ser quebrada
     * @throws AssertionError se a maquina nao quebrar dentro do limite de repeticoes
     */
    public static void quebrar(Maquina maquina) {
        for (int i = 0; i < LIMITE_DE_DESGASTES && !maquina.estaQuebrada(); i++) {
            maquina.aplicarDesgaste();
        }
        if (!maquina.estaQuebrada()) {
            throw new AssertionError("A maquina " + maquina.getNome() + " nao quebrou com o desgaste repetido.");
        }
    }
}
