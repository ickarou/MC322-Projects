package br.unicamp.ic.mc322.persistence;

/**
 * Nomes dos elementos e atributos do arquivo XML da planta.
 * <p>
 * Ficam reunidos em um unico lugar para que o escritor e o leitor usem exatamente os
 * mesmos nomes, sem repeticao de textos entre as duas classes.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
final class TagsXml {

    /** Versao do formato gravada no atributo {@link #VERSAO} da raiz. */
    static final String VERSAO_ATUAL = "1";

    static final String PLANTA = "planta";
    static final String VERSAO = "versao";
    static final String CENARIO = "cenario";
    static final String ORCAMENTO = "orcamento";
    static final String ESTRATEGIA = "estrategia";
    static final String CONTADOR_LOTES = "contadorLotes";
    static final String TOTAL_REJEITADOS = "totalRejeitados";

    static final String MATERIA_PRIMA = "materiaPrima";
    static final String ID = "id";
    static final String NOME = "nome";
    static final String QUANTIDADE = "quantidade";
    static final String UNIDADE = "unidade";
    static final String CUSTO_POR_UNIDADE = "custoPorUnidade";

    static final String LINHA = "linhaDeProducao";
    static final String MAQUINA = "maquina";
    static final String TIPO = "tipo";
    static final String CAPACIDADE_MAXIMA = "capacidadeMaxima";
    static final String CUSTO_OPERACAO = "custoOperacao";
    static final String PROBABILIDADE_FALHA = "probabilidadeFalha";
    static final String COMPONENTES = "componentes";
    static final String COMPONENTE = "componente";
    static final String SAUDE = "saude";

    static final String CATALOGO = "catalogo";
    static final String MOLDE = "molde";

    static final String DEMANDAS = "demandas";
    static final String DEMANDA = "demanda";
    static final String STATUS = "status";
    static final String CUSTO_UNITARIO = "custoUnitario";

    static final String ARMAZEM = "armazem";
    static final String PRODUTO = "produto";
    static final String LOTE = "lote";

    /** Classe de constantes, nao e instanciada. */
    private TagsXml() {
    }
}
