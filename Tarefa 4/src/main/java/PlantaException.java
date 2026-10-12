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
    
    /**
     * Constrói uma nova exceção da planta com a mensagem de detalhe especificada.
     * 
     * @param mensagem A mensagem detalhando o motivo pelo qual a regra de negócio falhou.
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }
}