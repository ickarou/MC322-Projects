/* Main.java
 *
 * última modificação: 02/09/2026
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
        System.out.println("║            FÁBRICA DE SCANNER AUTOMOTIVO - KTS 590             ║");
        System.out.println("║                    Injeção de Plástico ABS                     ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║           DIAGNÓSTICO INTELIGENTE NA PALMA DA SUA MÃO          ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  Operadores Logados: Eduardo Pontes e Icaro Amaral             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        /* 1. Matéria Prima */
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plástico ABS", 5000, "g", 200);

        /* 2. Produtos Disponíveis */
        Produto prod1 = new Produto("CARC_TOP", "Carcaça Superior KTS590", 250);
        Produto prod2 = new Produto("PROT_LAT", "Proteção Lateral KTS590", 200);
        Produto prod3 = new Produto("CARC_BOT", "Carcaça Inferior KTS590", 250);

        /* 3. Criação Equipamentos */
        Maquina injetora = new Maquina("Injetora de Plástico", 1000);
        Esteira esteira = new Esteira(1000);
        EstacaoInspecao estacaoInspecao = new EstacaoInspecao();

        boolean executando = true;

        while (executando) {
            System.out.println("\n╔═════════════ PAINEL DE CONTROLE CENTRAL ═════════════╗");
            System.out.println("║                                                      ║");
            System.out.println("║  [ 1 ] INICIAR CICLO DE PRODUÇÃO                     ║");
            System.out.println("║  [ 2 ] CONSULTAR ESTOQUE DE MATÉRIA-PRIMA            ║");
            System.out.println("║  [ 3 ] REABASTECER MATÉRIA-PRIMA                     ║");
            System.out.println("║  [ 4 ] DESLIGAR SISTEMA E ENCERRAR TURNO             ║");
            System.out.println("║                                                      ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.print("[SISTEMA] Digite o código da operação: ");

            //Validação para aceitar somente entradas numéricas
            while (!scanner.hasNextInt()) {
                System.out.println("[ERRO] Entrada inválida! Digite apenas números.");
                System.out.print("Tente novamente: ");
                scanner.next();
            }
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.println("\n┌─── SELEÇÃO DE MOLDE PARA INJEÇÃO ────────────────────────┐");
                System.out.println("│ 1. " + prod1.getNome() + " (Custo: " + prod1.getDemandaMateriaPrima() + " g/unidade)");
                System.out.println("│ 2. " + prod2.getNome() + " (Custo: " + prod2.getDemandaMateriaPrima() + " g/unidade)");
                System.out.println("│ 3. " + prod3.getNome() + " (Custo: " + prod3.getDemandaMateriaPrima() + " g/unidade)");
                System.out.println("└──────────────────────────────────────────────────────────┘");
                System.out.print("[SISTEMA] Selecione o código do produto: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("ERRO - Digite apenas números.");
                    System.out.print("Selecione o produto: ");
                    scanner.next();
                }
                int escolhaProd = scanner.nextInt();

                Produto produtoSelecionado = null;
                if (escolhaProd == 1) {
                    produtoSelecionado = prod1;
                } else if (escolhaProd == 2) {
                    produtoSelecionado = prod2;
                } else if (escolhaProd == 3) {
                    produtoSelecionado = prod3;
                } else {
                    System.out.println("Opção de produto inválida!");
                    continue;
                }

                System.out.print("Informe a quantidade de unidades a serem produzidas: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("ERRO - Digite apenas números.");
                    System.out.print("Informe a quantidade de unidades: ");
                    scanner.next();
                }
                int unidades = scanner.nextInt();
                
                // CÁLCULO DA DEMANDA TOTAL EM GRAMAS
                int demandaTotalGramas = unidades * produtoSelecionado.getDemandaMateriaPrima();
                System.out.println("-> Demanda total de matéria-prima necessária: " + demandaTotalGramas + " " + plasticoABS.getUnidade());

                System.out.println("\n>>> INICIANDO LINHA DE PRODUÇÃO <<<");

                // Verificação 1: Estoque de matéria-prima
                if (!plasticoABS.verificarDisponibilidade(demandaTotalGramas)) {
                    System.out.println("Estoque insuficiente de " + plasticoABS.getNome() + "!");
                    System.out.println("Disponível: " + plasticoABS.getQuantidade() + " g | Solicitado: " + demandaTotalGramas + " g");
                } 
                // Verificação 2: Capacidade máxima da esteira (CORREÇÃO AQUI)
                else if (!esteira.verificarCapacidade(demandaTotalGramas)) {
                    System.out.println("*ERRO CRUCIAL* A carga total solicitada (" + demandaTotalGramas + "g) excede a capacidade máxima da esteira de transporte (600g).");
                    System.out.println("Por favor, reduza a quantidade de unidades e tente novamente.");
                    System.out.println(">>> PRODUÇÃO CANCELADA <<<");
                } 
                // se passou nas duas verificações, inicia a produção
                else {
                    // Ligar Equipamentos
                    esteira.ligar();
                    injetora.ligar();
                    estacaoInspecao.ativar();

                    // Transporte da Matéria Prima
                    esteira.adicionarItem(plasticoABS.getId(), demandaTotalGramas);
                    esteira.removerItem();

                    // Processamento na Injetora
                    boolean sucessoProcessamento = injetora.processar(plasticoABS, demandaTotalGramas);

                    if (sucessoProcessamento) {
                        produtoSelecionado.processar();

                        // Transporte do Produto Acabado
                        esteira.adicionarItem(produtoSelecionado.getId(), demandaTotalGramas);
                        esteira.removerItem();

                        // Inspeção: Avalia cada unidade individualmente
                        System.out.println("\n--- Iniciando Inspeção de Qualidade ---");
                        for (int i = 0; i < unidades; i++) {
                            estacaoInspecao.inspecionar(produtoSelecionado.getNome() + " (Unidade " + (i + 1) + ")");
                        }

                        System.out.println("\n*SUCESSO* Produção concluída!");
                        System.out.println("Estoque restante de " + plasticoABS.getNome() + ": " + plasticoABS.getQuantidade() + " g");
                        System.out.println("Total de itens inspecionados hoje: " + estacaoInspecao.getTotalInspecionados());
                    }

                    // Desligar Equipamentos
                    esteira.desligar();
                    injetora.desligar();
                    estacaoInspecao.desativar();
                }

            } else if (opcao == 2) {
                System.out.println("\n┌─── RELATÓRIO DO ALMOXARIFADO ────────────────────────────┐");
                System.out.println("│ ID Material: " + plasticoABS.getId());
                System.out.println("│ Descrição:   " + plasticoABS.getNome());
                System.out.println("│ Disponível:  " + plasticoABS.getQuantidade() + " " + plasticoABS.getUnidade());
                System.out.println("└──────────────────────────────────────────────────────────┘");

            } else if (opcao == 3) {
                System.out.print("\nInforme a quantidade de " + plasticoABS.getNome() + " a ser adicionada (" + plasticoABS.getUnidade() + "): ");
                while (!scanner.hasNextInt()) {
                    System.out.println("ERRO - Digite apenas números.");
                    System.out.print("Informe a quantidade: ");
                    scanner.next();
                }
                int qtdAdicionar = scanner.nextInt();
                
                if (qtdAdicionar > 0) {
                    plasticoABS.adicionarEstoque(qtdAdicionar);
                    if (qtdAdicionar > 1000){
                        System.out.println("UAU! Isso é realmente bastante coisa! Estoque atualizado! Novo saldo: " + plasticoABS.getQuantidade() + " g");
                        System.out.println("BOSCH agradece pelo fornecimento!");
                        continue;
                    }
                    System.out.println("Tudo OK - Estoque atualizado! Novo saldo: " + plasticoABS.getQuantidade() + " g");
                    System.out.println("BOSCH agradece pelo fornecimento");
                } else {
                    System.out.println("ERRO - A quantidade deve ser positiva.");
                }

            } else if (opcao == 4) {
                System.out.println("\nEncerrando o sistema da fábrica. Até logo!");
                executando = false;
            } else {
                System.out.println("ERRO - Opção inválida!");
            }
        }

        scanner.close();
    }
}