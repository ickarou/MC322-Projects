/*Esteira.java
*
* última modificação: 29/08/2026
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
    public Esteira(String item, boolean emMovimento, int quantidadeMaxima) {
        this.item = null;
        this.emMovimento = false;
        this.quantidadeMaxima = quantidadeMaxima;
    }

    /*Métodos*/
    public void ligar(){
        this.emMovimento = true;
        System.out.println("Esteira Ligada");
    }

    public void desligar(){
        this.emMovimento = false;
        System.out.println("Esteira Desigada");
    }

    public void adicionarItem(MateriaPrima mat_adicionada){
        if (!this.emMovimento){
            System.out.println("Erro: A esteira está desligada");
            return;
        }

        if (this.item != null) {
            System.out.println("Erro: A esteira já contém um item (" + this.item + ").");
            return;
        }

        if (quantidadeMaxima < mat_adicionada.getQuantidade()){
            System.out.println("Erro: Quantidade de matéria excede o limite máximo da esteira");
            return;
        }
            this.item = mat_adicionada.getNome();
            System.out.println("A matéria prima " + mat_adicionada.getId() + " foi colocada na esteira");
    }

    public String removerItem(){
        String itemRemovido = this.item;
        System.out.println("Item: " + this.item + " removido da esteira");
        this.item = null;
        return itemRemovido;
    }

    public boolean verificarCapacidade(MateriaPrima mat_desejada){
        return quantidadeMaxima >= mat_desejada.getQuantidade();
    }

}
