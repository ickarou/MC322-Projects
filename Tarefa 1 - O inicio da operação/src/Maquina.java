/*Maquina.java
*
* última modificação: 29/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Maquina {
    
    /*Atributos privados */
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;

    /*Construtor*/
    public Maquina(String nome, int capacidadeMaxima) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /*Métodos*/
    public void ligar(){
        this.ligada = true;
        System.out.println("Máquina " + nome + " ligada, afaste-se do equipamento");
    }

    public void desligar(){
        this.ligada = false;
        System.out.println("Máquina " + nome + " desligada, segura para manuseio");
    }
    
    public boolean processar(MateriaPrima materia_prima, int demanda){
        if (!ligada) {
            System.out.println("A máquina " + nome + " está desligada, segura para manuseio");
            return false;
        }

        if (demanda > capacidadeMaxima){
            System.out.println("A demanda é maior que a capacidade da máquina, incapaz de processar"); 
            return false;
        }

        if (!materia_prima.verificarDisponibilidade(demanda)) {
            System.out.println("Estoque insuficiente de " + materia_prima.getNome() + ",inserir mais");
            return false;
        }

        materia_prima.consumir(demanda);
        System.out.println("Processou " + demanda + " de " + materia_prima.getNome() + " com sucesso, a máquina está a todo vapor!");
        return true;
    }

    public String getNome(){
        return nome;
    }

    public boolean estaLigada(){
        return ligada;
    }
    
}


