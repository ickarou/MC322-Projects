/* Enum StatusDemanda
*
* Tarefa 3
*
* última modificação: 22/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public enum StatusDemanda {
    
    //Constantes
    PENDENTE ("Demanda cadastrada, mas ainda não iniciada"),
    EM_PRODUCAO ("Demanda selecionada e em processo de fabricação"),
    CONCLUIDA ("Demanda com todos os produtos produzidos com sucesso"),
    CANCELADA ("Demanda cancelada por falta de orçamento ou insumos");

    private final String statusAtual;

    StatusDemanda(String statusAtual){
        this.statusAtual = statusAtual;
    }

    public String getDescricao (){
        return statusAtual;
    }
}
