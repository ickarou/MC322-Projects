package br.unicamp.ic.mc322.model.produto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Comportamento comum dos produtos e particularidades de cada peca. */
@DisplayName("Produto: pecas, qualidade e risco de falha")
class ProdutoTest {

    @Test
    void cadaPecaTemConsumoQualidadeETipoProprios() {
        Produto superior = new CarcacaSuperior("S1");
        Produto inferior = new CarcacaInferior("I1");
        Produto lateral = new ProtecaoLateral("L1");

        assertEquals(300, superior.getQuantidadeMateriaPrimaPorUnidade());
        assertEquals(200, inferior.getQuantidadeMateriaPrimaPorUnidade());
        assertEquals(150, lateral.getQuantidadeMateriaPrimaPorUnidade());
        assertEquals(0.9f, superior.getQualidade(), 0.0001f);
        assertEquals(0.7f, inferior.getQualidade(), 0.0001f);
        assertEquals(0.5f, lateral.getQualidade(), 0.0001f);
        assertEquals(CarcacaSuperior.TIPO, superior.getTipo());
        assertEquals(CarcacaInferior.TIPO, inferior.getTipo());
        assertEquals(ProtecaoLateral.TIPO, lateral.getTipo());
    }

    @Test
    void pecaNovaComecaSemLoteESemRiscoAcumulado() {
        Produto peca = new CarcacaSuperior("S1");
        assertEquals("S1", peca.getId());
        assertEquals("Aguardando injecao", peca.getStatus());
        assertEquals(0.0f, peca.getProbabilidadeFalhaAcumulada(), 0.0f);
        assertNull(peca.getLote());
    }

    @Test
    void identificadorVazioEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> new CarcacaSuperior(""));
        assertThrows(IllegalArgumentException.class, () -> new CarcacaInferior(null));
    }

    @Test
    void processarSomaORiscoDaPropriaPeca() {
        Produto peca = new CarcacaSuperior("S1");
        peca.processar();
        assertEquals(0.05f, peca.getProbabilidadeFalhaAcumulada(), 0.0001f);
    }

    @Test
    void riscoAcumuladoNuncaPassaDeCemPorCento() {
        Produto peca = new CarcacaInferior("I1");
        peca.aumentarProbabilidadeFalha(0.8f);
        peca.aumentarProbabilidadeFalha(0.8f);
        assertEquals(1.0f, peca.getProbabilidadeFalhaAcumulada(), 0.0f);
    }

    @Test
    void riscoForaDaFaixaEhRecusado() {
        Produto peca = new ProtecaoLateral("L1");
        assertThrows(IllegalArgumentException.class, () -> peca.aumentarProbabilidadeFalha(-0.1f));
        assertThrows(IllegalArgumentException.class, () -> peca.aumentarProbabilidadeFalha(1.1f));
        assertThrows(IllegalArgumentException.class, () -> peca.aumentarProbabilidadeFalha(Float.NaN));
        assertEquals(0.0f, peca.getProbabilidadeFalhaAcumulada(), 0.0f);
    }

    @Test
    void pecaPrecisaDeAtencaoAPartirDeVintePorCentoDeRisco() {
        Produto peca = new CarcacaSuperior("S1");
        peca.aumentarProbabilidadeFalha(0.19f);
        assertFalse(peca.precisaManutencao());
        peca.aumentarProbabilidadeFalha(0.01f);
        assertTrue(peca.precisaManutencao());
    }

    @Test
    void loteEStatusNaoAceitamValorVazio() {
        Produto peca = new CarcacaSuperior("S1");
        assertThrows(IllegalArgumentException.class, () -> peca.setLote(" "));
        assertThrows(IllegalArgumentException.class, () -> peca.setStatus(null));
        peca.setLote("LOTE-001");
        peca.setStatus("Aprovado");
        assertEquals("LOTE-001", peca.getLote());
        assertEquals("Aprovado", peca.getStatus());
    }

    @Test
    void soEstaAprovadaAPecaQueTerminouAInspecaoComoAprovada() {
        Produto peca = new CarcacaSuperior("S1");
        assertFalse(peca.estaAprovado(), "Uma peca nova ainda nao passou pela inspecao");

        peca.setStatus(Produto.STATUS_APROVADO);
        assertTrue(peca.estaAprovado());

        peca.setStatus(Produto.STATUS_APROVADO_INDEVIDAMENTE);
        assertTrue(peca.estaAprovado());

        for (String status : new String[] {"Injetado com Sucesso", "Empacotamento realizado", "Reprovado na Inspecao",
                "Reprovado Indevidamente", "Rejeitada na inspecao", "aprovado"}) {
            peca.setStatus(status);
            assertFalse(peca.estaAprovado(), "Nao deve contar como aprovada: " + status);
        }
    }

    @Test
    void criarNovaUnidadeGeraPecaDoMesmoTipoSemHistorico() {
        Produto molde = new CarcacaInferior("MOLDE");
        molde.aumentarProbabilidadeFalha(0.5f);

        Produto nova = molde.criarNovaUnidade("I2");

        assertNotSame(molde, nova);
        assertEquals("I2", nova.getId());
        assertEquals(molde.getTipo(), nova.getTipo());
        assertEquals(0.0f, nova.getProbabilidadeFalhaAcumulada(), 0.0f);
    }

    @Test
    void relatorioDeDiagnosticoInformaQualidadeERisco() {
        Produto peca = new CarcacaSuperior("S1");
        peca.aumentarProbabilidadeFalha(0.1f);
        String relatorio = peca.gerarRelatorioDiagnostico();
        assertTrue(relatorio.contains("0,90") || relatorio.contains("0.90"), relatorio);
        assertTrue(relatorio.contains("10") && relatorio.contains("%"), relatorio);
    }
}
