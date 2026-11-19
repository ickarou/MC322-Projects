package br.unicamp.ic.mc322.persistence;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.XMLConstants;
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

import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.service.EstadoPlanta;

/**
 * Converte o {@link EstadoPlanta} em um documento XML.
 * <p>
 * So le o estado, nunca o altera, e nao toca em arquivos: o resultado e entregue como
 * bytes para quem for gravar. A conversao inversa fica em {@link LeitorXmlPlanta}.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
final class EscritorXmlPlanta {

    private static final String CABECALHO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /** Classe utilitaria, nao e instanciada. */
    private EscritorXmlPlanta() {
    }

    /**
     * Gera o XML (UTF-8, indentado) que representa o estado informado.
     *
     * @param estado estado da planta
     * @return conteudo completo do arquivo
     * @throws PersistenciaException se o XML nao puder ser montado
     * @throws IllegalArgumentException se houver maquina de um tipo sem suporte
     */
    static byte[] gerar(EstadoPlanta estado) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document doc = fabrica.newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);
            doc.appendChild(montarPlanta(doc, estado));
            return serializar(doc);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new PersistenciaException("Nao foi possivel montar o XML da planta: " + e.getMessage(), e);
        }
    }

    private static Element montarPlanta(Document doc, EstadoPlanta estado) {
        Element planta = doc.createElement(TagsXml.PLANTA);
        planta.setAttribute(TagsXml.VERSAO, TagsXml.VERSAO_ATUAL);

        texto(doc, planta, TagsXml.CENARIO, estado.cenario().name());
        texto(doc, planta, TagsXml.ORCAMENTO, Float.toString(estado.orcamento()));
        if (estado.estrategia() != null) {
            texto(doc, planta, TagsXml.ESTRATEGIA, estado.estrategia().getNomeEstrategia());
        }
        texto(doc, planta, TagsXml.CONTADOR_LOTES, Integer.toString(estado.contadorLotes()));
        texto(doc, planta, TagsXml.TOTAL_REJEITADOS, Integer.toString(estado.totalRejeitados()));

        planta.appendChild(montarMateriaPrima(doc, estado.materiaPrima()));

        Element linha = doc.createElement(TagsXml.LINHA);
        linha.setAttribute(TagsXml.NOME, estado.nomeLinha());
        for (Maquina maquina : estado.maquinas()) {
            linha.appendChild(montarMaquina(doc, maquina));
        }
        planta.appendChild(linha);

        Element catalogo = doc.createElement(TagsXml.CATALOGO);
        for (Produto molde : estado.catalogo()) {
            Element elemento = doc.createElement(TagsXml.MOLDE);
            elemento.setAttribute(TagsXml.TIPO, molde.getTipo());
            elemento.setAttribute(TagsXml.ID, molde.getId());
            catalogo.appendChild(elemento);
        }
        planta.appendChild(catalogo);

        Element demandas = doc.createElement(TagsXml.DEMANDAS);
        for (Demanda demanda : estado.demandas()) {
            demandas.appendChild(montarDemanda(doc, demanda));
        }
        planta.appendChild(demandas);

        Element armazem = doc.createElement(TagsXml.ARMAZEM);
        for (Produto peca : estado.armazem()) {
            armazem.appendChild(montarPeca(doc, peca));
        }
        planta.appendChild(armazem);

        return planta;
    }

    private static Element montarMateriaPrima(Document doc, MateriaPrima materiaPrima) {
        Element elemento = doc.createElement(TagsXml.MATERIA_PRIMA);
        elemento.setAttribute(TagsXml.ID, materiaPrima.getId());
        texto(doc, elemento, TagsXml.NOME, materiaPrima.getNome());
        texto(doc, elemento, TagsXml.QUANTIDADE, Integer.toString(materiaPrima.getQuantidade()));
        texto(doc, elemento, TagsXml.UNIDADE, materiaPrima.getUnidade());
        texto(doc, elemento, TagsXml.CUSTO_POR_UNIDADE, Float.toString(materiaPrima.getCustoPorUnidade()));
        return elemento;
    }

    private static Element montarMaquina(Document doc, Maquina maquina) {
        Element elemento = doc.createElement(TagsXml.MAQUINA);
        elemento.setAttribute(TagsXml.TIPO, RegistroDeTipos.codigoDaMaquina(maquina));
        elemento.setAttribute(TagsXml.NOME, maquina.getNome());
        texto(doc, elemento, TagsXml.CAPACIDADE_MAXIMA, Integer.toString(maquina.getCapacidadeMaxima()));
        texto(doc, elemento, TagsXml.CUSTO_OPERACAO, Float.toString(maquina.getCustoOperacao()));
        texto(doc, elemento, TagsXml.PROBABILIDADE_FALHA, Float.toString(maquina.getProbabilidadeFalha()));

        Element componentes = doc.createElement(TagsXml.COMPONENTES);
        for (Map.Entry<String, Float> parte : maquina.getSaudeDasPartes().entrySet()) {
            Element componente = doc.createElement(TagsXml.COMPONENTE);
            componente.setAttribute(TagsXml.NOME, parte.getKey());
            componente.setAttribute(TagsXml.SAUDE, Float.toString(parte.getValue()));
            componentes.appendChild(componente);
        }
        elemento.appendChild(componentes);
        return elemento;
    }

    private static Element montarDemanda(Document doc, Demanda demanda) {
        Element elemento = doc.createElement(TagsXml.DEMANDA);
        elemento.setAttribute(TagsXml.TIPO, demanda.getTipoProduto());
        elemento.setAttribute(TagsXml.STATUS, demanda.getStatus().name());
        texto(doc, elemento, TagsXml.QUANTIDADE, Integer.toString(demanda.getQuantidadeProdutos()));
        texto(doc, elemento, TagsXml.CUSTO_UNITARIO, Double.toString(demanda.getCustoUnitario()));
        return elemento;
    }

    private static Element montarPeca(Document doc, Produto peca) {
        if (peca.getLote() == null) {
            throw new IllegalArgumentException("A peca " + peca.getId() + " do armazem nao esta associada a um lote.");
        }
        Element elemento = doc.createElement(TagsXml.PRODUTO);
        elemento.setAttribute(TagsXml.ID, peca.getId());
        elemento.setAttribute(TagsXml.TIPO, peca.getTipo());
        elemento.setAttribute(TagsXml.LOTE, peca.getLote());
        texto(doc, elemento, TagsXml.STATUS, peca.getStatus());
        texto(doc, elemento, TagsXml.PROBABILIDADE_FALHA, Float.toString(peca.getProbabilidadeFalhaAcumulada()));
        return elemento;
    }

    /** Acrescenta ao pai um elemento filho que contem apenas texto. */
    private static void texto(Document doc, Element pai, String nome, String conteudo) {
        Element filho = doc.createElement(nome);
        filho.setTextContent(conteudo);
        pai.appendChild(filho);
    }

    private static byte[] serializar(Document doc) throws TransformerException {
        TransformerFactory fabrica = TransformerFactory.newInstance();
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformador = fabrica.newTransformer();
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        saida.writeBytes(CABECALHO.getBytes(StandardCharsets.UTF_8));
        transformador.transform(new DOMSource(doc), new StreamResult(saida));
        return saida.toByteArray();
    }
}
