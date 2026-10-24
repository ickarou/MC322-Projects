/** 
 * Maquina responsavel pelo empacotamento das pecas produzidas.
 * Possui tres atuadores, que representam as juntas do braco robotico.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class BracoEncaixotador extends Maquina {

    /**
     * Cria um braco encaixotador para o cenario selecionado.
     *
     * @param nome identificacao do equipamento
     * @param cenario cenario de operacao
     */
    public BracoEncaixotador(String nome, Cenario cenario) {
        super(nome, 1000, 0.10f, 100.00f, cenario, 3);
    }

    /* Metodos da classe principal */

    /**
     * Empacota a peca e registra possiveis falhas de operacao.
     *
     * @param produto peca a processar
     * @return {@code true} se a etapa for realizada
     * @throws MaquinaIndisponivelException se o equipamento estiver desligado ou quebrado
     * @throws IllegalArgumentException se o produto for nulo
     */
    @Override
    public boolean processar(Produto produto) throws MaquinaIndisponivelException {
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        
        validarDisponibilidade();
        aplicarRiscoDasPartes(produto);

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(0.10f * getFatorFalha());
            System.out.println("AVISO: Ocorreu um problema nas juntas do " + getNome()
                    + ". Probabilidade de defeito aumentada em " + produto.getNome() + ".");
        } else {
            System.out.println(produto.getNome() + " empacotado!");
        }

        produto.setStatus("Empacotamento realizado");
        aplicarDesgaste();
        return true;
    }

    @Override
    public String getTipo() {
        return "Braco Robotico Empacotador";
    }
}