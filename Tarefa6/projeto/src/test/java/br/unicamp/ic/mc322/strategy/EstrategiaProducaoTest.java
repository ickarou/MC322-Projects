package br.unicamp.ic.mc322.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.model.Demanda;

/** Escolha da proxima demanda conforme cada estrategia (padrao Strategy). */
@DisplayName("Estrategias de producao")
class EstrategiaProducaoTest {

    private Demanda pequena;
    private Demanda grande;
    private Demanda vazia;
    private Demanda cancelada;
    private List<Demanda> demandas;

    @BeforeEach
    void preparar() {
        pequena = new Demanda("Carcaca Inferior", 10);
        grande = new Demanda("Carcaca Superior", 20);
        vazia = new Demanda("Protecao Lateral", 0);
        cancelada = new Demanda("Protecao Lateral", 50);
        cancelada.cancelar("teste");
        pequena.definirCustoUnitarioEstimado(100.0);
        grande.definirCustoUnitarioEstimado(100.0);

        demandas = new ArrayList<>(List.of(vazia, cancelada, pequena, grande));
    }

    @Test
    void linhaDeMontagemEscolheAPrimeiraDemandaElegivel() {
        assertSame(pequena, new EstrategiaLinhaDeMontagem().selecionarDemanda(demandas, 100000.0));
    }

    @Test
    void loteCriticoEscolheAMaiorQuantidade() {
        assertSame(grande, new EstrategiaLoteCritico().selecionarDemanda(demandas, 100000.0));
    }

    @Test
    void metaDeOrcamentoEscolheAMaiorDemandaQueCabeNoOrcamento() {
        EstrategiaMetaDeOrcamento estrategia = new EstrategiaMetaDeOrcamento();
        assertSame(grande, estrategia.selecionarDemanda(demandas, 2000.0));
        assertSame(pequena, estrategia.selecionarDemanda(demandas, 1500.0));
        assertNull(estrategia.selecionarDemanda(demandas, 500.0));
    }

    @Test
    void semDemandaElegivelNenhumaEstrategiaEscolheNada() {
        List<Demanda> inelegiveis = List.of(vazia, cancelada);
        assertNull(new EstrategiaLinhaDeMontagem().selecionarDemanda(inelegiveis, 1000.0));
        assertNull(new EstrategiaLoteCritico().selecionarDemanda(inelegiveis, 1000.0));
        assertNull(new EstrategiaMetaDeOrcamento().selecionarDemanda(inelegiveis, 1000.0));
    }

    @Test
    void demandaEmProducaoNaoEhElegivel() {
        pequena.iniciarProducao();
        grande.iniciarProducao();
        assertNull(new EstrategiaLoteCritico().selecionarDemanda(demandas, 100000.0));
    }

    @Test
    void entradasInvalidasSaoRecusadas() {
        EstrategiaProducao[] estrategias = {
            new EstrategiaLinhaDeMontagem(), new EstrategiaLoteCritico(), new EstrategiaMetaDeOrcamento()
        };
        for (EstrategiaProducao estrategia : estrategias) {
            assertThrows(IllegalArgumentException.class, () -> estrategia.selecionarDemanda(null, 100.0));
            assertThrows(IllegalArgumentException.class, () -> estrategia.selecionarDemanda(demandas, -1.0));
            assertThrows(IllegalArgumentException.class, () -> estrategia.selecionarDemanda(demandas, Double.NaN));
        }
    }

    @Test
    void listaComElementoNuloEhRecusada() {
        List<Demanda> comNulo = new ArrayList<>(demandas);
        comNulo.add(null);
        assertThrows(IllegalArgumentException.class, () -> new EstrategiaLinhaDeMontagem().selecionarDemanda(comNulo, 100.0));
    }

    @Test
    void cadaEstrategiaTemOSeuNome() {
        assertEquals("Linha de Montagem", new EstrategiaLinhaDeMontagem().getNomeEstrategia());
        assertEquals("Lote Critico", new EstrategiaLoteCritico().getNomeEstrategia());
        assertEquals("Meta de Orcamento", new EstrategiaMetaDeOrcamento().getNomeEstrategia());
    }
}
