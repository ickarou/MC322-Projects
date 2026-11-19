package br.unicamp.ic.mc322.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.service.EstadoPlanta;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

/**
 * Converte um documento XML em um {@link EstadoPlanta}, validando o que le.
 * <p>
 * A validacao acontece em duas camadas. Aqui se confere a estrutura (elementos e atributos
 * presentes, valores numericos, nomes de tipos e de status conhecidos, configuracao das
 * maquinas igual a esperada). As regras do dominio sao conferidas pelos proprios construtores
 * e metodos das classes recriadas, que lancam {@link IllegalArgumentException} ou excecoes
 * da planta; quem chama esta classe trata essas excecoes. Nada aqui altera uma planta
 * existente: o resultado e sempre um estado novo.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
final class LeitorXmlPlanta {

    private static final float TOLERANCIA = 0.0001f;

    /** Classe utilitaria, nao e instanciada. */
    private LeitorXmlPlanta() {
    }

    /**
     * Interpreta o conteudo bruto de um arquivo como um documento XML.
     *
     * @param conteudo bytes do arquivo
     * @return documento analisado
     * @throws DadosInvalidosException se o conteudo nao for um XML bem formado
     * @throws PersistenciaException se o analisador de XML nao puder ser configurado
     */
    static Document analisar(byte[] conteudo) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);
            DocumentBuilder construtor = fabrica.newDocumentBuilder();
            construtor.setErrorHandler(new DefaultHandler());
            return construtor.parse(new ByteArrayInputStream(conteudo));
        } catch (SAXParseException e) {
            throw new DadosInvalidosException("O arquivo nao e um XML valido (linha " + e.getLineNumber()
                    + ", coluna " + e.getColumnNumber() + "): " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new DadosInvalidosException("O arquivo nao e um XML valido: " + e.getMessage(), e);
        } catch (ParserConfigurationException | IOException e) {
            throw new PersistenciaException("Nao foi possivel ler o XML da planta: " + e.getMessage(), e);
        }
    }

    /**
     * Reconstroi o estado da planta a partir do documento.
     *
     * @param doc documento analisado
     * @return estado novo, ainda nao entregue a nenhuma planta
     * @throws DadosInvalidosException se a estrutura ou algum valor do documento for invalido
     * @throws IllegalArgumentException se algum dado contrariar as regras de uma classe de dominio
     */
    static EstadoPlanta ler(Document doc) {
        Element raiz = doc.getDocumentElement();
        if (!TagsXml.PLANTA.equals(raiz.getTagName())) {
            throw new DadosInvalidosException("O arquivo nao descreve uma planta (elemento raiz: <" + raiz.getTagName() + ">).");
        }
        String versao = raiz.getAttribute(TagsXml.VERSAO);
        if (!TagsXml.VERSAO_ATUAL.equals(versao)) {
            throw new DadosInvalidosException("Versao do arquivo nao suportada: '" + versao + "'.");
        }

        Cenario cenario = lerCenario(raiz);
        float orcamento = decimal(texto(raiz, TagsXml.ORCAMENTO), TagsXml.ORCAMENTO);
        EstrategiaProducao estrategia = lerEstrategia(raiz);
        int contadorLotes = inteiro(texto(raiz, TagsXml.CONTADOR_LOTES), TagsXml.CONTADOR_LOTES);
        int totalRejeitados = inteiro(texto(raiz, TagsXml.TOTAL_REJEITADOS), TagsXml.TOTAL_REJEITADOS);
        MateriaPrima materiaPrima = lerMateriaPrima(filho(raiz, TagsXml.MATERIA_PRIMA));

        Element linha = filho(raiz, TagsXml.LINHA);
        String nomeLinha = atributo(linha, TagsXml.NOME);
        List<Maquina> maquinas = new ArrayList<>();
        Set<String> nomesDasMaquinas = new HashSet<>();
        for (Element elemento : filhos(linha, TagsXml.MAQUINA)) {
            Maquina maquina = lerMaquina(elemento, cenario);
            if (!nomesDasMaquinas.add(maquina.getNome())) {
                throw new DadosInvalidosException("A maquina " + maquina.getNome() + " aparece mais de uma vez na linha.");
            }
            maquinas.add(maquina);
        }

        List<Produto> catalogo = new ArrayList<>();
        Map<String, Produto> moldesPorTipo = new HashMap<>();
        for (Element elemento : filhos(filho(raiz, TagsXml.CATALOGO), TagsXml.MOLDE)) {
            String tipo = atributo(elemento, TagsXml.TIPO);
            Produto molde = RegistroDeTipos.criarMolde(tipo, atributo(elemento, TagsXml.ID));
            if (molde == null) {
                throw new DadosInvalidosException("Tipo de produto desconhecido no catalogo: '" + tipo + "'.");
            }
            catalogo.add(molde);
            moldesPorTipo.put(tipo, molde);
        }

        List<Demanda> demandas = new ArrayList<>();
        for (Element elemento : filhos(filho(raiz, TagsXml.DEMANDAS), TagsXml.DEMANDA)) {
            demandas.add(lerDemanda(elemento));
        }

        List<Produto> armazem = new ArrayList<>();
        for (Element elemento : filhos(filho(raiz, TagsXml.ARMAZEM), TagsXml.PRODUTO)) {
            armazem.add(lerPeca(elemento, moldesPorTipo));
        }

        return new EstadoPlanta(cenario, orcamento, estrategia, contadorLotes, totalRejeitados,
                materiaPrima, nomeLinha, maquinas, catalogo, demandas, armazem);
    }

    private static Cenario lerCenario(Element raiz) {
        String nome = texto(raiz, TagsXml.CENARIO);
        try {
            return Cenario.valueOf(nome);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Cenario desconhecido: '" + nome + "'.", e);
        }
    }

    private static EstrategiaProducao lerEstrategia(Element raiz) {
        Element elemento = filhoOpcional(raiz, TagsXml.ESTRATEGIA);
        if (elemento == null) {
            return null;
        }
        String nome = elemento.getTextContent();
        EstrategiaProducao estrategia = RegistroDeTipos.criarEstrategia(nome);
        if (estrategia == null) {
            throw new DadosInvalidosException("Estrategia de producao desconhecida: '" + nome + "'.");
        }
        return estrategia;
    }

    private static MateriaPrima lerMateriaPrima(Element elemento) {
        return new MateriaPrima(
                atributo(elemento, TagsXml.ID),
                texto(elemento, TagsXml.NOME),
                inteiro(texto(elemento, TagsXml.QUANTIDADE), TagsXml.QUANTIDADE),
                texto(elemento, TagsXml.UNIDADE),
                decimal(texto(elemento, TagsXml.CUSTO_POR_UNIDADE), TagsXml.CUSTO_POR_UNIDADE));
    }

    /**
     * Recria uma maquina: o tipo escolhe a classe, o construtor monta as partes (composicao)
     * e depois a saude gravada e devolvida a cada uma. A configuracao gravada precisa ser a
     * mesma que o tipo de maquina tem no codigo, no cenario do arquivo.
     */
    private static Maquina lerMaquina(Element elemento, Cenario cenario) {
        String tipo = atributo(elemento, TagsXml.TIPO);
        String nome = atributo(elemento, TagsXml.NOME);
        Maquina maquina = RegistroDeTipos.criarMaquina(tipo, nome, cenario);
        if (maquina == null) {
            throw new DadosInvalidosException("Tipo de maquina desconhecido: '" + tipo + "'.");
        }

        conferir(nome, TagsXml.CAPACIDADE_MAXIMA, maquina.getCapacidadeMaxima(),
                inteiro(texto(elemento, TagsXml.CAPACIDADE_MAXIMA), TagsXml.CAPACIDADE_MAXIMA));
        conferir(nome, TagsXml.CUSTO_OPERACAO, maquina.getCustoOperacao(),
                decimal(texto(elemento, TagsXml.CUSTO_OPERACAO), TagsXml.CUSTO_OPERACAO));
        conferir(nome, TagsXml.PROBABILIDADE_FALHA, maquina.getProbabilidadeFalha(),
                decimal(texto(elemento, TagsXml.PROBABILIDADE_FALHA), TagsXml.PROBABILIDADE_FALHA));

        Map<String, Float> saudes = new LinkedHashMap<>();
        for (Element componente : filhos(filho(elemento, TagsXml.COMPONENTES), TagsXml.COMPONENTE)) {
            String nomeDoComponente = atributo(componente, TagsXml.NOME);
            float saude = decimal(atributo(componente, TagsXml.SAUDE), TagsXml.SAUDE);
            if (saudes.put(nomeDoComponente, saude) != null) {
                throw new DadosInvalidosException("O componente '" + nomeDoComponente + "' aparece mais de uma vez na maquina " + nome + ".");
            }
        }
        maquina.restaurarSaudeDasPartes(saudes);
        return maquina;
    }

    private static Demanda lerDemanda(Element elemento) {
        String nomeDoStatus = atributo(elemento, TagsXml.STATUS);
        StatusDemanda status;
        try {
            status = StatusDemanda.valueOf(nomeDoStatus);
        } catch (IllegalArgumentException e) {
            throw new DadosInvalidosException("Status de demanda desconhecido: '" + nomeDoStatus + "'.", e);
        }
        return Demanda.restaurar(
                atributo(elemento, TagsXml.TIPO),
                inteiro(texto(elemento, TagsXml.QUANTIDADE), TagsXml.QUANTIDADE),
                status,
                decimalDuplo(texto(elemento, TagsXml.CUSTO_UNITARIO), TagsXml.CUSTO_UNITARIO));
    }

    /**
     * Recria uma peca do armazem a partir do modelo do mesmo tipo, que garante que ela
     * nasce com o consumo e a qualidade corretos, e depois restaura lote, status e risco.
     */
    private static Produto lerPeca(Element elemento, Map<String, Produto> moldesPorTipo) {
        String tipo = atributo(elemento, TagsXml.TIPO);
        Produto molde = moldesPorTipo.get(tipo);
        if (molde == null) {
            throw new DadosInvalidosException("A peca " + atributo(elemento, TagsXml.ID) + " do armazem e do tipo '"
                    + tipo + "', que nao esta no catalogo.");
        }
        Produto peca = molde.criarNovaUnidade(atributo(elemento, TagsXml.ID));
        peca.setLote(atributo(elemento, TagsXml.LOTE));
        peca.setStatus(texto(elemento, TagsXml.STATUS));
        peca.aumentarProbabilidadeFalha(decimal(texto(elemento, TagsXml.PROBABILIDADE_FALHA), TagsXml.PROBABILIDADE_FALHA));
        return peca;
    }

    /** Garante que um dado de configuracao gravado coincide com o que o codigo define para a maquina. */
    private static void conferir(String maquina, String campo, float esperado, float lido) {
        if (!(Math.abs(esperado - lido) <= TOLERANCIA)) {
            throw new DadosInvalidosException("A configuracao da maquina " + maquina + " e incompativel: " + campo
                    + " no arquivo e " + lido + ", mas deveria ser " + esperado + ".");
        }
    }

    /* Acesso aos nos do documento */

    private static List<Element> filhos(Element pai, String nome) {
        List<Element> encontrados = new ArrayList<>();
        for (Node no = pai.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no.getNodeType() == Node.ELEMENT_NODE && nome.equals(((Element) no).getTagName())) {
                encontrados.add((Element) no);
            }
        }
        return encontrados;
    }

    private static Element filhoOpcional(Element pai, String nome) {
        List<Element> encontrados = filhos(pai, nome);
        if (encontrados.size() > 1) {
            throw new DadosInvalidosException("O elemento <" + nome + "> aparece mais de uma vez em <" + pai.getTagName() + ">.");
        }
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private static Element filho(Element pai, String nome) {
        Element elemento = filhoOpcional(pai, nome);
        if (elemento == null) {
            throw new DadosInvalidosException("O elemento <" + nome + "> nao foi encontrado em <" + pai.getTagName() + ">.");
        }
        return elemento;
    }

    private static String texto(Element pai, String nome) {
        return filho(pai, nome).getTextContent();
    }

    private static String atributo(Element elemento, String nome) {
        if (!elemento.hasAttribute(nome)) {
            throw new DadosInvalidosException("O atributo '" + nome + "' nao foi encontrado em <" + elemento.getTagName() + ">.");
        }
        return elemento.getAttribute(nome);
    }

    /* Conversao de valores */

    private static int inteiro(String valor, String campo) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("O valor '" + valor + "' do campo '" + campo + "' nao e um inteiro valido.", e);
        }
    }

    private static float decimal(String valor, String campo) {
        try {
            return Float.parseFloat(valor.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("O valor '" + valor + "' do campo '" + campo + "' nao e um numero valido.", e);
        }
    }

    private static double decimalDuplo(String valor, String campo) {
        try {
            return Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("O valor '" + valor + "' do campo '" + campo + "' nao e um numero valido.", e);
        }
    }
}
