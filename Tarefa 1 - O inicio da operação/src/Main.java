/* Main.java
 *
 * última modificação: 31/08/2026
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
        System.out.println("║       FÁBRICA DE COMPONENTES AUTOMOTIVOS - BOSCH KTS 590       ║");
        System.out.println("║              Produção e Injeção de Plástico ABS                ║");
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
        Maquina injetora = new Maquina("Injetora de Plástico CNC", 1000);
        Esteira esteira = new Esteira(600);
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

            //Validação simples para aceitar APENAS entradas numéricas
            while (!scanner.hasNextInt()) {
                System.out.println("[ERRO] Entrada inválida! Digite apenas números.");
                System.out.print("Tente novamente: ");
                scanner.next();
            }
            int opcao = scanner.nextInt();

            if (opcao == 1) {
                System.out.println("\n┌─── SELEÇÃO DE MOLDE PARA INJEÇÃO ────────────────────────┐");
                System.out.println("│ 1. " + prod1.getNome() + " (Demanda: " + prod1.getDemandaMateriaPrima() + " g)");
                System.out.println("│ 2. " + prod2.getNome() + " (Demanda: " + prod2.getDemandaMateriaPrima() + " g)");
                System.out.println("│ 3. " + prod3.getNome() + " (Demanda: " + prod3.getDemandaMateriaPrima() + " g)");
                System.out.println("└──────────────────────────────────────────────────────────┘");
                System.out.print("[SISTEMA] Selecione o código do produto: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("[Erro] Digite apenas números.");
                    System.out.print("Selecione o produto (1-3): ");
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

                System.out.print("Informe a demanda de matéria-prima (" + plasticoABS.getUnidade() + "): ");
                // while (!scanner.hasNextInt()) {
                //     System.out.println("[ERRO] Digite apenas números.");
                //     System.out.print("Informe a demanda: ");
                //     scanner.next();
                // }
                int demanda = scanner.nextInt();

                System.out.println("\n>>> INICIANDO LINHA DE PRODUÇÃO <<<");

                // Verificação de estoque
                if (!plasticoABS.verificarDisponibilidade(demanda)) {
                    System.out.println("Estoque insuficiente de " + plasticoABS.getNome() + "!");
                    System.out.println("Disponível: " + plasticoABS.getQuantidade() + " g | Solicitado: " + demanda + " g");
                } else {
                    // Ligar Equipamentos
                    esteira.ligar();
                    injetora.ligar();
                    estacaoInspecao.ativar();

                    // Transporte da Matéria Prima
                    esteira.adicionarItem(plasticoABS.getId(), demanda);
                    esteira.removerItem();

                    // Processamento
                    boolean sucessoProcessamento = injetora.processar(plasticoABS, demanda);

                    if (sucessoProcessamento) {
                        produtoSelecionado.processar();

                        // Transporte do Produto Acabado
                        esteira.adicionarItem(produtoSelecionado.getId(), demanda);
                        esteira.removerItem();

                        // Inspeção
                        estacaoInspecao.inspecionar(produtoSelecionado.getNome());

                        System.out.println("\n[SUCESSO] Produção concluída!");
                        System.out.println("Estoque restante de " + plasticoABS.getNome() + ": " + plasticoABS.getQuantidade() + " g");
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
                    System.out.println("[ERRO] Digite apenas números.");
                    System.out.print("Informe a quantidade: ");
                    scanner.next();
                }
                int qtdAdicionar = scanner.nextInt();
                
                if (qtdAdicionar > 0) {
                    plasticoABS.adicionarEstoque(qtdAdicionar);
                    System.out.println("[OK] Estoque atualizado! Novo saldo: " + plasticoABS.getQuantidade() + " g");
                } else {
                    System.out.println("[ERRO] A quantidade deve ser positiva.");
                }

            } else if (opcao == 4) {
                System.out.println("\nEncerrando o sistema da fábrica. Até logo!");
                executando = false;
            } else {
                System.out.println("[ERRO] Opção inválida!");
            }
        }

        scanner.close();
    }
}