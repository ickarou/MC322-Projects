/* Main.java
 *
 * Tarefa 2
 *
 * última modificação: 14/09/2026
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        /* Tela de Introdução */
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         FÁBRICA DE COMPONENTES AUTOMOTIVOS - KTS 590           ║");
        System.out.println("║       [V2.0] Controle Avançado de Demanda e Orçamento          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Operadores Logados: Eduardo Pontes e Icaro Amaral             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        /* 1. Matéria Prima e Gerenciador */
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plástico ABS", 5000, "g", 0.50f);
        GerenciadorProducao gerenciador = new GerenciadorProducao(plasticoABS, 50000.0f);

        /* 2. Criação Equipamentos */
        Maquina injetora = new Injetora("Injetora CNC Alpha");
        Maquina braco = new BracoEncaixotador("Braço Robótico KUKA");
        Maquina testes = new EstacaoTestes("Estação de Diagnóstico ESI");

        gerenciador.registrarMaquina(injetora);
        gerenciador.registrarMaquina(braco);
        gerenciador.registrarMaquina(testes);

        /* 3. Produtos Base */
        Produto moldeTop = new CarcacaSuperior("MOLDE-TOP");
        Produto moldeBot = new CarcacaInferior("MOLDE-BOT");
        Produto moldeLat = new ProtecaoLateral("MOLDE-LAT");

        /* 4. Demandas Iniciais */
        Demanda demTop = new Demanda("Carcaça Superior", 0);
        Demanda demBot = new Demanda("Carcaça Inferior", 0);
        Demanda demLat = new Demanda("Proteção Lateral", 0);

        gerenciador.registrarDemanda(demTop);
        gerenciador.registrarDemanda(demBot);
        gerenciador.registrarDemanda(demLat);

        boolean executando = true;

        while (executando) {
            System.out.println("\n╔═════════════ PAINEL DE GERENCIAMENTO ═════════════╗");
            gerenciador.exibirBudget();
            System.out.println("╠════════════════ ATUALIZAR DEMANDAS ═══════════════╣");
            System.out.println("║  [ 1 ] Demanda: Carcaça Superior                  ║");
            System.out.println("║  [ 2 ] Demanda: Carcaça Inferior                  ║");
            System.out.println("║  [ 3 ] Demanda: Proteção Lateral                  ║");
            System.out.println("╠═════════════════════ FABRICAR ════════════════════╣");
            System.out.println("║  [ 4 ] Fabricar Carcaça Superior                  ║");
            System.out.println("║  [ 5 ] Fabricar Carcaça Inferior                  ║");
            System.out.println("║  [ 6 ] Fabricar Proteção Lateral                  ║");
            System.out.println("╠════════════════════ CONSULTAR ════════════════════╣");
            System.out.println("║  [ 7 ] Ver Armazém de Produtos Finalizados        ║");
            System.out.println("║  [ 8 ] Ver Estoque de Matéria-Prima               ║");
            System.out.println("╠══════════════════ COMPRAS E SAÍDA ════════════════╣");
            System.out.println("║  [ 9 ] Comprar Matéria-Prima                      ║");
            System.out.println("║  [ 0 ] Encerrar Turno                             ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");
            System.out.print("[SISTEMA] Selecione uma opção: ");

            //Validação para aceitar somente entradas numéricas
            while (!scanner.hasNextInt()) {
                System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                System.out.print("  [SISTEMA] Tente novamente: ");
                scanner.next();
            }
            int opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    System.out.print("Quantidade extra para Carcaça Superior: ");
                    if(scanner.hasNextInt()) {
                        int qtd = scanner.nextInt();
                        
                        // Renovação da demanda
                        if (demTop.foiAtendida()) {
                            demTop = new Demanda("Carcaça Superior", qtd);
                            gerenciador.registrarDemanda(demTop);
                            System.out.println("Sucesso! Novo lote de demanda gerado para Carcaça Superior.");
                        } else {
                            gerenciador.atualizarDemanda("Carcaça Superior", qtd);
                        }
                    } else {
                        System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                        scanner.next();
                    }
                    break;
                case 2:
                    System.out.print("Quantidade extra para Carcaça Inferior: ");
                    if(scanner.hasNextInt()) {
                        int qtd = scanner.nextInt();
                        
                        // Renovação da demanda
                        if (demBot.foiAtendida()) {
                            demBot = new Demanda("Carcaça Inferior", qtd);
                            gerenciador.registrarDemanda(demBot);
                            System.out.println("Sucesso! Novo lote de demanda gerado para Carcaça Inferior.");
                        } else {
                            gerenciador.atualizarDemanda("Carcaça Inferior", qtd);
                        }
                    } else {
                        System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                        scanner.next();
                    }
                    break;
                case 3:
                    System.out.print("Quantidade extra para Proteção Lateral: ");
                    if(scanner.hasNextInt()) {
                        int qtd = scanner.nextInt();
                        
                        // Renovação da demanda
                        if (demLat.foiAtendida()) {
                            demLat = new Demanda("Proteção Lateral", qtd);
                            gerenciador.registrarDemanda(demLat);
                            System.out.println("Sucesso! Novo lote de demanda gerado para Proteção Lateral.");
                        } else {
                            gerenciador.atualizarDemanda("Proteção Lateral", qtd);
                        }
                    } else {
                        System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                        scanner.next();
                    }
                    break;
                case 4:
                    System.out.println("\n>>> INICIANDO LOTE: CARCAÇA SUPERIOR <<<");
                    gerenciador.fabricarDemanda(moldeTop, demTop);
                    break;
                case 5:
                    System.out.println("\n>>> INICIANDO LOTE: CARCAÇA INFERIOR <<<");
                    gerenciador.fabricarDemanda(moldeBot, demBot);
                    break;
                case 6:
                    System.out.println("\n>>> INICIANDO LOTE: PROTEÇÃO LATERAL <<<");
                    gerenciador.fabricarDemanda(moldeLat, demLat);
                    break;
                case 7:
                    System.out.println("\n--- RELATÓRIO DO ARMAZÉM ---");
                    gerenciador.exibirArmazem();
                    
                    // Desconto dos 3 moldes base instanciados na configuração inicial
                    int totalReal = Produto.getTotalProdutosFabricados() - 3;
                    System.out.println("Total absoluto de produtos gerados na fábrica (Histórico): " + totalReal);
                    break;
                case 8:
                    System.out.println("\n--- RELATÓRIO DO ALMOXARIFADO ---");
                    System.out.println("Disponível: " + plasticoABS.getQuantidade() + " " + plasticoABS.getUnidade());
                    break;
                case 9:
                    System.out.print("Quantidade de " + plasticoABS.getUnidade() + " para comprar (Custo: R$" + plasticoABS.getCustoPorUnidade() + " / " + plasticoABS.getUnidade() + "): ");
                    if(scanner.hasNextInt()) {
                        int qtdAdicionar = scanner.nextInt();
                        if (qtdAdicionar > 1000) {
                            System.out.println("Bela compra! Com esse volume, a produção não vai parar tão cedo.");
                        }
                        gerenciador.comprarMateriaPrima(qtdAdicionar);
                    } else {
                        System.out.println("  [ERRO] Entrada inválida! Digite apenas números.");
                        scanner.next();
                    }
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