/*EstacaoInspecao.java
*
* última modificação: 29/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class EstacaoInspecao {

    /*Atributos privados*/
    private boolean ativa;
    private int produtosInspecionados;

    /*Construtor*/
    public EstacaoInspecao() {
        this.ativa = false;
        this.produtosInspecionados = 0;
    }

    /*Métodos*/
    public void ativar(){
        ativa = true;
        System.out.println("Estação Ligada!");
    }

    public void desativar(){
        ativa = false;
        System.out.println("Estação Desligada!");
    }

    public void inspecionar(Esteira produto_inspecao){
        if (!ativa){
            System.out.println("Erro: Estação está desligada");
            return;
        }

        String itemRemovido = produto_inspecao.removerItem();

        if (itemRemovido != null){
            this.produtosInspecionados++;
            System.out.println("Item '" + itemRemovido + "' inspecionado");
        }
        else System.out.println("Sem itens para inspecionar");
    }

    public int getProdutosInspecionados() {
        return this.produtosInspecionados;
    }
}