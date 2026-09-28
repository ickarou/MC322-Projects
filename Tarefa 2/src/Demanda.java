/* Demanda.java
 *
 * Tarefa 3
 * 
 * última modificação: 23/09/2026
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

public class Demanda {
    
    /* Atributos privados */
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private double custoUnitario;

    /* Construtor */
    public Demanda(String tipoProduto, int quantidadeProdutos){
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitario = 0.0;
    }

    /* Métodos */
    public void atualizarQuantidade(int quantidadeDemandada){
        if (this.status == StatusDemanda.CONCLUIDA || this.status == StatusDemanda.CANCELADA) {
            throw new IllegalStateException("Não é possível atualizar a quantidade de uma demanda finalizada ou cancelada.");
        }
        
        this.quantidadeProdutos += quantidadeDemandada;
        
        // Se adicionaram mais itens enquanto fabricava, volta para pendente para reavaliar os custos
        if (this.status == StatusDemanda.EM_PRODUCAO) {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    public int calcularMateriaPrimaNecessaria(Produto produtoDemandado){
        return this.quantidadeProdutos * produtoDemandado.getQuantidadeMateriaPrimaPorUnidade();
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

    /* Getters */
    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus(){
        return status;
    }

    /* Transições de Estado */
    public void iniciarProducao(){
        if (this.status != StatusDemanda.PENDENTE){
            throw new IllegalStateException("Só é possível iniciar produção de uma demanda PENDENTE.");
        }
        this.status = StatusDemanda.EM_PRODUCAO;
    }

    public void cancelar(String motivo){
        if (this.status == StatusDemanda.CONCLUIDA || this.status == StatusDemanda.CANCELADA){
            throw new IllegalStateException("Não é possível cancelar uma demanda já concluída ou cancelada.");
        }
        this.status = StatusDemanda.CANCELADA;
        System.out.println("Demanda de " + tipoProduto + " cancelada. Motivo: " + motivo);
    }

    public void atender(){
        if (this.status == StatusDemanda.CANCELADA){
            throw new IllegalStateException("Não é possível atender um pedido cancelado.");
        }
        this.status = StatusDemanda.CONCLUIDA;
    }
}