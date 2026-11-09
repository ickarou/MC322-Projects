package br.unicamp.ic.mc322;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;

/**
 * Testes unitarios para persistencia e recuperacao de estado em XML,
 * garantindo isolamento de arquivos com @TempDir e verificacao de falhas.
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
class PersistenciaPlantaTest {

    @TempDir
    Path diretorioTemporario;

    @Test
    void deveSalvarECarregarEstadoCompletoDaPlantaPreservandoRelacionamentos() {
        Path arquivoXml = diretorioTemporario.resolve("planta_teste.xml");
        PersistenciaPlanta persistencia = new PersistenciaPlanta(arquivoXml);

        // 1. Prepara planta com estado customizado
        MateriaPrima abs = new MateriaPrima("ABS000", "Plastico ABS", 3450, "g", 0.50f);
        GerenciadorProducao original = new GerenciadorProducao(abs, Cenario.APOCALIPTICO);
        original.setEstrategia(new EstrategiaLoteCritico());

        Injetora injetora = new Injetora("Injetora CNC Alpha", Cenario.APOCALIPTICO);
        injetora.restaurarEstado(false, 75, 60, List.of(45, 80));

        BracoEncaixotador braco = new BracoEncaixotador("Braco KUKA", Cenario.APOCALIPTICO);
        braco.restaurarEstado(false, 90, 85, List.of(70, 65, 55));

        EstacaoTestes estacao = new EstacaoTestes("Estacao ESI", Cenario.APOCALIPTICO);
        estacao.restaurarEstado(false, 88, 92, List.of(95, 40));

        original.registrarMaquina(injetora);
        original.registrarMaquina(braco);
        original.registrarMaquina(estacao);

        CarcacaSuperior moldeTop = new CarcacaSuperior("MOLDE-TOP");
        CarcacaInferior moldeBot = new CarcacaInferior("MOLDE-BOT");

        Demanda d1 = new Demanda("Carcaca Superior", 12);
        Demanda d2 = new Demanda("Carcaca Inferior", 0);
        d2.restaurarStatus(StatusDemanda.CONCLUIDA);

        original.registrarDemanda(d1, moldeTop);
        original.registrarDemanda(d2, moldeBot);

        Produto pecaNoArmazem = new CarcacaSuperior("LOTE-001-MOLDE-TOP-1");
        pecaNoArmazem.restaurarEstado("LOTE-001", "Aprovado", 0.15f);
        original.restaurarEstadoPlanta(1850.50f, 3, 2, List.of(pecaNoArmazem));

        // 2. Salva e recarrega a partir do arquivo XML
        persistencia.salvar(original);
        assertTrue(Files.exists(arquivoXml));

        GerenciadorProducao recuperado = persistencia.carregar();

        // 3. Verifica preservacao exata do estado geral e estoque
        assertEquals(Cenario.APOCALIPTICO, recuperado.getCenarioAtivo());
        assertEquals(1850.50f, recuperado.getBudget(), 0.01f);
        assertEquals("Lote Critico", recuperado.getNomeEstrategiaAtual());
        assertEquals(3, recuperado.getContadorLotes());
        assertEquals(2, recuperado.getTotalRejeitados());
        assertEquals(3450, recuperado.getMateriaPrima().getQuantidade());

        // 4. Verifica maquinas e composicao das partes internas
        List<Maquina> maquinas = recuperado.getLinha().getMaquinas();
        assertEquals(3, maquinas.size());
        Maquina injetoraRec = maquinas.get(0);
        assertEquals("Injetora CNC Alpha", injetoraRec.getNome());
        assertEquals(75, injetoraRec.getSaudeMotor());
        assertEquals(60, injetoraRec.getSaudeSensor());
        assertEquals(List.of(45, 80), injetoraRec.getSaudeAtuadores());
        assertEquals(45, injetoraRec.getSaude());

        // 5. Verifica demandas e armazem
        assertEquals(2, recuperado.getDemandas().size());
        assertEquals(12, recuperado.getDemandas().get(0).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, recuperado.getDemandas().get(0).getStatus());
        assertEquals(StatusDemanda.CONCLUIDA, recuperado.getDemandas().get(1).getStatus());

        assertEquals(1, recuperado.getProdutosFabricados().size());
        Produto pecaRec = recuperado.getProdutosFabricados().get(0);
        assertEquals("LOTE-001-MOLDE-TOP-1", pecaRec.getId());
        assertEquals("LOTE-001", pecaRec.getLote());
        assertEquals(0.15f, pecaRec.getProbabilidadeFalhaAcumulada(), 0.001f);
    }

    @Test
    void deveLancarExcecaoQuandoArquivoNaoExistirOuEstiverVazio() throws IOException {
        Path inexistente = diretorioTemporario.resolve("nao_existe.xml");
        PersistenciaPlanta persistencia = new PersistenciaPlanta(inexistente);
        assertThrows(PersistenciaException.class, persistencia::carregar);

        Path arquivoVazio = diretorioTemporario.resolve("vazio.xml");
        Files.createFile(arquivoVazio);
        assertThrows(PersistenciaException.class, () -> persistencia.carregar(arquivoVazio));
    }

    @Test
    void deveLancarExcecaoQuandoXmlEstiverCorrompidoOuMalformado() throws IOException {
        Path corrompido = diretorioTemporario.resolve("corrompido.xml");
        Files.writeString(corrompido, "<planta><materiaPrima><tagSemFechar></planta>");

        PersistenciaPlanta persistencia = new PersistenciaPlanta(corrompido);
        assertThrows(PersistenciaException.class, persistencia::carregar);
    }
}