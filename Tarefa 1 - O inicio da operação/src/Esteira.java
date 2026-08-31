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
        System.out.println("Esteira Desigada, segura para manuseio");
    }

    public void adicionarItem(String idItem, int quantidadeDesejada){
        // Cláusula de guarda: Esteira desligada[cite: 1]
        if (!this.emMovimento){
            System.out.println("Erro: A esteira está desligada, necessário ligar");
            return;
        }

        // Cláusula de guarda: Esteira ocupada[cite: 1]
        if (this.item != null) {
            System.out.println("Erro: A esteira já contém um item (" + this.item + ").");
            return;
        }

        // Cláusula de guarda: Capacidade excedida[cite: 1]
        if (!verificarCapacidade(quantidadeDesejada)){
            System.out.println("Erro: Quantidade excede o limite máximo da esteira");
            return;
        }
        
        this.item = idItem;
        System.out.println("Item " + idItem + " foi colocado na esteira");
    }

    public String removerItem(){
        String itemRemovido = this.item;
        System.out.println("Item: " + this.item + " removido da esteira");
        this.item = null;
        return itemRemovido;
    }

    public boolean verificarCapacidade(int quantidadeDesejada){
        return quantidadeMaxima >= quantidadeDesejada;
    }

}