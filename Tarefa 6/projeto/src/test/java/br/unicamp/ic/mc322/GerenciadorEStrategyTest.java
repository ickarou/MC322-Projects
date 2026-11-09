package br.unicamp.ic.mc322;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaLinhaDeMontagem;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;
import br.unicamp.ic.mc322.strategy.EstrategiaMetaDeOrcamento;

/**
 * Testes unitarios para as estrategias de producao e regras de negocio do GerenciadorProducao.
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
class GerenciadorEStrategyTest {

    private GerenciadorProducao gerenciador;
    private MateriaPrima plastico;
    private CarcacaSuperior moldeTop;
    private CarcacaInferior moldeBot;
    private ProtecaoLateral moldeLat;

    @BeforeEach
    void prepararPlanta() {
        plastico = new MateriaPrima("ABS000", "Plastico ABS", 10000, "g", 0.50f);
        gerenciador = new GerenciadorProducao(plastico, Cenario.IDEAL);
        gerenciador.registrarMaquina(new Injetora("Injetora Alpha", Cenario.IDEAL));
        gerenciador.registrarMaquina(new BracoEncaixotador("Braco KUKA", Cenario.IDEAL));
        gerenciador.registrarMaquina(new EstacaoTestes("Estacao ESI", Cenario.IDEAL));

        moldeTop = new CarcacaSuperior("MOLDE-TOP");
        moldeBot = new CarcacaInferior("MOLDE-BOT");
        moldeLat = new ProtecaoLateral("MOLDE-LAT");
    }

    @Test
    void deveSelecionarDemandaCorretaConformeEstrategia() {
        Demanda d1 = new Demanda("Carcaca Superior", 5);
        d1.definirCustoUnitarioEstimado(800.0); // Total: 4000

        Demanda d2 = new Demanda("Carcaca Inferior", 20);
        d2.definirCustoUnitarioEstimado(800.0); // Total: 16000

        Demanda d3 = new Demanda("Protecao Lateral", 10);
        d3.definirCustoUnitarioEstimado(800.0); // Total: 8000

        List<Demanda> lista = List.of(d1, d2, d3);

        // 1. Linha de Montagem: primeira elegivel na ordem de chegada (d1)
        assertSame(d1, new EstrategiaLinhaDeMontagem().selecionarDemanda(lista, 5000.0));

        // 2. Lote Critico: maior quantidade absoluta (d2 = 20 un.)
        assertSame(d2, new EstrategiaLoteCritico().selecionarDemanda(lista, 5000.0));

        // 3. Meta de Orcamento: maior que cabe em R$ 10.000 (d3 = 10 un., custa 8000)
        assertSame(d3, new EstrategiaMetaDeOrcamento().selecionarDemanda(lista, 10000.0));

        // Se nenhuma couber no orcamento, Meta de Orcamento retorna null
        assertNull(new EstrategiaMetaDeOrcamento().selecionarDemanda(lista, 1000.0));
    }

    @Test
    void deveExecutarProducaoConsumindoRecursosEAtualizandoContadores() {
        Demanda demanda = new Demanda("Protecao Lateral", 3);
        gerenciador.registrarDemanda(demanda, moldeLat);
        gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());

        float budgetInicial = gerenciador.getBudget();
        int estoqueInicial = plastico.getQuantidade();

        gerenciador.executarProximaProducao();

        // Custo da linha por peca = 500 + 100 + 200 = 800 -> 3 pecas = 2400
        assertEquals(budgetInicial - 2400.0f, gerenciador.getBudget(), 0.01f);
        // Consumo Protecao Lateral = 150g * 3 = 450g
        assertEquals(estoqueInicial - 450, plastico.getQuantidade());
        assertEquals(1, gerenciador.getContadorLotes());
        assertEquals(3, gerenciador.getProdutosFabricados().size() + gerenciador.getTotalRejeitados());
    }

    @Test
    void deveCancelarDemandaELancarExcecaoQuandoOrcamentoForInsuficiente() {
        // Custo de 40 pecas = 40 * 800 = R$ 32.000 (> R$ 25.000 do Cenario IDEAL)
        Demanda demandaCara = new Demanda("Carcaca Superior", 40);
        gerenciador.registrarDemanda(demandaCara, moldeTop);

        assertThrows(OrcamentoInsuficienteException.class,
                () -> gerenciador.fabricarDemanda(moldeTop, demandaCara));
        assertEquals(StatusDemanda.CANCELADA, demandaCara.getStatus());
        assertEquals(25000.0f, gerenciador.getBudget(), 0.01f);
    }

    @Test
    void deveCancelarDemandaELancarExcecaoQuandoMateriaPrimaForInsuficiente() {
        // Estoque = 10.000g; 30 Carcacas Superiores exigem 30 * 300g = 9.000g, mas 40 exigem 12.000g
        // Aumentamos o budget comprando nada, apenas testando limite de estoque no cenario IDEAL:
        // Criamos gerenciador com apenas 500g de ABS
        MateriaPrima poucoPlastico = new MateriaPrima("ABS000", "Plastico ABS", 500, "g", 0.50f);
        GerenciadorProducao gpRestrito = new GerenciadorProducao(poucoPlastico, Cenario.IDEAL);
        gpRestrito.registrarMaquina(new Injetora("Injetora Alpha", Cenario.IDEAL));

        Demanda demanda = new Demanda("Carcaca Superior", 5); // Exige 1500g
        gpRestrito.registrarDemanda(demanda, moldeTop);

        assertThrows(RecursoInsuficienteException.class,
                () -> gpRestrito.fabricarDemanda(moldeTop, demanda));
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
        assertEquals(500, poucoPlastico.getQuantidade());
    }

    @Test
    void deveImpedirProducaoComMaquinaQuebradaERepararConsumindoOrcamento() {
        Injetora injetora = (Injetora) gerenciador.getLinha().getMaquinas().get(0);
        injetora.restaurarEstado(false, 0, 100, List.of(100, 100)); // Quebrada (saude = 0)

        Demanda demanda = new Demanda("Carcaca Inferior", 2);
        gerenciador.registrarDemanda(demanda, moldeBot);

        assertThrows(MaquinaIndisponivelException.class,
                () -> gerenciador.fabricarDemanda(moldeBot, demanda));

        // Reparo de 100 pontos * R$ 5,00 = R$ 500,00
        gerenciador.repararMaquinas();
        assertEquals(100, injetora.getSaude());
        assertEquals(24500.0f, gerenciador.getBudget(), 0.01f);
        assertTrue(gerenciador.getLinha().buscarNomeDaMaquinaQuebrada() == null);
    }
}