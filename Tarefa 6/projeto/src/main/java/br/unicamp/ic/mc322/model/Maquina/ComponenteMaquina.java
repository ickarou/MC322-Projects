import java.util.Random;

/**
 * Parte interna de uma {@link Maquina}.
 * <p>
 * Cada componente concreto define a sua taxa de desgaste, os nomes dos seus estados e o
 * seu efeito sobre a máquina, sobrescrevendo {@link #calcularFatorDeFalha()} ou
 * {@link #calcularRiscoParaPeca()}.
 * </p>
 * <p>
 * Um componente não tem existência fora da máquina: ele é criado no construtor
 * dela e nunca é entregue a código externo. Também não conhece orçamento, demandas
 * ou estratégias de produção; só se desgasta, quebra e informa a sua condição.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public abstract class ComponenteMaquina implements Auditavel {

    private static final int LIMITE_MANUTENCAO = 30;
    private static final int LIMITE_DESGASTE = 60;

    private final String nome;
    private final String rotuloDesgastado;
    private final String rotuloCritico;
    private final float taxaDesgaste;
    private final Random random = new Random();
    private float saude = 100.0f;

    /**
     * Cria um componente novo, com saúde máxima.
     *
     * @param nome identificação do componente
     * @param rotuloDesgastado nome do estado entre 31 e 60 de saúde
     * @param rotuloCritico nome do estado entre 1 e 30 de saúde
     * @param taxaDesgaste multiplicador do desgaste deste componente, maior que zero
     * @throws IllegalArgumentException se o nome, algum rótulo ou a taxa forem inválidos
     */
    protected ComponenteMaquina(String nome, String rotuloDesgastado, String rotuloCritico, float taxaDesgaste) {
        if (nome == null || nome.isBlank() || rotuloDesgastado == null || rotuloCritico == null) {
            throw new IllegalArgumentException("O nome e os rotulos do componente devem ser informados.");
        }
        if (!(taxaDesgaste > 0.0f)) {
            throw new IllegalArgumentException("A taxa de desgaste deve ser maior que zero.");
        }
        this.nome = nome;
        this.rotuloDesgastado = rotuloDesgastado;
        this.rotuloCritico = rotuloCritico;
        this.taxaDesgaste = taxaDesgaste;
    }

    /**
     * Aplica o desgaste de um uso: de 0 a 3 pontos sorteados, multiplicados pelo
     * fator do cenário e pela taxa própria do componente. A saúde nunca fica abaixo de zero.
     *
     * @param fatorCenario multiplicador de desgaste do cenário ativo
     * @throws IllegalArgumentException se o fator for negativo
     */
    public void desgastar(float fatorCenario) {
        if (!(fatorCenario >= 0.0f)) {
            throw new IllegalArgumentException("O fator de desgaste nao pode ser negativo.");
        }
        float desgaste = random.nextInt(4) * fatorCenario * taxaDesgaste;
        this.saude = Math.max(0.0f, this.saude - desgaste);
    }

    /** Restaura a saúde máxima do componente. */
    public void reparar() {
        this.saude = 100.0f;
    }

    /**
     * Indica se o componente quebrou.
     *
     * @return {@code true} quando a saúde chegou a zero
     */
    public boolean estaQuebrado() {
        return saude <= 0.0f;
    }

    /**
     * Consulta a saúde atual, arredondada para cima: só é zero quando o componente quebrou.
     *
     * @return saúde de 0 a 100
     */
    public int getSaude() {
        return (int) Math.ceil(saude);
    }

    /**
     * Consulta o nome do componente.
     *
     * @return nome de identificação
     */
    public String getNome() {
        return nome;
    }

    /**
     * Descreve a condição atual com o vocabulário próprio do componente.
     *
     * @return "OK", o rótulo de desgaste, o rótulo crítico ou "Quebrado"
     */
    public String getCondicao() {
        int atual = getSaude();
        if (atual <= 0) {
            return "Quebrado";
        }
        if (atual <= LIMITE_MANUTENCAO) {
            return rotuloCritico;
        }
        if (atual <= LIMITE_DESGASTE) {
            return rotuloDesgastado;
        }
        return "OK";
    }

    /**
     * Mede o desgaste como fração, para os componentes calcularem seus efeitos.
     *
     * @return 0 quando novo e 1 quando quebrado
     */
    protected float getNivelDeDesgaste() {
        return (100.0f - saude) / 100.0f;
    }

    /**
     * Efeito do componente na chance de falha da máquina. Por padrão não altera.
     *
     * @return multiplicador maior ou igual a 1 aplicado à probabilidade de falha
     */
    public float calcularFatorDeFalha() {
        return 1.0f;
    }

    /**
     * Efeito do componente na peça processada. Por padrão não altera.
     *
     * @return risco de falha, entre 0 e 1, somado à peça a cada uso
     */
    public float calcularRiscoParaPeca() {
        return 0.0f;
    }

    /** @return uma linha com o nome, a condição e a saúde do componente */
    @Override
    public String gerarRelatorioDiagnostico() {
        return nome + ": " + getCondicao() + " (" + getSaude() + ")";
    }

    /** @return {@code true} se a saúde for igual ou inferior a trinta */
    @Override
    public boolean precisaManutencao() {
        return getSaude() <= LIMITE_MANUTENCAO;
    }
}