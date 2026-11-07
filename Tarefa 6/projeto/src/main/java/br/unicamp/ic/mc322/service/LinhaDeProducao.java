import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrupa as máquinas que formam o fluxo de fabricação da planta.
 * <p>
 * A relação com {@link Maquina} é uma agregação: a linha mantém uma coleção de
 * máquinas, mas elas existem por conta própria e podem ser tiradas e colocadas
 * em outra linha. Já as partes internas de cada máquina nunca passam por aqui.
 * </p>
 * <p>
 * A ordem em que as máquinas são adicionadas é a ordem do fluxo: toda peça passa
 * por elas em sequência. O restante do sistema fala com a linha e com as máquinas, nunca com as partes. A linha delega
 * o trabalho para as máquinas (ligar, processar, calcular custos e auditar).
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class LinhaDeProducao implements Auditavel {

    private final String nome;
    private final List<Maquina> maquinas;

    /**
     * Cria uma linha vazia.
     *
     * @param nome identificação da linha
     * @throws IllegalArgumentException se o nome não for informado
     */
    public LinhaDeProducao(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da linha de producao deve ser informado.");
        }
        this.nome = nome;
        this.maquinas = new ArrayList<>();
    }

    /**
     * Adiciona uma máquina no final do fluxo.
     *
     * @param maquina equipamento a ser integrado à linha
     * @throws IllegalArgumentException se a máquina for nula
     * @throws OperacaoInvalidaException se a máquina já pertencer a esta linha
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new IllegalArgumentException("A maquina a ser adicionada deve ser informada.");
        }
        if (maquinas.contains(maquina)) {
            throw new OperacaoInvalidaException("A maquina " + maquina.getNome() + " ja pertence a linha " + nome + ".");
        }
        maquinas.add(maquina);
    }

    /**
     * Retira uma máquina da linha, sem destruí-la, para que possa ser usada em outra linha.
     *
     * @param maquina equipamento a ser retirado
     * @throws IllegalArgumentException se a máquina for nula
     * @throws OperacaoInvalidaException se a máquina não pertencer à linha ou ainda estiver ligada
     */
    public void removerMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new IllegalArgumentException("A maquina a ser removida deve ser informada.");
        }
        if (!maquinas.contains(maquina)) {
            throw new OperacaoInvalidaException("A maquina " + maquina.getNome() + " nao pertence a linha " + nome + ".");
        }
        if (maquina.estaLigada()) {
            throw new OperacaoInvalidaException("Desligue a maquina " + maquina.getNome() + " antes de retira-la da linha.");
        }
        maquinas.remove(maquina);
    }

    /**
     * Consulta as máquinas da linha, na ordem do fluxo.
     * A lista devolvida é somente para leitura: a estrutura só muda por meio
     * de {@link #adicionarMaquina(Maquina)} e {@link #removerMaquina(Maquina)}.
     *
     * @return visão imutável das máquinas da linha
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /** Consulta o nome da linha.
     * @return nome de identificação da linha
     */
    public String getNome() {
        return nome;
    }

    /** Consulta o tamanho da linha.
     * @return quantidade de máquinas da linha
     */
    public int getQuantidadeMaquinas() {
        return maquinas.size();
    }

    /**
     * Liga todas as máquinas da linha.
     * <p>
     * Se alguma estiver quebrada, nenhuma é ligada.
     * </p>
     *
     * @throws MaquinaIndisponivelException se alguma máquina da linha estiver quebrada
     * @throws OperacaoInvalidaException se a linha estiver vazia
     */
    public void ligarTodas() {
        if (maquinas.isEmpty()) {
            throw new OperacaoInvalidaException("A linha " + nome + " nao possui maquinas.");
        }
        String quebrada = buscarNomeDaMaquinaQuebrada();
        if (quebrada != null) {
            throw new MaquinaIndisponivelException("A maquina " + quebrada + " esta quebrada. Envie para manutencao antes de produzir.");
        }
        for (Maquina m : maquinas) {
            m.ligar();
        }
    }

    /** Desliga todas as máquinas da linha. */
    public void desligarTodas() {
        for (Maquina m : maquinas) {
            m.desligar();
        }
    }

    /**
     * Encaminha a peça por todas as máquinas, em ordem, e interrompe o percurso
     * se uma etapa a rejeitar.
     *
     * @param peca peça em processamento
     * @return {@code true} se a peça passar por todas as etapas sem rejeição
     * @throws MaquinaIndisponivelException se alguma máquina estiver desligada ou quebrada
     * @throws OperacaoInvalidaException se a linha não possuir máquinas
     * @throws IllegalArgumentException se a peça for nula
     */
    public boolean processarPeca(Produto peca) {
        if (peca == null) {
            throw new IllegalArgumentException("A peca a ser processada deve ser informada.");
        }
        if (maquinas.isEmpty()) {
            throw new OperacaoInvalidaException("A linha " + nome + " nao possui maquinas.");
        }
        for (Maquina m : maquinas) {
            if (!m.processar(peca)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Soma o custo de operação de todas as máquinas para uma peça.
     *
     * @return custo de operação de uma peça atravessando a linha inteira
     */
    public float calcularCustoPorPeca() {
        float custo = 0.0f;
        for (Maquina m : maquinas) {
            custo += m.getCustoOperacao();
        }
        return custo;
    }

    /**
     * Procura uma máquina impedida de operar.
     *
     * @return nome da primeira máquina quebrada ou {@code null} se todas estiverem aptas
     */
    public String buscarNomeDaMaquinaQuebrada() {
        for (Maquina m : maquinas) {
            if (m.estaQuebrada()) {
                return m.getNome();
            }
        }
        return null;
    }

    /**
     * Gera o diagnóstico hierárquico da linha (auditoria em cascata).
     * <p>
     * A primeira linha resume a linha. Abaixo vem o relatório de cada máquina,
     * que por sua vez traz o relatório de cada uma de suas partes, com um nível
     * de indentação a mais a cada passo: Linha &gt; Máquina &gt; Partes.
     * </p>
     *
     * @return relatório com várias linhas, separadas por quebra de linha
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append(nome).append(" (").append(maquinas.size()).append(" maquinas)");
        if (precisaManutencao()) {
            relatorio.append(" [ATENCAO]");
        }
        for (Maquina m : maquinas) {
            String[] linhas = m.gerarRelatorioDiagnostico().split("\n");
            relatorio.append("\n  > ").append(linhas[0]);
            for (int i = 1; i < linhas.length; i++) {
                relatorio.append("\n  ").append(linhas[i]);
            }
        }
        return relatorio.toString();
    }

    /** @return {@code true} se alguma máquina da linha precisar de manutenção */
    @Override
    public boolean precisaManutencao() {
        for (Maquina m : maquinas) {
            if (m.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }
}
