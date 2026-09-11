/* Classe Abstrata Máquina
*
* Tarefa 2
*
* última modificação: 11/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

import java.util.Random;

public abstract class Maquina {
    
    /*Atributos privados */
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private float custoOperacao;

    /*Construtor*/
    public Maquina(String nome, int capacidadeMaxima, float custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        
        this.probabilidadeFalha = 0.0f;
        this.ligada = false;
    }

    /*Métodos Abstratos*/
    public abstract boolean processar(MateriaPrima materiaPrima, int demanda);

    public abstract String getTipo();

    /*Métodos Concretos*/
    public void ligar(){
        this.ligada = true;
        System.out.println("Máquina " + nome + " ligada, afaste-se do equipamento");
    }

    public void desligar(){
        this.ligada = false;
        System.out.println("Máquina " + nome + " desligada, segura para manuseio");
    }

    public boolean estaLigada(){
        return ligada;
    }

    public String getNome(){
        return nome;
    }

    public float getCustoOperacao(){
        return custoOperacao;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    protected boolean verificarFalha(float probabilidadeAtual){
        Random random = new Random();
        
        float sorteio = random.nextFloat();

        /*Se o número sorteado cair dentro da faixa de falha a máquina vai falhar */
        if (sorteio < probabilidadeAtual) {
            return true; // A máquina falhou
        }
        
        return false; // Sem falha
    }
    
}
