import java.util.List;

/** Seleciona a primeira demanda elegivel, respeitando a ordem de cadastro.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class EstrategiaLinhaDeMontagem implements EstrategiaProducao {

    /** Cria a estrategia de selecao por ordem de cadastro. */
    public EstrategiaLinhaDeMontagem() {
    }

    /**
     * Escolhe a primeira demanda pendente com quantidade positiva.
     *
     * @param demandas demandas cadastradas
     * @param orcamentoDisponivel orcamento atual da planta
     * @return primeira demanda elegivel ou {@code null}
     * @throws IllegalArgumentException se a lista ou o orcamento forem invalidos
     */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        if (!Double.isFinite(orcamentoDisponivel) || orcamentoDisponivel < 0.0) {
            throw new IllegalArgumentException("O orcamento deve ser um valor finito e nao negativo.");
        }
        List<Demanda> elegiveis = filtro(demandas);
        return elegiveis.isEmpty() ? null : elegiveis.get(0);
    }

    /** Retorna o nome da estrategia.
     *  @return nome da estrategia */
    @Override
    public String getNomeEstrategia() {
        return "Linha de Montagem";
    }
}