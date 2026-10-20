/* Main.java
 *
 * Tarefa 4
 *
 * Material para a disciplina MC322 - Programacao orientada a objetos
 *
 */

import java.util.NoSuchElementException;
import java.util.Scanner;

/** Interface de terminal para operacao da fabrica de scanners automotivos.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class Main {

    /** A aplicacao utiliza metodos estaticos para operar o menu. */
    private Main() {
    }

    /**
     * Monta uma linha da caixa de abertura.
     *
     * @param texto conteudo da linha
     * @return linha com largura fixa
     */
    private static String linhaBanner(String texto) {
        return String.format("| %-62s |", texto);
    }

    /**
     * Monta uma linha do painel de gerenciamento.
     *
     * @param texto conteudo da linha
     * @return linha com largura fixa
     */
    private static String linhaPainel(String texto) {
        return String.format("| %-49s |", texto);
    }

    /**
     * Le um inteiro e repete a solicitacao ate receber um valor dentro do intervalo.
     *
     * @param scanner leitor de entrada do terminal
     * @param prompt mensagem apresentada antes da leitura
     * @param min menor valor aceito
     * @param max maior valor aceito
     * @return inteiro validado
     * @throws NoSuchElementException quando a entrada e encerrada
     */
    private static int lerInteiro(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String entrada = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  [ERRO] Valor fora do intervalo (" + min + " a " + max + ").");
            } catch (NumberFormatException e) {
                System.out.println("  [ERRO] Entrada invalida! Digite apenas numeros inteiros.");
            }
        }
    }

    /**
     * Mostra o erro ao operador e aguarda ENTER antes de voltar ao menu.
     *
     * @param scanner leitor de entrada
     * @param erro excecao capturada
     */
    private static void exibirErro(Scanner scanner, RuntimeException erro) {
        String titulo = (erro instanceof PlantaException) ? "ERRO" : "ENTRADA INVALIDA";
        String mensagem = (erro.getMessage() != null) ? erro.getMessage() : "A operacao nao pode ser realizada.";

        System.out.println();
        System.out.println("========================================");
        System.out.println(titulo);
        System.out.println("========================================");
        System.out.println(mensagem);
        System.out.println("Nenhuma alteracao foi realizada no estoque, no orcamento ou nas demandas.");
        System.out.print("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    /**
     * Atualiza a demanda aberta ou cadastra outra apos o encerramento do lote atual.
     *
     * @param gerenciador gerenciador da planta
     * @param demandas demandas atualmente associadas as opcoes do menu
     * @param moldes modelos de produto disponiveis
     * @param indice posicao do tipo de produto selecionado
     * @param qtd quantidade adicional solicitada
     */
    private static void atualizarDemanda(GerenciadorProducao gerenciador, Demanda[] demandas,
                                         Produto[] moldes, int indice, int qtd) {
        String tipo = moldes[indice].getTipo();
        Demanda atual = demandas[indice];
        if (atual.getStatus() == StatusDemanda.CONCLUIDA || atual.getStatus() == StatusDemanda.CANCELADA) {
            Demanda novoLote = new Demanda(tipo, qtd);
            gerenciador.registrarDemanda(novoLote, moldes[indice]);
            demandas[indice] = novoLote;
            System.out.println("Sucesso! Novo lote gerado para " + tipo + ".");
        } else {
            gerenciador.atualizarDemanda(tipo, qtd);
        }
    }

    /**
     * Exibe as opcoes de cadastro e consulta de demandas.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     * @param demandas demandas associadas as opcoes do menu
     * @param moldes modelos de produto disponiveis
     */
    private static void submenuDemandas(Scanner scanner, GerenciadorProducao gerenciador,
                                        Demanda[] demandas, Produto[] moldes) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- DEMANDAS ---");
            System.out.println("[ 1 ] Atualizar: Carcaca Superior");
            System.out.println("[ 2 ] Atualizar: Carcaca Inferior");
            System.out.println("[ 3 ] Atualizar: Protecao Lateral");
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
                try {
                    atualizarDemanda(gerenciador, demandas, moldes, opcao - 1, qtd);
                } catch (PlantaException | IllegalArgumentException e) {
                    exibirErro(scanner, e);
                }
            }
        }
    }

    /**
     * Exibe as opcoes de producao automatica e por tipo de peca.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     * @param demandas demandas associadas as opcoes do menu
     * @param moldes modelos de produto disponiveis
     */
    private static void submenuProducao(Scanner scanner, GerenciadorProducao gerenciador,
                                        Demanda[] demandas, Produto[] moldes) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- FABRICACAO ---");
            System.out.println("[ 1 ] Processar PROXIMA demanda (usa a estrategia ativa)");
            System.out.println("[ 2 ] Forcar producao: Carcaca Superior");
            System.out.println("[ 3 ] Forcar producao: Carcaca Inferior");
            System.out.println("[ 4 ] Forcar producao: Protecao Lateral");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha uma acao: ", 0, 4);

            if (opcao == 0) {
                noSubmenu = false;
            } else {
                try {
                    if (opcao == 1) {
                        System.out.println("\n>>> INICIANDO PRODUCAO AUTOMATICA <<<");
                        gerenciador.executarProximaProducao();
                    } else {
                        int indice = opcao - 2;
                        System.out.println("\n>>> INICIANDO LOTE: " + moldes[indice].getTipo().toUpperCase() + " <<<");
                        gerenciador.fabricarDemanda(moldes[indice], demandas[indice]);
                    }
                } catch (PlantaException | IllegalArgumentException e) {
                    exibirErro(scanner, e);
                }
            }
        }
    }

    /**
     * Exibe as consultas ao armazem e ao estoque de materia-prima.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     * @param materiaPrima material controlado no almoxarifado
     */
    private static void submenuConsultas(Scanner scanner, GerenciadorProducao gerenciador,
                                         MateriaPrima materiaPrima) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- CONSULTAS ---");
            System.out.println("[ 1 ] Ver Armazem (produtos finalizados)");
            System.out.println("[ 2 ] Ver Almoxarifado (materia-prima)");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 2);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                System.out.println();
                gerenciador.exibirArmazem();
            } else {
                System.out.println("\n--- RELATORIO DO ALMOXARIFADO ---");
                System.out.println("Disponivel: " + materiaPrima.getQuantidade() + " " + materiaPrima.getUnidade());
            }
        }
    }

    /**
     * Permite alterar a estrategia de selecao de demandas.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     */
    private static void submenuEstrategia(Scanner scanner, GerenciadorProducao gerenciador) {
        System.out.println("\n--- ALTERAR ESTRATEGIA ---");
        System.out.println("[ 1 ] Linha de Montagem (ordem de chegada)");
        System.out.println("[ 2 ] Lote Critico (maior quantidade de pecas)");
        System.out.println("[ 3 ] Meta de Orcamento (maximo de pecas dentro do orcamento)");
        System.out.println("[ 0 ] Voltar");
        int opcao = lerInteiro(scanner, "Escolha a nova estrategia: ", 0, 3);

        if (opcao == 1) {
            gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());
        } else if (opcao == 2) {
            gerenciador.setEstrategia(new EstrategiaLoteCritico());
        } else if (opcao == 3) {
            gerenciador.setEstrategia(new EstrategiaMetaDeOrcamento());
        }
    }

    /**
     * Exibe relatorios de auditoria e permite solicitar manutencao.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     */
    private static void submenuAuditoria(Scanner scanner, GerenciadorProducao gerenciador) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- AUDITORIA E MANUTENCAO ---");
            System.out.println("[ 1 ] Relatorio geral da planta");
            System.out.println("[ 2 ] Detalhar maquinas");
            System.out.println("[ 3 ] Detalhar produtos");
            System.out.println("[ 4 ] Enviar maquinas para manutencao");
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

    /**
     * Pergunta a quantidade e solicita a compra de materia-prima.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     * @param materiaPrima material comprado
     */
    private static void comprarMateriaPrima(Scanner scanner, GerenciadorProducao gerenciador,
                                            MateriaPrima materiaPrima) {
        String unidade = materiaPrima.getUnidade();
        String prompt = String.format("Quantidade de %s para comprar (R$ %.2f por %s): ",
                unidade, materiaPrima.getCustoPorUnidade(), unidade);
        int quantidade = lerInteiro(scanner, prompt, 1, 1000000);

        gerenciador.comprarMateriaPrima(quantidade);
        if (quantidade > 1000) {
            System.out.println("Bela compra! Com esse volume, a producao nao vai parar tao cedo.");
        }
    }

    /**
     * Inicializa a planta e mantem o menu ativo ate o encerramento do turno.
     *
     * @param args argumentos de linha de comando, nao utilizados
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            String bordaBanner = "+" + "=".repeat(64) + "+";
            System.out.println();
            System.out.println(bordaBanner);
            System.out.println(linhaBanner("       FABRICA DE SCANNERS AUTOMOTIVOS - KTS 590"));
            System.out.println(linhaBanner("     [V4.0] Gradle, Javadoc e Tratamento de Erros"));
            System.out.println(bordaBanner);
            System.out.println(linhaBanner("Operadores Logados: Eduardo Pontes e Icaro Amaral"));
            System.out.println(bordaBanner);
            System.out.println();

            System.out.println(">>> CONFIGURACAO DE TURNO <<<");
            System.out.println("[ 1 ] Cenario IDEAL (orcamento farto, maquinas novas)");
            System.out.println("[ 2 ] Cenario APOCALIPTICO (orcamento apertado, maquinas desgastadas)");

            Cenario cenarioEscolhido = Cenario.IDEAL;
            int esc = lerInteiro(scanner, "Escolha o cenario de operacao: ", 1, 2);
            if (esc == 2) {
                cenarioEscolhido = Cenario.APOCALIPTICO;
            }

            MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plastico ABS", 5000, "g", 0.50f);
            GerenciadorProducao gerenciador = new GerenciadorProducao(plasticoABS, cenarioEscolhido);
            gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());

            gerenciador.registrarMaquina(new Injetora("Injetora CNC Alpha", cenarioEscolhido));
            gerenciador.registrarMaquina(new BracoEncaixotador("Braco Robotico KUKA", cenarioEscolhido));
            gerenciador.registrarMaquina(new EstacaoTestes("Estacao de Diagnostico ESI", cenarioEscolhido));

            Produto[] moldes = {
                new CarcacaSuperior("MOLDE-TOP"),
                new CarcacaInferior("MOLDE-BOT"),
                new ProtecaoLateral("MOLDE-LAT")
            };

            Demanda[] demandas = new Demanda[moldes.length];
            for (int i = 0; i < moldes.length; i++) {
                demandas[i] = new Demanda(moldes[i].getTipo(), 0);
                gerenciador.registrarDemanda(demandas[i], moldes[i]);
            }

            boolean executando = true;
            while (executando) {
                System.out.println("\n+============= PAINEL DE GERENCIAMENTO =============+");
                System.out.println(linhaPainel("CENARIO: " + gerenciador.getNomeCenarioAtivo()));
                System.out.println(linhaPainel("ESTRATEGIA: " + gerenciador.getNomeEstrategiaAtual()));
                gerenciador.exibirBudget();
                System.out.println("+===================================================+");
                System.out.println(linhaPainel(" [ 1 ] Demandas (atualizar pedidos)"));
                System.out.println(linhaPainel(" [ 2 ] Producao (fabricar pecas)"));
                System.out.println(linhaPainel(" [ 3 ] Consultas (armazem e estoque)"));
                System.out.println(linhaPainel(" [ 4 ] Comprar Materia-Prima"));
                System.out.println(linhaPainel(" [ 5 ] Alterar Estrategia de Producao"));
                System.out.println(linhaPainel(" [ 6 ] Auditoria e Manutencao"));
                System.out.println(linhaPainel(" [ 0 ] Encerrar Turno"));
                System.out.println("+===================================================+");

                int opcao = lerInteiro(scanner, "[SISTEMA] Selecione um menu: ", 0, 6);
                try {
                    switch (opcao) {
                        case 1:
                            submenuDemandas(scanner, gerenciador, demandas, moldes);
                            break;
                        case 2:
                            submenuProducao(scanner, gerenciador, demandas, moldes);
                            break;
                        case 3:
                            submenuConsultas(scanner, gerenciador, plasticoABS);
                            break;
                        case 4:
                            comprarMateriaPrima(scanner, gerenciador, plasticoABS);
                            break;
                        case 5:
                            submenuEstrategia(scanner, gerenciador);
                            break;
                        case 6:
                            submenuAuditoria(scanner, gerenciador);
                            break;
                        case 0:
                            System.out.println("\n  [SISTEMA] Salvando dados de producao...");
                            System.out.println("  [SISTEMA] Turno encerrado. Ate logo!\n");
                            executando = false;
                            break;
                        default:
                            System.out.println("  [ERRO] Codigo de operacao nao reconhecido.");
                    }
                } catch (PlantaException | IllegalArgumentException e) {
                    exibirErro(scanner, e);
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\n  [SISTEMA] Entrada encerrada. Turno finalizado.");
        } finally {
            scanner.close();
        }
    }
}