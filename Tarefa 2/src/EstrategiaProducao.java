import java.util.ArrayList;
import java.util.List;

public interface EstrategiaProducao {

    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    String getNomeEstrategia();

    default List<Demanda> filtro(List<Demanda> demandas) {
        List<Demanda> elegiveis = new ArrayList<>();
        
        for (Demanda d : demandas) {
            if (d.getStatus() == StatusDemanda.PENDENTE) {
                elegiveis.add(d);
            }
        }
        return elegiveis;
    }
}
