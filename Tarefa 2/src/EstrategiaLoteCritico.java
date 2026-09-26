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
        return "Estratégia Lote Crítico: Prioriza os itens de maior demanda pendente";
    }
}
