package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;

/** Regras de estoque e validacao da materia-prima. */
@DisplayName("MateriaPrima: estoque e validacao")
class MateriaPrimaTest {

    private MateriaPrima abs;

    @BeforeEach
    void preparar() {
        abs = new MateriaPrima("ABS000", "Plastico ABS", 1000, "g", 0.50f);
    }

    @Test
    void criacaoGuardaOsDadosInformados() {
        assertEquals("ABS000", abs.getId());
        assertEquals("Plastico ABS", abs.getNome());
        assertEquals(1000, abs.getQuantidade());
        assertEquals("g", abs.getUnidade());
        assertEquals(0.50f, abs.getCustoPorUnidade(), 0.0001f);
    }

    @Test
    void criacaoComDadosInvalidosEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("", "Plastico", 10, "g", 1f));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("A", " ", 10, "g", 1f));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("A", "Plastico", 10, null, 1f));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("A", "Plastico", -1, "g", 1f));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("A", "Plastico", 10, "g", -0.5f));
        assertThrows(IllegalArgumentException.class, () -> new MateriaPrima("A", "Plastico", 10, "g", Float.NaN));
    }

    @Test
    void consumirReduzOEstoque() {
        abs.consumir(300);
        assertEquals(700, abs.getQuantidade());
    }

    @Test
    void consumirMaisQueOEstoqueLancaExcecaoEMantemOSaldo() {
        RecursoInsuficienteException erro = assertThrows(RecursoInsuficienteException.class, () -> abs.consumir(1001));
        assertTrue(erro.getMessage().contains("Plastico ABS"), "A mensagem deve citar o material em falta");
        assertEquals(1000, abs.getQuantidade());
    }

    @Test
    void consumirPodeEsvaziarOEstoqueExatamente() {
        abs.consumir(1000);
        assertEquals(0, abs.getQuantidade());
    }

    @Test
    void consumirQuantidadeNaoPositivaEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> abs.consumir(0));
        assertThrows(IllegalArgumentException.class, () -> abs.consumir(-5));
        assertEquals(1000, abs.getQuantidade());
    }

    @Test
    void adicionarEstoqueSomaAQuantidade() {
        abs.adicionarEstoque(250);
        assertEquals(1250, abs.getQuantidade());
    }

    @Test
    void adicionarQuantidadeNaoPositivaEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> abs.adicionarEstoque(0));
        assertThrows(IllegalArgumentException.class, () -> abs.adicionarEstoque(-1));
    }

    @Test
    void adicionarAlemDoLimiteNumericoLancaExcecaoEMantemOSaldo() {
        assertThrows(RecursoInsuficienteException.class, () -> abs.adicionarEstoque(Integer.MAX_VALUE));
        assertEquals(1000, abs.getQuantidade());
    }

    @Test
    void verificarDisponibilidadeComparaComOEstoque() {
        assertTrue(abs.verificarDisponibilidade(1000));
        assertFalse(abs.verificarDisponibilidade(1001));
        assertThrows(IllegalArgumentException.class, () -> abs.verificarDisponibilidade(-1));
    }
}
