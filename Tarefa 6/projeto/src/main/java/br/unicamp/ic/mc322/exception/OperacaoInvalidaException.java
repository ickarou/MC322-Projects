/**
 * Lancada quando uma operacao generica nao pode ser concluida por falta de parametros.
 * <p>
 * Representa erros de fluxo do usuario, como tentar operar a fabrica sem definir 
 * uma estrategia de producao ou tentar comprar quantidades nulas de insumos.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class OperacaoInvalidaException extends PlantaException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria uma excecao para uma operacao generica invalida.
     * 
     * @param mensagem A descricao do motivo pelo qual a operacao falhou.
     */
    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}