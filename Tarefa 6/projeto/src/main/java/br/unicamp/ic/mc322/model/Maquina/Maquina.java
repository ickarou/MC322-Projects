import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa uma máquina genérica operando na planta.
 * <p>
 * Classe abstrata responsável por gerenciar a saúde, o desgaste de operação, 
 * as probabilidades de falha baseadas no cenário e o estado de funcionamento 
 * (ligada/desligada) de qualquer equipamento da fábrica.
 * </p>
 * <p>
 * A máquina é o "Todo" da composição, de modo que, ela própria cria no construtor, as suas
 * partes internas e controla o ciclo de vida delas. São um {@link Motor} (1:1), um
 * {@link SensorDeTemperatura} (1:1) e um ou mais {@link Atuador} (1:N, guardados em
 * uma lista porque a ordem identifica cada atuador). As partes nunca saem da máquina, logo,
 * não há métodos que as devolvam, e quem usa a máquina só chama operações de alto
 * nível, como {@link #aplicarDesgaste()} e {@link #repararMaquina()}, que ela delega.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */

public abstract class Maquina implements Auditavel {
    
    // Atributos privados
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private float custoOperacao;
    private Random random = new Random();

    private float fatorFalha;
    private float fatorDesgaste;

    // Partes Internas
    private final Motor motor;
    private final SensorDeTemperatura sensor;
    private final List<Atuador> atuadores;

    /**
     * Inicializa uma máquina conforme a capacidade e o cenário de operação.
     *
     * As partes internas são criadas aqui, dentro do construtor.
     * 
     * @param nome identificação da máquina
     * @param capacidadeMaxima capacidade máxima declarada do equipamento
     * @param probabilidadeFalhaBase probabilidade base de falha entre zero e um
     * @param custoOperacao custo de operação por peça
     * @param cenario cenário que altera a falha e o desgaste
     * @param quantidadeAtuadores quantos atuadores a máquina possui, no mínimo um
     * @throws IllegalArgumentException se algum dado for inválido ou se a quantidade de atuadores for menor que um
     */
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalhaBase, float custoOperacao, Cenario cenario, int quantidadeAtuadores) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da maquina deve ser informado.");
        }
        if (cenario == null) {
            throw new IllegalArgumentException("O cenario da maquina deve ser informado.");
        }
        if (capacidadeMaxima <= 0 || !(custoOperacao >= 0.0f)) {
            throw new IllegalArgumentException("Capacidade e custo de operacao devem ser valores validos.");
        }
        if (!(probabilidadeFalhaBase >= 0.0f && probabilidadeFalhaBase <= 1.0f)) {
            throw new IllegalArgumentException("A probabilidade de falha base deve estar entre zero e um.");
        }
        if (quantidadeAtuadores < 1) {
            throw new IllegalArgumentException("A maquina precisa de pelo menos um atuador.");
        }
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        this.fatorFalha = cenario.getFatorFalha();
        this.fatorDesgaste = cenario.getFatorDesgaste();
        this.probabilidadeFalha = Math.min(1.0f, probabilidadeFalhaBase * fatorFalha);
        this.ligada = false;
 
        this.motor = new Motor();
        this.sensor = new SensorDeTemperatura();
        this.atuadores = new ArrayList<>();
        for (int i = 1; i <= quantidadeAtuadores; i++) {
            this.atuadores.add(new Atuador(i));
        }
    }

    /**
     * Processa uma peça no equipamento.
     *
     * @param produto produto a processar
     * @return {@code true} se a peça passou pela etapa; {@code false} se foi rejeitada
     * @throws MaquinaIndisponivelException se a máquina estiver desligada ou quebrada
     * @throws IllegalArgumentException se o produto for nulo
     */
    public abstract boolean processar(Produto produto);

    /** Informa a categoria do equipamento.
     * @return descrição do tipo de equipamento
     */
    public abstract String getTipo();

    /**
     * Liga a máquina para iniciar o processamento de produtos.
     * 
     * @throws MaquinaIndisponivelException se a saúde da máquina for zero (quebrada).
     */
    public void ligar() throws MaquinaIndisponivelException {
        if(estaQuebrada()){
            throw new MaquinaIndisponivelException("A maquina " + this.nome + " esta quebrada! Enviar para a manutencao imediatamente.");
        }
        this.ligada = true;
        System.out.println("Maquina " + this.nome + " ligada, afaste-se do equipamento");
    }

    /**
     * Desliga a máquina, interrompendo o risco de acidentes operacionais.
     */
    public void desligar(){
        this.ligada = false;
        System.out.println("Maquina " + this.nome + " desligada, segura para manuseio");
    }

    /** Consulta o estado de operação da máquina.
     * @return {@code true} se o equipamento estiver ligado
     */
    public boolean estaLigada(){
        return ligada;
    }

    /**
     * Sorteia um evento conforme a probabilidade informada.
     *
     * @param chance probabilidade entre zero e um
     * @return {@code true} se o evento ocorrer
     * @throws IllegalArgumentException se a probabilidade for inválida
     */
    protected boolean sortearChance(float chance) {
        if (chance < 0.0f || chance > 1.0f) {
            throw new IllegalArgumentException("A probabilidade deve estar entre zero e um.");
        }
        return random.nextFloat() < chance;
    }

    /**
     * Verifica se a máquina falhará na iteração atual, considerando o desgaste das partes.
     * <p>
     * A probabilidade base é multiplicada pelo efeito de cada parte (motor e sensor
     * gastos aumentam a chance) e limitada a 100%.
     * </p>
     * 
     * @return {@code true} se a máquina falhou nesta etapa de processo.
     */
    protected boolean verificarFalha() {
        float fator = 1.0f;
        for (ComponenteMaquina parte : listarPartes()) {
            fator *= parte.calcularFatorDeFalha();
        }
        return sortearChance(Math.min(1.0f, probabilidadeFalha * fator));
    }

    /**
     * Desgasta todas as partes após cada processamento.
     * <p>
     * Cada parte sorteia o seu desgaste base (0 a 3 pontos), multiplicado pelo fator
     * de desgaste do cenário e pela taxa própria da parte. A saúde de uma parte nunca
     * fica abaixo de 0.
     * </p>
     */
    public void aplicarDesgaste(){
        for (ComponenteMaquina parte : listarPartes()) {
            parte.desgastar(fatorDesgaste);
        }
    }

    /**
     * Soma à peça o risco deixado pelas partes desgastadas (atuadores imprecisos).
     * As subclasses chamam este método no início do processamento, logo depois de validar a máquina.
     *
     * @param produto peça que acabou de ser processada
     * @throws IllegalArgumentException se o produto for nulo
     */
    protected void aplicarRiscoDasPartes(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto deve ser informado.");
        }
        float risco = 0.0f;
        for (ComponenteMaquina parte : listarPartes()) {
            risco += parte.calcularRiscoParaPeca();
        }
        if (risco > 0.0f) {
            produto.aumentarProbabilidadeFalha(Math.min(1.0f, risco));
        }
    }

    /**
     * Verifica se o equipamento quebrou.
     * 
     * @return {@code true} se alguma parte da máquina quebrou
     */
    public boolean estaQuebrada(){
        for (ComponenteMaquina parte : listarPartes()) {
            if (parte.estaQuebrado()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restaura a saúde de todas as partes para 100%, simulando uma intervenção técnica.
     */
    public void repararMaquina(){
        for (ComponenteMaquina parte : listarPartes()) {
            parte.reparar();
        }
        System.out.println("A maquina " + this.nome + " passou por reparos e esta a todo vapor de novo!");
    }

    /**
     * Reúne as partes em uma lista nova, só para as operações que valem para todas.
     * A lista é descartável: as partes continuam sem sair da máquina.
     *
     * @return lista com o motor, o sensor e os atuadores
     */
    private List<ComponenteMaquina> listarPartes() {
        List<ComponenteMaquina> partes = new ArrayList<>();
        partes.add(motor);
        partes.add(sensor);
        partes.addAll(atuadores);
        return partes;
    }

    // Getters

    /** Consulta o nome da máquina.
     * @return nome de identificação da máquina
     */
    public String getNome(){
        return nome;
    }

    /** Consulta a saúde atual do equipamento, que é a da parte mais desgastada.
     * @return saúde atual do equipamento, de zero a cem
     */
    public int getSaude(){
        int menor = 100;
        for (ComponenteMaquina parte : listarPartes()) {
            menor = Math.min(menor, parte.getSaude());
        }
        return menor;
    }

    /** Consulta o multiplicador de falhas do cenário.
     * @return multiplicador de falhas do cenário
     */
    protected float getFatorFalha(){
        return fatorFalha;
    }

    /** Consulta o custo de operação por peça.
     * @return custo de operação por peça
     */
    public float getCustoOperacao(){
        return custoOperacao;
    }

    /** Consulta a capacidade nominal do equipamento.
     * @return capacidade máxima declarada para o equipamento
     */
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
    
    /**
     * Gera o diagnóstico hierárquico da máquina: uma linha com a saúde geral e, abaixo,
     * uma linha indentada para cada parte (auditoria em cascata).
     *
     * @return relatório com várias linhas, separadas por quebra de linha
     */
    @Override 
    public String gerarRelatorioDiagnostico(){
        StringBuilder relatorio = new StringBuilder();
        relatorio.append(this.nome).append(" - saude geral ").append(getSaude());
        if (getSaude() < 50) {
            relatorio.append(" | ATENCAO: avaliar intervencao");
        }
        for (ComponenteMaquina parte : listarPartes()) {
            relatorio.append("\n    |-- ").append(parte.gerarRelatorioDiagnostico());
        }
        return relatorio.toString();
    }

    /**
     * Valida se a maquina esta apta para operacao.
     * 
     * @throws MaquinaIndisponivelException se a maquina estiver desligada ou quebrada.
     */
    protected void validarDisponibilidade() {
        if (!estaLigada()) {
            throw new MaquinaIndisponivelException("A maquina " + getNome() + " esta desligada. Ligue-a primeiro.");
        }
        if (estaQuebrada()) {
            throw new MaquinaIndisponivelException(getNome() + " esta quebrada e precisa de manutencao.");
        }
    }
    
    /** @return {@code true} se a saúde da máquina for igual ou inferior a trinta */
    @Override 
    public boolean precisaManutencao(){
        return getSaude() <= 30;
    }
}