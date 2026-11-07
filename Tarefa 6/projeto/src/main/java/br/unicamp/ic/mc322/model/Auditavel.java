/** Elemento da planta que fornece informações para a auditoria.
 *
 * @author Eduardo Pontes
 * @author Icaro Amaral
 */
public interface Auditavel {

    /** Gera o diagnóstico atual do elemento.
     * @return relatório de diagnóstico do elemento
     */
    String gerarRelatorioDiagnostico();

    /** Verifica se o elemento exige alguma intervenção.
     * @return {@code true} se o elemento exigir manutenção ou atenção
     */
    boolean precisaManutencao();
}
