/* Classe Abstrata Máquina
*
* Tarefa 3
*
* última modificação: 24/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

import java.util.Random;

public abstract class Maquina implements Auditavel {
    
    //Atributos privados
    private String nome;
    private boolean ligada;
    private int capacidadeMaxima;
    private float probabilidadeFalha;
    private float custoOperacao;
    private Random random = new Random();

    protected int saude = 100;

    //Construtor
    public Maquina(String nome, int capacidadeMaxima, float probabilidadeFalha, float custoOperacao) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.custoOperacao = custoOperacao;
        this.probabilidadeFalha = probabilidadeFalha;
        this.ligada = false;
    }

    //Métodos Abstratos
    public abstract boolean processar(Produto produto);

    public abstract String getTipo();

    //Métodos Concretos
    public void ligar(){
        if(estaQuebrada()){
            System.out.println(">>ATENÇÃO! A máquina " + this.nome + " está quebrada! Enviar para a manutenção imediatamente.");
            return;
        }
        this.ligada = true;
        System.out.println("Máquina " + this.nome + " ligada, afaste-se do equipamento");
    }

    public void desligar(){
        this.ligada = false;
        System.out.println("Máquina " + this.nome + " desligada, segura para manuseio");
    }

    public boolean estaLigada(){
        return ligada;
    }

    protected boolean verificarFalha() {
        float porcentagemDesgaste = (100 - this.saude)/100f; //se a saude estiver em 100 vira 0%
        float chanceReal = this.probabilidadeFalha + (porcentagemDesgaste * probabilidadeFalha);
        return random.nextFloat() < chanceReal;
    }

    public void aplicarDesgaste(){
        int desgaste = random.nextInt(4);
        this.saude = Math.max(0, this.saude - desgaste); //limite inferior de 0, se a subtração for menor que zero a saude zera
    }

    public boolean estaQuebrada(){
        return this.saude <= 0;
    }

    public void repararMaquina(){
        this.saude = 100;
        System.out.println("A maquina " + this.nome + " passou por reparos e está a todo vapor de novo!");
    }

    //Getters
    public String getNome(){
        return nome;
    }

    public int getSaude(){
        return saude;
    }

    public float getCustoOperacao(){
        return custoOperacao;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
    
    //Implementação Interface Auditavel
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
