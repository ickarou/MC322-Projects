package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.UtilidadesDeTeste;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OperacaoInvalidaException;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;

/** Agregacao de maquinas na linha de producao. */
@DisplayName("LinhaDeProducao: agregacao de maquinas")
class LinhaDeProducaoTest {

    private LinhaDeProducao linha;
    private Maquina injetora;
    private Maquina braco;
    private Maquina estacao;

    @BeforeEach
    void preparar() {
        linha = new LinhaDeProducao("Linha de Teste");
        injetora = new Injetora("Injetora", Cenario.IDEAL);
        braco = new BracoEncaixotador("Braco", Cenario.IDEAL);
        estacao = new EstacaoTestes("Estacao", Cenario.IDEAL);
    }

    private void montarLinhaCompleta() {
        linha.adicionarMaquina(injetora);
        linha.adicionarMaquina(braco);
        linha.adicionarMaquina(estacao);
    }

    @Test
    void linhaNovaComecaVazia() {
        assertEquals("Linha de Teste", linha.getNome());
        assertEquals(0, linha.getQuantidadeMaquinas());
        assertEquals(0.0f, linha.calcularCustoPorPeca(), 0.0f);
        assertNull(linha.buscarNomeDaMaquinaQuebrada());
    }

    @Test
    void nomeDaLinhaEhObrigatorio() {
        assertThrows(IllegalArgumentException.class, () -> new LinhaDeProducao(" "));
        assertThrows(IllegalArgumentException.class, () -> new LinhaDeProducao(null));
    }

    @Test
    void maquinasFicamNaOrdemEmQueForamAdicionadas() {
        montarLinhaCompleta();
        assertEquals(List.of(injetora, braco, estacao), linha.getMaquinas());
    }

    @Test
    void maquinaNulaOuRepetidaEhRecusada() {
        linha.adicionarMaquina(injetora);
        assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(null));
        assertThrows(OperacaoInvalidaException.class, () -> linha.adicionarMaquina(injetora));
        assertEquals(1, linha.getQuantidadeMaquinas());
    }

    @Test
    void listaDeMaquinasEhSomenteParaLeitura() {
        linha.adicionarMaquina(injetora);
        assertThrows(UnsupportedOperationException.class, () -> linha.getMaquinas().add(braco));
        assertThrows(UnsupportedOperationException.class, () -> linha.getMaquinas().clear());
        assertEquals(1, linha.getQuantidadeMaquinas());
    }

    @Test
    void removerTiraAMaquinaDaLinhaSemDestruiLa() {
        montarLinhaCompleta();
        linha.removerMaquina(braco);
        assertEquals(List.of(injetora, estacao), linha.getMaquinas());
        assertFalse(braco.estaQuebrada(), "A maquina removida continua existindo e utilizavel");
    }

    @Test
    void removerMaquinaForaDaLinhaOuLigadaEhRecusado() {
        linha.adicionarMaquina(injetora);
        assertThrows(OperacaoInvalidaException.class, () -> linha.removerMaquina(braco));
        assertThrows(IllegalArgumentException.class, () -> linha.removerMaquina(null));

        injetora.ligar();
        assertThrows(OperacaoInvalidaException.class, () -> linha.removerMaquina(injetora));
        assertEquals(1, linha.getQuantidadeMaquinas());
    }

    @Test
    void custoPorPecaSomaOCustoDeTodasAsMaquinas() {
        montarLinhaCompleta();
        assertEquals(800.0f, linha.calcularCustoPorPeca(), 0.0001f);
    }

    @Test
    void ligarEDesligarTodasAfetaCadaMaquina() {
        montarLinhaCompleta();
        linha.ligarTodas();
        for (Maquina m : linha.getMaquinas()) {
            assertTrue(m.estaLigada());
        }
        linha.desligarTodas();
        for (Maquina m : linha.getMaquinas()) {
            assertFalse(m.estaLigada());
        }
    }

    @Test
    void linhaVaziaNaoLigaNemProcessa() {
        assertThrows(OperacaoInvalidaException.class, () -> linha.ligarTodas());
        assertThrows(OperacaoInvalidaException.class, () -> linha.processarPeca(new CarcacaSuperior("S1")));
    }

    @Test
    void processarPecaExigeAPecaEAsMaquinasLigadas() {
        montarLinhaCompleta();
        assertThrows(IllegalArgumentException.class, () -> linha.processarPeca(null));
        assertThrows(MaquinaIndisponivelException.class, () -> linha.processarPeca(new CarcacaSuperior("S1")));
    }

    @Test
    void maquinaQuebradaEhLocalizadaEImpedeALigacaoDaLinha() {
        montarLinhaCompleta();
        UtilidadesDeTeste.quebrar(braco);

        assertEquals("Braco", linha.buscarNomeDaMaquinaQuebrada());
        assertThrows(MaquinaIndisponivelException.class, () -> linha.ligarTodas());
        for (Maquina m : linha.getMaquinas()) {
            assertFalse(m.estaLigada(), "Nenhuma maquina deve ser ligada quando uma esta quebrada");
        }
    }

    @Test
    void relatorioEmCascataPercorreLinhaMaquinasEPartes() {
        montarLinhaCompleta();
        String relatorio = linha.gerarRelatorioDiagnostico();

        assertTrue(relatorio.startsWith("Linha de Teste (3 maquinas)"), relatorio);
        assertTrue(relatorio.contains("Injetora"), relatorio);
        assertTrue(relatorio.contains("Estacao"), relatorio);
        assertTrue(relatorio.contains("Atuador 3"), "O braco tem tres atuadores: " + relatorio);
    }

    @Test
    void linhaPrecisaDeManutencaoQuandoUmaMaquinaPrecisa() {
        montarLinhaCompleta();
        assertFalse(linha.precisaManutencao());
        UtilidadesDeTeste.quebrar(estacao);
        assertTrue(linha.precisaManutencao());
    }
}
