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
    private Random random = new Random();

    /*Construtor*/
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, float custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        this.probabilidadeFalha = probabilidadeFalha;
        this.ligada = false;
    }

    /*Métodos Abstratos*/
    public abstract boolean processar(Produto produto);

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

    protected boolean verificarFalha() {
        return random.nextFloat() < this.probabilidadeFalha;
    }
    
}
