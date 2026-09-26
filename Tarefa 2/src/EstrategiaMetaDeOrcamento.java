import java.util.List;

public class EstrategiaMetaDeOrcamento implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda quantidadeMaxima = null;
        List<Demanda> elegiveis = filtro(demandas);

        for (Demanda d : elegiveis) {
            if (!d.viavelFinanceiramente(orcamentoDisponivel)) { continue; }
            if (quantidadeMaxima == null || d.getQuantidadeProdutos() > quantidadeMaxima.getQuantidadeProdutos()) {
                quantidadeMaxima = d;
            }
        }

        return quantidadeMaxima;
    }

    @Override
    public String getNomeEstrategia(){
        return "Estratégia Meta de Orçamento: Máximo de Produtos custo benefício";
    }
}