package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao tentar carregar a planta de um arquivo que não existe.
 * <p>
 * Na primeira execução da aplicação essa situação é normal: ainda não houve nenhum
 * salvamento. Por isso ela tem um tipo próprio, que permite ao programa distinguir a
 * ausência do arquivo de um arquivo que existe, mas está corrompido.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class ArquivoInexistenteException extends PersistenciaException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria uma exceção para um arquivo de persistência ausente.
     *
     * @param mensagem descrição que inclui o caminho procurado
     */
    public ArquivoInexistenteException(String mensagem) {
        super(mensagem);
    }
}
