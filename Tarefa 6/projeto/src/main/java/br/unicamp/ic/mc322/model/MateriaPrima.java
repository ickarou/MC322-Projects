/** Controla o estoque e o custo de um tipo de materia-prima.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class MateriaPrima {

    private final String id;
    private final String nome;
    private int quantidade;
    private final String unidade;
    private final float custoPorUnidade;

    /**
     * Cadastra uma materia-prima.
     *
     * @param id identificador do material
     * @param nome nome do material
     * @param quantidade estoque inicial
     * @param unidade unidade usada no estoque
     * @param custoPorUnidade custo de cada unidade comprada
     * @throws IllegalArgumentException se algum dado for invalido
     */
    public MateriaPrima(String id, String nome, int quantidade, String unidade, float custoPorUnidade) {
        if (id == null || id.isBlank() || nome == null || nome.isBlank() || unidade == null || unidade.isBlank()) {
            throw new IllegalArgumentException("ID, nome e unidade da materia-prima devem ser informados.");
        }
        if (quantidade < 0 || !Float.isFinite(custoPorUnidade) || custoPorUnidade < 0) {
            throw new IllegalArgumentException("Estoque e custo devem ser valores nao negativos e validos.");
        }
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.custoPorUnidade = custoPorUnidade;
    }

    /**
     * Retira materia-prima do estoque.
     *
     * @param qtdDemandada quantidade a consumir
     * @throws IllegalArgumentException se a quantidade nao for positiva
     * @throws RecursoInsuficienteException se o estoque nao cobrir a quantidade solicitada
     */
    public void consumir(int qtdDemandada) {
        if (qtdDemandada <= 0) {
            throw new IllegalArgumentException("A quantidade consumida deve ser maior que zero.");
        }
        if (qtdDemandada > quantidade) {
            throw new RecursoInsuficienteException("Estoque insuficiente de " + nome + ": solicitado "
                    + qtdDemandada + " " + unidade + ", disponivel " + quantidade + " " + unidade + ".");
        }
        quantidade -= qtdDemandada;
    }

    /**
     * Acrescenta unidades ao estoque.
     *
     * @param qtdAdicionada quantidade a adicionar
     * @throws IllegalArgumentException se a quantidade nao for positiva
     * @throws RecursoInsuficienteException se o total ultrapassar o limite numerico
     */
    public void adicionarEstoque(int qtdAdicionada) {
        if (qtdAdicionada <= 0) {
            throw new IllegalArgumentException("A quantidade adicionada deve ser maior que zero.");
        }
        try {
            quantidade = Math.addExact(quantidade, qtdAdicionada);
        } catch (ArithmeticException e) {
            throw new RecursoInsuficienteException("A quantidade em estoque excederia o limite permitido.");
        }
    }

    /**
     * Verifica se o estoque cobre uma demanda.
     *
     * @param demanda quantidade solicitada
     * @return {@code true} se houver quantidade suficiente
     * @throws IllegalArgumentException se a demanda for negativa
     */
    public boolean verificarDisponibilidade(int demanda) {
        if (demanda < 0) {
            throw new IllegalArgumentException("A quantidade consultada nao pode ser negativa.");
        }
        return quantidade >= demanda;
    }

    /** Consulta o identificador cadastrado.
     * @return identificador do material
     */
    public String getId() {
        return id;
    }

    /** Consulta o nome do material.
     * @return nome do material
     */
    public String getNome() {
        return nome;
    }

    /** Consulta o saldo atual do estoque.
     * @return quantidade disponivel em estoque
     */
    public int getQuantidade() {
        return quantidade;
    }

    /** Consulta a unidade do material.
     * @return unidade usada para controlar o estoque
     */
    public String getUnidade() {
        return unidade;
    }

    /** Consulta o custo unitario de compra.
     * @return custo de compra por unidade
     */
    public float getCustoPorUnidade() {
        return custoPorUnidade;
    }
}