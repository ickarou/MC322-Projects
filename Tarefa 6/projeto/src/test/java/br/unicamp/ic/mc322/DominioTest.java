package br.unicamp.ic.mc322;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OperacaoInvalidaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;

/**
 * Testes unitarios para as entidades de dominio, composicao de maquinas e invariantes.
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
class DominioTest {

    @Test
    void deveControlarEstoqueDeMateriaPrimaCorretamente() {
        MateriaPrima abs = new MateriaPrima("ABS000", "Plastico ABS", 1000, "g", 0.50f);
        assertTrue(abs.verificarDisponibilidade(600));

        abs.consumir(400);
        assertEquals(600, abs.getQuantidade());

        abs.adicionarEstoque(250);
        assertEquals(850, abs.getQuantidade());
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueForInsuficienteOuInvalido() {
        MateriaPrima abs = new MateriaPrima("ABS000", "Plastico ABS", 100, "g", 0.50f);

        assertThrows(RecursoInsuficienteException.class, () -> abs.consumir(150));
        assertThrows(IllegalArgumentException.class, () -> abs.consumir(0));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("", "ABS", 10, "g", 1.0f));
    }

    @Test
    void deveGerenciarCicloDeVidaDaDemandaECalcularMateriaPrima() {
        Produto molde = new CarcacaSuperior("MOLDE-TOP");
        Demanda demanda = new Demanda("Carcaca Superior", 10);

        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(3000, demanda.calcularMateriaPrimaNecessaria(molde));

        demanda.atualizarQuantidade(5);
        assertEquals(15, demanda.getQuantidadeProdutos());

        demanda.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());

        // Entrega parcial mantem a demanda PENDENTE com o saldo restante
        demanda.registrarPecasAprovadas(10);
        assertEquals(5, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());

        // Entrega total conclui a demanda
        demanda.iniciarProducao();
        demanda.registrarPecasAprovadas(5);
        assertEquals(0, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());

        // Demanda concluida nao pode ser alterada nem cancelada
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(2));
        assertThrows(DemandaInvalidaException.class, () -> demanda.cancelar("Teste"));
    }

    @Test
    void deveRespeitarComposicaoDaMaquinaEImpedirOperacaoQuandoQuebrada() {
        Injetora injetora = new Injetora("Injetora Alpha", Cenario.IDEAL);
        assertEquals(100, injetora.getSaude());
        assertFalse(injetora.precisaManutencao());

        // Restaura um estado com atuador em nivel critico (25) e motor desgastado (50)
        injetora.restaurarEstado(false, 50, 80, List.of(25, 90));
        assertEquals(25, injetora.getSaude());
        assertTrue(injetora.precisaManutencao());
        assertFalse(injetora.estaQuebrada());

        // Quebra um componente (saude = 0) e verifica bloqueio de acionamento
        injetora.restaurarEstado(false, 0, 80, List.of(25, 90));
        assertTrue(injetora.estaQuebrada());
        assertThrows(MaquinaIndisponivelException.class, injetora::ligar);

        // Reparo restaura todas as partes para 100%
        injetora.repararMaquina();
        assertEquals(100, injetora.getSaude());
        assertFalse(injetora.estaQuebrada());
    }

    @Test
    void deveGerenciarAgregacaoNaLinhaDeProducaoECustoPorPeca() {
        LinhaDeProducao linha = new LinhaDeProducao("Linha Teste");
        Injetora injetora = new Injetora("Injetora 1", Cenario.IDEAL);
        BracoEncaixotador braco = new BracoEncaixotador("Braco 1", Cenario.IDEAL);

        linha.adicionarMaquina(injetora);
        linha.adicionarMaquina(braco);

        assertEquals(2, linha.getQuantidadeMaquinas());
        assertEquals(600.0f, linha.calcularCustoPorPeca(), 0.001f);

        // Nao permite duplicar a mesma maquina na linha
        assertThrows(OperacaoInvalidaException.class, () -> linha.adicionarMaquina(injetora));

        // Nao permite remover maquina ligada
        linha.ligarTodas();
        assertThrows(OperacaoInvalidaException.class, () -> linha.removerMaquina(injetora));

        linha.desligarTodas();
        linha.removerMaquina(injetora);
        assertEquals(1, linha.getQuantidadeMaquinas());
    }
}