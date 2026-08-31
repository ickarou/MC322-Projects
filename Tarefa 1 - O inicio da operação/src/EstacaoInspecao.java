/*EstacaoInspecao.java
*
* última modificação: 31/08/2026
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
        this.ativa = true;
        System.out.println("Estação de Inspeção Ligada!");
    }

    public void desativar(){
        this.ativa = false;
        System.out.println("Estação Desligada!");
    }

    // A estação recebe apenas o nome do produto a ser inspecionado
    public void inspecionar(String produto){
        // Cláusula de guarda: Estação desativada[cite: 1]
        if (!ativa){
            System.out.println("Erro: Estação está desligada, ligue para inspecionar produtos");
            return;
        }

        if (produto != null){
            this.produtosInspecionados++;
            System.out.println("Item '" + produto + "' inspecionado, pronto para ser embalado e vendido!");
        }
        else {
            System.out.println("Sem itens para inspecionar");
        }
    }

    public int getTotalInspecionados() {
        return this.produtosInspecionados;
    }
}