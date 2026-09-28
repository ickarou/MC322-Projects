/* Cenario.java
 *
 * Tarefa 3
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

public enum Cenario {
    
    /* Cenários */
    // Ordem: Nome, Budget, falha da maquina, desgaste por uso
    IDEAL("Ideal", 25000.0f, 0.5f, 1.0f),
    APOCALIPTICO("Apocalíptico", 10000.0f, 2.0f, 2.5f);

    private String nomeExibicao;
    private float budgetInicial;
    private float fatorProbabilidadeFalha;
    private float fatorDesgaste;

    Cenario(String nomeExibicao, float budgetInicial, float fatorProbabilidadeFalha, float fatorDesgaste){
        this.nomeExibicao = nomeExibicao;
        this.budgetInicial = budgetInicial;
        this.fatorProbabilidadeFalha = fatorProbabilidadeFalha;
        this.fatorDesgaste = fatorDesgaste;
    }

    public String getNomeExibicao() { return nomeExibicao; }
    public float getBudgetInicial() { return budgetInicial; }
    public float getFatorFalha() { return fatorProbabilidadeFalha; }
    public float getFatorDesgaste() { return fatorDesgaste; }
}