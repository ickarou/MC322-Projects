/*Esteira.java
*
* última modificação: 31/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Esteira {

    /*Atributos privados*/
    private String item;
    private boolean emMovimento;
    private int quantidadeMaxima;

    /*Construtor*/
    public Esteira(int quantidadeMaxima) {
        this.item = null;
        this.emMovimento = false;
        this.quantidadeMaxima = quantidadeMaxima;
    }

    /*Métodos*/
    public void ligar(){
        this.emMovimento = true;
        System.out.println("Esteira Ligada, afaste-se do equipamento");
    }

    public void desligar(){
        this.emMovimento = false;
        System.out.println("Esteira Desligada, segura para manuseio");
    }

    public void adicionarItem(String idItem, int quantidadeDesejada){

        if (!this.emMovimento){
            System.out.println("Erro: A esteira está desligada, necessário ligar");
            return;
        }

        if (this.item != null) {
            System.out.println("Erro: A esteira já contém um produto (" + this.item + ").");
            return;
        }

        if (!verificarCapacidade(quantidadeDesejada)){
            System.out.println("Erro: Quantidade excede o limite máximo da esteira");
            return;
        }
        
        this.item = idItem;
        System.out.println("Produto " + idItem + " foi colocado na esteira");
    }

    public String removerItem() {
        if (this.item == null) {
            System.out.println("A esteira está vazia!");
            return null;
        }
    String itemRemovido = this.item;
    System.out.println("Produto: " + itemRemovido + " removido da esteira.");
    this.item = null;
    return itemRemovido;
    }

    public boolean verificarCapacidade(int quantidadeDesejada){
        return quantidadeMaxima >= quantidadeDesejada;
    }

}