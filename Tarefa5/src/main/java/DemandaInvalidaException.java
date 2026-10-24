/**
 * Lançada para indicar foi realizada uma operação inválida sobre uma demanda.
 * <p>
 * Esta exceção é utilizada quando ocorrem violações no ciclo de vida de uma demanda, 
 * como tentar iniciar a produção de um lote já cancelado, atualizar quantidades de 
 * pedidos concluídos ou operar sobre demandas inexistentes.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */

public class DemandaInvalidaException extends PlantaException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constrói uma nova exceção de demanda inválida com a mensagem especificada.
     * 
     * @param mensagem A mensagem explicando por que o estado ou a operação da demanda é inválida.
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
