/* Classe Abstrata Produto
*
* Tarefa 3
*
* última modificação: 24/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public abstract class Produto implements Auditavel{
    
    //Atributos privados
    private String id;
    private String nome;
    private String status;
    private int quantidadeMateriaPrimaPorUnidade;
    private float qualidade;
    private float probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados = 0; 

    //Construtor
    public Produto(String id, String nome, int quantidadeMateriaPrimaPorUnidade, float qualidade) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;

        this.status = "Aguardando injeção";
        this.probabilidadeFalhaAcumulada = 0.0f;

        totalProdutosFabricados++;
    }

    //Métodos Abstratos
    public abstract void processar();

    public abstract int calcularTempoProducao();

    public abstract String getTipo();

    public abstract Produto criarNovaUnidade(String id);

    //Métodos Concretos
    public void setStatus(String newStatus){
        this.status = newStatus;
    }

    public void aumentarProbabilidadeFalha(float probabilidadeMaquina){
        this.probabilidadeFalhaAcumulada += probabilidadeMaquina;
    }
    
    //Getters
    public String getId(){
        return id;
    } 

    public String getNome(){
        return nome;
    }

    public String getStatus(){
        return status;
    }

    public int getQuantidadeMateriaPrimaPorUnidade(){
        return quantidadeMateriaPrimaPorUnidade;
    }

    public float getQualidade(){
        return qualidade;
    }

    public float getProbabilidadeFalhaAcumulada(){
        return probabilidadeFalhaAcumulada;
    }

    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }

    //Implementação da interface Auditavel
    @Override 
    public String gerarRelatorioDiagnostico(){
        return "O produto " + this.nome + " de qualidade: " + this.qualidade + " está em " + (this.probabilidadeFalhaAcumulada * 100) + "% de falhar";
    }

    @Override 
    public boolean precisaManutencao(){
        return this.probabilidadeFalhaAcumulada >= 0.5f;
    }
}