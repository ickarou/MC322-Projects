/**
 * Representa uma ordem de produção para um tipo específico de produto (Demanda).
 * <p>
 * A classe gerencia o ciclo de vida de um pedido na fábrica, controlando as quantidades 
 * solicitadas, o status atual e a viabilidade financeira da produção frente ao orçamento.
 * </p>
 * @author Eduardo Pontes, Icaro Amaral
 * 
 * 
 * 
 */

public class Demanda {
    
    /* Atributos privados */
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;
    private double custoUnitario;

    /**
     * Inicializa uma nova demanda de produção.
     * <p>
     * O pedido é criado com a quantidade informada, o status inicializado 
     * obrigatoriamente como PENDENTE e o custo unitário estimado zerado.
     * </p>
     *
     * @param tipoProduto O tipo do produto solicitado.
     * @param quantidadeProdutos A quantidade inicial do pedido.
     */
    public Demanda(String tipoProduto, int quantidadeProdutos){
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitario = 0.0;
    }

    /**
     * Atualiza a quantidade de produtos exigidos pela demanda.
     * <p>
     * Caso a demanda esteja em andamento (status EM_PRODUCAO), o status retornará 
     * para PENDENTE para que o gerenciador reavalie os novos custos e viabilidade dessa produção.
     * </p>
     *
     * @param quantidadeDemandada Quantidade de peças a ser somada na demanda existente.
     * @throws DemandaInvalidaException se a demanda já estiver com status CONCLUIDA ou CANCELADA.
     */
    public void atualizarQuantidade(int quantidadeDemandada){
        if (this.status == StatusDemanda.CONCLUIDA || this.status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("Não é possível atualizar a quantidade de uma demanda finalizada ou cancelada.");
        }
        
        this.quantidadeProdutos += quantidadeDemandada;
        
        if (this.status == StatusDemanda.EM_PRODUCAO) {
            this.status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Calcula a quantidade de matéria-prima necessária para fabricar esta demanda inteira.
     * <p>
     * Multiplica a quantidade atual de produtos da demanda pela quantidade de 
     * matéria-prima exigida por uma única unidade do produto.
     * </p>
     *
     * @param produtoDemandado O molde do produto que será fabricado.
     * @return O total de matéria-prima exigida para produzir a quantidade desta demanda.
     */
    public int calcularMateriaPrimaNecessaria(Produto produtoDemandado){
        return this.quantidadeProdutos * produtoDemandado.getQuantidadeMateriaPrimaPorUnidade();
    }

    /**
     * Define o custo estimado para produção de uma peça.
     * <p>
     * Este valor é utilizado depois para verificar se o lote inteiro é 
     * viável financeiramente.
     * </p>
     *
     * @param custo O custo unitário estimado de produção.
     */
    public void definirCustoUnitarioEstimado(double custo) {
        this.custoUnitario = custo;
    }

    /**
     * Calcula o custo total previsto para concluir a demanda atual.
     * 
     * @return A multiplicação do custo unitário estimado pela quantidade de produtos.
     */
    public double calcularCustoTotalEstimado() {
        return this.custoUnitario * this.quantidadeProdutos;
    }

    /**
     * Verifica se o orçamento disponível é suficiente para pagar a produção da demanda.
     * 
     * @param orcamentoDisponivel O caixa atual da fábrica.
     * @return true se o custo total estimado for menor ou igual ao orçamento disponível, false caso contrário.
     */
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

    /**
     * Altera o status da demanda para EM_PRODUCAO.
     * <p>
     * Representa o instante em que a fábrica inicia a execução do lote.
     * </p>
     *
     * @throws DemandaInvalidaException se a demanda não estiver no status PENDENTE.
     */
    public void iniciarProducao(){
        if (this.status != StatusDemanda.PENDENTE){
            throw new DemandaInvalidaException("Só é possível iniciar a produção de uma demanda PENDENTE.");
        }
        this.status = StatusDemanda.EM_PRODUCAO;
    }

    /**
     * Altera o status da demanda para CANCELADA.
     * <p>
     * Cancela a demanda, impedindo que continue sendo processada.
     * </p>
     * 
     * @param motivo motivo do cancelamento que será exibida no terminal.
     * @throws DemandaInvalidaException se a demanda já estiver no status CONCLUIDA ou CANCELADA.
     */
    public void cancelar(String motivo){
        if (this.status == StatusDemanda.CONCLUIDA || this.status == StatusDemanda.CANCELADA){
            throw new DemandaInvalidaException("Não é possível cancelar uma demanda já concluída ou cancelada.");
        }
        this.status = StatusDemanda.CANCELADA;
        System.out.println("Demanda de " + tipoProduto + " cancelada. Motivo: " + motivo);
    }

    /**
     * Altera o status para CONCLUIDA.
     * <p>
     * Chamado quando as máquinas já trabalharam e as peças foram aprovadas.
     * </p>
     * 
     * @throws DemandaInvalidaException se a demanda estiver no status CANCELADA.
     */
    public void atender(){
        if (this.status == StatusDemanda.CANCELADA){
            throw new DemandaInvalidaException("Não é possível atender um pedido cancelado.");
        }
        this.status = StatusDemanda.CONCLUIDA;
    }
}