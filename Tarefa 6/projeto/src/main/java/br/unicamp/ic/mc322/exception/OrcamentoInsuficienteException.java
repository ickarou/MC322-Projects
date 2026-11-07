/** Indica que o orçamento disponível não cobre o custo de uma operação.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */

public class OrcamentoInsuficienteException extends PlantaException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Cria uma exceção para uma operação sem orçamento suficiente.
     *
     * @param mensagem descrição do custo e do orçamento disponível
     */
    public OrcamentoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
