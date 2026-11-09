package br.unicamp.ic.mc322.exception;

/**
 * Classe de exceções base para todos os tratamentos de exceções do sistema.
 * <p>
 * Esta classe estende {@code RuntimeException} e serve como raiz para uma hierarquia 
 * de exceções específicas do sistema da fábrica.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */

public abstract class PlantaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constrói uma nova exceção da planta com a mensagem de detalhe especificada.
     * 
     * @param mensagem A mensagem detalhando o motivo pelo qual a regra de negócio falhou.
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Constrói uma nova exceção da planta preservando a causa original da falha.
     * 
     * @param mensagem A mensagem detalhando o motivo pelo qual a operação falhou.
     * @param causa A exceção de baixo nível que originou o problema.
     */
    public PlantaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
