package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o conteúdo do arquivo de persistência não pode ser aproveitado.
 * <p>
 * Cobre o arquivo vazio, o XML mal formado, a estrutura diferente da esperada (elementos
 * ou atributos ausentes, valores que não são números) e os dados que contrariam as regras
 * do domínio, como uma quantidade negativa ou uma máquina com saúde maior que cem.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class DadosInvalidosException extends PersistenciaException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção para dados persistidos inválidos.
     *
     * @param mensagem descrição do campo ou da regra que foi violada
     */
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria uma exceção para dados persistidos inválidos, preservando a causa.
     *
     * @param mensagem descrição do campo ou da regra que foi violada
     * @param causa exceção original, como uma falha do analisador de XML
     */
    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
