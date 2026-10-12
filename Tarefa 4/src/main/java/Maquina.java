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
     * Inicializa os parâmetros operacionais da máquina.
     * <p>
     * A probabilidade de falha real e o fator de desgaste são calculados 
     * multiplicando as bases informadas pelos multiplicadores do cenário atual.
     * </p>
     *
     * @param nome O nome de identificação da máquina.
     * @param capacidadeMaxima A capacidade de produção.
     * @param probabilidadeFalhaBase A chance da máquina apresentar um erro no ciclo (0.0 a 1.0).
     * @param custoOperacao O custo financeiro de cada operação da máquina.
     * @param cenario O cenário ativo da fábrica que afeta o desgaste e a falha.
     */

    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalhaBase, float custoOperacao, Cenario cenario) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        this.fatorFalha = cenario.getFatorFalha();
        this.fatorDesgaste = cenario.getFatorDesgaste();
        this.probabilidadeFalha = probabilidadeFalhaBase * this.fatorFalha;
        this.ligada = false;
    }

    /**
     * Executa a operação principal da máquina sobre uma unidade de produto.
     * 
     * @param produto A peça que será submetida à máquina.
     * @return {@code true} se o processamento obteve sucesso, {@code false} se a peça foi rejeitada.
     * @throws MaquinaIndisponivelException se a máquina estiver quebrada ou desligada.
     */
    public abstract boolean processar(Produto produto);

    public abstract String getTipo();

    /**
     * Liga a máquina para iniciar o processamento de produtos.
     * 
     * @throws MaquinaIndisponivelException se a saúde da máquina for zero (quebrada).
     */
    public void ligar() throws MaquinaIndisponivelException {
        if(estaQuebrada()){
            throw new MaquinaIndisponivelException("A máquina " + this.nome + " está quebrada! Enviar para a manutenção imediatamente.");
        }
        this.ligada = true;
        System.out.println("Máquina " + this.nome + " ligada, afaste-se do equipamento");
    }

    /**
     * Desliga a máquina, interrompendo o risco de acidentes operacionais.
     */
    public void desligar(){
        this.ligada = false;
        System.out.println("Máquina " + this.nome + " desligada, segura para manuseio");
    }

    public boolean estaLigada(){
        return ligada;
    }

    /**
     * Sorteia o acontecimento de um evento aleatório baseado na probabilidade informada.
     * 
     * @param chance A probabilidade do evento ocorrer (ex: 0.10f para 10%).
     * @return {@code true} se o evento ocorreu no sorteio, {@code false} caso contrário.
     */
    protected boolean sortearChance(float chance) {
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

    public boolean estaQuebrada(){
        return this.saude <= 0;
    }

    /**
     * Restaura a saúde da máquina para 100%, simulando uma intervenção técnica.
     */
    public void repararMaquina(){
        this.saude = 100;
        System.out.println("A maquina " + this.nome + " passou por reparos e está a todo vapor de novo!");
    }

    // Getters
    public String getNome(){
        return nome;
    }

    public int getSaude(){
        return saude;
    }

    protected float getFatorFalha(){
        return fatorFalha;
    }

    public float getCustoOperacao(){
        return custoOperacao;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
    
    // Implementação Interface Auditavel
    @Override 
    public String gerarRelatorioDiagnostico(){
        if (this.saude < 50){
            return "Atenção! A saúde da maquina " + this.nome + " está em " + this.saude + " avaliar intervenção";
        }
        return "A saúde da máquina " + this.nome + " está em " + this.saude;
    }
    
    @Override 
    public boolean precisaManutencao(){
        return this.saude <= 30;
    }
}