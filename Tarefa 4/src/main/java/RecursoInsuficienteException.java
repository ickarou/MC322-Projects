/** Indica que a planta não dispõe dos recursos necessários para uma operação.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class RecursoInsuficienteException extends PlantaException {
    
    /**
     * Cria uma exceção para falta de recursos.
     *
     * @param mensagem descrição do recurso indisponível
     */
    public RecursoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
