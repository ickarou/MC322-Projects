/**
 * Representa uma ordem de produção para um tipo específico de produto (Demanda).
 * <p>
 * A classe gerencia o ciclo de vida de um pedido na fábrica, controlando as quantidades 
 * solicitadas, o status atual e a viabilidade financeira da produção frente ao orçamento.
 * </p>
 * 
 * 
 * @author Eduardo Pontes, Icaro Amaral
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
     * @param quantidadeProdutos A quantidade inicial do pedido, que pode ser zero.
     * @throws DemandaInvalidaException se o tipo não for informado ou a quantidade for negativa.
     */
    public Demanda(String tipoProduto, int quantidadeProdutos) {
        if (tipoProduto == null || tipoProduto.isBlank()) {
            throw new DemandaInvalidaException("O tipo do produto da demanda deve ser informado.");
        }
        if (quantidadeProdutos < 0) {
            throw new DemandaInvalidaException("A quantidade inicial da demanda nao pode ser negativa.");
        }
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
     * @throws DemandaInvalidaException se a demanda estiver finalizada, a quantidade não for positiva ou houver excesso numérico.
     */
    public void atualizarQuantidade(int quantidadeDemandada) {
        if (status == StatusDemanda.CONCLUIDA || status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("Nao e possivel atualizar uma demanda finalizada ou cancelada.");
        }
        if (quantidadeDemandada <= 0) {
            throw new DemandaInvalidaException("A quantidade adicional deve ser maior que zero.");
        }
        if (quantidadeDemandada > Integer.MAX_VALUE - quantidadeProdutos) {
            throw new DemandaInvalidaException("A quantidade solicitada excede o limite permitido.");
        }

        quantidadeProdutos += quantidadeDemandada;
        if (status == StatusDemanda.EM_PRODUCAO) {
            status = StatusDemanda.PENDENTE;
        }
    }

    /**
     * Desconta da demanda as peças aprovadas em uma rodada de produção.
     * <p>
     * Se ainda restarem peças, a demanda volta para PENDENTE para ser reavaliada na
     * próxima tentativa. Se não restar nenhuma, ela é concluída.
     * </p>
     *
     * @param aprovadas quantidade de peças aprovadas, entre zero e a quantidade restante.
     * @throws DemandaInvalidaException se a demanda estiver finalizada ou a quantidade for inválida.
     */
    public void registrarPecasAprovadas(int aprovadas) {
        if (status == StatusDemanda.CONCLUIDA || status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("Nao e possivel registrar pecas em uma demanda finalizada ou cancelada.");
        }
        if (aprovadas < 0 || aprovadas > quantidadeProdutos) {
            throw new DemandaInvalidaException("A quantidade de pecas aprovadas e invalida para esta demanda.");
        }
        quantidadeProdutos -= aprovadas;
        status = (quantidadeProdutos == 0) ? StatusDemanda.CONCLUIDA : StatusDemanda.PENDENTE;
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
        if (produtoDemandado == null || !tipoProduto.equals(produtoDemandado.getTipo())) {
            throw new DemandaInvalidaException("O produto informado nao corresponde a demanda.");
        }
        return this.quantidadeProdutos * produtoDemandado.getQuantidadeMateriaPrimaPorUnidade();
    }

    /**
     * Define o custo estimado de operação para cada unidade da demanda.
     *
     * @param custo custo estimado por unidade.
     * @throws IllegalArgumentException se o custo for negativo ou não for um número finito.
     */
    public void definirCustoUnitarioEstimado(double custo) {
        if (!Double.isFinite(custo) || custo < 0) {
            throw new IllegalArgumentException("O custo estimado deve ser um valor nao negativo.");
        }
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
     * @param orcamentoDisponivel valor disponível para a produção.
     * @return {@code true} se o orçamento for suficiente.
     * @throws IllegalArgumentException se o orçamento for negativo.
     */
    public boolean viavelFinanceiramente(double orcamentoDisponivel) {
        if (orcamentoDisponivel < 0) {
            throw new IllegalArgumentException("O orcamento deve ser um valor nao negativo.");
        }
        return calcularCustoTotalEstimado() <= orcamentoDisponivel;
    }


    /* Getters */
    /** Consulta o tipo solicitado.
     * @return tipo do produto solicitado
     */
    public String getTipoProduto() {
        return tipoProduto;
    }

    /** Consulta o volume ainda solicitado.
     * @return quantidade ainda solicitada
     */
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    /** Consulta o estado atual.
     * @return estado atual da demanda
     */
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
            throw new DemandaInvalidaException("So e possivel iniciar a producao de uma demanda PENDENTE.");
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
     * @throws DemandaInvalidaException se a demanda estiver finalizada ou o motivo não for informado.
     */
    public void cancelar(String motivo) {
        if (status == StatusDemanda.CONCLUIDA || status == StatusDemanda.CANCELADA) {
            throw new DemandaInvalidaException("Nao e possivel cancelar uma demanda ja concluida ou cancelada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new DemandaInvalidaException("Informe o motivo do cancelamento.");
        }
        status = StatusDemanda.CANCELADA;
        System.out.println("Demanda de " + tipoProduto + " cancelada. Motivo: " + motivo);
    }

    /**
     * Altera o status para CONCLUIDA.
     * <p>
     * Chamado quando as máquinas já trabalharam e as peças foram aprovadas.
     * </p>
     * 
     * @throws DemandaInvalidaException se a demanda não estiver no status EM_PRODUCAO.
     */
    public void atender(){
        if (this.status != StatusDemanda.EM_PRODUCAO){
            throw new DemandaInvalidaException("So e possivel atender uma demanda que esta em producao.");
        }
        this.status = StatusDemanda.CONCLUIDA;
    }
}