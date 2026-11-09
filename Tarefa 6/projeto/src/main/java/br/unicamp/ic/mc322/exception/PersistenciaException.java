package br.unicamp.ic.mc322.exception;

/**
 * Lancada quando ocorre uma falha durante a leitura, escrita ou validacao
 * do arquivo XML de persistencia da planta industrial.
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class PersistenciaException extends PlantaException {

    private static final long serialVersionUID = 1L;

    /**
     * Constroi uma nova excecao de persistencia com a mensagem especificada.
     *
     * @param mensagem descricao detalhada da falha de leitura, escrita ou validacao
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Constroi uma nova excecao de persistencia encadeando a causa original.
     *
     * @param mensagem descricao detalhada da falha
     * @param causa excecao original capturada durante o processamento do arquivo
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem);
        initCause(causa);
    }
}