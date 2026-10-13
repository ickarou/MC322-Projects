/**
 * Representa a carcaça superior, o componente mais crítico e complexo do scanner KTS590.
 * <p>
 * Esta classe estende {@code Produto} e exige a maior quantidade de matéria-prima da linha 
 * (300 unidades), tendo também o maior grau de exigência em qualidade (0.9f).
 * </p>
 * 
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public class CarcacaSuperior extends Produto {

    /**
     * Instancia uma nova carcaça superior com as configurações e consumos máximos.
     * 
     * @param id O identificador único de rastreabilidade desta unidade.
     */
    public CarcacaSuperior(String id) {
        super(id, "Carcaca Superior KTS590", 300, 0.9f);
    }

    /**
     * Executa o processo de injeção plástica para formar a carcaça superior.
     * <p>
     * Devido à sua complexidade, o processo de injeção adiciona um peso significativo 
     * de 5% (0.05f) à probabilidade de falha acumulada da peça.
     * </p>
     */
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.05f); 
    }

    /**
     * Determina o tempo que a máquina leva para injetar este componente.
     * 
     * @return O tempo de produção estimado em segundos (150).
     */
    @Override
    public int calcularTempoProducao(){
        return 150;
    }

    /**
     * Retorna a classificação desta peça para uso nos relatórios de auditoria.
     * 
     * @return Uma string contendo {@code "Carcaça Superior"}.
     */
    @Override
    public String getTipo(){
        return "Carcaca Superior";
    }

    /**
     * Gera uma nova instância limpa desta carcaça para compor a produção de um lote.
     * 
     * @param id O código de rastreio que será atribuído à nova carcaça.
     * @return Uma nova instância de {@code CarcacaSuperior} para a linha de montagem.
     */
    @Override
    public Produto criarNovaUnidade(String id){
        return new CarcacaSuperior(id);
    }
}