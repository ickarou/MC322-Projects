import java.util.List;

/** Prioriza a demanda elegível com a maior quantidade de peças.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */

public class EstrategiaLoteCritico implements EstrategiaProducao {

    /** Cria a estratégia que prioriza a maior demanda. */
    public EstrategiaLoteCritico() {
    }

    /**
     * Localiza a maior demanda entre as pendentes.
     *
     * @param demandas demandas cadastradas
     * @param orcamentoDisponivel orçamento atual da planta
     * @return demanda com maior quantidade ou {@code null}
     * @throws IllegalArgumentException se a lista ou o orçamento forem inválidos
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (!Double.isFinite(orcamentoDisponivel) || orcamentoDisponivel < 0.0) {
            throw new IllegalArgumentException("O orcamento deve ser um valor finito e nao negativo.");
        }
        Demanda maiorDemandaEncontrada = null;
        List<Demanda> elegiveis = filtro(demandas);
        for (Demanda demanda : elegiveis) {
            if (maiorDemandaEncontrada == null
                    || demanda.getQuantidadeProdutos() > maiorDemandaEncontrada.getQuantidadeProdutos()) {
                maiorDemandaEncontrada = demanda;
            }
        }
        return maiorDemandaEncontrada;
    }

    /** Retorna o nome da estratégia.
     *
     *  @return nome da estratégia */
    @Override
    public String getNomeEstrategia() {
        return "Lote Critico";
    }
}
