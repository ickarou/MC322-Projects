package br.unicamp.ic.mc322.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.UtilidadesDeTeste;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OperacaoInvalidaException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;

/**
 * Regras de negocio da planta: demandas, orcamento, estoque e producao.
 * <p>
 * A planta padrao no cenario IDEAL custa 800 por peca (injetora 500, braco 100 e estacao 200),
 * comeca com R$ 25000 e 5000 g de plastico, e a carcaca superior consome 300 g por peca.
 * Lotes de poucas pecas nao desgastam o bastante para quebrar uma maquina nova, entao o
 * consumo de orcamento e de materia-prima nesses testes e exato, mesmo com a inspecao sorteada.
 * </p>
 */
@DisplayName("GerenciadorProducao: regras de negocio da planta")
class GerenciadorProducaoTest {

    private GerenciadorProducao planta;
    private Produto moldeSuperior;
    private Produto moldeInferior;

    @BeforeEach
    void preparar() {
        planta = PlantaPadrao.criar(Cenario.IDEAL);
        moldeSuperior = planta.getCatalogo().get(0);
        moldeInferior = planta.getCatalogo().get(1);
    }

    private Demanda demandaAtual(GerenciadorProducao g, String tipo) {
        return g.buscarDemandaAtual(tipo);
    }

    /* Montagem da planta */

    @Test
    void plantaPadraoIdealComecaComOsValoresDoCenario() {
        assertEquals("Ideal", planta.getNomeCenarioAtivo());
        assertEquals(25000.0f, planta.getBudget(), 0.001f);
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
        assertEquals(3, planta.getCatalogo().size());
        assertEquals(3, planta.obterEstado().maquinas().size());
        assertEquals("Linha de Montagem", planta.getNomeEstrategiaAtual());
    }

    @Test
    void plantaPadraoApocalipticaComecaComOrcamentoReduzido() {
        GerenciadorProducao apocaliptica = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        assertEquals("Apocaliptico", apocaliptica.getNomeCenarioAtivo());
        assertEquals(4000.0f, apocaliptica.getBudget(), 0.001f);
    }

    @Test
    void construtorExigeMateriaPrimaECenario() {
        MateriaPrima mp = new MateriaPrima("X", "Material", 10, "g", 1f);
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(null, Cenario.IDEAL));
        assertThrows(IllegalArgumentException.class, () -> new GerenciadorProducao(mp, null));
    }

    @Test
    void plantaSemEstrategiaInformaQueNaoHaNenhuma() {
        GerenciadorProducao nova = new GerenciadorProducao(new MateriaPrima("X", "Material", 10, "g", 1f), Cenario.IDEAL);
        assertEquals("Nenhuma estrategia definida", nova.getNomeEstrategiaAtual());
    }

    /* Demandas */

    @Test
    void registrarDemandaEstimaOCustoUnitarioPelaLinha() {
        Demanda nova = new Demanda(CarcacaSuperior.TIPO, 2);
        planta.registrarDemanda(nova, moldeSuperior);
        assertEquals(800.0, nova.getCustoUnitario(), 0.001);
    }

    @Test
    void demandaMaisRecenteDoTipoEhAAtual() {
        Demanda anterior = demandaAtual(planta, CarcacaSuperior.TIPO);
        Demanda nova = new Demanda(CarcacaSuperior.TIPO, 2);
        planta.registrarDemanda(nova, moldeSuperior);

        assertSame(nova, planta.buscarDemandaAtual(CarcacaSuperior.TIPO));
        assertNotNull(anterior);
        assertNull(planta.buscarDemandaAtual("Peca Inexistente"));
    }

    @Test
    void registrarDemandaInvalidaLancaDemandaInvalida() {
        Demanda tipoErrado = new Demanda(CarcacaInferior.TIPO, 1);
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(tipoErrado, moldeSuperior));
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(null, moldeSuperior));
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(new Demanda(CarcacaSuperior.TIPO, 1), null));
    }

    @Test
    void registrarAMesmaDemandaDuasVezesEhRecusado() {
        Demanda nova = new Demanda(CarcacaSuperior.TIPO, 1);
        planta.registrarDemanda(nova, moldeSuperior);
        assertThrows(DemandaInvalidaException.class, () -> planta.registrarDemanda(nova, moldeSuperior));
    }

    @Test
    void atualizarDemandaSomaAoPedidoPendente() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 5);
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 2);
        assertEquals(7, demandaAtual(planta, CarcacaSuperior.TIPO).getQuantidadeProdutos());
    }

    @Test
    void atualizarDemandaSemPedidoPendenteLancaExcecao() {
        demandaAtual(planta, CarcacaSuperior.TIPO).cancelar("teste");
        assertThrows(DemandaInvalidaException.class, () -> planta.atualizarDemanda(CarcacaSuperior.TIPO, 1));
        assertThrows(DemandaInvalidaException.class, () -> planta.atualizarDemanda("Peca Inexistente", 1));
    }

    /* Orcamento e estoque */

    @Test
    void comprarMateriaPrimaAumentaOEstoqueEDescontaOOrcamento() {
        planta.comprarMateriaPrima(100);
        assertEquals(5100, planta.getMateriaPrima().getQuantidade());
        assertEquals(24950.0f, planta.getBudget(), 0.001f);
    }

    @Test
    void comprarQuantidadeNaoPositivaLancaOperacaoInvalida() {
        assertThrows(OperacaoInvalidaException.class, () -> planta.comprarMateriaPrima(0));
        assertThrows(OperacaoInvalidaException.class, () -> planta.comprarMateriaPrima(-10));
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void comprarSemOrcamentoLancaExcecaoESemAlterarNada() {
        GerenciadorProducao apocaliptica = PlantaPadrao.criar(Cenario.APOCALIPTICO);

        OrcamentoInsuficienteException erro = assertThrows(OrcamentoInsuficienteException.class,
                () -> apocaliptica.comprarMateriaPrima(10000));

        assertTrue(erro.getMessage().contains("Orcamento insuficiente"), erro.getMessage());
        assertEquals(4000.0f, apocaliptica.getBudget(), 0.001f);
        assertEquals(5000, apocaliptica.getMateriaPrima().getQuantidade());
    }

    /* Producao */

    @Test
    void fabricarDemandaConsomeOrcamentoEMateriaPrimaEContabilizaOLote() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 3);
        Demanda demanda = demandaAtual(planta, CarcacaSuperior.TIPO);

        planta.fabricarDemanda(moldeSuperior, demanda);

        EstadoPlanta estado = planta.obterEstado();
        assertEquals(25000.0f - 2400.0f, planta.getBudget(), 0.01f);
        assertEquals(5000 - 900, planta.getMateriaPrima().getQuantidade());
        assertEquals(1, estado.contadorLotes());

        int aprovadas = estado.armazem().size();
        int restantes = demanda.getQuantidadeProdutos();
        assertEquals(3, aprovadas + restantes, "Cada peca ou foi para o armazem ou continua na demanda");
        assertEquals(restantes, estado.totalRejeitados());
        assertEquals(restantes == 0 ? StatusDemanda.CONCLUIDA : StatusDemanda.PENDENTE, demanda.getStatus());
        for (Produto peca : estado.armazem()) {
            assertEquals("LOTE-001", peca.getLote());
        }
        for (Maquina maquina : estado.maquinas()) {
            assertFalse(maquina.estaLigada(), "Ao fim do lote todas as maquinas sao desligadas");
        }
    }

    @Test
    void fabricarSemPedidoAbertoLancaDemandaInvalida() {
        Demanda semPedido = demandaAtual(planta, CarcacaSuperior.TIPO);
        DemandaInvalidaException erro = assertThrows(DemandaInvalidaException.class,
                () -> planta.fabricarDemanda(moldeSuperior, semPedido));
        assertTrue(erro.getMessage().contains("pedido"), erro.getMessage());
    }

    @Test
    void fabricarDemandaNaoPendenteOuNulaEhRecusado() {
        Demanda cancelada = demandaAtual(planta, CarcacaSuperior.TIPO);
        cancelada.cancelar("teste");
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(moldeSuperior, cancelada));
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(moldeSuperior, null));
        assertThrows(DemandaInvalidaException.class, () -> planta.fabricarDemanda(null, cancelada));
    }

    @Test
    void semOrcamentoAProducaoEhRecusadaEADemandaEhCancelada() {
        GerenciadorProducao apocaliptica = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        apocaliptica.atualizarDemanda(CarcacaSuperior.TIPO, 6);
        Demanda demanda = apocaliptica.buscarDemandaAtual(CarcacaSuperior.TIPO);
        Produto molde = apocaliptica.getCatalogo().get(0);

        assertThrows(OrcamentoInsuficienteException.class, () -> apocaliptica.fabricarDemanda(molde, demanda));

        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
        assertEquals(4000.0f, apocaliptica.getBudget(), 0.001f);
        assertEquals(5000, apocaliptica.getMateriaPrima().getQuantidade());
        assertEquals(0, apocaliptica.obterEstado().contadorLotes());
    }

    @Test
    void semMateriaPrimaAProducaoEhRecusadaEADemandaEhCancelada() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 20);
        Demanda demanda = demandaAtual(planta, CarcacaSuperior.TIPO);

        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class,
                () -> planta.fabricarDemanda(moldeSuperior, demanda));

        assertTrue(erro.getMessage().contains("Plastico ABS"), erro.getMessage());
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
        assertEquals(25000.0f, planta.getBudget(), 0.001f);
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void maquinaQuebradaImpedeAProducaoSemAlterarRecursos() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 2);
        Demanda demanda = demandaAtual(planta, CarcacaSuperior.TIPO);
        UtilidadesDeTeste.quebrar(planta.obterEstado().maquinas().get(0));

        MaquinaIndisponivelException erro = assertThrows(MaquinaIndisponivelException.class,
                () -> planta.fabricarDemanda(moldeSuperior, demanda));

        assertTrue(erro.getMessage().contains("Injetora CNC Alpha"), erro.getMessage());
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(2, demanda.getQuantidadeProdutos());
        assertEquals(25000.0f, planta.getBudget(), 0.001f);
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void linhaSemMaquinasNaoProduz() {
        GerenciadorProducao semMaquinas = new GerenciadorProducao(new MateriaPrima("X", "Material", 1000, "g", 1f), Cenario.IDEAL);
        Produto molde = new ProtecaoLateral("MOLDE");
        Demanda demanda = new Demanda(ProtecaoLateral.TIPO, 1);
        semMaquinas.registrarDemanda(demanda, molde);

        assertThrows(OperacaoInvalidaException.class, () -> semMaquinas.fabricarDemanda(molde, demanda));
    }

    /* Estrategia */

    @Test
    void executarProximaProducaoExigeEstrategiaDefinida() {
        GerenciadorProducao semEstrategia = new GerenciadorProducao(new MateriaPrima("X", "Material", 10, "g", 1f), Cenario.IDEAL);
        OperacaoInvalidaException erro = assertThrows(OperacaoInvalidaException.class, semEstrategia::executarProximaProducao);
        assertTrue(erro.getMessage().contains("estrategia"), erro.getMessage());
    }

    @Test
    void executarProximaProducaoSemDemandaElegivelLancaExcecao() {
        assertThrows(OperacaoInvalidaException.class, planta::executarProximaProducao);
    }

    @Test
    void estrategiaAtivaEscolheADemandaQueSeraProduzida() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 1);
        planta.atualizarDemanda(CarcacaInferior.TIPO, 3);
        planta.setEstrategia(new EstrategiaLoteCritico());
        assertEquals("Lote Critico", planta.getNomeEstrategiaAtual());

        planta.executarProximaProducao();

        assertEquals(1, demandaAtual(planta, CarcacaSuperior.TIPO).getQuantidadeProdutos(), "A demanda menor nao deve ser tocada");
        assertTrue(demandaAtual(planta, CarcacaInferior.TIPO).getQuantidadeProdutos() <= 3);
        assertEquals(25000.0f - 2400.0f, planta.getBudget(), 0.01f);
        assertEquals(1, planta.obterEstado().contadorLotes());
    }

    @Test
    void estrategiaNulaEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> planta.setEstrategia(null));
        assertEquals("Linha de Montagem", planta.getNomeEstrategiaAtual());
    }

    /* Manutencao */

    @Test
    void repararMaquinasCobraPeloDesgasteERestauraASaude() {
        Maquina quebrada = planta.obterEstado().maquinas().get(1);
        UtilidadesDeTeste.quebrar(quebrada);
        float custoEsperado = (100 - quebrada.getSaude()) * 5.0f;

        planta.repararMaquinas();

        assertFalse(quebrada.estaQuebrada());
        assertEquals(100, quebrada.getSaude());
        assertEquals(25000.0f - custoEsperado, planta.getBudget(), 0.01f);
    }

    @Test
    void repararSemNecessidadeNaoCobraNada() {
        planta.repararMaquinas();
        assertEquals(25000.0f, planta.getBudget(), 0.001f);
    }

    /* Estado e restauracao */

    @Test
    void estadoEntregueTemListasSomenteParaLeitura() {
        EstadoPlanta estado = planta.obterEstado();
        assertThrows(UnsupportedOperationException.class, () -> estado.demandas().add(new Demanda(CarcacaSuperior.TIPO, 1)));
        assertThrows(UnsupportedOperationException.class, () -> estado.maquinas().clear());
        assertThrows(UnsupportedOperationException.class, () -> estado.armazem().clear());
    }

    @Test
    void restaurarReproduzOEstadoEmUmNovoGerenciador() {
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 4);

        GerenciadorProducao outra = GerenciadorProducao.restaurar(planta.obterEstado());

        assertNotSame(planta, outra);
        assertEquals(planta.getBudget(), outra.getBudget(), 0.0f);
        assertEquals(planta.getNomeCenarioAtivo(), outra.getNomeCenarioAtivo());
        assertEquals(planta.getNomeEstrategiaAtual(), outra.getNomeEstrategiaAtual());
        assertEquals(3, outra.obterEstado().maquinas().size());
        assertEquals(4, outra.buscarDemandaAtual(CarcacaSuperior.TIPO).getQuantidadeProdutos());
    }

    private EstadoPlanta estadoCom(float orcamento, int lotes, List<Demanda> demandas, List<Produto> armazem) {
        EstadoPlanta base = planta.obterEstado();
        return new EstadoPlanta(base.cenario(), orcamento, base.estrategia(), lotes, base.totalRejeitados(),
                base.materiaPrima(), base.nomeLinha(), base.maquinas(), base.catalogo(), demandas, armazem);
    }

    private Produto pecaDoArmazem(String id, String lote) {
        Produto peca = moldeSuperior.criarNovaUnidade(id);
        peca.setStatus(Produto.STATUS_APROVADO);
        if (lote != null) {
            peca.setLote(lote);
        }
        return peca;
    }

    @Test
    void restaurarRecusaEstadoQueContrarieAsRegrasDoDominio() {
        List<Demanda> demandas = planta.obterEstado().demandas();

        assertThrows(IllegalArgumentException.class, () -> GerenciadorProducao.restaurar(null));
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(-1.0f, 0, demandas, List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(Float.NaN, 0, demandas, List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, -1, demandas, List.of())));
    }

    @Test
    void restaurarRecusaDemandaSemProdutoNoCatalogoOuRepetida() {
        Demanda semMolde = new Demanda("Peca Misteriosa", 1);
        assertThrows(DemandaInvalidaException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 0, List.of(semMolde), List.of())));

        Demanda repetida = new Demanda(CarcacaSuperior.TIPO, 1);
        assertThrows(DemandaInvalidaException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 0, List.of(repetida, repetida), List.of())));
    }

    @Test
    void restaurarRecusaPecasDoArmazemIncoerentes() {
        List<Demanda> demandas = planta.obterEstado().demandas();

        Produto loteAlemDoContador = pecaDoArmazem("P1", "LOTE-005");
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 1, demandas, List.of(loteAlemDoContador))));

        Produto semLote = pecaDoArmazem("P2", null);
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 1, demandas, List.of(semLote))));

        Produto loteMalFormado = pecaDoArmazem("P3", "LOTE-ABC");
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 1, demandas, List.of(loteMalFormado))));

        Produto original = pecaDoArmazem("P4", "LOTE-001");
        Produto mesmoId = pecaDoArmazem("P4", "LOTE-001");
        assertThrows(IllegalArgumentException.class,
                () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 1, demandas, List.of(original, mesmoId))));
    }

    @Test
    void restaurarRecusaPecasDoArmazemQueNaoForamAprovadasNaInspecao() {
        List<Demanda> demandas = planta.obterEstado().demandas();
        String[] statusNaoAprovados = {"Aguardando injecao", "Injetado com Sucesso", "Empacotamento realizado",
                "Reprovado na Inspecao", "Reprovado Indevidamente", "Rejeitada na inspecao", "aprovado"};

        for (String status : statusNaoAprovados) {
            Produto peca = pecaDoArmazem("P1", "LOTE-001");
            peca.setStatus(status);

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> GerenciadorProducao.restaurar(estadoCom(100.0f, 1, demandas, List.of(peca))),
                    "Status recusado: " + status);
            assertTrue(erro.getMessage().contains("P1") && erro.getMessage().contains(status), erro.getMessage());
        }
    }

    @Test
    void restaurarAceitaPecasAprovadasCorretamenteOuIndevidamente() {
        Produto correta = pecaDoArmazem("P1", "LOTE-001");
        Produto indevida = pecaDoArmazem("P2", "LOTE-001");
        indevida.setStatus(Produto.STATUS_APROVADO_INDEVIDAMENTE);

        GerenciadorProducao restaurada = GerenciadorProducao.restaurar(
                estadoCom(100.0f, 1, planta.obterEstado().demandas(), List.of(correta, indevida)));

        List<Produto> armazem = restaurada.obterEstado().armazem();
        assertEquals(2, armazem.size());
        assertEquals(Produto.STATUS_APROVADO, armazem.get(0).getStatus());
        assertEquals(Produto.STATUS_APROVADO_INDEVIDAMENTE, armazem.get(1).getStatus());
    }

    @Test
    void restaurarAceitaPecasDeLotesJaProduzidos() {
        Produto peca = pecaDoArmazem("P1", "LOTE-001");
        GerenciadorProducao restaurada = GerenciadorProducao.restaurar(
                estadoCom(100.0f, 1, planta.obterEstado().demandas(), List.of(peca)));
        assertEquals(1, restaurada.obterEstado().armazem().size());
        assertEquals(100.0f, restaurada.getBudget(), 0.0f);
    }

    @Test
    void catalogoEhSomenteParaLeitura() {
        assertThrows(UnsupportedOperationException.class, () -> planta.getCatalogo().add(new CarcacaInferior("X")));
        assertEquals(moldeInferior.getTipo(), planta.getCatalogo().get(1).getTipo());
    }
}
