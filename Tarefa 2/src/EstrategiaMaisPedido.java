import java.util.List;

public class EstrategiaMaisPedido implements EstrategiaProducao{
    
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda maiorDemandaEncontrada = null;
        List<Demanda> elegiveis = filtro(demandas);

        for (Demanda d : elegiveis) {
            if (d.getQuantidadeProdutos() > maiorDemandaEncontrada.getQuantidadeProdutos()) {
                maiorDemandaEncontrada = d;
            }
        }

        return maiorDemandaEncontrada;
    }

    @Override 
    public String getNomeEstrategia(){
        return "Estratégia: Prioriza os itens de maior demanda";
    }
}
