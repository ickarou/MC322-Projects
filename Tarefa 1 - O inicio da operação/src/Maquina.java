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
    public Maquina(String nome, boolean ligada, int capacidadeMaxima) {
        this.nome = nome;
        this.ligada = false;
        this.capacidadeMaxima = capacidadeMaxima;
    }

    /*Métodos*/
    public void ligar(){
        this.ligada = true;
    }

    public void desligar(){
        this.ligada = false;
    }
    
    public boolean processar(MateriaPrima materia_prima, int demanda){
        if (!ligada) {
            System.out.println("A maquina " + nome + " está desligada");
            return false;
        }

        if (demanda > capacidadeMaxima){
            System.out.println("A demanda é maior que a capacidade da máquina"); //@edu vamo revisar depois as mensagens, vale nota por criatividade :(
            return false;
        }

        if (!materia_prima.verificarDisponibilidade(demanda)) {
            System.out.println("Estoque insuficiente de " + materia_prima.getNome());
            return false;
        }

        materia_prima.consumir(demanda);
        System.out.println("Processou " + demanda + " de " + materia_prima.getNome());
        return true;
    }

    public String getNome(){
        return nome;
    }

    public boolean estaLigada(){
        return ligada;
    }
    
}


