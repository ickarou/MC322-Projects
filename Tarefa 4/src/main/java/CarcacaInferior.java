/**
 * Representa a carcaça inferior, o componente base de sustentação do scanner KTS590.
 * <p>
 * Esta classe estende {@code Produto} e possui um nível intermediário de exigência, 
 * consumindo 200 unidades de matéria-prima e com um fator de qualidade de 0.7f.
 * </p>
 * 
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class CarcacaInferior extends Produto {
    
    /**
     * Instancia uma nova carcaça inferior com as configurações intermediárias da linha.
     * 
     * @param id O identificador único de rastreabilidade desta unidade.
     */
    public CarcacaInferior(String id) {
        super(id, "Carcaca Inferior KTS590", 200, 0.7f);
    }

    /**
     * Executa o processo de injeção plástica para formar a carcaça inferior.
     * <p>
     * A injeção desta peça gera um stress mecânico moderado, adicionando 3% (0.03f) 
     * à sua probabilidade de falha acumulada.
     * </p>
     */
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.03f); 
    }

    /**
     * Determina o tempo que a máquina leva para injetar este componente.
     * 
     * @return O tempo de produção estimado em segundos (120).
     */
    @Override
    public int calcularTempoProducao(){
        return 120;
    }

    /**
     * Retorna a classificação desta peça para consulta nos menus do sistema.
     * 
     * @return Uma string contendo {@code "Carcaça Inferior"}.
     */
    @Override
    public String getTipo(){
        return "Carcaca Inferior";
    }

    /**
     * Fabrica uma nova instância limpa desta carcaça para ser processada em massa.
     * 
     * @param id O código de rastreio que será atribuído à nova carcaça gerada.
     * @return Uma nova instância de {@code CarcacaInferior}.
     */
    @Override
    public Produto criarNovaUnidade(String id){
        return new CarcacaInferior(id);
    }
}