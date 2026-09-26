/* Classe Gerenciador de Produção
*
* Tarefa 3
*
* última modificação: 26/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

import java.util.ArrayList;

public class GerenciadorProducao {

    /*Atributos Privados*/
    private MateriaPrima materiaPrima;
    private float budget;
    private String cenarioAtivo;
    private EstrategiaProducao estrategiaAtual;
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;
    private ArrayList<Produto> catalogoProdutos;
    

    /*Construtor*/
    public GerenciadorProducao(MateriaPrima materiaPrima, float budget, String cenarioAtivo) {
        this.materiaPrima = materiaPrima;
        this.budget = budget;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.catalogoProdutos = new ArrayList<>();
        this.cenarioAtivo = cenarioAtivo;
    }

    /*Consulta de cenario*/

    public String getNomeCenarioAtivo() {
        return cenarioAtivo;
    }

    /*Métodos de registro*/

    public void registrarMaquina(Maquina novaMaquina) {
        this.maquinas.add(novaMaquina);
    }

    public void registrarProduto(Produto produto) {
        this.catalogoProdutos.add(produto);
    }

    private Produto buscarProdutoPorTipo(String tipo) {
        for (Produto p : catalogoProdutos) {
            if (p.getTipo().equals(tipo)) {
                return p;
            }
        }
        return null;
    }

    public void registrarDemanda(Demanda novaDemanda, Produto produtoAssociado) {
        double custoOperacaoUnitario = calcularCustoProducao(1);
        double custoMateriaPrimaUnitario = produtoAssociado.getQuantidadeMateriaPrimaPorUnidade() * this.materiaPrima.getCustoPorUnidade();

        novaDemanda.definirCustoUnitarioEstimado(custoOperacaoUnitario + custoMateriaPrimaUnitario);
        this.demandas.add(novaDemanda);
    }

    /*Metódos da estrategia*/

    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        this.estrategiaAtual = novaEstrategia;
        System.out.println("Estratégia de produção alterada para: " + novaEstrategia.getNomeEstrategia());
    }

    public String getNomeEstrategiaAtual() {
        return (estrategiaAtual != null) ? estrategiaAtual.getNomeEstrategia() : "Nenhuma estratégia definida";
    }

    /*Métodos de produção e de demanda*/

    public void fabricarDemanda(Produto produtoRequerido, Demanda demandaRequerida) {
        if (demandaRequerida.getStatus() != StatusDemanda.PENDENTE) {
            System.out.println("Esta demanda não está mais pendente (status atual: " + demandaRequerida.getStatus() + ")");
            return;
        }

        int totalPecas = demandaRequerida.getQuantidadeProdutos();
        int materiaPrimaNecessaria = demandaRequerida.calcularMateriaPrimaNecessaria(produtoRequerido);
        float custoTotal = calcularCustoProducao(totalPecas);

        demandaRequerida.iniciarProducao();

        //Verificação de custo
        if (!demandaRequerida.viavelFinanceiramente(this.budget)) {
            System.out.println("Erro: Orçamento insuficiente para cobrir o custo de produção (R$ " + custoTotal + ")");
            demandaRequerida.cancelar("Orçamento insuficiente");
            return;
        }

        //Verificação de quantidade de matéria-prima disponível
        if (!this.materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            System.out.println("Erro: Matéria-prima insuficiente");
            demandaRequerida.cancelar("Matéria-prima insuficiente");
            return;
        }

        this.budget -= custoTotal; //desconto
        this.materiaPrima.consumir(materiaPrimaNecessaria);

        for (Maquina m : this.maquinas) {
            m.ligar();
        }

        //Produção
        for (int i = 0; i < totalPecas; i++) {
            String idNovo = produtoRequerido.getId() + "-" + i;
            Produto peca = produtoRequerido.criarNovaUnidade(idNovo);

            for (Maquina m : this.maquinas) {
                m.processar(peca);
                m.aplicarDesgaste();
            }
            this.produtosFabricados.add(peca);
        }

        for (Maquina m : this.maquinas) {
            m.desligar();
        }

        demandaRequerida.atender();
    }

    public void executarProximaProducao() {
        if (estrategiaAtual == null) {
            System.out.println("Erro: Nenhuma estratégia de produção foi definida.");
            return;
        }

        Demanda escolhida = estrategiaAtual.selecionarDemanda(this.demandas, this.budget);

        if (escolhida == null) {
            System.out.println("Nenhuma demanda elegível para produção no momento");
            return;
        }

        Produto produto = buscarProdutoPorTipo(escolhida.getTipoProduto());
        if (produto == null) {
            System.out.println("Erro: nenhum produto cadastrado para o tipo '" + escolhida.getTipoProduto());
            return;
        }

        fabricarDemanda(produto, escolhida);
    }

    public void atualizarDemanda(String tipoProduto, int quantidadeExtra) {
        boolean encontrou = false;

        for (Demanda d : demandas) {
            if (d.getTipoProduto().equals(tipoProduto) && d.getStatus() == StatusDemanda.PENDENTE) {
                d.atualizarQuantidade(quantidadeExtra);
                System.out.println("Sucesso! Demanda de " + tipoProduto + " atualizada.");
                encontrou = true;
                break;
            }
        }

        if (!encontrou) {
            System.out.println("Aviso: Nenhuma demanda PENDENTE encontrada para o produto " + tipoProduto);
        }
    }

    /*Métodos financeiros*/

    public void comprarMateriaPrima(int materiaAdicionada) {
        float custoCompra = materiaAdicionada * this.materiaPrima.getCustoPorUnidade();

        if (this.budget >= custoCompra) {
            this.budget -= custoCompra; //desconto
            this.materiaPrima.adicionarEstoque(materiaAdicionada);
            System.out.println("Compra realizada com sucesso!");
        } 
        else {
            System.out.println("Erro: Orçamento insuficiente para comprar matéria-prima");
        }
    }

    public void exibirBudget() {
        System.out.println("O budget atual é: R$" + budget);
    }

    private float calcularCustoProducao(int quantidadePecas) {
        float custoPorPeca = 0.0f;

        for (Maquina m : maquinas) {
            custoPorPeca += m.getCustoOperacao();
        }

        return custoPorPeca * quantidadePecas;
    }

    /*Consultas + relatórios*/

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazém vazio.");
            return;
        }

        System.out.println("--- ARMAZÉM DE PRODUTOS ACABADOS ---");
        for (Produto p : produtosFabricados) {
            String alerta = p.precisaManutencao() ? " [RISCO ALTO]" : "";
            System.out.printf(
                    "Lote: %-15s | Tipo: %-20s | Qualidade: %.2f%s%n",
                    p.getId(), p.getTipo(), p.getQualidade(), alerta
            );
        }
    }

    public void gerarAuditoriaGeral() {
        System.out.println("========== RELATÓRIO DE AUDITORIA ==========");

        System.out.println("-- Máquinas --");
        for (Maquina m : maquinas) {
            System.out.println(m.gerarRelatorioDiagnostico());
        }

        System.out.println("-- Produtos em armazém --");
        for (Produto p : produtosFabricados) {
            System.out.println(p.gerarRelatorioDiagnostico());
        }

        System.out.println("=============================================");
    }
}