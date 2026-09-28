/* Main.java
 *
 * Tarefa 3
 *
 * última modificação: 28/09/2026
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String[] NOMES_PRODUTOS = {
        "Carcaça Superior", "Carcaça Inferior", "Proteção Lateral"
    };

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
        Demanda atual = demandas[indice];
        if (atual.getStatus() == StatusDemanda.CONCLUIDA || atual.getStatus() == StatusDemanda.CANCELADA) {
            demandas[indice] = new Demanda(NOMES_PRODUTOS[indice], qtd);
            gerenciador.registrarDemanda(demandas[indice], moldes[indice]);
            System.out.println("Sucesso! Novo lote gerado para " + NOMES_PRODUTOS[indice] + ".");
        } else {
            gerenciador.atualizarDemanda(NOMES_PRODUTOS[indice], qtd);
        }
    }

    private static void submenuDemandas(Scanner scanner, GerenciadorProducao gerenciador,
                                        Demanda[] demandas, Produto[] moldes) {
        System.out.println("\n--- ATUALIZAR DEMANDAS ---");
        System.out.println("[ 1 ] Carcaça Superior");
        System.out.println("[ 2 ] Carcaça Inferior");
        System.out.println("[ 3 ] Proteção Lateral");
        System.out.println("[ 0 ] Voltar");
        int opcao = lerInteiro(scanner, "Escolha o produto: ", 0, 3);
        if (opcao == 0) {
            return;
        }
        int qtd = lerInteiro(scanner, "Quantidade extra: ", 1, 100000);
        atualizarDemanda(gerenciador, demandas, moldes, opcao - 1, qtd);
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
                if (demandas[indice].getQuantidadeProdutos() <= 0) {
                    System.out.println("  [AVISO] Não há pedido aberto para " + NOMES_PRODUTOS[indice] + ". Atualize a demanda primeiro.");
                } else {
                    System.out.println("\n>>> INICIANDO LOTE: " + NOMES_PRODUTOS[indice].toUpperCase() + " <<<");
                    gerenciador.fabricarDemanda(moldes[indice], demandas[indice]);
                }
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
                System.out.println("\n--- RELATÓRIO DO ARMAZÉM ---");
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

    private static void submenuAuditoria(Scanner scanner, GerenciadorProducao gerenciador,
                                         List<Maquina> maquinas) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- AUDITORIA ---");
            System.out.println("[ 1 ] Relatório geral da planta");
            System.out.println("[ 2 ] Detalhar máquinas");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 2);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                gerenciador.gerarAuditoriaGeral();
            } else {
                System.out.println("\n--- DIAGNÓSTICO DAS MÁQUINAS ---");
                for (Maquina m : maquinas) {
                    String situacao = m.precisaManutencao() ? "[MANUTENÇÃO NECESSÁRIA]" : "[OPERACIONAL]";
                    System.out.println(m.getNome() + " " + situacao);
                    System.out.println("  " + m.gerarRelatorioDiagnostico());
                }
            }
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        /* Tela de Introdução */
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        FÁBRICA DE COMPONENTES AUTOMOTIVOS - KTS 590            ║");
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

        /* 2. Criação Equipamentos (Agora recebem o cenário para calcular o desgaste) */
        List<Maquina> maquinas = new ArrayList<>();
        maquinas.add(new Injetora("Injetora CNC Alpha", cenarioEscolhido));
        maquinas.add(new BracoEncaixotador("Braço Robótico KUKA", cenarioEscolhido));
        maquinas.add(new EstacaoTestes("Estação de Diagnóstico ESI", cenarioEscolhido));

        for (Maquina m : maquinas) {
            gerenciador.registrarMaquina(m);
        }

        /* 3. Produtos Base */
        Produto[] moldes = {
            new CarcacaSuperior("MOLDE-TOP"),
            new CarcacaInferior("MOLDE-BOT"),
            new ProtecaoLateral("MOLDE-LAT")
        };

        /* 4. Demandas Iniciais */
        Demanda[] demandas = new Demanda[NOMES_PRODUTOS.length];
        for (int i = 0; i < NOMES_PRODUTOS.length; i++) {
            demandas[i] = new Demanda(NOMES_PRODUTOS[i], 0);
            gerenciador.registrarProduto(moldes[i]);
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
            System.out.println("║  [ 6 ] Auditoria                                  ║");
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
                    submenuAuditoria(scanner, gerenciador, maquinas);
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