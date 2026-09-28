/* Cenario.java
 *
 * Tarefa 3
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

public enum Cenario {
    
    /* Cenários */
    // Ordem: Budget, falha da maquina, desgaste por uso
    IDEAL(20000.0f, 0.5f, 1.0f),
    APOCALIPTICO(5000.0f, 2.0f, 2.5f);

    private float budgetInicial;
    private float fatorProbabilidadeFalha;
    private float fatorDesgaste;

    Cenario(float budgetInicial, float fatorProbabilidadeFalha, float fatorDesgaste){
        this.budgetInicial = budgetInicial;
        this.fatorProbabilidadeFalha = fatorProbabilidadeFalha;
        this.fatorDesgaste = fatorDesgaste;
    }

    public float getBudgetInicial() { return budgetInicial; }
    public float getFatorFalha() { return fatorProbabilidadeFalha; }
    public float getFatorDesgaste() { return fatorDesgaste; }
}