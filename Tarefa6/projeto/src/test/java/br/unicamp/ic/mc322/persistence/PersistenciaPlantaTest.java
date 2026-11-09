package br.unicamp.ic.mc322.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import br.unicamp.ic.mc322.UtilidadesDeTeste;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.service.EstadoPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.PlantaPadrao;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;
import br.unicamp.ic.mc322.strategy.EstrategiaMetaDeOrcamento;

/**
 * Salvar, carregar e atualizar a planta em XML: o que e gravado precisa voltar igual.
 * <p>
 * Todos os arquivos ficam em uma pasta temporaria criada pelo JUnit para cada teste,
 * longe do arquivo real da aplicacao.
 * </p>
 */
@DisplayName("PersistenciaPlanta: salvar, carregar e atualizar")
class PersistenciaPlantaTest {

    @TempDir
    Path pasta;

    private PersistenciaPlanta persistencia;
    private Path arquivo;

    @BeforeEach
    void preparar() {
        persistencia = new PersistenciaPlanta();
        arquivo = pasta.resolve("planta.xml");
    }

    /* Comparacao de plantas */

    private static void assertPlantasEquivalentes(GerenciadorProducao esperada, GerenciadorProducao obtida) {
        EstadoPlanta e = esperada.obterEstado();
        EstadoPlanta o = obtida.obterEstado();

        assertEquals(e.cenario(), o.cenario(), "cenario");
        assertEquals(e.orcamento(), o.orcamento(), 0.0f, "orcamento");
        assertEquals(esperada.getNomeEstrategiaAtual(), obtida.getNomeEstrategiaAtual(), "estrategia");
        assertEquals(e.contadorLotes(), o.contadorLotes(), "contador de lotes");
        assertEquals(e.totalRejeitados(), o.totalRejeitados(), "pecas rejeitadas");

        assertEquals(e.materiaPrima().getId(), o.materiaPrima().getId());
        assertEquals(e.materiaPrima().getNome(), o.materiaPrima().getNome());
        assertEquals(e.materiaPrima().getQuantidade(), o.materiaPrima().getQuantidade(), "estoque");
        assertEquals(e.materiaPrima().getUnidade(), o.materiaPrima().getUnidade());
        assertEquals(e.materiaPrima().getCustoPorUnidade(), o.materiaPrima().getCustoPorUnidade(), 0.0f);

        assertEquals(e.nomeLinha(), o.nomeLinha());
        assertEquals(e.maquinas().size(), o.maquinas().size(), "quantidade de maquinas");
        for (int i = 0; i < e.maquinas().size(); i++) {
            Maquina me = e.maquinas().get(i);
            Maquina mo = o.maquinas().get(i);
            assertEquals(me.getNome(), mo.getNome());
            assertEquals(me.getClass(), mo.getClass(), "tipo da maquina " + me.getNome());
            assertEquals(me.getCapacidadeMaxima(), mo.getCapacidadeMaxima());
            assertEquals(me.getCustoOperacao(), mo.getCustoOperacao(), 0.0f);
            assertEquals(me.getProbabilidadeFalha(), mo.getProbabilidadeFalha(), 0.0f);
            assertEquals(new ArrayList<>(me.getSaudeDasPartes().entrySet()),
                    new ArrayList<>(mo.getSaudeDasPartes().entrySet()), "partes da maquina " + me.getNome());
            assertEquals(me.gerarRelatorioDiagnostico(), mo.gerarRelatorioDiagnostico());
        }

        assertEquals(e.catalogo().size(), o.catalogo().size(), "catalogo");
        for (int i = 0; i < e.catalogo().size(); i++) {
            assertEquals(e.catalogo().get(i).getTipo(), o.catalogo().get(i).getTipo());
            assertEquals(e.catalogo().get(i).getId(), o.catalogo().get(i).getId());
        }

        assertEquals(e.demandas().size(), o.demandas().size(), "quantidade de demandas");
        for (int i = 0; i < e.demandas().size(); i++) {
            Demanda de = e.demandas().get(i);
            Demanda dobt = o.demandas().get(i);
            assertEquals(de.getTipoProduto(), dobt.getTipoProduto());
            assertEquals(de.getQuantidadeProdutos(), dobt.getQuantidadeProdutos());
            assertEquals(statusAposRecuperar(de.getStatus()), dobt.getStatus(), "status da demanda " + de.getTipoProduto());
            assertEquals(de.getCustoUnitario(), dobt.getCustoUnitario(), 0.0);
        }

        assertEquals(e.armazem().size(), o.armazem().size(), "pecas no armazem");
        for (int i = 0; i < e.armazem().size(); i++) {
            Produto pe = e.armazem().get(i);
            Produto po = o.armazem().get(i);
            assertEquals(pe.getId(), po.getId());
            assertEquals(pe.getTipo(), po.getTipo());
            assertEquals(pe.getLote(), po.getLote());
            assertEquals(pe.getStatus(), po.getStatus());
            assertEquals(pe.getQualidade(), po.getQualidade(), 0.0f);
            assertEquals(pe.getProbabilidadeFalhaAcumulada(), po.getProbabilidadeFalhaAcumulada(), 0.0f);
        }
    }

    /** Uma demanda em producao volta como pendente, porque a producao em andamento nao sobrevive ao encerramento. */
    private static StatusDemanda statusAposRecuperar(StatusDemanda salvo) {
        return salvo == StatusDemanda.EM_PRODUCAO ? StatusDemanda.PENDENTE : salvo;
    }

    /** Planta com armazem montado a mao, para nao depender do resultado sorteado da inspecao. */
    private GerenciadorProducao plantaComArmazem() {
        GerenciadorProducao base = PlantaPadrao.criar(Cenario.IDEAL);
        EstadoPlanta estado = base.obterEstado();

        Produto p1 = new CarcacaSuperior("LOTE-001-MOLDE-TOP-1");
        p1.setLote("LOTE-001");
        p1.setStatus("Aprovado");
        p1.aumentarProbabilidadeFalha(0.25f);

        Produto p2 = new ProtecaoLateral("LOTE-002-MOLDE-LAT-1");
        p2.setLote("LOTE-002");
        p2.setStatus("Aprovado Indevidamente");
        p2.aumentarProbabilidadeFalha(0.0123f);

        return GerenciadorProducao.restaurar(new EstadoPlanta(Cenario.IDEAL, 1234.5f, new EstrategiaMetaDeOrcamento(),
                2, 3, estado.materiaPrima(), estado.nomeLinha(), estado.maquinas(), estado.catalogo(),
                estado.demandas(), List.of(p1, p2)));
    }

    /* Arquivo padrao */

    @Test
    void aAplicacaoGuardaOEstadoEmDadosPlantaXml() {
        assertEquals(Path.of("dados", "planta.xml"), PersistenciaPlanta.ARQUIVO_PADRAO);
        assertEquals(PersistenciaPlanta.ARQUIVO_PADRAO, new PersistenciaPlanta().getArquivoPadrao());
    }

    @Test
    void salvarECarregarSemCaminhoUsamOArquivoPadraoDoServico() {
        PersistenciaPlanta comPadrao = new PersistenciaPlanta(arquivo);
        assertEquals(arquivo, comPadrao.getArquivoPadrao());
        assertFalse(comPadrao.existeEstadoSalvo(), "Antes de salvar nao ha estado salvo");

        GerenciadorProducao original = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        comPadrao.salvar(original);

        assertTrue(comPadrao.existeEstadoSalvo());
        assertTrue(Files.isRegularFile(arquivo));
        assertPlantasEquivalentes(original, comPadrao.carregar());
    }

    @Test
    void oArquivoPadraoNaoImpedeUsarOutroCaminho() {
        PersistenciaPlanta comPadrao = new PersistenciaPlanta(arquivo);
        Path outro = pasta.resolve("outro.xml");

        comPadrao.salvar(PlantaPadrao.criar(Cenario.IDEAL), outro);

        assertTrue(Files.isRegularFile(outro));
        assertFalse(comPadrao.existeEstadoSalvo(), "O arquivo padrao nao deve ser tocado");
        assertPlantasEquivalentes(PlantaPadrao.criar(Cenario.IDEAL), comPadrao.carregar(outro));
    }

    /* Salvar */

    @Test
    void salvarGravaUmXmlBemFormadoComAsSecoesDaPlanta() throws Exception {
        persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), arquivo);

        assertTrue(Files.isRegularFile(arquivo));
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(arquivo.toFile());
        assertEquals("planta", doc.getDocumentElement().getTagName());

        String xml = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), "Deve ter a declaracao XML");
        assertTrue(xml.contains("<materiaPrima"), "Deve registrar o estoque de materia-prima");
        assertEquals(3, doc.getElementsByTagName("maquina").getLength(), "Uma maquina por equipamento da linha");
        assertEquals(13, doc.getElementsByTagName("componente").getLength(), "Motor, sensor e atuadores de cada maquina");
        assertEquals(3, doc.getElementsByTagName("demanda").getLength());
    }

    @Test
    void salvarCriaAsPastasQueFaltamNoCaminho() {
        Path profundo = pasta.resolve("dados").resolve("backup").resolve("planta.xml");
        persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), profundo);
        assertTrue(Files.isRegularFile(profundo));
    }

    @Test
    void salvarOMesmoEstadoDuasVezesGeraOMesmoConteudo() throws IOException {
        GerenciadorProducao planta = PlantaPadrao.criar(Cenario.IDEAL);
        Path outro = pasta.resolve("outro.xml");

        persistencia.salvar(planta, arquivo);
        persistencia.salvar(planta, outro);

        assertEquals(Files.readString(arquivo), Files.readString(outro));
    }

    @Test
    void salvarNaoDeixaArquivosTemporariosNaPasta() throws IOException {
        persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), arquivo);

        try (Stream<Path> arquivos = Files.list(pasta)) {
            List<String> nomes = arquivos.map(p -> p.getFileName().toString()).collect(Collectors.toList());
            assertEquals(List.of("planta.xml"), nomes);
        }
    }

    /* Carregar: o que foi salvo volta igual */

    @Test
    void recuperaAPlantaPadraoSemPerdas() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        persistencia.salvar(original, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertNotSame(original, recuperada);
        assertPlantasEquivalentes(original, recuperada);
    }

    @Test
    void recuperaOCenarioApocalipticoComAsConfiguracoesDoCenario() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        persistencia.salvar(original, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertEquals("Apocaliptico", recuperada.getNomeCenarioAtivo());
        assertEquals(4000.0f, recuperada.getBudget(), 0.001f);
        assertEquals(0.30f, recuperada.obterEstado().maquinas().get(0).getProbabilidadeFalha(), 0.0001f);
        assertPlantasEquivalentes(original, recuperada);
    }

    @Test
    void recuperaEstoqueOrcamentoEstrategiaEDemandasEmTodosOsStatus() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        original.atualizarDemanda(CarcacaSuperior.TIPO, 3);
        original.fabricarDemanda(original.getCatalogo().get(0), original.buscarDemandaAtual(CarcacaSuperior.TIPO));
        original.atualizarDemanda(CarcacaInferior.TIPO, 2);
        original.buscarDemandaAtual(CarcacaInferior.TIPO).iniciarProducao();
        original.buscarDemandaAtual(ProtecaoLateral.TIPO).cancelar("teste de persistencia");
        original.comprarMateriaPrima(250);
        original.setEstrategia(new EstrategiaLoteCritico());

        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertPlantasEquivalentes(original, recuperada);
        assertEquals(StatusDemanda.PENDENTE, recuperada.buscarDemandaAtual(CarcacaInferior.TIPO).getStatus());
        assertEquals(StatusDemanda.CANCELADA, recuperada.buscarDemandaAtual(ProtecaoLateral.TIPO).getStatus());
        assertEquals("Lote Critico", recuperada.getNomeEstrategiaAtual());
        assertEquals(5000 - 900 + 250, recuperada.getMateriaPrima().getQuantidade());
    }

    @Test
    void demandaQueEstavaEmProducaoVoltaPendenteEPodeSerProduzida() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        original.atualizarDemanda(CarcacaSuperior.TIPO, 3);
        Demanda emProducao = original.buscarDemandaAtual(CarcacaSuperior.TIPO);
        emProducao.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, emProducao.getStatus());

        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        Demanda demanda = recuperada.buscarDemandaAtual(CarcacaSuperior.TIPO);
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(3, demanda.getQuantidadeProdutos());

        float orcamentoAntes = recuperada.getBudget();
        recuperada.fabricarDemanda(recuperada.getCatalogo().get(0), demanda);

        EstadoPlanta depois = recuperada.obterEstado();
        assertTrue(recuperada.getBudget() < orcamentoAntes, "A producao deve ter gasto orcamento");
        assertEquals(1, depois.contadorLotes());
        assertEquals(3, depois.armazem().size() + demanda.getQuantidadeProdutos(),
                "Cada peca ou foi para o armazem ou continua na demanda");
    }

    @Test
    void recuperaMaisDeUmaDemandaDoMesmoTipoNaOrdemDeRegistro() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        original.buscarDemandaAtual(CarcacaSuperior.TIPO).cancelar("pedido antigo");
        original.registrarDemanda(new Demanda(CarcacaSuperior.TIPO, 8), original.getCatalogo().get(0));

        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertPlantasEquivalentes(original, recuperada);
        assertEquals(8, recuperada.buscarDemandaAtual(CarcacaSuperior.TIPO).getQuantidadeProdutos());
        assertEquals(StatusDemanda.PENDENTE, recuperada.buscarDemandaAtual(CarcacaSuperior.TIPO).getStatus());
    }

    @Test
    void recuperaOArmazemComLoteStatusERiscoDeCadaPeca() {
        GerenciadorProducao original = plantaComArmazem();
        persistencia.salvar(original, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertPlantasEquivalentes(original, recuperada);
        List<Produto> armazem = recuperada.obterEstado().armazem();
        assertEquals(2, armazem.size());
        assertEquals("LOTE-002", armazem.get(1).getLote());
        assertEquals("Aprovado Indevidamente", armazem.get(1).getStatus());
        assertEquals(0.25f, armazem.get(0).getProbabilidadeFalhaAcumulada(), 0.0f);
        assertEquals(0.9f, armazem.get(0).getQualidade(), 0.0001f);
        assertEquals(3, recuperada.obterEstado().totalRejeitados());
    }

    @Test
    void recuperaPlantaSemEstrategiaDefinida() {
        GerenciadorProducao base = PlantaPadrao.criar(Cenario.IDEAL);
        EstadoPlanta e = base.obterEstado();
        GerenciadorProducao semEstrategia = GerenciadorProducao.restaurar(new EstadoPlanta(e.cenario(), e.orcamento(), null,
                0, 0, e.materiaPrima(), e.nomeLinha(), e.maquinas(), e.catalogo(), e.demandas(), e.armazem()));

        persistencia.salvar(semEstrategia, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertEquals("Nenhuma estrategia definida", recuperada.getNomeEstrategiaAtual());
    }

    /* Composicao */

    @Test
    void recuperaODesgasteExatoDeCadaParteDeCadaMaquina() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        for (Maquina maquina : original.obterEstado().maquinas()) {
            for (int i = 0; i < 7; i++) {
                maquina.aplicarDesgaste();
            }
        }

        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        assertPlantasEquivalentes(original, recuperada);
    }

    @Test
    void recuperaMaquinaQuebradaEAindaImpedeAProducao() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        UtilidadesDeTeste.quebrar(original.obterEstado().maquinas().get(2));

        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        Maquina estacao = recuperada.obterEstado().maquinas().get(2);
        assertTrue(estacao.estaQuebrada());
        assertTrue(estacao.precisaManutencao());
        assertFalse(recuperada.obterEstado().maquinas().get(0).estaQuebrada());
    }

    @Test
    void maquinaRecuperadaTemSuasPropriasPartesSemCompartilharNadaComAOriginal() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        persistencia.salvar(original, arquivo);
        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        Maquina daOriginal = original.obterEstado().maquinas().get(0);
        Maquina daRecuperada = recuperada.obterEstado().maquinas().get(0);
        assertNotSame(daOriginal, daRecuperada);
        assertEquals(List.of("Motor", "Sensor", "Atuador 1", "Atuador 2"),
                new ArrayList<>(daRecuperada.getSaudeDasPartes().keySet()));

        for (int i = 0; i < 50; i++) {
            daRecuperada.aplicarDesgaste();
        }
        assertEquals(100, daOriginal.getSaude(), "Desgastar a maquina recuperada nao pode afetar a original");
    }

    /* Substituicao do estado e atualizacao */

    @Test
    void carregarSubstituiOEstadoSemCombinarComODaMemoria() {
        GerenciadorProducao salva = PlantaPadrao.criar(Cenario.IDEAL);
        salva.atualizarDemanda(CarcacaSuperior.TIPO, 4);
        persistencia.salvar(salva, arquivo);

        GerenciadorProducao emMemoria = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        emMemoria.atualizarDemanda(CarcacaInferior.TIPO, 9);
        emMemoria.comprarMateriaPrima(100);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        EstadoPlanta estado = recuperada.obterEstado();
        assertEquals(Cenario.IDEAL, estado.cenario(), "O estado do arquivo vale por inteiro");
        assertEquals(3, estado.maquinas().size(), "As maquinas nao podem ser duplicadas");
        assertEquals(3, estado.demandas().size(), "As demandas nao podem ser duplicadas");
        assertEquals(3, estado.catalogo().size());
        assertEquals(0, recuperada.buscarDemandaAtual(CarcacaInferior.TIPO).getQuantidadeProdutos());
        assertEquals(4, recuperada.buscarDemandaAtual(CarcacaSuperior.TIPO).getQuantidadeProdutos());
        assertEquals(5000, recuperada.getMateriaPrima().getQuantidade());
    }

    @Test
    void alteracoesFeitasDepoisDeSalvarNaoEntramNoArquivo() {
        GerenciadorProducao planta = PlantaPadrao.criar(Cenario.IDEAL);
        persistencia.salvar(planta, arquivo);

        planta.comprarMateriaPrima(1000);
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 5);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);
        assertEquals(5000, recuperada.getMateriaPrima().getQuantidade());
        assertEquals(25000.0f, recuperada.getBudget(), 0.001f);
        assertEquals(0, recuperada.buscarDemandaAtual(CarcacaSuperior.TIPO).getQuantidadeProdutos());
    }

    @Test
    void salvarNovamenteAtualizaOArquivoExistente() {
        GerenciadorProducao planta = PlantaPadrao.criar(Cenario.IDEAL);
        persistencia.salvar(planta, arquivo);

        planta.comprarMateriaPrima(1000);
        planta.atualizarDemanda(CarcacaSuperior.TIPO, 5);
        persistencia.salvar(planta, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);
        assertPlantasEquivalentes(planta, recuperada);
        assertEquals(6000, recuperada.getMateriaPrima().getQuantidade());
        assertEquals(24500.0f, recuperada.getBudget(), 0.001f);
    }

    @Test
    void numeracaoDeLotesContinuaDepoisDeRecuperarAPlanta() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        original.atualizarDemanda(CarcacaSuperior.TIPO, 3);
        original.fabricarDemanda(original.getCatalogo().get(0), original.buscarDemandaAtual(CarcacaSuperior.TIPO));
        persistencia.salvar(original, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);
        recuperada.atualizarDemanda(CarcacaInferior.TIPO, 3);
        recuperada.fabricarDemanda(recuperada.getCatalogo().get(1), recuperada.buscarDemandaAtual(CarcacaInferior.TIPO));

        EstadoPlanta estado = recuperada.obterEstado();
        assertEquals(2, estado.contadorLotes());
        Set<String> ids = new HashSet<>();
        for (Produto peca : estado.armazem()) {
            assertTrue(peca.getLote().equals("LOTE-001") || peca.getLote().equals("LOTE-002"), peca.getLote());
            assertTrue(ids.add(peca.getId()), "Identificador repetido no armazem: " + peca.getId());
        }
    }

    @Test
    void nomesComAcentosSobrevivemAoSalvarECarregar() {
        GerenciadorProducao original = PlantaPadrao.criar(Cenario.IDEAL);
        original.registrarMaquina(new Injetora("Injetora Ágil nº 2", Cenario.IDEAL));
        persistencia.salvar(original, arquivo);

        GerenciadorProducao recuperada = persistencia.carregar(arquivo);

        List<String> nomes = recuperada.obterEstado().maquinas().stream()
                .map(Maquina::getNome).collect(Collectors.toList());
        assertTrue(nomes.contains("Injetora Ágil nº 2"), "Os nomes recuperados foram: " + nomes);
        assertPlantasEquivalentes(original, recuperada);
    }
}
