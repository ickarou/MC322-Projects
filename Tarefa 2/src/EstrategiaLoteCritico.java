/* Estratégia Lote Crítico (Maior Demanda)
 *
 * Tarefa 3
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.List;

public class EstrategiaLoteCritico implements EstrategiaProducao{
    
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda maiorDemandaEncontrada = null;
        List<Demanda> elegiveis = filtro(demandas);

        for (Demanda d : elegiveis) {
            if (maiorDemandaEncontrada == null || d.getQuantidadeProdutos() > maiorDemandaEncontrada.getQuantidadeProdutos()) {
                maiorDemandaEncontrada = d;
            }
        }

        return maiorDemandaEncontrada;
    }

    @Override 
    public String getNomeEstrategia(){
        return "Lote Crítico";
    }
}