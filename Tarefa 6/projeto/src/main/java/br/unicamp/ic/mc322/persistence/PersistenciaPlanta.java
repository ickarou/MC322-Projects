package br.unicamp.ic.mc322.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.LinhaDeProducao;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaLinhaDeMontagem;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteCritico;
import br.unicamp.ic.mc322.strategy.EstrategiaMetaDeOrcamento;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

/**
 * Responsavel por salvar e recuperar o estado completo da planta industrial em arquivos XML.
 * <p>
 * Isola toda a logica de manipulacao de arquivos e construcao/leitura da arvore DOM,
 * garantindo que falhas de leitura, escrita ou violacoes das regras de negocio
 * sejam convertidas em {@link PersistenciaException} sem corromper o estado em memoria.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class PersistenciaPlanta {

    /** Caminho padrao do arquivo XML utilizado pela aplicacao principal. */
    public static final Path ARQUIVO_PADRAO = Path.of("dados", "planta_kts590.xml");

    private final Path caminhoArquivo;

    /**
     * Cria um gerenciador de persistencia apontando para o arquivo padrao da planta.
     */
    public PersistenciaPlanta() {
        this(ARQUIVO_PADRAO);
    }

    /**
     * Cria um gerenciador de persistencia apontando para um caminho especifico.
     *
     * @param caminhoArquivo caminho do arquivo XML a ser lido ou gravado
     * @throws IllegalArgumentException se o caminho for nulo
     */
    public PersistenciaPlanta(Path caminhoArquivo) {
        if (caminhoArquivo == null) {
            throw new IllegalArgumentException("O caminho do arquivo de persistencia deve ser informado.");
        }
        this.caminhoArquivo = caminhoArquivo;
    }

    /**
     * Consulta o caminho configurado para persistencia.
     *
     * @return caminho do arquivo XML
     */
    public Path getCaminhoArquivo() {
        return caminhoArquivo;
    }

    /**
     * Salva o estado atual do gerenciador da planta no arquivo configurado.
     *
     * @param gerenciador instancia contendo o estado atual da planta
     * @throws PersistenciaException se o gerenciador for nulo ou ocorrer falha de escrita
     */
    public void salvar(GerenciadorProducao gerenciador) {
        salvar(gerenciador, this.caminhoArquivo);
    }

    /**
     * Salva o estado atual do gerenciador da planta no caminho informado.
     *
     * @param gerenciador instancia contendo o estado atual da planta
     * @param destino caminho do arquivo XML de destino
     * @throws PersistenciaException se os parametros forem invalidos ou ocorrer erro de E/S
     */
    public void salvar(GerenciadorProducao gerenciador, Path destino) {
        if (gerenciador == null) {
            throw new PersistenciaException("O gerenciador de producao nao pode ser nulo para salvamento.");
        }
        if (destino == null) {
            throw new PersistenciaException("O caminho de destino do arquivo XML deve ser informado.");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // 1. Elemento raiz <planta> com atributos gerais
            Element raiz = doc.createElement("planta");
            raiz.setAttribute("cenario", gerenciador.getCenarioAtivo().name());
            raiz.setAttribute("budget", Float.toString(gerenciador.getBudget()));
            raiz.setAttribute("contadorLotes", Integer.toString(gerenciador.getContadorLotes()));
            raiz.setAttribute("totalRejeitados", Integer.toString(gerenciador.getTotalRejeitados()));
            EstrategiaProducao estrategia = gerenciador.getEstrategiaAtual();
            raiz.setAttribute("estrategia", (estrategia != null) ? estrategia.getNomeEstrategia() : "NENHUMA");
            doc.appendChild(raiz);

            // 2. Estoque de Materia-Prima (<materiasPrimas> -> <materiaPrima>)
            Element tagMaterias = doc.createElement("materiasPrimas");
            MateriaPrima mp = gerenciador.getMateriaPrima();
            Element tagMp = doc.createElement("materiaPrima");
            tagMp.setAttribute("id", mp.getId());
            adicionarTagTexto(doc, tagMp, "nome", mp.getNome());
            adicionarTagTexto(doc, tagMp, "quantidade", Integer.toString(mp.getQuantidade()));
            adicionarTagTexto(doc, tagMp, "unidade", mp.getUnidade());
            adicionarTagTexto(doc, tagMp, "custoPorUnidade", Float.toString(mp.getCustoPorUnidade()));
            tagMaterias.appendChild(tagMp);
            raiz.appendChild(tagMaterias);

            // 3. Linha de Producao, Maquinas e Subcomponentes (<linhaDeProducao>)
            LinhaDeProducao linha = gerenciador.getLinha();
            Element tagLinha = doc.createElement("linhaDeProducao");
            tagLinha.setAttribute("nome", linha.getNome());

            for (Maquina m : linha.getMaquinas()) {
                Element tagMaquina = doc.createElement("maquina");
                tagMaquina.setAttribute("classe", m.getClass().getSimpleName());
                tagMaquina.setAttribute("nome", m.getNome());
                tagMaquina.setAttribute("ligada", Boolean.toString(m.estaLigada()));

                Element tagMotor = doc.createElement("motor");
                tagMotor.setAttribute("saude", Integer.toString(m.getSaudeMotor()));
                tagMaquina.appendChild(tagMotor);

                Element tagSensor = doc.createElement("sensor");
                tagSensor.setAttribute("saude", Integer.toString(m.getSaudeSensor()));
                tagMaquina.appendChild(tagSensor);

                Element tagAtuadores = doc.createElement("atuadores");
                List<Integer> saudesAtuadores = m.getSaudeAtuadores();
                for (int i = 0; i < saudesAtuadores.size(); i++) {
                    Element tagAtuador = doc.createElement("atuador");
                    tagAtuador.setAttribute("numero", Integer.toString(i + 1));
                    tagAtuador.setAttribute("saude", Integer.toString(saudesAtuadores.get(i)));
                    tagAtuadores.appendChild(tagAtuador);
                }
                tagMaquina.appendChild(tagAtuadores);
                tagLinha.appendChild(tagMaquina);
            }
            raiz.appendChild(tagLinha);

            // 4. Demandas (<demandas> -> <demanda>)
            Element tagDemandas = doc.createElement("demandas");
            for (Demanda d : gerenciador.getDemandas()) {
                Element tagDemanda = doc.createElement("demanda");
                tagDemanda.setAttribute("tipoProduto", d.getTipoProduto());
                tagDemanda.setAttribute("quantidade", Integer.toString(d.getQuantidadeProdutos()));
                tagDemanda.setAttribute("status", d.getStatus().name());
                tagDemandas.appendChild(tagDemanda);
            }
            raiz.appendChild(tagDemandas);

            // 5. Armazem de Produtos Acabados (<armazem> -> <produto>)
            Element tagArmazem = doc.createElement("armazem");
            for (Produto p : gerenciador.getProdutosFabricados()) {
                Element tagProd = doc.createElement("produto");
                tagProd.setAttribute("id", p.getId());
                tagProd.setAttribute("tipo", p.getTipo());
                tagProd.setAttribute("lote", (p.getLote() != null) ? p.getLote() : "LOTE-000");
                tagProd.setAttribute("status", p.getStatus());
                tagProd.setAttribute("probabilidadeFalha", Float.toString(p.getProbabilidadeFalhaAcumulada()));
                tagArmazem.appendChild(tagProd);
            }
            raiz.appendChild(tagArmazem);

            // Gravacao fisica do arquivo
            Path diretorioPai = destino.getParent();
            if (diretorioPai != null && !Files.exists(diretorioPai)) {
                Files.createDirectories(diretorioPai);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            try (OutputStream out = Files.newOutputStream(destino)) {
                transformer.transform(new DOMSource(doc), new StreamResult(out));
            }
        } catch (ParserConfigurationException | TransformerException | IOException | RuntimeException e) {
            throw new PersistenciaException("Falha ao salvar os dados da planta no arquivo XML: " + e.getMessage(), e);
        }
    }

    /**
     * Carrega e reconstroi o estado completo da planta a partir do arquivo padrao configurado.
     *
     * @return nova instancia de {@link GerenciadorProducao} com todo o estado restaurado e validado
     * @throws PersistenciaException se o arquivo nao existir, estiver vazio, corrompido ou contiver dados invalidos
     */
    public GerenciadorProducao carregar() {
        return carregar(this.caminhoArquivo);
    }

    /**
     * Carrega e reconstroi o estado completo da planta a partir do caminho informado.
     *
     * @param origem caminho do arquivo XML a ser lido
     * @return nova instancia de {@link GerenciadorProducao} com todo o estado restaurado e validado
     * @throws PersistenciaException se o arquivo nao existir, estiver vazio, corrompido ou contiver dados invalidos
     */
    public GerenciadorProducao carregar(Path origem) {
        if (origem == null) {
            throw new PersistenciaException("O caminho do arquivo para carregamento deve ser informado.");
        }
        if (!Files.exists(origem)) {
            throw new PersistenciaException("Arquivo de persistencia nao encontrado: " + origem);
        }
        if (Files.isDirectory(origem)) {
            throw new PersistenciaException("O caminho informado e um diretorio, nao um arquivo XML: " + origem);
        }

        try {
            if (Files.size(origem) == 0) {
                throw new PersistenciaException("O arquivo de persistencia esta vazio: " + origem);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao verificar tamanho do arquivo XML: " + e.getMessage(), e);
        }

        try (InputStream in = Files.newInputStream(origem)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // Silencia logs padrao do SAXParser no console para tratar via excecao
            builder.setErrorHandler(new DefaultHandler());
            Document doc = builder.parse(in);
            doc.getDocumentElement().normalize();

            Element raiz = doc.getDocumentElement();
            if (raiz == null || !"planta".equals(raiz.getNodeName())) {
                throw new PersistenciaException("Estrutura XML invalida: elemento raiz <planta> nao encontrado.");
            }

            // 1. Leitura dos atributos gerais da planta
            Cenario cenario = lerCenario(lerAtributoObrigatorio(raiz, "cenario"));
            float budget = lerFloat(lerAtributoObrigatorio(raiz, "budget"), "budget");
            int contadorLotes = lerInteiroOpcional(raiz, "contadorLotes", 0);
            int totalRejeitados = lerInteiroOpcional(raiz, "totalRejeitados", 0);
            String nomeEstrategia = raiz.getAttribute("estrategia");

            // 2. Leitura da Materia-Prima
            MateriaPrima materiaPrima = carregarMateriaPrima(raiz);

            // Instancia o novo gerenciador isolado (so substitui na aplicacao se tudo validar ate o fim)
            GerenciadorProducao gerenciador = new GerenciadorProducao(materiaPrima, cenario);
            gerenciador.registrarProduto(new CarcacaSuperior("MOLDE-TOP"));
            gerenciador.registrarProduto(new CarcacaInferior("MOLDE-BOT"));
            gerenciador.registrarProduto(new ProtecaoLateral("MOLDE-LAT"));

            if (nomeEstrategia != null && !nomeEstrategia.isBlank() && !"NENHUMA".equals(nomeEstrategia)) {
                gerenciador.setEstrategia(criarEstrategiaPorNome(nomeEstrategia));
            }

            // 3. Leitura das Maquinas e Subcomponentes
            carregarMaquinas(raiz, cenario, gerenciador);

            // 4. Leitura das Demandas
            carregarDemandas(raiz, gerenciador);

            // 5. Leitura do Armazem e restauracao dos indicadores financeiros
            List<Produto> produtosArmazem = carregarArmazem(raiz);
            gerenciador.restaurarEstadoPlanta(budget, contadorLotes, totalRejeitados, produtosArmazem);

            return gerenciador;

        } catch (PersistenciaException e) {
            throw e;
        } catch (SAXException | ParserConfigurationException e) {
            throw new PersistenciaException("Arquivo XML malformado ou corrompido: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PersistenciaException("Falha de leitura do arquivo XML: " + e.getMessage(), e);
        } catch (PlantaException | IllegalArgumentException e) {
            throw new PersistenciaException("Dados do XML incompativeis com as regras de negocio: " + e.getMessage(), e);
        }
    }

    /* Metodos auxiliares privados de construcao e validacao */

    private void adicionarTagTexto(Document doc, Element pai, String nomeTag, String valor) {
        Element tag = doc.createElement(nomeTag);
        tag.setTextContent(valor);
        pai.appendChild(tag);
    }

    private MateriaPrima carregarMateriaPrima(Element raiz) {
        NodeList listaMaterias = raiz.getElementsByTagName("materiaPrima");
        if (listaMaterias.getLength() == 0) {
            throw new PersistenciaException("Elemento obrigatorio <materiaPrima> ausente no XML.");
        }
        Element tagMp = (Element) listaMaterias.item(0);
        String id = lerAtributoObrigatorio(tagMp, "id");
        String nome = lerTextoTagObrigatoria(tagMp, "nome");
        int quantidade = lerInteiro(lerTextoTagObrigatoria(tagMp, "quantidade"), "quantidade de materia-prima");
        String unidade = lerTextoTagObrigatoria(tagMp, "unidade");
        float custo = lerFloat(lerTextoTagObrigatoria(tagMp, "custoPorUnidade"), "custoPorUnidade");

        return new MateriaPrima(id, nome, quantidade, unidade, custo);
    }

    private void carregarMaquinas(Element raiz, Cenario cenario, GerenciadorProducao gerenciador) {
        NodeList linhas = raiz.getElementsByTagName("linhaDeProducao");
        if (linhas.getLength() == 0) {
            throw new PersistenciaException("Elemento obrigatorio <linhaDeProducao> ausente no XML.");
        }
        Element tagLinha = (Element) linhas.item(0);
        NodeList nosMaquinas = tagLinha.getElementsByTagName("maquina");
        if (nosMaquinas.getLength() == 0) {
            throw new PersistenciaException("A linha de producao no XML deve conter pelo menos uma maquina.");
        }

        for (int i = 0; i < nosMaquinas.getLength(); i++) {
            Element tagMaq = (Element) nosMaquinas.item(i);
            String classe = lerAtributoObrigatorio(tagMaq, "classe");
            String nome = lerAtributoObrigatorio(tagMaq, "nome");
            boolean ligada = lerBooleanoEstrito(lerAtributoObrigatorio(tagMaq, "ligada"), "ligada");

            Maquina maquina = instanciarMaquina(classe, nome, cenario);

            NodeList motores = tagMaq.getElementsByTagName("motor");
            NodeList sensores = tagMaq.getElementsByTagName("sensor");
            if (motores.getLength() == 0 || sensores.getLength() == 0) {
                throw new PersistenciaException("Subcomponentes <motor> ou <sensor> ausentes na maquina " + nome + ".");
            }

            int saudeMotor = lerInteiro(lerAtributoObrigatorio((Element) motores.item(0), "saude"), "saude do motor");
            int saudeSensor = lerInteiro(lerAtributoObrigatorio((Element) sensores.item(0), "saude"), "saude do sensor");

            NodeList nosAtuadores = tagMaq.getElementsByTagName("atuador");
            List<Integer> saudesAtuadores = new ArrayList<>();
            for (int j = 0; j < nosAtuadores.getLength(); j++) {
                Element tagAtuador = (Element) nosAtuadores.item(j);
                int saudeAtuador = lerInteiro(lerAtributoObrigatorio(tagAtuador, "saude"), "saude do atuador");
                saudesAtuadores.add(saudeAtuador);
            }

            maquina.restaurarEstado(ligada, saudeMotor, saudeSensor, saudesAtuadores);
            gerenciador.registrarMaquina(maquina);
        }
    }

    private void carregarDemandas(Element raiz, GerenciadorProducao gerenciador) {
        NodeList listaContainer = raiz.getElementsByTagName("demandas");
        if (listaContainer.getLength() == 0) {
            throw new PersistenciaException("Elemento obrigatorio <demandas> ausente no XML.");
        }
        Element container = (Element) listaContainer.item(0);
        NodeList nosDemandas = container.getElementsByTagName("demanda");

        for (int i = 0; i < nosDemandas.getLength(); i++) {
            Element tagDem = (Element) nosDemandas.item(i);
            String tipoProduto = lerAtributoObrigatorio(tagDem, "tipoProduto");
            int quantidade = lerInteiro(lerAtributoObrigatorio(tagDem, "quantidade"), "quantidade da demanda");
            StatusDemanda status = lerStatusDemanda(lerAtributoObrigatorio(tagDem, "status"));

            Produto molde = criarMoldePorTipo(tipoProduto);
            Demanda demanda = new Demanda(tipoProduto, quantidade);
            demanda.restaurarStatus(status);
            gerenciador.registrarDemanda(demanda, molde);
        }
    }

    private List<Produto> carregarArmazem(Element raiz) {
        List<Produto> produtos = new ArrayList<>();
        NodeList listaArmazem = raiz.getElementsByTagName("armazem");
        if (listaArmazem.getLength() == 0) {
            return produtos;
        }
        Element tagArmazem = (Element) listaArmazem.item(0);
        NodeList nosProdutos = tagArmazem.getElementsByTagName("produto");

        for (int i = 0; i < nosProdutos.getLength(); i++) {
            Element tagProd = (Element) nosProdutos.item(i);
            String id = lerAtributoObrigatorio(tagProd, "id");
            String tipo = lerAtributoObrigatorio(tagProd, "tipo");
            String lote = lerAtributoObrigatorio(tagProd, "lote");
            String status = lerAtributoObrigatorio(tagProd, "status");
            float probFalha = lerFloat(lerAtributoObrigatorio(tagProd, "probabilidadeFalha"), "probabilidadeFalha");

            Produto molde = criarMoldePorTipo(tipo);
            Produto peca = molde.criarNovaUnidade(id);
            peca.restaurarEstado(lote, status, probFalha);
            produtos.add(peca);
        }
        return produtos;
    }

    private Maquina instanciarMaquina(String classe, String nome, Cenario cenario) {
        switch (classe) {
            case "Injetora":
                return new Injetora(nome, cenario);
            case "BracoEncaixotador":
                return new BracoEncaixotador(nome, cenario);
            case "EstacaoTestes":
                return new EstacaoTestes(nome, cenario);
            default:
                throw new PersistenciaException("Tipo de maquina desconhecido no XML: " + classe);
        }
    }

    private Produto criarMoldePorTipo(String tipo) {
        switch (tipo) {
            case "Carcaca Superior":
                return new CarcacaSuperior("MOLDE-TOP");
            case "Carcaca Inferior":
                return new CarcacaInferior("MOLDE-BOT");
            case "Protecao Lateral":
                return new ProtecaoLateral("MOLDE-LAT");
            default:
                throw new PersistenciaException("Tipo de produto desconhecido no XML: " + tipo);
        }
    }

    private EstrategiaProducao criarEstrategiaPorNome(String nome) {
        switch (nome) {
            case "Linha de Montagem":
                return new EstrategiaLinhaDeMontagem();
            case "Lote Critico":
                return new EstrategiaLoteCritico();
            case "Meta de Orcamento":
                return new EstrategiaMetaDeOrcamento();
            default:
                throw new PersistenciaException("Estrategia de producao desconhecida no XML: " + nome);
        }
    }

    private Cenario lerCenario(String valor) {
        try {
            return Cenario.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Cenario invalido no XML: " + valor, e);
        }
    }

    private StatusDemanda lerStatusDemanda(String valor) {
        try {
            return StatusDemanda.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Status de demanda invalido no XML: " + valor, e);
        }
    }

    private String lerAtributoObrigatorio(Element elemento, String nomeAtributo) {
        if (!elemento.hasAttribute(nomeAtributo)) {
            throw new PersistenciaException("Atributo obrigatorio '" + nomeAtributo
                    + "' ausente na tag <" + elemento.getTagName() + ">.");
        }
        String valor = elemento.getAttribute(nomeAtributo).trim();
        if (valor.isEmpty()) {
            throw new PersistenciaException("Atributo '" + nomeAtributo
                    + "' vazio na tag <" + elemento.getTagName() + ">.");
        }
        return valor;
    }

    private String lerTextoTagObrigatoria(Element pai, String nomeTag) {
        NodeList nos = pai.getElementsByTagName(nomeTag);
        if (nos.getLength() == 0 || nos.item(0).getTextContent() == null) {
            throw new PersistenciaException("Tag obrigatoria <" + nomeTag + "> ausente dentro de <" + pai.getTagName() + ">.");
        }
        String texto = nos.item(0).getTextContent().trim();
        if (texto.isEmpty()) {
            throw new PersistenciaException("Tag <" + nomeTag + "> nao pode estar vazia.");
        }
        return texto;
    }

    private int lerInteiro(String valor, String nomeCampo) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Valor inteiro invalido para " + nomeCampo + ": '" + valor + "'.", e);
        }
    }

    private int lerInteiroOpcional(Element elemento, String nomeAtributo, int valorPadrao) {
        if (!elemento.hasAttribute(nomeAtributo)) {
            return valorPadrao;
        }
        return lerInteiro(elemento.getAttribute(nomeAtributo), nomeAtributo);
    }

    private float lerFloat(String valor, String nomeCampo) {
        try {
            float numero = Float.parseFloat(valor.trim());
            if (!Float.isFinite(numero)) {
                throw new PersistenciaException("Valor numerico nao finito para " + nomeCampo + ".");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new PersistenciaException("Valor decimal invalido para " + nomeCampo + ": '" + valor + "'.", e);
        }
    }

    private boolean lerBooleanoEstrito(String valor, String nomeCampo) {
        if ("true".equalsIgnoreCase(valor)) {
            return true;
        }
        if ("false".equalsIgnoreCase(valor)) {
            return false;
        }
        throw new PersistenciaException("Valor booleano invalido para " + nomeCampo + ": '" + valor + "'.");
    }
}