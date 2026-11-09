package br.unicamp.ic.mc322.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.unicamp.ic.mc322.exception.ArquivoInexistenteException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.service.EstadoPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.PlantaPadrao;

/**
 * Falhas de persistencia: arquivo ausente, vazio, mal formado, incompativel com o dominio
 * e problemas de leitura e escrita.
 * <p>
 * Os arquivos invalidos sao fabricados a partir de um XML valido, trocando um trecho por
 * vez, de modo que cada teste isola exatamente um defeito. Nenhum teste toca no arquivo
 * real da aplicacao: tudo fica na pasta temporaria do JUnit.
 * </p>
 */
@DisplayName("PersistenciaPlanta: situacoes de erro")
class PersistenciaErrosTest {

    @TempDir
    Path pasta;

    private PersistenciaPlanta persistencia;
    private Path entrada;

    @BeforeEach
    void preparar() {
        persistencia = new PersistenciaPlanta();
        entrada = pasta.resolve("entrada.xml");
    }

    /* Apoio */

    private String xmlDaPlantaPadrao() {
        return xmlDe(PlantaPadrao.criar(Cenario.IDEAL));
    }

    private String xmlDe(GerenciadorProducao planta) {
        Path modelo = pasta.resolve("modelo.xml");
        persistencia.salvar(planta, modelo);
        try {
            return Files.readString(modelo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Nao foi possivel ler o arquivo de apoio", e);
        }
    }

    /** Planta padrao com duas pecas no armazem, para testar os dados das pecas. */
    private String xmlComArmazem() {
        GerenciadorProducao base = PlantaPadrao.criar(Cenario.IDEAL);
        EstadoPlanta estado = base.obterEstado();

        Produto p1 = new CarcacaSuperior("LOTE-001-MOLDE-TOP-1");
        p1.setLote("LOTE-001");
        p1.setStatus("Aprovado");
        p1.aumentarProbabilidadeFalha(0.25f);
        Produto p2 = new CarcacaSuperior("LOTE-001-MOLDE-TOP-2");
        p2.setLote("LOTE-001");
        p2.setStatus("Aprovado");

        GerenciadorProducao planta = GerenciadorProducao.restaurar(new EstadoPlanta(Cenario.IDEAL, 1000.0f,
                estado.estrategia(), 1, 0, estado.materiaPrima(), estado.nomeLinha(), estado.maquinas(),
                estado.catalogo(), estado.demandas(), List.of(p1, p2)));
        return xmlDe(planta);
    }

    private Path escrever(String conteudo) {
        try {
            Files.writeString(entrada, conteudo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Nao foi possivel preparar o arquivo de teste", e);
        }
        return entrada;
    }

    /** Troca todas as ocorrencias de um trecho, garantindo antes que o trecho existe no XML. */
    private static String trocar(String xml, String trecho, String novo) {
        assertTrue(xml.contains(trecho), "O XML de apoio nao contem o trecho esperado: " + trecho);
        return xml.replace(trecho, novo);
    }

    /** Troca a primeira ocorrencia de uma expressao regular, garantindo antes que ela existe. */
    private static String trocarRegex(String xml, String regex, String novo) {
        assertTrue(Pattern.compile(regex).matcher(xml).find(),
                "O XML de apoio nao contem o padrao esperado: " + regex);
        return xml.replaceFirst(regex, novo);
    }

    private DadosInvalidosException recusar(String xml) {
        Path arquivo = escrever(xml);
        return assertThrows(DadosInvalidosException.class, () -> persistencia.carregar(arquivo));
    }

    private void assertMensagemCita(DadosInvalidosException erro, String trecho) {
        assertNotNull(erro.getMessage());
        assertTrue(erro.getMessage().contains(trecho),
                "A mensagem deveria citar '" + trecho + "', mas foi: " + erro.getMessage());
    }

    /* Arquivo ausente, vazio ou que nao e XML */

    @Test
    void arquivoInexistenteLancaArquivoInexistente() {
        Path ausente = pasta.resolve("nao-existe.xml");

        ArquivoInexistenteException erro = assertThrows(ArquivoInexistenteException.class,
                () -> persistencia.carregar(ausente));

        assertTrue(erro.getMessage().contains("nao-existe.xml"), "A mensagem deve dizer qual arquivo faltou");
    }

    @Test
    void asExcecoesDePersistenciaFazemParteDaHierarquiaDaPlanta() {
        PersistenciaException inexistente = assertThrows(PersistenciaException.class,
                () -> persistencia.carregar(pasta.resolve("nao-existe.xml")));
        assertTrue(inexistente instanceof ArquivoInexistenteException);
        assertTrue(inexistente instanceof PlantaException);

        DadosInvalidosException invalido = recusar("<planta");
        assertTrue(invalido instanceof PersistenciaException);
        assertTrue(invalido instanceof PlantaException);
    }

    @Test
    void arquivoVazioLancaDadosInvalidos() {
        DadosInvalidosException erro = recusar("");
        assertMensagemCita(erro, "vazio");
    }

    @Test
    void arquivoSoComEspacosEQuebrasDeLinhaTambemEhVazio() {
        DadosInvalidosException erro = recusar("   \n\n \t \n");
        assertMensagemCita(erro, "vazio");
    }

    @Test
    void textoQueNaoEXmlLancaDadosInvalidos() {
        DadosInvalidosException erro = recusar("isto nao e um arquivo de planta");
        assertMensagemCita(erro, "XML");
        assertNotNull(erro.getCause(), "A falha original do analisador deve ser preservada");
    }

    @Test
    void xmlTruncadoLancaDadosInvalidos() {
        String xml = xmlDaPlantaPadrao();
        recusar(xml.substring(0, xml.length() / 2));
    }

    @Test
    void xmlComElementoSemFechamentoLancaDadosInvalidos() {
        recusar(trocar(xmlDaPlantaPadrao(), "</materiaPrima>", ""));
    }

    @Test
    void declaracaoDeDoctypeEhRecusada() {
        String xml = xmlDaPlantaPadrao();
        String comEntidade = trocar(xml, "<planta ",
                "<!DOCTYPE planta [<!ENTITY externa SYSTEM \"file:///etc/passwd\">]>\n<planta ");
        recusar(comEntidade);
    }

    /* Estrutura diferente da esperada */

    @Test
    void raizDiferenteDePlantaLancaDadosInvalidos() {
        DadosInvalidosException erro = recusar("<?xml version=\"1.0\" encoding=\"UTF-8\"?><fabrica/>");
        assertMensagemCita(erro, "fabrica");
    }

    @Test
    void versaoDoFormatoNaoSuportadaEhRecusada() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(), "versao=\"1\"", "versao=\"99\""));
        assertMensagemCita(erro, "99");
    }

    @Test
    void elementoObrigatorioAusenteEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<orcamento>25000.0</orcamento>", ""));
        assertMensagemCita(erro, "orcamento");
    }

    @Test
    void atributoObrigatorioAusenteEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<materiaPrima id=\"ABS000\">", "<materiaPrima>"));
        assertMensagemCita(erro, "id");
    }

    @Test
    void secaoDeMaquinasAusenteEhRecusada() {
        DadosInvalidosException erro = recusar(trocarRegex(xmlDaPlantaPadrao(),
                "<linhaDeProducao[\\s\\S]*</linhaDeProducao>", ""));
        assertMensagemCita(erro, "linhaDeProducao");
    }

    @Test
    void elementoRepetidoQueDeveSerUnicoEhRecusado() {
        String xml = xmlDaPlantaPadrao();
        recusar(trocar(xml, "<orcamento>25000.0</orcamento>",
                "<orcamento>25000.0</orcamento><orcamento>10.0</orcamento>"));
    }

    @Test
    void valorQueNaoEhNumericoEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<orcamento>25000.0</orcamento>", "<orcamento>muito</orcamento>"));
        assertMensagemCita(erro, "orcamento");
    }

    @Test
    void inteiroComCasasDecimaisEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<quantidade>5000</quantidade>", "<quantidade>5000.5</quantidade>"));
        assertMensagemCita(erro, "quantidade");
    }

    @Test
    void cenarioDesconhecidoEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<cenario>IDEAL</cenario>", "<cenario>CAOTICO</cenario>"));
        assertMensagemCita(erro, "CAOTICO");
    }

    @Test
    void estrategiaDesconhecidaEhRecusada() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<estrategia>Linha de Montagem</estrategia>", "<estrategia>Aleatoria</estrategia>"));
        assertMensagemCita(erro, "Aleatoria");
    }

    @Test
    void statusDeDemandaDesconhecidoEhRecusado() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(), "status=\"PENDENTE\"", "status=\"ATRASADA\""));
        assertMensagemCita(erro, "ATRASADA");
    }

    @Test
    void tipoDeMaquinaDesconhecidoEhRecusado() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(), "tipo=\"Injetora\"", "tipo=\"Prensa\""));
        assertMensagemCita(erro, "Prensa");
    }

    @Test
    void tipoDeProdutoDesconhecidoNoCatalogoEhRecusado() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(),
                "<molde id=\"MOLDE-TOP\" tipo=\"Carcaca Superior\"/>", "<molde id=\"MOLDE-TOP\" tipo=\"Parafuso\"/>"));
        assertMensagemCita(erro, "Parafuso");
    }

    /* Dados incompativeis com as regras do dominio */

    @Test
    void orcamentoNegativoEhRecusado() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<orcamento>25000.0</orcamento>", "<orcamento>-10.0</orcamento>"));
        assertMensagemCita(erro, "orcamento");
    }

    @Test
    void estoqueNegativoEhRecusado() {
        recusar(trocar(xmlDaPlantaPadrao(), "<quantidade>5000</quantidade>", "<quantidade>-5</quantidade>"));
    }

    @Test
    void contadorDeLotesNegativoEhRecusado() {
        recusar(trocar(xmlDaPlantaPadrao(), "<contadorLotes>0</contadorLotes>", "<contadorLotes>-1</contadorLotes>"));
    }

    @Test
    void saudeDeComponenteAcimaDeCemEhRecusada() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<componente nome=\"Motor\" saude=\"100.0\"/>", "<componente nome=\"Motor\" saude=\"150.0\"/>"));
        assertMensagemCita(erro, "Motor");
    }

    @Test
    void saudeDeComponenteNegativaEhRecusada() {
        recusar(trocar(xmlDaPlantaPadrao(), "<componente nome=\"Sensor\" saude=\"100.0\"/>", "<componente nome=\"Sensor\" saude=\"-1.0\"/>"));
    }

    @Test
    void maquinaSemTodasAsSuasPartesEhRecusada() {
        recusar(trocar(xmlDaPlantaPadrao(), "<componente nome=\"Atuador 2\" saude=\"100.0\"/>", ""));
    }

    @Test
    void maquinaComPartePerdidaNoLugarDeOutraEhRecusada() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<componente nome=\"Motor\" saude=\"100.0\"/>", "<componente nome=\"Turbina\" saude=\"100.0\"/>"));
        assertMensagemCita(erro, "Motor");
    }

    @Test
    void componenteRepetidoNaMaquinaEhRecusado() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(),
                "<componente nome=\"Atuador 2\" saude=\"100.0\"/>", "<componente nome=\"Atuador 1\" saude=\"100.0\"/>"));
        assertMensagemCita(erro, "Atuador 1");
    }

    @Test
    void maquinaRepetidaNaLinhaEhRecusada() {
        recusar(trocar(xmlDaPlantaPadrao(), "nome=\"Braco Robotico KUKA\"", "nome=\"Injetora CNC Alpha\""));
    }

    @Test
    void configuracaoDaMaquinaDiferenteDaDoCodigoEhRecusada() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(),
                "<capacidadeMaxima>5000</capacidadeMaxima>", "<capacidadeMaxima>9999</capacidadeMaxima>"));
        assertMensagemCita(erro, "incompativel");
    }

    @Test
    void probabilidadeDeFalhaIncompativelComOCenarioEhRecusada() {
        DadosInvalidosException erro = recusar(trocar(xmlDaPlantaPadrao(),
                "<probabilidadeFalha>0.075</probabilidadeFalha>", "<probabilidadeFalha>0.3</probabilidadeFalha>"));
        assertMensagemCita(erro, "probabilidadeFalha");
    }

    @Test
    void demandaConcluidaComPecasPendentesEhRecusada() {
        recusar(trocarRegex(xmlDaPlantaPadrao(),
                "<demanda status=\"PENDENTE\"( tipo=\"Carcaca Superior\">\\s*<quantidade>)0<",
                "<demanda status=\"CONCLUIDA\"$1 5<"));
    }

    @Test
    void demandaComQuantidadeNegativaEhRecusada() {
        recusar(trocarRegex(xmlDaPlantaPadrao(),
                "(<demanda [^>]*>\\s*<quantidade>)0<", "$1-3<"));
    }

    @Test
    void demandaComCustoNegativoEhRecusada() {
        recusar(trocarRegex(xmlDaPlantaPadrao(),
                "<custoUnitario>800.0</custoUnitario>", "<custoUnitario>-1.0</custoUnitario>"));
    }

    @Test
    void demandaDeProdutoForaDoCatalogoEhRecusada() {
        DadosInvalidosException erro = recusar(
                trocar(xmlDaPlantaPadrao(), "<molde id=\"MOLDE-TOP\" tipo=\"Carcaca Superior\"/>", ""));
        assertMensagemCita(erro, "Carcaca Superior");
    }

    @Test
    void pecaDoArmazemDeLoteMaiorQueOContadorEhRecusada() {
        recusar(trocar(xmlComArmazem(), "lote=\"LOTE-001\"", "lote=\"LOTE-009\""));
    }

    @Test
    void pecaDoArmazemComCodigoDeLoteInvalidoEhRecusada() {
        recusar(trocar(xmlComArmazem(), "lote=\"LOTE-001\"", "lote=\"PRIMEIRO\""));
    }

    @Test
    void pecaDoArmazemDeTipoForaDoCatalogoEhRecusada() {
        recusar(trocarRegex(xmlComArmazem(), "(<produto [^>]*)tipo=\"Carcaca Superior\"", "$1tipo=\"Parafuso\""));
    }

    @Test
    void pecasComOMesmoIdentificadorNoArmazemSaoRecusadas() {
        recusar(trocar(xmlComArmazem(), "id=\"LOTE-001-MOLDE-TOP-2\"", "id=\"LOTE-001-MOLDE-TOP-1\""));
    }

    @Test
    void riscoDePecaForaDeZeroAUmEhRecusado() {
        recusar(trocar(xmlComArmazem(), "<probabilidadeFalha>0.25</probabilidadeFalha>", "<probabilidadeFalha>1.5</probabilidadeFalha>"));
    }

    @Test
    void pecaSemStatusEhRecusada() {
        recusar(trocarRegex(xmlComArmazem(), "<produto ([^>]*)>\\s*<status>Aprovado</status>", "<produto $1><status> </status>"));
    }

    @Test
    void pecaDoArmazemQueNaoFoiAprovadaNaInspecaoEhRecusada() {
        String[] statusNaoAprovados = {"Reprovado na Inspecao", "Reprovado Indevidamente", "Rejeitada na inspecao",
                "Aguardando injecao", "Injetado com Sucesso", "Empacotamento realizado", "Status Desconhecido"};

        for (String status : statusNaoAprovados) {
            DadosInvalidosException erro = recusar(
                    trocar(xmlComArmazem(), "<status>Aprovado</status>", "<status>" + status + "</status>"));
            assertMensagemCita(erro, "LOTE-001-MOLDE-TOP-1");
            assertMensagemCita(erro, status);
        }
    }

    @Test
    void umaPecaNaoAprovadaNoMeioDoArmazemTambemInvalidaOArquivo() {
        recusar(trocarRegex(xmlComArmazem(), "(id=\"LOTE-001-MOLDE-TOP-2\"[^>]*>\\s*<status>)Aprovado(</status>)",
                "$1Reprovado na Inspecao$2"));
    }

    @Test
    void pecaDoArmazemAprovadaIndevidamenteContinuaSendoAceita() {
        GerenciadorProducao recuperada = persistencia.carregar(escrever(
                trocar(xmlComArmazem(), "<status>Aprovado</status>", "<status>Aprovado Indevidamente</status>")));

        List<Produto> armazem = recuperada.obterEstado().armazem();
        assertEquals(2, armazem.size());
        assertEquals(Produto.STATUS_APROVADO_INDEVIDAMENTE, armazem.get(0).getStatus());
        assertEquals(Produto.STATUS_APROVADO_INDEVIDAMENTE, armazem.get(1).getStatus());
    }

    /* Falhas de leitura e de escrita */

    @Test
    void caminhoQueEUmaPastaNaoEhLidoComoArquivo() {
        PersistenciaException erro = assertThrows(PersistenciaException.class, () -> persistencia.carregar(pasta));

        assertFalse(erro instanceof DadosInvalidosException);
        assertFalse(erro instanceof ArquivoInexistenteException);
        assertTrue(erro.getMessage().contains("nao e um arquivo"));
    }

    @Test
    void salvarOndeNaoEPossivelCriarAPastaLancaPersistenciaException() throws IOException {
        Path comum = pasta.resolve("comum.txt");
        Files.writeString(comum, "uma pasta nao pode ser criada sobre um arquivo");

        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), comum.resolve("planta.xml")));

        assertNotNull(erro.getCause(), "A falha de E/S original deve ser preservada");
        assertTrue(erro.getMessage().contains("planta.xml"));
    }

    @Test
    void salvarSobreUmaPastaLancaPersistenciaExceptionENaoDeixaTemporarios() throws IOException {
        Path destino = pasta.resolve("destino");
        Files.createDirectory(destino);
        Files.writeString(destino.resolve("conteudo.txt"), "o destino nao pode ser substituido");

        assertThrows(PersistenciaException.class, () -> persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), destino));

        try (Stream<Path> arquivos = Files.list(pasta)) {
            List<String> nomes = arquivos.map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
            assertEquals(List.of("destino"), nomes, "Nenhum arquivo temporario pode sobrar");
        }
        assertTrue(Files.exists(destino.resolve("conteudo.txt")));
    }

    @Test
    void limpezaDoTemporarioDevolveAFalhaQuandoNaoConsegueRemover() throws IOException {
        assertNull(PersistenciaPlanta.apagarTemporario(null), "Sem temporario nao ha o que limpar");

        Path inexistente = pasta.resolve("nao-existe.tmp");
        assertNull(PersistenciaPlanta.apagarTemporario(inexistente), "Um temporario que nao existe mais nao e falha");

        Path temporario = pasta.resolve("planta.tmp");
        Files.writeString(temporario, "sobra de uma gravacao interrompida");
        assertNull(PersistenciaPlanta.apagarTemporario(temporario));
        assertFalse(Files.exists(temporario), "O temporario deve ter sido removido");

        Path naoVazia = pasta.resolve("pasta.tmp");
        Files.createDirectory(naoVazia);
        Files.writeString(naoVazia.resolve("conteudo.txt"), "impede a remocao da pasta");
        IOException falha = PersistenciaPlanta.apagarTemporario(naoVazia);
        assertNotNull(falha, "Quando a remocao falha, a causa deve ser devolvida e nao ignorada");
        assertTrue(Files.exists(naoVazia.resolve("conteudo.txt")));
    }

    @Test
    void falhaAoGerarOXmlNaoDestroiOUltimoArquivoSalvo() throws IOException {
        GerenciadorProducao boa = PlantaPadrao.criar(Cenario.IDEAL);
        Path arquivo = pasta.resolve("planta.xml");
        persistencia.salvar(boa, arquivo);
        byte[] antes = Files.readAllBytes(arquivo);

        GerenciadorProducao comTipoSemSuporte = PlantaPadrao.criar(Cenario.IDEAL);
        comTipoSemSuporte.registrarMaquina(new Maquina("Prototipo", 100, 0.1f, 10.0f, Cenario.IDEAL, 1) {
            @Override
            public boolean processar(Produto produto) {
                return true;
            }

            @Override
            public String getTipo() {
                return "Prototipo sem registro de persistencia";
            }
        });

        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> persistencia.salvar(comTipoSemSuporte, arquivo));

        assertTrue(erro.getMessage().contains("salvar"));
        assertTrue(Arrays.equals(antes, Files.readAllBytes(arquivo)), "O arquivo salvo antes deve continuar intacto");
        try (Stream<Path> arquivos = Files.list(pasta)) {
            assertEquals(1, arquivos.count(), "Nenhum arquivo temporario pode sobrar");
        }
    }

    /* Falha preserva o estado */

    @Test
    void carregamentoComFalhaNaoAlteraAPlantaQueEstavaEmUso() {
        GerenciadorProducao emUso = PlantaPadrao.criar(Cenario.IDEAL);
        emUso.comprarMateriaPrima(500);
        float orcamentoAntes = emUso.getBudget();
        int estoqueAntes = emUso.getMateriaPrima().getQuantidade();

        GerenciadorProducao depois = emUso;
        try {
            depois = persistencia.carregar(escrever(trocar(xmlDaPlantaPadrao(), "<cenario>IDEAL</cenario>", "<cenario>CAOTICO</cenario>")));
        } catch (DadosInvalidosException esperado) {
            // a aplicacao segue com a planta que ja tinha
        }

        assertSame(emUso, depois, "Sem carregamento valido, a planta anterior continua em uso");
        assertEquals(orcamentoAntes, emUso.getBudget(), 0.0f);
        assertEquals(estoqueAntes, emUso.getMateriaPrima().getQuantidade());
    }

    @Test
    void carregamentoComFalhaNaoAlteraOArquivo() throws IOException {
        Path arquivo = escrever(trocar(xmlDaPlantaPadrao(), "<orcamento>25000.0</orcamento>", "<orcamento>muito</orcamento>"));
        byte[] antes = Files.readAllBytes(arquivo);

        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar(arquivo));

        assertTrue(Arrays.equals(antes, Files.readAllBytes(arquivo)), "Um arquivo invalido nao pode ser apagado nem corrigido pela leitura");
    }

    @Test
    void depoisDeUmaFalhaUmArquivoValidoAindaPodeSerCarregado() {
        GerenciadorProducao boa = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        persistencia.salvar(boa, pasta.resolve("valido.xml"));

        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar(escrever("")));
        GerenciadorProducao recuperada = persistencia.carregar(pasta.resolve("valido.xml"));

        assertEquals(Cenario.APOCALIPTICO, recuperada.obterEstado().cenario());
    }

    /* Arquivo padrao */

    @Test
    void semArquivoPadraoNaoHaEstadoSalvoECarregarLancaArquivoInexistente() {
        PersistenciaPlanta semArquivo = new PersistenciaPlanta(pasta.resolve("nao-existe.xml"));

        assertFalse(semArquivo.existeEstadoSalvo());
        assertThrows(ArquivoInexistenteException.class, semArquivo::carregar);
    }

    @Test
    void arquivoPadraoInvalidoContaComoSalvoMasNaoEhCarregado() {
        escrever("isto nao e um XML");
        PersistenciaPlanta comPadrao = new PersistenciaPlanta(entrada);

        assertTrue(comPadrao.existeEstadoSalvo(), "O arquivo existe, mesmo invalido");
        assertThrows(DadosInvalidosException.class, comPadrao::carregar);
    }

    @Test
    void salvarNoArquivoPadraoNuloOuSemPlantaEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> new PersistenciaPlanta(null));
        assertThrows(IllegalArgumentException.class, () -> persistencia.salvar(null));
    }

    /* Parametros invalidos */

    @Test
    void parametrosNulosSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> persistencia.salvar(null, entrada));
        assertThrows(IllegalArgumentException.class, () -> persistencia.salvar(PlantaPadrao.criar(Cenario.IDEAL), null));
        assertThrows(IllegalArgumentException.class, () -> persistencia.carregar(null));
    }
}
