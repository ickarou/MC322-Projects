import java.util.List;

public class EstrategiaFila implements EstrategiaProducao {
    
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        List<Demanda> elegiveis = filtro(demandas);

        if (elegiveis.isEmpty()) {
            return null;
        }

        return elegiveis.get(0); //fila já está na ordem certa, pega o primeiro
    }

    @Override 
    public String getNomeEstrategia(){
        return "Estratégia: Ordem de chegada";
    }
}
