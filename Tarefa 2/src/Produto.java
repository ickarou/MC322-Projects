/* Classe Abstrata Produto
*
* Tarefa 2
*
* última modificação: 10/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public abstract class Produto {
    
    /*Atributos privados*/
    private String id;
    private String nome;
    private String status;
    private int quantidadeMateriaPrimaPorUnidade;
    private float qualidade;
    private float probabilidadeFalhaAcumulada;
    private static int totalProdutosFabricados = 0; 

    /*Construtor*/
    public Produto(String id, String nome, int quantidadeMateriaPrimaPorUnidade, float qualidade) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;

        this.status = "Aguardando injeção";
        this.probabilidadeFalhaAcumulada = 0.0f;

        totalProdutosFabricados++;
    }

    /*Métodos Abstratos*/
    public abstract void processar();

    public abstract int calcularTempoProducao();

    public abstract String getTipo();

    /*Métodos Concretos*/
    public String getId(){
        return id;
    } 

    public String getNome(){
        return nome;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String newStatus){
        this.status = newStatus;
    }

    public int getQuantidadeMateriaPrimaPorUnidade(){
        return quantidadeMateriaPrimaPorUnidade;
    }

    public void aumentarProbabilidadeFalha(float probabilidadeMaquina){
        this.probabilidadeFalhaAcumulada += probabilidadeMaquina;
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
}