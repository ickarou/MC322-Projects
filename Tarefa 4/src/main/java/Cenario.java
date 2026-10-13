/* Cenario.java
 *
 * Tarefa 4
 *
 * Material para a disciplina MC322 - Programacao orientada a objetos
 *
 */

/** Configuracoes iniciais de orcamento, falha e desgaste da planta.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public enum Cenario {
    /** Orcamento maior e maquinas em condicoes favoraveis. */
    IDEAL("Ideal", 25000.0f, 0.5f, 1.0f),
    /** Orcamento reduzido e desgaste mais intenso dos equipamentos. */
    APOCALIPTICO("Apocaliptico", 4000.0f, 2.0f, 2.5f);

    private final String nomeExibicao;
    private final float budgetInicial;
    private final float fatorProbabilidadeFalha;
    private final float fatorDesgaste;

    Cenario(String nomeExibicao, float budgetInicial,
            float fatorProbabilidadeFalha, float fatorDesgaste) {
        this.nomeExibicao = nomeExibicao;
        this.budgetInicial = budgetInicial;
        this.fatorProbabilidadeFalha = fatorProbabilidadeFalha;
        this.fatorDesgaste = fatorDesgaste;
    }

    /** Consulta o nome exibido no menu.
     * @return nome do cenario para exibicao
     */
    public String getNomeExibicao() { return nomeExibicao; }

    /** Consulta o orcamento inicial definido para o cenario.
     * @return orcamento inicial da planta
     */
    public float getBudgetInicial() { return budgetInicial; }

    /** Consulta o fator aplicado a probabilidade de falha.
     * @return multiplicador de probabilidade de falha
     */
    public float getFatorFalha() { return fatorProbabilidadeFalha; }

    /** Consulta o fator aplicado ao desgaste.
     * @return multiplicador de desgaste por operacao
     */
    public float getFatorDesgaste() { return fatorDesgaste; }
}