/**
 * Representa a proteção lateral que compõe a carcaça do scanner automotivo KTS590.
 * <p>
 * Esta classe estende {@code Produto} e define as características operacionais específicas 
 * desta peça, configurando o seu consumo base de matéria-prima (150 unidades) e o seu 
 * fator estrutural de qualidade (0.5f).
 * </p>
 * 
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class ProtecaoLateral extends Produto {
    
    /**
     * Instancia uma nova proteção lateral com as configurações base do componente.
     * 
     * @param id O identificador único de rastreabilidade desta unidade.
     */
    public ProtecaoLateral(String id) {
        super(id, "Protecao Lateral KTS590", 150, 0.5f);
    }

    /**
     * Executa o processo de injeção plástica para formar a proteção lateral.
     * <p>
     * A cada injeção, a peça sofre um ligeiro acréscimo de 1% (0.01f) na sua 
     * probabilidade de falha acumulada devido ao stress natural do material.
     * </p>
     */
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.01f); 
    }

    /**
     * Determina o tempo que a máquina leva para injetar este componente.
     * 
     * @return O tempo de produção estimado em segundos (80).
     */
    @Override
    public int calcularTempoProducao(){
        return 80;
    }

    /**
     * Retorna a classificação desta peça para uso nos menus.
     * 
     * @return Uma string contendo {@code "Proteção Lateral"}.
     */
    @Override
    public String getTipo(){
        return "Protecao Lateral";
    }

    /**
     * Cria outra proteção lateral
     * 
     * @param id O código de rastreio que será atribuído à nova peça gerada.
     * @return Uma nova instância de {@code ProtecaoLateral} pronta para a linha de montagem.
     */
    @Override
    public Produto criarNovaUnidade(String id){
        return new ProtecaoLateral(id);
    }
}