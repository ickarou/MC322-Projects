/*MateriaPrima.java
*
* última modificação: 29/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class MateriaPrima {

    /*Atributos privados*/
    private String id;
    private String nome;
    private int quantidade;
    private String unidade;
    private int quantidadeMinima;

    /*Construtor*/
    public MateriaPrima(String id, String nome, int quantidade, String unidade, int quantidadeMinima) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
    }


    /*Métodos*/
    public void consumir(int qtd_demandada){
        this.quantidade -= qtd_demandada;
    }

    public void adicionarEstoque(int qtd_adicionada){
        this.quantidade += qtd_adicionada;
    }

    public boolean verificarDisponibilidade(int demanda){
        return this.quantidade >= demanda;
    }

    public String getId(){
        return id;
    } 

    public String getNome(){
        return nome;
    }

    public int getQuantidade(){
        return quantidade;
    }

    public String getUnidade(){
        return unidade;
    }
}  