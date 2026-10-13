/** Equipamento que inspeciona a qualidade das pecas apos a fabricacao.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class EstacaoTestes extends Maquina {

    /**
     * Cria uma estacao de testes para o cenario selecionado.
     *
     * @param nome identificacao do equipamento
     * @param cenario cenario de operacao
     */
    public EstacaoTestes(String nome, Cenario cenario) {
        super(nome, 1000, 0.05f, 200.00f, cenario);
    }

    /**
     * Inspeciona a peca e define o resultado dos testes.
     *
     * @param produto peca a inspecionar
     * @return {@code true} se a peca for aprovada pela inspecao
     * @throws MaquinaIndisponivelException se o equipamento estiver desligado ou quebrado
     * @throws IllegalArgumentException se o produto for nulo
     */
    @Override
    public boolean processar(Produto produto) throws MaquinaIndisponivelException {
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        
        validarDisponibilidade();

        float chanceRejeicao = (produto.getProbabilidadeFalhaAcumulada() * 0.5f)
                + ((1.0f - produto.getQualidade()) * 0.2f);
        boolean produtoComDefeito = sortearChance(Math.min(1.0f, chanceRejeicao));
        boolean maquinaFalhou = verificarFalha();
        
        aplicarDesgaste();

        if (maquinaFalhou) {
            System.out.println("ERRO DE SENSOR A " + getNome()
                    + " desregulou e gerou uma inspecao incorreta para " + produto.getNome() + "!");
            boolean aprovadoPeloSensor = produtoComDefeito;
            produto.setStatus(aprovadoPeloSensor ? "Aprovado Indevidamente" : "Reprovado Indevidamente");
            return aprovadoPeloSensor;
        }

        if (produtoComDefeito) {
            System.out.println("REPROVADO: A peca " + produto.getNome() + " apresentou falhas na inspecao.");
            produto.setStatus("Reprovado na Inspecao");
            return false;
        }

        System.out.println("APROVADO: A peca " + produto.getNome() + " passou nos testes.");
        produto.setStatus("Aprovado");
        return true;
    }

    /** @return tipo da maquina */
    @Override
    public String getTipo() {
        return "Estacao de Testes e Validacoes";
    }
}