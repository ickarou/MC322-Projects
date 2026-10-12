/* Estratégia Linha de Montagem (Ordem de Chegada)
 *
 * Tarefa 3
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.List;

public class EstrategiaLinhaDeMontagem implements EstrategiaProducao {
    
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        List<Demanda> elegiveis = filtro(demandas);

        if (elegiveis.isEmpty()) {
            return null;
        }

        return elegiveis.get(0); // fila já está na ordem certa, pega o primeiro
    }

    @Override 
    public String getNomeEstrategia(){
        return "Linha de Montagem";
    }
}