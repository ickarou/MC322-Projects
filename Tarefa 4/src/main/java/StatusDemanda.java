/** Estados possíveis durante o ciclo de vida de uma demanda.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public enum StatusDemanda {
    /** Demanda aberta e disponível para produção. */
    PENDENTE("Demanda cadastrada, mas ainda nao iniciada"),
    /** Lote em processamento na linha de produção. */
    EM_PRODUCAO("Demanda selecionada e em processo de fabricacao"),
    /** Todas as unidades solicitadas foram aprovadas. */
    CONCLUIDA("Demanda com todos os produtos produzidos com sucesso"),
    /** Demanda encerrada por decisão do sistema ou do operador. */
    CANCELADA("Demanda cancelada");

    private final String descricao;

    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /** Consulta a descrição do estado.
     * @return descrição textual do estado
     */
    public String getDescricao() {
        return descricao;
    }
}
