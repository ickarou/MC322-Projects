/* Classe Gerenciador de Produção
*
* Tarefa 2
*
* última modificação: 13/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

import java.util.ArrayList;

public class GerenciadorProducao {
    
    /*Atributos Privados*/
    private MateriaPrima materiaPrima;
    private float budget;
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;

    /*Construtor*/
    public GerenciadorProducao(MateriaPrima materiaPrima, float budget){
        this.materiaPrima = materiaPrima;
        this.budget = budget;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
    }

    /*Métodos*/
    public void registrarDemanda(Demanda novaDemanda){
        this.demandas.add(novaDemanda);
    }

    public void registrarMaquina(Maquina novaMaquina){
        this.maquinas.add(novaMaquina);
    }

    public void atualizarDemanda(String tipoProduto, int quantidadeExtra) {
        boolean encontrou = false;

        for (Demanda d : demandas) {
            if (d.getTipoProduto().equals(tipoProduto) && !d.foiAtendida()) {
                d.atualizarQuantidade(quantidadeExtra);
                System.out.println("Sucesso! Demanda de " + tipoProduto + " atualizada");
                encontrou = true;
                break; 
            }
        }
    
        if (!encontrou) {
            System.out.println("Aviso: Nenhuma demanda pendente encontrada para o produto " + tipoProduto);
        }
    }

    public void comprarMateriaPrima (int materiaAdicionada){
        float custoCompra = materiaAdicionada * this.materiaPrima.getCustoPorUnidade();
    
        if (this.budget >= custoCompra) {
            this.budget -= custoCompra;
            this.materiaPrima.adicionarEstoque(materiaAdicionada);
            System.out.println("Compra realizada com sucesso!");
        } 
        else {
        System.out.println("Erro: Orçamento insuficiente para comprar matéria-prima.");
        }
    }

    public void exibirBudget(){
        System.out.println("O budget atual é: R$" + budget);
    }

    private float calcularCustoProducao(int quantidadePecas) {
        float custoPorPeca = 0.0f; 

        for (Maquina m : maquinas) {
            custoPorPeca += m.getCustoOperacao();
        }

        return custoPorPeca * quantidadePecas; 
    }

    public void fabricarDemanda(Produto produtoRequerido, Demanda demandaRequerida){
       if (demandaRequerida.foiAtendida()){
            System.out.println("Esta demanda já foi atendida!");
            return;
       }

       int totalPecas = demandaRequerida.getQuantidadeProdutos();
       int materiaPrimaNecessaria = demandaRequerida.calcularMateriaPrimaNecessaria(produtoRequerido);
       float custoTotal = calcularCustoProducao(totalPecas);
       
       //Verificação de custo
       if (this.budget < custoTotal) {
            System.out.println("Erro: Orçamento insuficiente para cobrir o custo de produção (R$ " + custoTotal + ").");
            return;
       }

       //Verificação de quantidade de materia prima disponivel
        if (!this.materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            System.out.println("Erro: Matéria-prima insuficiente.");
            return;
        }

        this.budget -= custoTotal; //desconto
        this.materiaPrima.consumir(materiaPrimaNecessaria);

        for (Maquina m : this.maquinas) {
            m.ligar();
        }

        //Produção
        for (int i = 0; i < totalPecas; i++) {
            Produto peca = null;

            String idNovo = produtoRequerido.getId() + "-" + i;
            String tipoReq = produtoRequerido.getTipo();

            // Instancia passando APENAS o ID, como exigido pelos seus construtores
            switch (tipoReq) {
                case "Carcaça Superior":
                    peca = new CarcacaSuperior(idNovo);
                    break;
                case "Carcaça Inferior":
                    peca = new CarcacaInferior(idNovo);
                    break;
                case "Proteção Lateral":
                    peca = new ProtecaoLateral(idNovo);
                    break;
            }
            
            if (peca != null) {
                for (Maquina m : this.maquinas) {
                    m.processar(peca);
                }
                this.produtosFabricados.add(peca);
            }
        }

        for (Maquina m : this.maquinas) {
            m.desligar();
        }

        demandaRequerida.atender();
    }

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazém vazio");
            return;
        }

        System.out.print("Itens no armazém: ");
        for (Produto p : produtosFabricados) {
            System.out.printf("%s, ", p.getNome());
        }
        System.out.println(); 
    }
}
