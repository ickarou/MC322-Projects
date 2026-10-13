/**
 * Classe base dos produtos fabricados na planta e auditados pelo sistema.
 * Cada produto define seu processamento, tempo de fabricacao e forma de criar
 * uma nova unidade do mesmo tipo.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public abstract class Produto implements Auditavel {

    private final String id;
    private final String nome;
    private String status;
    private final int quantidadeMateriaPrimaPorUnidade;
    private final float qualidade;
    private float probabilidadeFalhaAcumulada;
    private String lote;
    private static int totalProdutosFabricados = 0;

    /**
     * Inicializa uma unidade ou modelo de produto.
     *
     * @param id identificador do produto
     * @param nome nome de exibicao
     * @param quantidadeMateriaPrimaPorUnidade materia-prima consumida por peca
     * @param qualidade indice de qualidade entre zero e um
     * @throws IllegalArgumentException se os dados forem invalidos
     */
    public Produto(String id, String nome, int quantidadeMateriaPrimaPorUnidade, float qualidade) {
        if (id == null || id.isBlank() || nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("ID e nome do produto devem ser informados.");
        }
        if (quantidadeMateriaPrimaPorUnidade <= 0 || !Float.isFinite(qualidade)
                || qualidade < 0.0f || qualidade > 1.0f) {
            throw new IllegalArgumentException("Consumo de materia-prima e qualidade devem estar em faixas validas.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.status = "Aguardando injecao";
        this.probabilidadeFalhaAcumulada = 0.0f;
        totalProdutosFabricados++;
    }

    /** Executa a etapa de processamento especifica do produto. */
    public abstract void processar();

    /** Consulta o tempo estimado de producao.
     * @return tempo estimado de producao em segundos
     */
    public abstract int calcularTempoProducao();

    /** Consulta o tipo do produto.
     * @return tipo usado para relacionar o produto a uma demanda
     */
    public abstract String getTipo();

    /**
     * Cria outra unidade do mesmo tipo.
     *
     * @param id identificador da nova unidade
     * @return nova instancia do produto
     */
    public abstract Produto criarNovaUnidade(String id);

    /**
     * Atualiza o estado de processamento da peca.
     *
     * @param novoStatus descricao do estado atual
     * @throws IllegalArgumentException se o status estiver vazio
     */
    public void setStatus(String novoStatus) {
        if (novoStatus == null || novoStatus.isBlank()) {
            throw new IllegalArgumentException("O status do produto deve ser informado.");
        }
        this.status = novoStatus;
    }

    /**
     * Soma uma probabilidade de falha ao historico da peca, limitada a 100%.
     *
     * @param probabilidadeMaquina probabilidade adicional entre zero e um
     * @throws IllegalArgumentException se o valor estiver fora da faixa valida
     */
    public void aumentarProbabilidadeFalha(float probabilidadeMaquina) {
        if (!Float.isFinite(probabilidadeMaquina) || probabilidadeMaquina < 0.0f || probabilidadeMaquina > 1.0f) {
            throw new IllegalArgumentException("A probabilidade de falha deve estar entre zero e um.");
        }
        probabilidadeFalhaAcumulada = Math.min(1.0f, probabilidadeFalhaAcumulada + probabilidadeMaquina);
    }

    /** Consulta o identificador da peca.
     * @return identificador da peca
     */
    public String getId() {
        return id;
    }

    /** Consulta o nome do produto.
     * @return nome do produto
     */
    public String getNome() {
        return nome;
    }

    /** Consulta o estado de processamento.
     * @return estado atual de processamento
     */
    public String getStatus() {
        return status;
    }

    /** Consulta o consumo por unidade.
     * @return quantidade de materia-prima usada em cada unidade
     */
    public int getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaPorUnidade;
    }

    /** Consulta o indice de qualidade.
     * @return indice de qualidade do produto
     */
    public float getQualidade() {
        return qualidade;
    }

    /** Consulta a probabilidade de falha acumulada.
     * @return probabilidade de falha acumulada durante a fabricacao
     */
    public float getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    /**
     * Associa a peca a um lote.
     *
     * @param lote identificador do lote
     * @throws IllegalArgumentException se o identificador estiver vazio
     */
    public void setLote(String lote) {
        if (lote == null || lote.isBlank()) {
            throw new IllegalArgumentException("O lote deve ser informado.");
        }
        this.lote = lote;
    }

    /** Consulta o lote associado a peca.
     * @return identificador do lote ou {@code null} antes da associacao
     */
    public String getLote() {
        return lote;
    }

    /** Consulta o numero de objetos de produto criados.
     * @return total de instancias de produto criadas durante a execucao
     */
    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }

    /** Gera um relatorio de diagnostico da peca.
     *  @return relatorio com a qualidade e a probabilidade de falha da peca */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("O produto %s de qualidade: %.2f tem %.1f%% de chance de falha",
                nome, qualidade, probabilidadeFalhaAcumulada * 100);
    }

    /** Verifica se a peca precisa de manutencao.
     *  @return {@code true} quando a probabilidade acumulada chega a 20% */
    @Override
    public boolean precisaManutencao() {
        return probabilidadeFalhaAcumulada >= 0.2f;
    }
}