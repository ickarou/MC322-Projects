/* Main.java
 *
 * Tarefa 6
 *
 * Material para a disciplina MC322 - Programacao orientada a objetos
 *
 */

package br.unicamp.ic.mc322;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.PlantaPadrao;
import br.unicamp.ic.mc322.strategy.EstrategiaLinhaDeMontagem;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;
import br.unicamp.ic.mc322.strategy.EstrategiaMetaDeOrcamento;

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
        System.out.print("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    /**
     * Atualiza a demanda aberta ou cadastra outra apos o encerramento do lote atual.
     *
     * @param gerenciador gerenciador da planta
     * @param molde modelo do produto selecionado
     * @param qtd quantidade adicional solicitada
     */
    private static void atualizarDemanda(GerenciadorProducao gerenciador, Produto molde, int qtd) {
        String tipo = molde.getTipo();
        Demanda atual = gerenciador.buscarDemandaAtual(tipo);
        if (atual == null || atual.getStatus() == StatusDemanda.CONCLUIDA || atual.getStatus() == StatusDemanda.CANCELADA) {
            gerenciador.registrarDemanda(new Demanda(tipo, qtd), molde);
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
     */
    private static void submenuDemandas(Scanner scanner, GerenciadorProducao gerenciador) {
        List<Produto> moldes = gerenciador.getCatalogo();
        int opcaoListar = moldes.size() + 1;
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- DEMANDAS ---");
            for (int i = 0; i < moldes.size(); i++) {
                System.out.println("[ " + (i + 1) + " ] Atualizar: " + moldes.get(i).getTipo());
            }
            System.out.println("[ " + opcaoListar + " ] Listar demandas e status");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, opcaoListar);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == opcaoListar) {
                System.out.println();
                gerenciador.listarDemandas();
            } else {
                int qtd = lerInteiro(scanner, "Quantidade extra: ", 1, 100000);
                try {
                    atualizarDemanda(gerenciador, moldes.get(opcao - 1), qtd);
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
     */
    private static void submenuProducao(Scanner scanner, GerenciadorProducao gerenciador) {
        List<Produto> moldes = gerenciador.getCatalogo();
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- FABRICACAO ---");
            System.out.println("[ 1 ] Processar PROXIMA demanda (usa a estrategia ativa)");
            for (int i = 0; i < moldes.size(); i++) {
                System.out.println("[ " + (i + 2) + " ] Forcar producao: " + moldes.get(i).getTipo());
            }
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha uma acao: ", 0, moldes.size() + 1);

            if (opcao == 0) {
                noSubmenu = false;
            } else {
                try {
                    if (opcao == 1) {
                        System.out.println("\n>>> INICIANDO PRODUCAO AUTOMATICA <<<");
                        gerenciador.executarProximaProducao();
                    } else {
                        Produto molde = moldes.get(opcao - 2);
                        System.out.println("\n>>> INICIANDO LOTE: " + molde.getTipo().toUpperCase() + " <<<");
                        gerenciador.fabricarDemanda(molde, gerenciador.buscarDemandaAtual(molde.getTipo()));
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
     */
    private static void submenuConsultas(Scanner scanner, GerenciadorProducao gerenciador) {
        MateriaPrima materiaPrima = gerenciador.getMateriaPrima();
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
     * Exibe as opcoes de consulta e manutencao da linha de producao.
     *
     * @param scanner leitor de entrada
     * @param gerenciador gerenciador da planta
     */
    private static void submenuLinha(Scanner scanner, GerenciadorProducao gerenciador) {
        boolean noSubmenu = true;
        while (noSubmenu) {
            System.out.println("\n--- LINHA DE PRODUCAO ---");
            System.out.println("[ 1 ] Ver fluxo da linha (maquinas em ordem)");
            System.out.println("[ 2 ] Auditoria estrutural (Linha > Maquina > Partes)");
            System.out.println("[ 3 ] Enviar a linha para manutencao");
            System.out.println("[ 0 ] Voltar");
            int opcao = lerInteiro(scanner, "Escolha: ", 0, 3);

            if (opcao == 0) {
                noSubmenu = false;
            } else if (opcao == 1) {
                System.out.println();
                gerenciador.exibirFluxoDaLinha();
            } else if (opcao == 2) {
                System.out.println();
                gerenciador.exibirAuditoriaEstrutural();
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
     */
    private static void comprarMateriaPrima(Scanner scanner, GerenciadorProducao gerenciador) {
        MateriaPrima materiaPrima = gerenciador.getMateriaPrima();
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
     * Grava o estado atual da planta no arquivo de persistencia.
     *
     * @param gerenciador planta a ser salva
     * @param persistencia servico de persistencia
     * @throws PersistenciaException se o arquivo nao puder ser gravado
     */
    private static void salvarPlanta(GerenciadorProducao gerenciador, PersistenciaPlanta persistencia) {
        persistencia.salvar(gerenciador);
        System.out.println("  [SISTEMA] Estado da planta salvo em: " + persistencia.getArquivoPadrao().toAbsolutePath());
    }

    /**
     * Recupera a planta salva, apos confirmar que o estado atual sera descartado.
     * Se a leitura falhar, a planta atual continua valendo.
     *
     * @param scanner leitor de entrada
     * @param atual planta em uso
     * @param persistencia servico de persistencia
     * @return a planta recuperada ou, se o operador desistir, a planta atual
     * @throws PersistenciaException se o arquivo nao existir, estiver corrompido ou nao puder ser lido
     */
    private static GerenciadorProducao carregarPlanta(Scanner scanner, GerenciadorProducao atual,
                                                      PersistenciaPlanta persistencia) {
        System.out.println("\n--- CARREGAR ESTADO DA PLANTA ---");
        System.out.println("O estado atual sera substituido pelo que esta salvo em:");
        System.out.println(persistencia.getArquivoPadrao().toAbsolutePath());
        System.out.println("[ 1 ] Carregar (alteracoes nao salvas serao perdidas)");
        System.out.println("[ 0 ] Cancelar");
        if (lerInteiro(scanner, "Escolha: ", 0, 1) == 0) {
            return atual;
        }
        GerenciadorProducao carregada = persistencia.carregar();
        System.out.println("  [SISTEMA] Estado da planta recuperado do arquivo.");
        return carregada;
    }

    /**
     * Define a planta com que o turno comeca. Se houver um estado salvo, o operador escolhe entre
     * recupera-lo e iniciar uma planta nova; sem arquivo, ou se o arquivo nao puder ser lido,
     * a planta padrao e criada a partir do cenario escolhido. O arquivo salvo nunca e alterado
     * nem apagado aqui: so e substituido se o operador salvar mais tarde. Fica visivel no pacote
     * para ser testado sem o menu.
     *
     * @param scanner leitor de entrada
     * @param persistencia servico de persistencia
     * @return planta pronta para operar
     */
    static GerenciadorProducao iniciarPlanta(Scanner scanner, PersistenciaPlanta persistencia) {
        if (persistencia.existeEstadoSalvo()) {
            System.out.println(">>> ESTADO SALVO ENCONTRADO <<<");
            System.out.println(persistencia.getArquivoPadrao().toAbsolutePath());
            System.out.println("[ 1 ] Recuperar a planta salva");
            System.out.println("[ 2 ] Iniciar uma nova planta (o arquivo so e substituido se voce salvar depois)");
            if (lerInteiro(scanner, "Escolha: ", 1, 2) == 1) {
                try {
                    GerenciadorProducao salva = persistencia.carregar();
                    System.out.println(">>> Estado da planta recuperado <<<");
                    return salva;
                } catch (PersistenciaException e) {
                    System.out.println(">>> [AVISO] Nao foi possivel recuperar o estado salvo: " + e.getMessage());
                    System.out.println(">>> O arquivo foi mantido como estava. Iniciando a planta padrao. <<<");
                }
            }
        } else {
            System.out.println(">>> Nenhum estado salvo encontrado (primeira execucao). Iniciando a planta padrao. <<<");
        }

        System.out.println("\n>>> CONFIGURACAO DE TURNO <<<");
        System.out.println("[ 1 ] Cenario IDEAL (orcamento farto, maquinas novas)");
        System.out.println("[ 2 ] Cenario APOCALIPTICO (orcamento apertado, maquinas desgastadas)");

        Cenario cenarioEscolhido = Cenario.IDEAL;
        int esc = lerInteiro(scanner, "Escolha o cenario de operacao: ", 1, 2);
        if (esc == 2) {
            cenarioEscolhido = Cenario.APOCALIPTICO;
        }
        return PlantaPadrao.criar(cenarioEscolhido);
    }

    /**
     * Pergunta se o estado deve ser salvo antes de fechar o programa.
     *
     * @param scanner leitor de entrada
     * @param gerenciador planta em uso
     * @param persistencia servico de persistencia
     * @return {@code true} se o turno deve ser encerrado, {@code false} se o operador voltou ao menu
     * @throws PersistenciaException se o salvamento escolhido falhar; nesse caso o programa nao encerra
     */
    private static boolean encerrarTurno(Scanner scanner, GerenciadorProducao gerenciador,
                                         PersistenciaPlanta persistencia) {
        System.out.println("\n--- ENCERRAR TURNO ---");
        System.out.println("[ 1 ] Salvar o estado da planta e encerrar");
        System.out.println("[ 2 ] Encerrar sem salvar");
        System.out.println("[ 0 ] Voltar ao menu");
        int opcao = lerInteiro(scanner, "Escolha: ", 0, 2);

        if (opcao == 0) {
            return false;
        }
        if (opcao == 1) {
            salvarPlanta(gerenciador, persistencia);
        }
        System.out.println("  [SISTEMA] Turno encerrado. Ate logo!\n");
        return true;
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
            System.out.println(linhaBanner("     [V6.0] Persistencia XML e Testes Automatizados"));
            System.out.println(bordaBanner);
            System.out.println(linhaBanner("Operadores Logados: Eduardo Pontes e Icaro Amaral"));
            System.out.println(bordaBanner);
            System.out.println();

            PersistenciaPlanta persistencia = new PersistenciaPlanta();
            GerenciadorProducao gerenciador = iniciarPlanta(scanner, persistencia);

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
                System.out.println(linhaPainel(" [ 7 ] Linha de Producao (estrutura e partes)"));
                System.out.println(linhaPainel(" [ 8 ] Salvar estado da planta"));
                System.out.println(linhaPainel(" [ 9 ] Carregar estado da planta"));
                System.out.println(linhaPainel(" [ 0 ] Encerrar Turno"));
                System.out.println("+===================================================+");

                int opcao = lerInteiro(scanner, "[SISTEMA] Selecione um menu: ", 0, 9);
                try {
                    switch (opcao) {
                        case 1:
                            submenuDemandas(scanner, gerenciador);
                            break;
                        case 2:
                            submenuProducao(scanner, gerenciador);
                            break;
                        case 3:
                            submenuConsultas(scanner, gerenciador);
                            break;
                        case 4:
                            comprarMateriaPrima(scanner, gerenciador);
                            break;
                        case 5:
                            submenuEstrategia(scanner, gerenciador);
                            break;
                        case 6:
                            submenuAuditoria(scanner, gerenciador);
                            break;
                        case 7:
                            submenuLinha(scanner, gerenciador);
                            break;
                        case 8:
                            salvarPlanta(gerenciador, persistencia);
                            break;
                        case 9:
                            gerenciador = carregarPlanta(scanner, gerenciador, persistencia);
                            break;
                        case 0:
                            executando = !encerrarTurno(scanner, gerenciador, persistencia);
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
