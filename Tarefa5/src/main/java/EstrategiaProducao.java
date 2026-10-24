import java.util.ArrayList;
import java.util.List;

/** Define como uma demanda e escolhida para a proxima producao.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public interface EstrategiaProducao {

    /**
     * Escolhe uma demanda de acordo com a estrategia implementada.
     *
     * @param demandas demandas cadastradas na planta
     * @param orcamentoDisponivel orcamento atual
     * @return demanda escolhida ou {@code null} quando nenhuma for elegivel
     * @throws IllegalArgumentException se a lista for nula ou o orcamento invalido
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /** Informa o nome da estrategia implementada.
     * @return nome da estrategia
     */
    String getNomeEstrategia();

    /**
     * Filtra demandas pendentes que possuem quantidade positiva.
     *
     * @param demandas demandas cadastradas
     * @return lista com as demandas elegiveis para avaliacao
     * @throws IllegalArgumentException se a lista for nula ou contiver uma referencia nula
     */
    default List<Demanda> filtro(List<Demanda> demandas) {
        if (demandas == null) {
            throw new IllegalArgumentException("A lista de demandas deve ser informada.");
        }
        List<Demanda> elegiveis = new ArrayList<>();
        for (Demanda demanda : demandas) {
            if (demanda == null) {
                throw new IllegalArgumentException("A lista nao pode conter demandas nulas.");
            }
            if (demanda.getStatus() == StatusDemanda.PENDENTE && demanda.getQuantidadeProdutos() > 0) {
                elegiveis.add(demanda);
            }
        }
        return elegiveis;
    }
}