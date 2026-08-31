/*Produto.java
*
* última modificação: 29/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Produto {
    
    /*Atributos privados */
    private String id;
    private String nome;
    private String status;
    private int quantidadeMateriaPrimaNecessaria;

    /*Construtor*/
    public Produto(String id, String nome, String status, int quantidadeMateriaPrimaNecessaria) {
        this.id = id;
        this.nome = nome;
        this.status = "Aguardando processametno";
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
    }

    /*Métodos*/
    public void processar(){
        this.status = "Processado";
    }

    public void definirDemandaMateriaPrima(int demanda){
        this.quantidadeMateriaPrimaNecessaria = demanda;
    }

    public int getDemandaMateriaPrima(){
        return quantidadeMateriaPrimaNecessaria;
    }

        public String getId(){
        return id;
    } 

    public String getNome(){
        return nome;
    }

    public String getStatus(){
        return status;
    }

}