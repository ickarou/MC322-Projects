package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;

/** Ciclo de vida e regras de uma demanda de producao. */
@DisplayName("Demanda: ciclo de vida e regras")
class DemandaTest {

    private Demanda demanda;

    @BeforeEach
    void preparar() {
        demanda = new Demanda(CarcacaSuperior.TIPO, 4);
    }

    @Test
    void demandaNovaComecaPendenteESemCustoEstimado() {
        assertEquals(CarcacaSuperior.TIPO, demanda.getTipoProduto());
        assertEquals(4, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(0.0, demanda.getCustoUnitario(), 0.0);
    }

    @Test
    void criacaoInvalidaLancaDemandaInvalida() {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(null, 1));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda("  ", 1));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(CarcacaSuperior.TIPO, -1));
    }

    @Test
    void atualizarQuantidadeSomaAoPedido() {
        demanda.atualizarQuantidade(6);
        assertEquals(10, demanda.getQuantidadeProdutos());
    }

    @Test
    void atualizarQuantidadeInvalidaEhRecusada() {
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(0));
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(-3));
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(Integer.MAX_VALUE));
        assertEquals(4, demanda.getQuantidadeProdutos());
    }

    @Test
    void atualizarDuranteAProducaoDevolveAPendente() {
        demanda.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());
        demanda.atualizarQuantidade(1);
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
    }

    @Test
    void demandaCanceladaNaoPodeSerAtualizada() {
        demanda.cancelar("sem verba");
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(1));
    }

    @Test
    void cancelarExigeMotivoEDemandaAberta() {
        assertThrows(DemandaInvalidaException.class, () -> demanda.cancelar(" "));
        assertThrows(DemandaInvalidaException.class, () -> demanda.cancelar(null));
        demanda.cancelar("motivo");
        assertThrows(DemandaInvalidaException.class, () -> demanda.cancelar("de novo"));
    }

    @Test
    void iniciarProducaoSoValeParaDemandaPendente() {
        demanda.iniciarProducao();
        assertThrows(DemandaInvalidaException.class, () -> demanda.iniciarProducao());
    }

    @Test
    void atenderSoValeParaDemandaEmProducao() {
        assertThrows(DemandaInvalidaException.class, () -> demanda.atender());
        demanda.iniciarProducao();
        demanda.atender();
        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());
    }

    @Test
    void aprovacaoParcialMantemADemandaPendenteComORestante() {
        demanda.iniciarProducao();
        demanda.registrarPecasAprovadas(3);
        assertEquals(1, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
    }

    @Test
    void aprovacaoTotalConcluiADemanda() {
        demanda.iniciarProducao();
        demanda.registrarPecasAprovadas(4);
        assertEquals(0, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());
    }

    @Test
    void aprovacaoInvalidaEhRecusadaSemAlterarADemanda() {
        assertThrows(DemandaInvalidaException.class, () -> demanda.registrarPecasAprovadas(5));
        assertThrows(DemandaInvalidaException.class, () -> demanda.registrarPecasAprovadas(-1));
        assertEquals(4, demanda.getQuantidadeProdutos());
    }

    @Test
    void calculaAMateriaPrimaPeloConsumoDoProduto() {
        assertEquals(1200, demanda.calcularMateriaPrimaNecessaria(new CarcacaSuperior("M")));
    }

    @Test
    void produtoDeOutroTipoNaoServeParaACalculoDeMateriaPrima() {
        assertThrows(DemandaInvalidaException.class, () -> demanda.calcularMateriaPrimaNecessaria(new CarcacaInferior("M")));
        assertThrows(DemandaInvalidaException.class, () -> demanda.calcularMateriaPrimaNecessaria(null));
    }

    @Test
    void viabilidadeFinanceiraCompararCustoTotalComOOrcamento() {
        demanda.definirCustoUnitarioEstimado(100.0);
        assertEquals(400.0, demanda.calcularCustoTotalEstimado(), 0.0001);
        assertTrue(demanda.viavelFinanceiramente(400.0));
        assertFalse(demanda.viavelFinanceiramente(399.99));
        assertThrows(IllegalArgumentException.class, () -> demanda.viavelFinanceiramente(-1.0));
    }

    @Test
    void custoEstimadoInvalidoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> demanda.definirCustoUnitarioEstimado(-1.0));
        assertThrows(IllegalArgumentException.class, () -> demanda.definirCustoUnitarioEstimado(Double.NaN));
    }

    @Test
    void restaurarRecriaADemandaComOEstadoSalvo() {
        Demanda restaurada = Demanda.restaurar(CarcacaInferior.TIPO, 7, StatusDemanda.CANCELADA, 800.0);
        assertEquals(CarcacaInferior.TIPO, restaurada.getTipoProduto());
        assertEquals(7, restaurada.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CANCELADA, restaurada.getStatus());
        assertEquals(800.0, restaurada.getCustoUnitario(), 0.0);
    }

    @Test
    void restaurarPreservaOsStatusQueNaoSaoTransitorios() {
        assertEquals(StatusDemanda.PENDENTE, Demanda.restaurar(CarcacaInferior.TIPO, 2, StatusDemanda.PENDENTE, 0.0).getStatus());
        assertEquals(StatusDemanda.CONCLUIDA, Demanda.restaurar(CarcacaInferior.TIPO, 0, StatusDemanda.CONCLUIDA, 0.0).getStatus());
        assertEquals(StatusDemanda.CANCELADA, Demanda.restaurar(CarcacaInferior.TIPO, 2, StatusDemanda.CANCELADA, 0.0).getStatus());
    }

    @Test
    void restaurarDevolveParaPendenteUmaDemandaQueEstavaEmProducao() {
        Demanda restaurada = Demanda.restaurar(CarcacaInferior.TIPO, 7, StatusDemanda.EM_PRODUCAO, 800.0);
        assertEquals(StatusDemanda.PENDENTE, restaurada.getStatus());
        assertEquals(7, restaurada.getQuantidadeProdutos());
        assertEquals(800.0, restaurada.getCustoUnitario(), 0.0);
    }

    @Test
    void demandaRestauradaDeEmProducaoPodeVoltarAProduzir() {
        Demanda restaurada = Demanda.restaurar(CarcacaInferior.TIPO, 7, StatusDemanda.EM_PRODUCAO, 0.0);
        restaurada.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, restaurada.getStatus());
    }

    @Test
    void restaurarAplicaAsMesmasRegrasDeUmaDemandaNormal() {
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(CarcacaInferior.TIPO, -1, StatusDemanda.PENDENTE, 0.0));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(CarcacaInferior.TIPO, 1, null, 0.0));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(CarcacaInferior.TIPO, 1, StatusDemanda.PENDENTE, -1.0));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(CarcacaInferior.TIPO, 1, StatusDemanda.PENDENTE, Double.NaN));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(CarcacaInferior.TIPO, 3, StatusDemanda.CONCLUIDA, 0.0));
        assertThrows(DemandaInvalidaException.class, () -> Demanda.restaurar(" ", 1, StatusDemanda.PENDENTE, 0.0));
    }
}
