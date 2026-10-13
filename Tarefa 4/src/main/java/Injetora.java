/* Injetora.java
 *
 * Tarefa 4
 *
 * Material para a disciplina MC322 - Programação orientada a objetos
 *
 */

/**
 * Máquina responsável por derreter e injetar o plástico ABS nos moldes.
 * <p>
 * Se houver variação térmica na injeção, não descarta imediatamente a peça, 
 * mas aumenta a probabilidade de falha oculta nela, a ser capturada mais tarde 
 * pela estação de testes.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class Injetora extends Maquina {
    
    /**
     * Inicializa a Injetora com os custos e capacidades padrões da máquina.
     * 
     * @param nome A nomenclatura do equipamento.
     * @param cenario O cenário de estresse operacional da fábrica.
     */
    public Injetora(String nome, Cenario cenario) {
        super(nome, 5000, 0.15f, 500.00f, cenario);
    }

    /**
     * Injeta a matéria-prima no molde para formatar o produto base.
     * 
     * @param produto O objeto que representa o produto físico na linha de montagem.
     * @return {@code true} independentemente da injeção ter sofrido variação ou não.
     * @throws MaquinaIndisponivelException se a máquina não puder rodar por estar desligada ou quebrada.
     */
    @Override 
    public boolean processar(Produto produto) throws MaquinaIndisponivelException {
        if (!estaLigada()) {
            throw new MaquinaIndisponivelException("A maquina " + getNome() + " esta desligada. Ligue-a para iniciar a injecao.");
        }

        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException(getNome() + " esta quebrada e não pode processar " + produto.getNome());
        }

        produto.processar();

        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(0.05f * getFatorFalha());
            System.out.println("AVISO: Ocorreu uma variação termica no bico de injecao da " + getNome() + ". Probabilidade de defeito aumentada em " + produto.getNome());
        } else {
            System.out.println("Injecao de " + produto.getNome() + " realizada!");
        }
 
        produto.setStatus("Injetado com Sucesso");
        aplicarDesgaste();
        
        return true;
    }

    /** @return tipo da máquina */
    @Override
    public String getTipo() {
        return "Maquina Injetora de Plastico ABS";
    }
}