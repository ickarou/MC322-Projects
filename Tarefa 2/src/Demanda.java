/*Demanda.java
*
* Tarefa 3
*
* última modificação: 23/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Demanda {
    
    /*Atributos privados*/
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private double custoUnitario;

    /*Construtor*/
    public Demanda(String tipoProduto, int quantidadeProdutos, double custoUnitario){
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitario = custoUnitario;
    }

    /*Métodos*/
    public void atualizarQuantidade(int quantidadeDemandada){
        this.quantidadeProdutos += quantidadeDemandada;
    }

    public int calcularMateriaPrimaNecessaria(Produto produtoDemandado){
        int totalNecessario = this.quantidadeProdutos * produtoDemandado.getQuantidadeMateriaPrimaPorUnidade();
        return totalNecessario;
    }

    public void definirCustoUnitarioEstimado(double custo) {
        this.custoUnitario = custo;
    }

    public double calcularCustoTotalEstimado() {
        return this.custoUnitario * this.quantidadeProdutos;
    }

    public boolean viavelFinanceiramente(double orcamentoDisponivel) {
        return calcularCustoTotalEstimado() <= orcamentoDisponivel;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus(){
        return status;
    }

    public void atender(){
        if (status == StatusDemanda.CANCELADA){
            throw new IllegalStateException("Não é possível atender um pedido cancelado");
        }
        status = StatusDemanda.CONCLUIDA;
    }
    
}