import java.util.ArrayList;

/**
 * Orquestra todas as operacoes e regras de negocio da planta industrial.
 * <p>
 * O gerenciador e responsavel por coordenar as demandas, aplicar as estrategias 
 * de producao, gerir o orcamento (budget), controlar o consumo de materia-prima 
 * e monitorar o estado e auditoria das maquinas e produtos.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
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

    /**
     * Instancia o gerenciador central da fabrica.
     * 
     * @param materiaPrima O objeto de controle de estoque do insumo principal.
     * @param cenario O cenario operacional que dita o orcamento base e as falhas.
     */
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

    public String getNomeCenarioAtivo() {
        return cenarioAtivo.getNomeExibicao();
    }

    /**
     * Adiciona uma nova maquina a linha de montagem da fabrica.
     * 
     * @param novaMaquina O equipamento a ser integrado.
     */
    public void registrarMaquina(Maquina novaMaquina) {
        this.maquinas.add(novaMaquina);
    }

    /**
     * Cadastra um novo tipo de produto no catalogo, caso ainda nao exista.
     * 
     * @param produto O molde do produto a ser cadastrado.
     */
    public void registrarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto a ser cadastrado deve ser informado.");
        }
        if (buscarProdutoPorTipo(produto.getTipo()) == null) {
            this.catalogoProdutos.add(produto);
        }
    }

    /**
     * Localiza o modelo de produto que corresponde ao tipo informado.
     *
     * @param tipo tipo procurado
     * @return modelo cadastrado ou {@code null} se nao houver correspondencia
     */
    private Produto buscarProdutoPorTipo(String tipo) {
        for (Produto p : catalogoProdutos) {
            if (p.getTipo().equals(tipo)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Associa uma nova demanda de producao e calcula o seu custo estimado.
     * 
     * @param novaDemanda O pedido a ser registrado.
     * @param produtoAssociado O molde do produto referente a esta demanda.
     * @throws DemandaInvalidaException se os dados nao forem compativeis ou a demanda ja estiver registrada
     */
    public void registrarDemanda(Demanda novaDemanda, Produto produtoAssociado) {
        if (novaDemanda == null || produtoAssociado == null) {
            throw new DemandaInvalidaException("A demanda e o produto associado devem ser informados.");
        }
        if (!novaDemanda.getTipoProduto().equals(produtoAssociado.getTipo())) {
            throw new DemandaInvalidaException("O tipo do produto nao corresponde ao tipo da demanda.");
        }
        if (this.demandas.contains(novaDemanda)) {
            throw new DemandaInvalidaException("Essa demanda ja esta registrada.");
        }
        registrarProduto(produtoAssociado);
        novaDemanda.definirCustoUnitarioEstimado(calcularCustoProducao(1));
        this.demandas.add(novaDemanda);
    }

    /**
     * Define a estrategia ativa de selecao de demandas.
     * 
     * @param novaEstrategia A nova logica de decisao a ser adotada.
     * @throws IllegalArgumentException Se a estrategia for nula.
     */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia == null) {
            throw new IllegalArgumentException("A estrategia de producao deve ser informada.");
        }
        this.estrategiaAtual = novaEstrategia;
        System.out.println("Estrategia de producao alterada para: " + novaEstrategia.getNomeEstrategia());
    }

    /** Consulta a estrategia configurada.
     * @return nome da estrategia ativa ou uma mensagem caso ainda nao exista estrategia
     */
    public String getNomeEstrategiaAtual() {
        return (estrategiaAtual != null) ? estrategiaAtual.getNomeEstrategia() : "Nenhuma estrategia definida";
    }

    /**
     * Executa a producao forcada de um lote especifico, validando todos os recursos antes de iniciar.
     * <p>
     * Garante a consistencia do estado: orcamentos e estoques so sao reduzidos se todas as 
     * precondicoes de maquinas, dinheiro e materia-prima forem atendidas.
     * </p>
     * 
     * @param produtoRequerido O molde do produto a ser fabricado.
     * @param demandaRequerida A demanda pendente correspondente.
     * @throws DemandaInvalidaException se a demanda nao estiver PENDENTE ou nao houver pecas solicitadas.
     * @throws MaquinaIndisponivelException se alguma maquina da linha estiver quebrada.
     * @throws OrcamentoInsuficienteException se o budget nao cobrir o custo de operacao do lote.
     * @throws RecursoInsuficienteException se faltar materia-prima no almoxarifado.
     */
    public void fabricarDemanda(Produto produtoRequerido, Demanda demandaRequerida) {
        if (demandaRequerida.getStatus() != StatusDemanda.PENDENTE) {
            throw new DemandaInvalidaException("Esta demanda nao este pendente (status atual: " + demandaRequerida.getStatus() + ").");
        }

        int totalPecas = demandaRequerida.getQuantidadeProdutos();
        if (totalPecas <= 0) {
            throw new DemandaInvalidaException("Nao ha pedido aberto para " + demandaRequerida.getTipoProduto() + ". Atualize a demanda primeiro.");
        }

        Maquina quebrada = buscarMaquinaQuebrada();
        if (quebrada != null) {
            throw new MaquinaIndisponivelException("A maquina " + quebrada.getNome() + " este quebrada. Envie para manutencao antes de produzir.");
        }

        int materiaPrimaNecessaria = demandaRequerida.calcularMateriaPrimaNecessaria(produtoRequerido);
        float custoTotal = calcularCustoProducao(totalPecas);

        // Verificacao estrita de recursos antes de qualquer alteracao de estado
        if (custoTotal > this.budget) {
            demandaRequerida.cancelar("Orcamento insuficiente");
            throw new OrcamentoInsuficienteException(String.format("Orcamento insuficiente. Custo de operacao: R$ %.2f | Disponivel: R$ %.2f", custoTotal, this.budget));
        }

        if (!this.materiaPrima.verificarDisponibilidade(materiaPrimaNecessaria)) {
            demandaRequerida.cancelar("Materia-prima insuficiente");
            throw new RecursoInsuficienteException("Falta materia-prima (" + this.materiaPrima.getNome() + ") para fabricar o lote solicitado.");
        }

        // Estado e alterado somente apos passar por todas as validacoes de seguranca
        demandaRequerida.iniciarProducao();
        this.budget -= custoTotal; 
        this.materiaPrima.consumir(materiaPrimaNecessaria);

        String lote = gerarCodigoLote();

        for (Maquina m : this.maquinas) {
            m.ligar();
        }

        int processadas = 0;
        int aprovadas = 0;
        int rejeitadas = 0;

        for (int i = 0; i < totalPecas; i++) {
            if (buscarMaquinaQuebrada() != null) {
                System.out.println(">> Producao interrompida: uma maquina quebrou durante o lote " + lote + ".");
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
                peca.setStatus("Rejeitada na inspecao");
                rejeitadas++;
            }
        }

        for (Maquina m : this.maquinas) {
            m.desligar();
        }

        this.totalRejeitados += rejeitadas;

        // Recuperacao de consistencia: estorno de pecas nao processadas devido a quebra no meio do lote
        int naoProcessadas = totalPecas - processadas;
        if (naoProcessadas > 0) {
            this.budget += calcularCustoProducao(naoProcessadas);
            this.materiaPrima.adicionarEstoque(naoProcessadas * produtoRequerido.getQuantidadeMateriaPrimaPorUnidade());
            System.out.println("Estorno de orcamento e materia-prima referente a " + naoProcessadas + " peca(s) nao processada(s).");
        }

        System.out.printf("Lote %s finalizado: %d aprovada(s), %d reprovada(s) na inspecao.%n", lote, aprovadas, rejeitadas);

        int faltantes = totalPecas - aprovadas;
        if (faltantes == 0) {
            demandaRequerida.atender();
        } else {
            demandaRequerida.atualizarQuantidade(-aprovadas);
            System.out.println("A demanda de " + demandaRequerida.getTipoProduto() + " segue PENDENTE com " + faltantes + " peca(s) a refazer.");
        }
    }

    /**
     * Utiliza a estrategia de producao ativa para selecionar e produzir a demanda ideal.
     * 
     * @throws OperacaoInvalidaException se nao houver estrategia definida, se nao houver demandas elegiveis, 
     *         ou se o produto nao estiver cadastrado no sistema.
     */
    public void executarProximaProducao() {
        if (estrategiaAtual == null) {
            throw new OperacaoInvalidaException("Erro: Nenhuma estrategia de producao foi definida. Altere a estrategia no menu.");
        }

        Demanda escolhida = estrategiaAtual.selecionarDemanda(this.demandas, this.budget);

        if (escolhida == null) {
            throw new OperacaoInvalidaException("Nenhuma demanda elegivel para producao no momento (verifique o status e quantidades).");
        }

        Produto produto = buscarProdutoPorTipo(escolhida.getTipoProduto());
        if (produto == null) {
            throw new OperacaoInvalidaException("Erro interno: nenhum produto cadastrado no catalogo para o tipo '" + escolhida.getTipoProduto() + "'.");
        }

        fabricarDemanda(produto, escolhida);
    }

    /**
     * Atualiza a quantidade exigida de uma demanda registrada.
     * 
     * @param tipoProduto O tipo de produto da demanda.
     * @param quantidadeExtra O numero de pecas a ser somado.
     * @throws DemandaInvalidaException se nenhuma demanda pendente for encontrada para o tipo.
     */
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
            throw new DemandaInvalidaException("Nenhuma demanda PENDENTE encontrada para o produto " + tipoProduto);
        }
    }

    /**
     * Imprime a lista de demandas registradas no sistema e seus respectivos status.
     */
    public void listarDemandas() {
        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda registrada.");
            return;
        }

        System.out.println("--- DEMANDAS REGISTRADAS ---");
        System.out.printf("%-3s | %-20s | %4s | %-12s | %s%n", "N", "Tipo", "Qtd", "Status", "Situacao");

        int numero = 1;
        for (Demanda d : demandas) {
            System.out.printf("%-3d | %-20s | %4d | %-12s | %s%n",
                    numero++, d.getTipoProduto(), d.getQuantidadeProdutos(),
                    d.getStatus(), d.getStatus().getDescricao());
        }
    }

    /**
     * Deduz o valor do orcamento para adquirir novas unidades de materia-prima.
     * 
     * @param materiaAdicionada Quantidade de insumo a ser comprada.
     * @throws OperacaoInvalidaException se a quantidade solicitada for nula ou negativa.
     * @throws OrcamentoInsuficienteException se nao houver saldo para efetuar a transacao.
     */
    public void comprarMateriaPrima(int materiaAdicionada) {
        if (materiaAdicionada <= 0) {
            throw new OperacaoInvalidaException("A quantidade de materia-prima para compra deve ser maior que zero.");
        }

        float custoCompra = materiaAdicionada * this.materiaPrima.getCustoPorUnidade();

        if (this.budget < custoCompra) {
            throw new OrcamentoInsuficienteException(String.format("Orcamento insuficiente para compra. Custo: R$ %.2f | Saldo: R$ %.2f", custoCompra, this.budget));
        }
        
        this.budget -= custoCompra;
        this.materiaPrima.adicionarEstoque(materiaAdicionada);
        System.out.println("Compra realizada com sucesso! Novo saldo: R$ " + String.format("%.2f", this.budget));
    }

    /** Exibe o orcamento disponivel. */
    public void exibirBudget() {
        System.out.println(String.format("| BUDGET: R$ %-38.2f |", budget));
    }

    /**
     * Soma os custos de operacao da linha para a quantidade indicada.
     *
     * @param quantidadePecas numero de pecas da operacao
     * @return custo total das operacoes das maquinas
     */
    private float calcularCustoProducao(int quantidadePecas) {
        float custoPorPeca = 0.0f;
        for (Maquina m : maquinas) {
            custoPorPeca += m.getCustoOperacao();
        }
        return custoPorPeca * quantidadePecas;
    }

    /**
     * Repara iterativamente todas as maquinas cuja saude acione a necessidade de manutencao.
     * <p>
     * O reparo e feito individualmente por maquina. Se o orcamento acabar antes 
     * de consertar todas as maquinas quebradas, o processo e encerrado.
     * </p>
     */
    public void repararMaquinas() {
        int precisavam = 0;

        for (Maquina m : maquinas) {
            if (!m.precisaManutencao()) {
                continue;
            }
            precisavam++;

            float custoReparo = (100 - m.getSaude()) * CUSTO_REPARO_POR_PONTO;
            
            // Aqui capturamos internamente para nao abortar o laco caso ainda possamos consertar as mais baratas
            if (this.budget >= custoReparo) {
                this.budget -= custoReparo; 
                m.repararMaquina();
                System.out.printf("Reparo de %s custou R$ %.2f%n", m.getNome(), custoReparo);
            } else {
                System.out.printf("[ERRO] Orcamento insuficiente para reparar %s (custo: R$ %.2f)%n", m.getNome(), custoReparo);
            }
        }

        if (precisavam == 0) {
            System.out.println("Nenhuma maquina precisa de manutencao no momento.");
        }
    }

    /**
     * Procura uma maquina que nao possa operar por falta de saude.
     *
     * @return primeira maquina quebrada ou {@code null} se todas estiverem disponiveis
     */
    private Maquina buscarMaquinaQuebrada() {
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) {
                return m;
            }
        }
        return null;
    }

    /**
     * Encaminha a peca pela linha e interrompe o percurso se uma etapa a rejeitar.
     *
     * @param peca peca em processamento
     * @return {@code true} se todas as etapas forem concluidas sem rejeicao
     * @throws MaquinaIndisponivelException se uma maquina ficar indisponivel
     */
    private boolean processarNaLinha(Produto peca) {
        for (Maquina m : maquinas) {
            if (!m.processar(peca)) {
                return false;
            }
        }
        return true;
    }

    /** Gera o identificador do proximo lote de producao.
     * @return identificador unico nesta execucao
     */
    private String gerarCodigoLote() {
        this.contadorLotes++;
        return String.format("LOTE-%03d", this.contadorLotes);
    }

    private ArrayList<String> listarLotesUnicos() {
        ArrayList<String> lotes = new ArrayList<>();
        for (Produto p : produtosFabricados) {
            if (!lotes.contains(p.getLote())) {
                lotes.add(p.getLote());
            }
        }
        return lotes;
    }

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
        return a.precisaManutencao() ? " [ATENCAO]" : "";
    }

    /**
     * Imprime no terminal o relatorio fisico dos produtos aprovados no armazem.
     */
    public void exibirArmazem() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Armazem vazio.");
            System.out.println("Pecas reprovadas na inspecao (historico): " + totalRejeitados);
            return;
        }

        System.out.println("--- ARMAZEM DE PRODUTOS ACABADOS ---");
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
        System.out.println("Pecas reprovadas na inspecao (historico): " + totalRejeitados);
    }

    /**
     * Exibe a auditoria geral da planta, analisando todas as instancias implementadas da interface Auditavel.
     */
    public void gerarAuditoriaGeral() {
        System.out.println("========== RELATORIO DE AUDITORIA ==========");

        System.out.println("-- Maquinas --");
        for (Auditavel a : maquinas) {
            System.out.println(a.gerarRelatorioDiagnostico() + sinalizarRisco(a));
        }

        System.out.println("-- Produtos em armazem --");
        int emRisco = 0;
        for (Auditavel a : produtosFabricados) {
            if (a.precisaManutencao()) {
                emRisco++;
            }
        }
        System.out.println("Pecas em armazem: " + produtosFabricados.size()
                + " | Em risco: " + emRisco
                + " | Reprovadas na inspecao: " + totalRejeitados);

        System.out.println("=============================================");
    }

    /**
     * Imprime os diagnosticos detalhados do estado e saude das maquinas de operacao.
     */
    public void detalharMaquinas() {
        System.out.println("--- DIAGNOSTICO DAS MAQUINAS ---");
        for (Maquina m : maquinas) {
            String situacao = m.estaQuebrada() ? "[QUEBRADA]"
                    : (m.precisaManutencao() ? "[MANUTENCAO NECESSARIA]" : "[OPERACIONAL]");
            System.out.println(m.getNome() + " (" + m.getTipo() + ") " + situacao);
            System.out.println("  " + m.gerarRelatorioDiagnostico());
        }
    }

    /**
     * Imprime o relatorio descritivo das pecas estocadas no armazem e eventuais riscos fisicos.
     */
    public void detalharProdutos() {
        if (produtosFabricados.isEmpty()) {
            System.out.println("Nenhum produto em armazem para detalhar.");
            return;
        }

        System.out.println("--- DIAGNOSTICO DOS PRODUTOS ---");
        for (Produto p : produtosFabricados) {
            System.out.println(p.getLote() + " | " + p.getId() + sinalizarRisco(p));
            System.out.println("  " + p.gerarRelatorioDiagnostico());
        }
    }
}