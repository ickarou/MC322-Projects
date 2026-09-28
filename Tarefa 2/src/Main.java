/* Main.java
 *
 * Tarefa 3
 *
 * última modificação: 28/09/2026
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.Scanner;

public class Main {

    /* Leitura de inteiros com validação de tipo e de intervalo */
    private static int lerInteiro(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int valor = scanner.nextInt();
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  [ERRO] Valor fora do intervalo (" + min + " a " + max + ").");
            } else {
                System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                scanner.next();
            }
        }
    }

    /* Renovação da demanda: lote finalizado gera um novo, lote aberto recebe a quantidade extra */
    private static void atualizarDemanda(GerenciadorProducao gerenciador, Demanda[] demandas,
                                         Produto[] moldes, int indice, int qtd) {
        String tipo = moldes[indice].getTipo();
        Demanda atual = demandas[indice];
        if (atual.getStatus() == StatusDemanda.CONCLUIDA || atual.getStatus() == StatusDemanda.CANCELADA) {
            demandas[indice] = new Demanda(tipo, qtd);
            gerenciador.registrarDemanda(demandas[indice], moldes[indice]);
            System.out.println("Sucesso! Novo lote gerado para " + tipo + ".");
        } else {
            gerenciador.atualizarDemanda(tipo, qtd);
        }
    }

    private static void submenuDemandas(Scanner scanner, GerenciadorProducao gerenciador,
                                        Demanda[] demandas, Produto[] moldes) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- DEMANDAS ---");
            System.out.println("[ 1 ] Atualizar: Carcaça Superior");
            System.out.println("[ 2 ] Atualizar: Carcaça Inferior");
            System.out.println("[ 3 ] Atualizar: Proteção Lateral");
            System.out.println("[ 4 ] Listar demandas e status");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 4);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 4) {
                System.out.println();
                gerenciador.listarDemandas();
            } else {
                int qtd = lerInteiro(scanner, "Quantidade extra: ", 1, 100000);
                atualizarDemanda(gerenciador, demandas, moldes, opcao - 1, qtd);
            }
        }
    }

    private static void submenuProducao(Scanner scanner, GerenciadorProducao gerenciador,
                                        Demanda[] demandas, Produto[] moldes) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- FABRICAÇÃO ---");
            System.out.println("[ 1 ] Processar PRÓXIMA demanda (Usa a estratégia ativa)");
            System.out.println("[ 2 ] Forçar produção: Carcaça Superior");
            System.out.println("[ 3 ] Forçar produção: Carcaça Inferior");
            System.out.println("[ 4 ] Forçar produção: Proteção Lateral");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha uma ação: ", 0, 4);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                System.out.println("\n>>> INICIANDO PRODUÇÃO AUTOMÁTICA <<<");
                gerenciador.executarProximaProducao();
            } else {
                int indice = opcao - 2;
                System.out.println("\n>>> INICIANDO LOTE: " + moldes[indice].getTipo().toUpperCase() + " <<<");
                gerenciador.fabricarDemanda(moldes[indice], demandas[indice]);
            }
        }
    }

    private static void submenuConsultas(Scanner scanner, GerenciadorProducao gerenciador,
                                         MateriaPrima materiaPrima) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- CONSULTAS ---");
            System.out.println("[ 1 ] Ver Armazém (Produtos Finalizados)");
            System.out.println("[ 2 ] Ver Almoxarifado (Matéria-Prima)");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 2);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                System.out.println();
                gerenciador.exibirArmazem();
            } else {
                System.out.println("\n--- RELATÓRIO DO ALMOXARIFADO ---");
                System.out.println("Disponível: " + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade());
            }
        }
    }

    private static void submenuEstrategia(Scanner scanner, GerenciadorProducao gerenciador) {
        System.out.println("\n--- ALTERAR ESTRATÉGIA ---");
        System.out.println("[ 1 ] Linha de Montagem (Ordem de chegada)");
        System.out.println("[ 2 ] Lote Crítico (Maior quantidade de peças)");
        System.out.println("[ 3 ] Meta de Orçamento (Máximo de peças dentro do orçamento)");
        System.out.println("[ 0 ] Voltar");
        int opcao = lerInteiro(scanner, "Escolha a nova estratégia: ", 0, 3);

        if (opcao == 1) {
            gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());
        } else if (opcao == 2) {
            gerenciador.setEstrategia(new EstrategiaLoteCritico());
        } else if (opcao == 3) {
            gerenciador.setEstrategia(new EstrategiaMetaDeOrcamento());
        }
    }

    private static void submenuAuditoria(Scanner scanner, GerenciadorProducao gerenciador) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- AUDITORIA E MANUTENÇÃO ---");
            System.out.println("[ 1 ] Relatório geral da planta");
            System.out.println("[ 2 ] Detalhar máquinas");
            System.out.println("[ 3 ] Detalhar produtos");
            System.out.println("[ 4 ] Enviar máquinas para manutenção");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 4);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                gerenciador.gerarAuditoriaGeral();
            } else if (opcao == 2) {
                System.out.println();
                gerenciador.detalharMaquinas();
            } else if (opcao == 3) {
                System.out.println();
                gerenciador.detalharProdutos();
            } else {
                gerenciador.repararMaquinas();
            }
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        /* Tela de Introdução */
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        FÁBRICA DE SCANNERS AUTOMOTIVOS - KTS 590               ║");
        System.out.println("║      [V3.0] Estratégias de Produção e Auditoria Ativa          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Operadores Logados: Eduardo Pontes e Icaro Amaral             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        /* Seleção de Cenário (Obrigatório na Tarefa 3) */
        System.out.println(">>> CONFIGURAÇÃO DE TURNO <<<");
        System.out.println("[ 1 ] Cenário IDEAL (Orçamento farto, máquinas novas)");
        System.out.println("[ 2 ] Cenário APOCALÍPTICO (Orçamento apertado, máquinas desgastadas)");

        Cenario cenarioEscolhido = Cenario.IDEAL; // padrão
        int esc = lerInteiro(scanner, "Escolha o cenário de operação: ", 1, 2);
        if (esc == 2) {
            cenarioEscolhido = Cenario.APOCALIPTICO;
        }

        /* 1. Matéria Prima e Gerenciador */
        // O gerenciador agora recebe o cenário escolhido para definir o budget inicial
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plástico ABS", 5000, "g", 0.50f);
        GerenciadorProducao gerenciador = new GerenciadorProducao(plasticoABS, cenarioEscolhido);

        // Já definimos uma estratégia inicial para não dar erro se o usuário tentar fabricar direto
        gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());

        /* 2. Criação Equipamentos (recebem o cenário para calcular o desgaste) */
        gerenciador.registrarMaquina(new Injetora("Injetora CNC Alpha", cenarioEscolhido));
        gerenciador.registrarMaquina(new BracoEncaixotador("Braço Robótico KUKA", cenarioEscolhido));
        gerenciador.registrarMaquina(new EstacaoTestes("Estação de Diagnóstico ESI", cenarioEscolhido));

        /* 3. Produtos Base */
        Produto[] moldes = {
            new CarcacaSuperior("MOLDE-TOP"),
            new CarcacaInferior("MOLDE-BOT"),
            new ProtecaoLateral("MOLDE-LAT")
        };

        /* 4. Demandas Iniciais */
        Demanda[] demandas = new Demanda[moldes.length];
        for (int i = 0; i < moldes.length; i++) {
            demandas[i] = new Demanda(moldes[i].getTipo(), 0);
            gerenciador.registrarDemanda(demandas[i], moldes[i]);
        }

        boolean executando = true;

        while (executando) {
            System.out.println("\n╔═════════════ PAINEL DE GERENCIAMENTO ═════════════╗");
            System.out.println(String.format("║ CENÁRIO: %-40s ║", gerenciador.getNomeCenarioAtivo()));
            System.out.println(String.format("║ ESTRATÉGIA: %-37s ║", gerenciador.getNomeEstrategiaAtual()));
            gerenciador.exibirBudget();
            System.out.println("╠═══════════════════════════════════════════════════╣");
            System.out.println("║  [ 1 ] Demandas (Atualizar pedidos)               ║");
            System.out.println("║  [ 2 ] Produção (Fabricar peças)                  ║");
            System.out.println("║  [ 3 ] Consultas (Armazém e Estoque)              ║");
            System.out.println("║  [ 4 ] Comprar Matéria-Prima                      ║");
            System.out.println("║  [ 5 ] Alterar Estratégia de Produção             ║");
            System.out.println("║  [ 6 ] Auditoria e Manutenção                     ║");
            System.out.println("║  [ 0 ] Encerrar Turno                             ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");

            int opcao = lerInteiro(scanner, "[SISTEMA] Selecione um menu: ", 0, 6);

            switch (opcao) {
                case 1: // SUBMENU DE DEMANDAS
                    submenuDemandas(scanner, gerenciador, demandas, moldes);
                    break;

                case 2: // SUBMENU DE PRODUÇÃO
                    submenuProducao(scanner, gerenciador, demandas, moldes);
                    break;

                case 3: // SUBMENU DE CONSULTAS
                    submenuConsultas(scanner, gerenciador, plasticoABS);
                    break;

                case 4: // COMPRAR MATÉRIA PRIMA
                    int qtdAdicionar = lerInteiro(scanner,
                        "Quantidade de " + plasticoABS.getUnidade() + " para comprar (Custo: R$"
                        + plasticoABS.getCustoPorUnidade() + " / " + plasticoABS.getUnidade() + "): ",
                        1, 1000000);
                    if (qtdAdicionar > 1000) {
                        System.out.println("Bela compra! Com esse volume, a produção não vai parar tão cedo.");
                    }
                    gerenciador.comprarMateriaPrima(qtdAdicionar);
                    break;

                case 5: // SUBMENU DE ESTRATÉGIAS
                    submenuEstrategia(scanner, gerenciador);
                    break;

                case 6: // SUBMENU DE AUDITORIA
                    submenuAuditoria(scanner, gerenciador);
                    break;

                case 0:
                    System.out.println("\n  [SISTEMA] Salvando dados de produção...");
                    System.out.println("  [SISTEMA] Turno encerrado. Até logo!\n");
                    executando = false;
                    break;

                default:
                    System.out.println("  [ERRO] Código de operação não reconhecido.");
            }
        }
        scanner.close();
    }
}