/**
 * Representa uma máquina genérica operando na planta.
 * <p>
 * Classe abstrata responsável por gerenciar a saúde, o desgaste de operação, 
 * as probabilidades de falha baseadas no cenário e o estado de funcionamento 
 * (ligada/desligada) de qualquer equipamento da fábrica.
 * </p>
 * 
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */

import java.util.Random;

public abstract class Maquina implements Auditavel {
    
    // Atributos privados
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private float custoOperacao;
    private Random random = new Random();

    private int saude = 100;

    private float fatorFalha;
    private float fatorDesgaste;

    /**
     * Inicializa uma máquina conforme a capacidade e o cenário de operação.
     *
     * @param nome identificação da máquina
     * @param capacidadeMaxima capacidade máxima declarada do equipamento
     * @param probabilidadeFalhaBase probabilidade base de falha entre zero e um
     * @param custoOperacao custo de operação por peça
     * @param cenario cenário que altera a falha e o desgaste
     */
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalhaBase, float custoOperacao, Cenario cenario) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        this.fatorFalha = cenario.getFatorFalha();
        this.fatorDesgaste = cenario.getFatorDesgaste();
        this.probabilidadeFalha = Math.min(1.0f, probabilidadeFalhaBase * fatorFalha);
        this.ligada = false;
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
     * Verifica se a máquina falhará na iteração atual, considerando o desgaste.
     * <p>
     * Quanto menor a saúde, maior o incremento sobre a probabilidade de falha base.
     * </p>
     * 
     * @return {@code true} se a máquina falhou nesta etapa de processo.
     */
    protected boolean verificarFalha() {
        float porcentagemDesgaste = (100 - this.saude)/100f; // se a saude estiver em 100 vira 0%
        float chanceReal = this.probabilidadeFalha + (porcentagemDesgaste * probabilidadeFalha);
        return sortearChance(chanceReal);
    }

    /**
     * Reduz aleatoriamente a saúde da máquina após cada processamento produtivo.
     * <p>
     * O desgaste base (0 a 3 pontos) é multiplicado pelo fator de desgaste do cenário.
     * A saúde da máquina nunca atinge valores inferiores a 0.
     * </p>
     */
    public void aplicarDesgaste(){
        int desgasteBase = random.nextInt(4); // reduz a saude com um valor aleatorio entre 0 e 3
        int desgasteReal = Math.round(desgasteBase * this.fatorDesgaste);
        this.saude = Math.max(0, this.saude - desgasteReal); // limite inferior de 0 para não bugar
    }

    /**
     * Verifica se o equipamento quebrou.
     * 
     * @return 
     */
    public boolean estaQuebrada(){
        return this.saude <= 0;
    }

    /**
     * Restaura a saúde da máquina para 100%, simulando uma intervenção técnica.
     */
    public void repararMaquina(){
        this.saude = 100;
        System.out.println("A maquina " + this.nome + " passou por reparos e esta a todo vapor de novo!");
    }

    // Getters

    /** Consulta o nome da máquina.
     * @return nome de identificação da máquina
     */
    public String getNome(){
        return nome;
    }

    /** Consulta a saúde atual do equipamento.
     * @return saúde atual do equipamento, de zero a cem
     */
    public int getSaude(){
        return saude;
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
    
    /** @return relatório de diagnóstico da saúde da máquina */
    @Override 
    public String gerarRelatorioDiagnostico(){
        if (this.saude < 50){
            return "Atencao! A saude da maquina " + this.nome + " esta em " + this.saude + " avaliar intervencao";
        }
        return "A saude da maquina " + this.nome + " esta em " + this.saude;
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
    
    /** @return {@code true} se a saúde for igual ou inferior a trinta */
    @Override 
    public boolean precisaManutencao(){
        return this.saude <= 30;
    }
}