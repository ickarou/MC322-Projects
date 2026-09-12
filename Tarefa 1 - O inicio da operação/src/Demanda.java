/*Demanda.java
*
* Tarefa 2
*
* última modificação: 12/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Demanda {
    
    /*Atributos privados*/
    private String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    /*Construtor*/
    public Demanda(String tipoProduto, int quantidadeProdutos){
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.atendida = false;
    }

    /*Métodos*/
    public void atualizarQuantidade(int quantidadeDemandada){
        this.quantidadeProdutos += quantidadeDemandada;
    }

    public int calcularMateriaPrimaNecessaria(Produto produtoDemandado){
        int totalNecessario = this.quantidadeProdutos * produtoDemandado.getQuantidadeMateriaPrimaPorUnidade();
        return totalNecessario;
    }

    public void atender(){
        this.atendida = true;
        System.out.println("Solicitação atendida! Deixe sua avaliação do sistema ao fim do processo.");
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public boolean foiAtendida() {
        return atendida;
    }
}