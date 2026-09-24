import java.util.List;

public class EstrategiaMaximoProdutos implements EstrategiaProducao{
    
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda quantidadeMaxima = null;
        List<Demanda> elegiveis = filtro(demandas);

        for (Demanda d : demandas){
            if(!d.viavelFinanceiramente(orcamentoDisponivel)){ continue; }
            if(d.getQuantidadeProdutos() > quantidadeMaxima.getQuantidadeProdutos()){
                quantidadeMaxima = d;
            }
        }

        return quantidadeMaxima;
    }

    @Override 
    public String getNomeEstrategia(){
        return "Estratégia: Prioriza os itens de maior demanda";
    }
}
