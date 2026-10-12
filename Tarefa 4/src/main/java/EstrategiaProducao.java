/* Interface Estratégia de Produção
 *
 * Tarefa 3
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.ArrayList;
import java.util.List;

public interface EstrategiaProducao {

    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    String getNomeEstrategia();

    // Método padrão para entregar só o que pode ser fabricado
    default List<Demanda> filtro(List<Demanda> demandas) {
        List<Demanda> elegiveis = new ArrayList<>();
        
        for (Demanda d : demandas) {
            if (d.getStatus() == StatusDemanda.PENDENTE && d.getQuantidadeProdutos() > 0) {
                elegiveis.add(d);
            }
        }
        return elegiveis;
    }
}