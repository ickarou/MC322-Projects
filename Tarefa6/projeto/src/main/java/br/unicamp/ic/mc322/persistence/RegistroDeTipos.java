package br.unicamp.ic.mc322.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.strategy.EstrategiaLinhaDeMontagem;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;
import br.unicamp.ic.mc322.strategy.EstrategiaMetaDeOrcamento;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

/**
 * Relaciona o texto gravado no arquivo com a classe concreta que ele representa.
 * <p>
 * O arquivo guarda apenas o nome do tipo de cada maquina, produto e estrategia; para
 * recriar o objeto e preciso saber qual construtor chamar. Esse conhecimento fica
 * concentrado aqui, de modo que o escritor e o leitor nao precisam repeti-lo. Ao criar
 * um novo tipo de maquina ou de produto, basta registra-lo neste lugar.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
final class RegistroDeTipos {

    private static final Map<String, BiFunction<String, Cenario, Maquina>> MAQUINAS = new LinkedHashMap<>();
    private static final Map<String, Function<String, Produto>> PRODUTOS = new LinkedHashMap<>();

    static {
        MAQUINAS.put(Injetora.class.getSimpleName(), Injetora::new);
        MAQUINAS.put(BracoEncaixotador.class.getSimpleName(), BracoEncaixotador::new);
        MAQUINAS.put(EstacaoTestes.class.getSimpleName(), EstacaoTestes::new);

        PRODUTOS.put(CarcacaSuperior.TIPO, CarcacaSuperior::new);
        PRODUTOS.put(CarcacaInferior.TIPO, CarcacaInferior::new);
        PRODUTOS.put(ProtecaoLateral.TIPO, ProtecaoLateral::new);
    }

    /** Classe utilitaria, nao e instanciada. */
    private RegistroDeTipos() {
    }

    /**
     * Informa o nome com que o tipo da maquina e gravado no arquivo.
     *
     * @param maquina maquina a ser gravada
     * @return nome da classe da maquina
     * @throws IllegalArgumentException se o tipo da maquina nao estiver registrado
     */
    static String codigoDaMaquina(Maquina maquina) {
        String codigo = maquina.getClass().getSimpleName();
        if (!MAQUINAS.containsKey(codigo)) {
            throw new IllegalArgumentException("O tipo de maquina " + maquina.getClass().getName() + " nao pode ser persistido.");
        }
        return codigo;
    }

    /**
     * Cria uma maquina nova do tipo informado.
     *
     * @param codigo nome do tipo, como gravado no arquivo
     * @param nome nome da maquina
     * @param cenario cenario de operacao
     * @return maquina nova ou {@code null} se o tipo for desconhecido
     */
    static Maquina criarMaquina(String codigo, String nome, Cenario cenario) {
        BiFunction<String, Cenario, Maquina> fabrica = MAQUINAS.get(codigo);
        return (fabrica == null) ? null : fabrica.apply(nome, cenario);
    }

    /**
     * Cria um modelo de produto do tipo informado.
     *
     * @param tipo tipo do produto, como retornado por {@link Produto#getTipo()}
     * @param id identificador do modelo
     * @return modelo novo ou {@code null} se o tipo for desconhecido
     */
    static Produto criarMolde(String tipo, String id) {
        Function<String, Produto> fabrica = PRODUTOS.get(tipo);
        return (fabrica == null) ? null : fabrica.apply(id);
    }

    /**
     * Procura a estrategia de producao com o nome informado.
     *
     * @param nome nome retornado por {@link EstrategiaProducao#getNomeEstrategia()}
     * @return estrategia nova ou {@code null} se nenhuma tiver esse nome
     */
    static EstrategiaProducao criarEstrategia(String nome) {
        List<EstrategiaProducao> conhecidas = List.of(
                new EstrategiaLinhaDeMontagem(), new EstrategiaLoteCritico(), new EstrategiaMetaDeOrcamento());
        for (EstrategiaProducao estrategia : conhecidas) {
            if (estrategia.getNomeEstrategia().equals(nome)) {
                return estrategia;
            }
        }
        return null;
    }
}
