/**
 * Lançada quando uma operação de produção é impedida pelo estado da máquina.
 * <p>
 * Esta exceção é acionada sempre que o sistema tenta ligar ou processar um produto 
 * em um equipamento que se encontra desligado ou quebrado (com a saúde zerada).
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class MaquinaIndisponivelException extends PlantaException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constrói uma nova exceção de máquina indisponível com a mensagem especificada.
     * 
     * @param mensagem A mensagem detalhando o motivo da indisponibilidade (ex: desligada ou quebrada).
     */
    public MaquinaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}