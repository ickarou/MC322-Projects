package br.unicamp.ic.mc322.model.maquina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.UtilidadesDeTeste;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;

/** Composicao, desgaste, reparo e restauracao de estado das maquinas. */
@DisplayName("Maquina: composicao, desgaste e estado")
class MaquinaTest {

    private Injetora injetora;

    @BeforeEach
    void preparar() {
        injetora = new Injetora("Injetora Teste", Cenario.IDEAL);
    }

    private static List<String> nomesDasPartes(Maquina maquina) {
        return new ArrayList<>(maquina.getSaudeDasPartes().keySet());
    }

    @Test
    void maquinaCriaSuasPartesNoConstrutor() {
        assertEquals(List.of("Motor", "Sensor", "Atuador 1", "Atuador 2"), nomesDasPartes(injetora));
        assertEquals(List.of("Motor", "Sensor", "Atuador 1", "Atuador 2", "Atuador 3"),
                nomesDasPartes(new BracoEncaixotador("Braco", Cenario.IDEAL)));
        assertEquals(List.of("Motor", "Sensor", "Atuador 1", "Atuador 2"),
                nomesDasPartes(new EstacaoTestes("Estacao", Cenario.IDEAL)));
    }

    @Test
    void maquinaNovaEstaDesligadaSaudavelEOperacional() {
        assertFalse(injetora.estaLigada());
        assertFalse(injetora.estaQuebrada());
        assertFalse(injetora.precisaManutencao());
        assertEquals(100, injetora.getSaude());
        assertEquals(5000, injetora.getCapacidadeMaxima());
        assertEquals(500.0f, injetora.getCustoOperacao(), 0.0001f);
    }

    @Test
    void construtorRecusaDadosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Injetora(null, Cenario.IDEAL));
        assertThrows(IllegalArgumentException.class, () -> new Injetora("  ", Cenario.IDEAL));
        assertThrows(IllegalArgumentException.class, () -> new Injetora("Injetora", null));
    }

    @Test
    void probabilidadeDeFalhaDependeDoCenario() {
        assertEquals(0.075f, injetora.getProbabilidadeFalha(), 0.0001f);
        assertEquals(0.30f, new Injetora("Injetora", Cenario.APOCALIPTICO).getProbabilidadeFalha(), 0.0001f);
    }

    @Test
    void ligarEDesligarAlteramOEstadoDeOperacao() {
        injetora.ligar();
        assertTrue(injetora.estaLigada());
        injetora.desligar();
        assertFalse(injetora.estaLigada());
    }

    @Test
    void processarComAMaquinaDesligadaLancaMaquinaIndisponivel() {
        Produto peca = new CarcacaSuperior("S1");
        MaquinaIndisponivelException erro = assertThrows(MaquinaIndisponivelException.class, () -> injetora.processar(peca));
        assertTrue(erro.getMessage().contains("Injetora Teste"));
    }

    @Test
    void processarProdutoNuloEhRecusado() {
        injetora.ligar();
        assertThrows(IllegalArgumentException.class, () -> injetora.processar(null));
    }

    @Test
    void injetoraProcessaEMarcaAPeca() {
        injetora.ligar();
        Produto peca = new CarcacaSuperior("S1");

        boolean aprovada = injetora.processar(peca);

        assertTrue(aprovada, "A injetora nunca rejeita a peca");
        assertEquals("Injetado com Sucesso", peca.getStatus());
        assertTrue(peca.getProbabilidadeFalhaAcumulada() >= 0.05f, "A injecao soma pelo menos o risco da propria peca");
    }

    @Test
    void desgasteReduzASaudeDaMaquina() {
        Injetora apocaliptica = new Injetora("Injetora", Cenario.APOCALIPTICO);
        for (int i = 0; i < 1000 && apocaliptica.getSaude() == 100; i++) {
            apocaliptica.aplicarDesgaste();
        }
        assertTrue(apocaliptica.getSaude() < 100, "Depois de varios usos a saude deve ter diminuido");
    }

    @Test
    void saudeDaMaquinaEhADaParteMaisDesgastada() {
        Map<String, Float> saudes = injetora.getSaudeDasPartes();
        saudes.put("Atuador 2", 42.0f);
        injetora.restaurarSaudeDasPartes(saudes);
        assertEquals(42, injetora.getSaude());
        assertFalse(injetora.precisaManutencao());

        saudes.put("Motor", 25.0f);
        injetora.restaurarSaudeDasPartes(saudes);
        assertEquals(25, injetora.getSaude());
        assertTrue(injetora.precisaManutencao());
    }

    @Test
    void quebraDeQualquerParteImpedeALigacao() {
        UtilidadesDeTeste.quebrar(injetora);

        assertTrue(injetora.estaQuebrada());
        assertEquals(0, injetora.getSaude());
        assertThrows(MaquinaIndisponivelException.class, () -> injetora.ligar());
        assertFalse(injetora.estaLigada());
    }

    @Test
    void repararRestauraTodasAsPartes() {
        UtilidadesDeTeste.quebrar(injetora);

        injetora.repararMaquina();

        assertFalse(injetora.estaQuebrada());
        assertEquals(100, injetora.getSaude());
        for (float saude : injetora.getSaudeDasPartes().values()) {
            assertEquals(100.0f, saude, 0.0f);
        }
        injetora.ligar();
        assertTrue(injetora.estaLigada());
    }

    @Test
    void restaurarSaudeDasPartesDevolveOEstadoSalvo() {
        Map<String, Float> salvo = new LinkedHashMap<>();
        salvo.put("Motor", 90.5f);
        salvo.put("Sensor", 80.0f);
        salvo.put("Atuador 1", 70.25f);
        salvo.put("Atuador 2", 0.0f);

        injetora.restaurarSaudeDasPartes(salvo);

        assertEquals(salvo, injetora.getSaudeDasPartes());
        assertTrue(injetora.estaQuebrada(), "Um atuador com saude zero quebra a maquina");
    }

    @Test
    void restaurarComEstadoInvalidoLancaExcecaoSemAlterarAMaquina() {
        Map<String, Float> original = injetora.getSaudeDasPartes();

        Map<String, Float> faltando = new LinkedHashMap<>(original);
        faltando.remove("Atuador 2");
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(faltando));

        Map<String, Float> sobrando = new LinkedHashMap<>(original);
        sobrando.put("Atuador 3", 50.0f);
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(sobrando));

        Map<String, Float> nomeErrado = new LinkedHashMap<>(original);
        nomeErrado.remove("Motor");
        nomeErrado.put("Turbina", 50.0f);
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(nomeErrado));

        Map<String, Float> acimaDoLimite = new LinkedHashMap<>(original);
        acimaDoLimite.put("Motor", 101.0f);
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(acimaDoLimite));

        Map<String, Float> negativa = new LinkedHashMap<>(original);
        negativa.put("Sensor", -1.0f);
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(negativa));

        Map<String, Float> invalida = new LinkedHashMap<>(original);
        invalida.put("Sensor", Float.NaN);
        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(invalida));

        assertThrows(IllegalArgumentException.class, () -> injetora.restaurarSaudeDasPartes(null));

        assertEquals(original, injetora.getSaudeDasPartes(), "Uma restauracao recusada nao pode alterar parte alguma");
    }

    @Test
    void saudeDasPartesEhUmaCopiaEnaoExpoeAsPartes() {
        Map<String, Float> copia = injetora.getSaudeDasPartes();
        copia.put("Motor", 1.0f);
        assertEquals(100.0f, injetora.getSaudeDasPartes().get("Motor"), 0.0f);
    }

    @Test
    void relatorioDeDiagnosticoListaAMaquinaEAsPartes() {
        String relatorio = injetora.gerarRelatorioDiagnostico();
        assertNotNull(relatorio);
        assertTrue(relatorio.startsWith("Injetora Teste"), relatorio);
        for (String parte : nomesDasPartes(injetora)) {
            assertTrue(relatorio.contains(parte), "O relatorio deve citar " + parte);
        }
    }
}
