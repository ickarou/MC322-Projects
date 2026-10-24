import java.util.List;

/** Escolhe a maior demanda cujo custo estimado cabe no orcamento atual.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class EstrategiaMetaDeOrcamento implements EstrategiaProducao {

    /** Cria a estrategia que considera a viabilidade financeira. */
    public EstrategiaMetaDeOrcamento() {
    }

    /**
     * Seleciona a demanda de maior quantidade entre as financeiramente viaveis.
     *
     * @param demandas demandas cadastradas
     * @param orcamentoDisponivel orcamento atual da planta
     * @return demanda escolhida ou {@code null} se nenhuma couber no orcamento
     * @throws IllegalArgumentException se a lista ou o orcamento forem invalidos
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (!Double.isFinite(orcamentoDisponivel) || orcamentoDisponivel < 0) {
            throw new IllegalArgumentException("O orcamento deve ser um valor finito e nao negativo.");
        }
        Demanda quantidadeMaxima = null;
        List<Demanda> elegiveis = filtro(demandas);
        for (Demanda demanda : elegiveis) {
            if (!demanda.viavelFinanceiramente(orcamentoDisponivel)) {
                continue;
            }
            if (quantidadeMaxima == null
                    || demanda.getQuantidadeProdutos() > quantidadeMaxima.getQuantidadeProdutos()) {
                quantidadeMaxima = demanda;
            }
        }
        return quantidadeMaxima;
    }

    /** Retorna o nome da estrategia.
     *  @return nome da estrategia */
    @Override
    public String getNomeEstrategia() {
        return "Meta de Orcamento";
    }
}