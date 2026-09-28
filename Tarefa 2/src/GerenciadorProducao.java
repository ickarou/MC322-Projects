/* Classe Gerenciador de Produção
 *
 * Tarefa 3
 *
 * última modificação: 28/09/2026
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.ArrayList;

public class GerenciadorProducao {

    /* Constantes */
    private static final float CUSTO_REPARO_POR_PONTO = 5.0f;

    /* Atributos Privados */
    private MateriaPrima materiaPrima;
    private float budget;
    private Cenario cenarioAtivo;
    private EstrategiaProducao estrategiaAtual;
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;
    private ArrayList<Produto> catalogoProdutos;
    private int contadorLotes;
    private int totalRejeitados;

    /* Construtor */
    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario) {
        this.materiaPrima = materiaPrima;
        this.budget = cenario.getBudgetInicial();
        this.cenarioAtivo = cenario;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.catalogoProdutos = new ArrayList<>();
        this.contadorLotes = 0;
        this.totalRejeitados = 0;
    }

    /* Consulta de cenario */
    public String getNomeCenarioAtivo() {
        return cenarioAtivo.getNomeExibicao();
    }

    /* Métodos de registro */
    public void registrarMaquina(Maquina novaMaquina) {
        this.maquinas.add(novaMaquina);
    }

    public void registrarProduto(Produto produto) {
        if (!this.catalogoProdutos.contains(produto)) {
            this.catalogoProdutos.add(produto);
        }
    }

    private Produto buscarProdutoPorTipo(String tipo) {
        for (Produto p : catalogoProdutos) {
            if (p.getTipo().equals(tipo)) {
                return p;
            }
        }
        return null;
    }

    //O custo unitário estimado considera apenas a operação das máquinas, pois a matéria-prima já foi paga na compra
    public void registrarDemanda(Demanda novaDemanda, Produto produtoAssociado) {
        registrarProduto(produtoAssociado);
        novaDemanda.definirCustoUnitarioEstimado(calcularCustoProducao(1));
        this.demandas.add(novaDemanda);
    }

    /* Metódos da estrategia */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        this.estrategiaAtual = novaEstrategia;
        System.out.println("Estratégia de produção alterada para: " + novaEstrategia.getNomeEstrategia());
    }

    public String getNomeEstrategiaAtual() {
        return (estrategiaAtual != null) ? estrategiaAtual.getNomeEstrategia() : "Nenhuma estratégia definida";
    }

    /* Métodos de produção e de demanda */
    public void fabricarDemanda(Produto produtoRequerido, Demanda demandaRequerida) {
        if (demandaRequerida.getStatus() != StatusDemanda.PENDENTE) {
            System.out.println("Esta demanda não está mais pendente (status atual: " + demandaRequerida.getStatus() + ")");
            return;
        }

        int totalPecas = demandaRequerida.getQuantidadeProdutos();
        if (totalPecas <= 0) {
            System.out.println("Não há pedido aberto para " + demandaRequerida.getTipoProduto() + ". Atualize a demanda primeiro.");
            return;
        }

        Maquina quebrada = buscarMaquinaQuebrada();
        if (quebrada != null) {
            System.out.println("Erro: a máquina " + quebrada.getNome() + " está quebrada. Envie para manutenção antes de produzir.");
            return;
        }

        int materiaPrimaNecessaria = demandaRequerida.calcularMateriaPrimaNecessaria(produtoRequerido);
        float custoTotal = calcularCustoProducao(totalPecas);

        // Verificação de custo
        if (custoTotal > this.budget) {
            System.out.printf("Erro: Orçamento insuficiente. Custo de operação: R$ %.2f | Disponível: R$ %.2f%n", custoTotal, this.budget);
            demandaRequerida.cancelar("Orçamento insuficiente");
            return;
        }

        // Verificação de quantidade de matéria-prima disponível
        if (!this.materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            System.out.println("Erro: Matéria-prima insuficiente");
            demandaRequerida.cancelar("Matéria-prima insuficiente");
            return;
        }

        demandaRequerida.iniciarProducao();
        this.budget -= custoTotal; // desconto
        this.materiaPrima.consumir(materiaPrimaNecessaria);

        String lote = gerarCodigoLote();

        for (Maquina m : this.maquinas) {
            m.ligar();
        }

        // Produção
        int processadas = 0;
        int aprovadas = 0;
        int rejeitadas = 0;

        for (int i = 0; i < totalPecas; i++) {
            if (buscarMaquinaQuebrada() != null) {
                System.out.println(">> Produção interrompida: uma máquina quebrou durante o lote " + lote + ".");
                break;
            }

            String idNovo = lote + "-" + produtoRequerido.getId() + "-" + (i + 1);
            Produto peca = produtoRequerido.criarNovaUnidade(idNovo);
            peca.setLote(lote);
            processadas++;

            if (processarNaLinha(peca)) {
                this.produtosFabricados.add(peca);
                aprovadas++;
            } else {
                peca.setStatus("Rejeitada na inspeção");
                rejeitadas++;
            }
        }

        for (Maquina m : this.maquinas) {
            m.desligar();
        }

        this.totalRejeitados += rejeitadas;

        // Peças que não chegaram a entrar na linha voltam ao orçamento e ao estoque
        int naoProcessadas = totalPecas - processadas;
        if (naoProcessadas > 0) {
            this.budget += calcularCustoProducao(naoProcessadas);
            this.materiaPrima.adicionarEstoque(naoProcessadas * produtoRequerido.getQuantidadeMateriaPrimaPorUnidade());
            System.out.println("Estorno de orçamento e matéria-prima referente a " + naoProcessadas + " peça(s) não processada(s).");
        }

        System.out.printf("Lote %s finalizado: %d aprovada(s), %d reprovada(s) na inspeção.%n", lote, aprovadas, rejeitadas);

        // Peças que faltaram permanecem como demanda aberta
        int faltantes = totalPecas - aprovadas;
        if (faltantes == 0) {
            demandaRequerida.atender();
        } else {
            demandaRequerida.atualizarQuantidade(-aprovadas);
            System.out.println("A demanda de " + demandaRequerida.getTipoProduto() + " segue PENDENTE com " + faltantes + " peça(s) a refazer.");
        }
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
            System.out.println("Erro: nenhum produto cadastrado para o tipo '" + escolhida.getTipoProduto() + "'");
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

    public void listarDemandas() {
        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda registrada.");
            return;
        }

        System.out.println("--- DEMANDAS REGISTRADAS ---");
        System.out.printf("%-3s | %-20s | %4s | %-12s | %s%n", "Nº", "Tipo", "Qtd", "Status", "Situação");

        int numero = 1;
        for (Demanda d : demandas) {
            System.out.printf("%-3d | %-20s | %4d | %-12s | %s%n",
                    numero++, d.getTipoProduto(), d.getQuantidadeProdutos(),
                    d.getStatus(), d.getStatus().getDescricao());
        }
    }

    /* Métodos financeiros */
    public void comprarMateriaPrima(int materiaAdicionada) {
        if (materiaAdicionada <= 0) {
            System.out.println("Erro: A quantidade de compra deve ser maior que zero");
            return;
        }

        float custoCompra = materiaAdicionada * this.materiaPrima.getCustoPorUnidade();

        if (this.budget >= custoCompra) {
            this.budget -= custoCompra; // desconto
            this.materiaPrima.adicionarEstoque(materiaAdicionada);
            System.out.println("Compra realizada com sucesso!");
        } 
        else {
            System.out.println("Erro: Orçamento insuficiente para comprar matéria-prima");
        }
    }

    public void exibirBudget() {
        System.out.println(String.format("║ BUDGET: R$ %-38.2f ║", budget));
    }

    private float calcularCustoProducao(int quantidadePecas) {
        float custoPorPeca = 0.0f;

        for (Maquina m : maquinas) {
            custoPorPeca += m.getCustoOperacao();
        }

        return custoPorPeca * quantidadePecas;
    }

    /* Manutenção das máquinas */
    public void repararMaquinas() {
        int precisavam = 0;

        for (Maquina m : maquinas) {
            if (!m.precisaManutencao()) {
                continue;
            }
            precisavam++;

            float custoReparo = (100 - m.getSaude()) * CUSTO_REPARO_POR_PONTO;
            if (this.budget >= custoReparo) {
                this.budget -= custoReparo; // desconto
                m.repararMaquina();
                System.out.printf("Reparo de %s custou R$ %.2f%n", m.getNome(), custoReparo);
            } else {
                System.out.printf("Orçamento insuficiente para reparar %s (custo: R$ %.2f)%n", m.getNome(), custoReparo);
            }
        }

        if (precisavam == 0) {
            System.out.println("Nenhuma máquina precisa de manutenção no momento.");
        }
    }

    private Maquina buscarMaquinaQuebrada() {
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) {
                return m;
            }
        }
        return null;
    }

    /* A peça segue pela linha até ser rejeitada por alguma máquina */
    private boolean processarNaLinha(Produto peca) {
        for (Maquina m : maquinas) {
            if (!m.processar(peca)) {
                return false;
            }
        }
        return true;
    }

    private String gerarCodigoLote() {
        this.contadorLotes++;
        return String.format("LOTE-%03d", this.contadorLotes);
    }

    /* Consultas + relatórios */

    private ArrayList<String> listarLotesUnicos() {
        ArrayList<String> lotes = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            if (!lotes.contains(p.getLote())) {
                lotes.add(p.getLote());
            }
        }
        return lotes;
    }

    // Retorna todas as peças de produtosFabricados que pertencem a um lote específico.
    private ArrayList<Produto> buscarPecasDoLote(String lote) {
        ArrayList<Produto> pecas = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            if (p.getLote().equals(lote)) {
                pecas.add(p);
            }
        }
        return pecas;
    }

    private String sinalizarRisco(Auditavel a) {
        return a.precisaManutencao() ? " [ATENÇÃO]" : "";
    }

    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazém vazio.");
            System.out.println("Peças reprovadas na inspeção (histórico): " + totalRejeitados);
            return;
        }

        System.out.println("--- ARMAZÉM DE PRODUTOS ACABADOS ---");
        System.out.printf("%-10s | %-20s | %4s | %-9s | %s%n", "Lote", "Tipo", "Qtd", "Qualidade", "Em risco");

        ArrayList<String> tiposEncontrados = new ArrayList<>();
        ArrayList<Integer> totaisPorTipo = new ArrayList<>();

        for (String lote : listarLotesUnicos()) {
            ArrayList<Produto> pecas = buscarPecasDoLote(lote);
            float somaQualidade = 0.0f;
            int emRisco = 0;

            for (Produto p : pecas) {
                somaQualidade += p.getQualidade();
                if (p.precisaManutencao()) {
                    emRisco++;
                }
            }

            String tipo = pecas.get(0).getTipo();
            System.out.printf("%-10s | %-20s | %4d | %-9.2f | %d/%d%s%n",
                    lote, tipo, pecas.size(), somaQualidade / pecas.size(),
                    emRisco, pecas.size(), (emRisco > 0 ? "  [RISCO]" : ""));

            int indice = tiposEncontrados.indexOf(tipo);
            if (indice == -1) {
                tiposEncontrados.add(tipo);
                totaisPorTipo.add(pecas.size());
            } else {
                totaisPorTipo.set(indice, totaisPorTipo.get(indice) + pecas.size());
            }
        }

        System.out.println("Totais por tipo:");
        for (int i = 0; i < tiposEncontrados.size(); i++) {
            System.out.printf("  %-20s %d un.%n", tiposEncontrados.get(i), totaisPorTipo.get(i));
        }
        System.out.println("Peças reprovadas na inspeção (histórico): " + totalRejeitados);
    }

    public void gerarAuditoriaGeral() {
        System.out.println("========== RELATÓRIO DE AUDITORIA ==========");

        System.out.println("-- Máquinas --");
        for (Auditavel a : maquinas) {
            System.out.println(a.gerarRelatorioDiagnostico() + sinalizarRisco(a));
        }

        System.out.println("-- Produtos em armazém --");
        int emRisco = 0;
        for (Auditavel a : produtosFabricados) {
            if (a.precisaManutencao()) {
                emRisco++;
            }
        }
        System.out.println("Peças em armazém: " + produtosFabricados.size()
                + " | Em risco: " + emRisco
                + " | Reprovadas na inspeção: " + totalRejeitados);

        System.out.println("=============================================");
    }

    public void detalharMaquinas() {
        System.out.println("--- DIAGNÓSTICO DAS MÁQUINAS ---");
        for (Maquina m : maquinas) {
            String situacao = m.estaQuebrada() ? "[QUEBRADA]"
                    : (m.precisaManutencao() ? "[MANUTENÇÃO NECESSÁRIA]" : "[OPERACIONAL]");
            System.out.println(m.getNome() + " (" + m.getTipo() + ") " + situacao);
            System.out.println("  " + m.gerarRelatorioDiagnostico());
        }
    }

    public void detalharProdutos() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto em armazém para detalhar.");
            return;
        }

        System.out.println("--- DIAGNÓSTICO DOS PRODUTOS ---");
        for (Produto p : produtosFabricados) {
            System.out.println(p.getLote() + " | " + p.getId() + sinalizarRisco(p));
            System.out.println("  " + p.gerarRelatorioDiagnostico());
        }
    }
}