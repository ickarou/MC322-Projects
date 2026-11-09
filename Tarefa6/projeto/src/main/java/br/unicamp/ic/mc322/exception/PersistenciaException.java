package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o estado da planta não pôde ser gravado ou lido do arquivo de persistência.
 * <p>
 * Representa as falhas de entrada e saída (permissão negada, disco indisponível, caminho que
 * não é um arquivo) e serve de base para os problemas mais específicos de persistência:
 * {@link ArquivoInexistenteException} e {@link DadosInvalidosException}. Quando existe,
 * a causa original é preservada.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class PersistenciaException extends PlantaException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção de persistência.
     *
     * @param mensagem descrição compreensível do que falhou
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria uma exceção de persistência a partir de uma falha de baixo nível.
     *
     * @param mensagem descrição compreensível do que falhou
     * @param causa exceção original, como uma {@code IOException}
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
