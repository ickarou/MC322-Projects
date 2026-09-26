/* Subclasse Estação de testes (Classe Máquina)
*
* Tarefa 2
*
* última modificação: 12/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class EstacaoTestes extends Maquina {
    
    public EstacaoTestes(String nome) {
        super(nome, 1000, 0.05f, 200.00f);
    }

    @Override 
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            System.out.println("A máquina " + getNome() + " está desligada. Ligue-a para o início dos testes.");
            return false;
        }

        if (estaQuebrada()) {
            System.out.println(getNome() + " está quebrada e não pode processar " + produto.getNome());
            return false;
        }

        //chance de rejeição baseada na qualidade do produto e das falhas acumuladas nas máquinas anteriores
        float chanceRejeicao = (produto.getProbabilidadeFalhaAcumulada() * 0.5f) + (produto.getQualidade() * 0.2f);
        
        //verifica se a peça física tem defeito sorteando um número com math.random
        boolean produtoComDefeito = Math.random() < chanceRejeicao;

        boolean maquinaFalhou = verificarFalha();

        //se maquina de teste falhar ela realiza um inspeção incorreta
        if (maquinaFalhou) {
            System.out.println("[ERRO DE SENSOR] A " + getNome() + " desregulou e gerou uma inspeção incorreta para " + produto.getNome() + "!");
            boolean laudoIncorreto = !produtoComDefeito;
            
            if (laudoIncorreto) {
                produto.setStatus("Aprovado Indevidamente");
            } else {
                produto.setStatus("Reprovado Indevidamente");
            }
            return laudoIncorreto;
        }

        if (produtoComDefeito) {
            System.out.println("REPROVADO: A peça " + produto.getNome() + " apresentou falhas na inspeção.");
            produto.setStatus("Reprovado na Inspeção");
            return false;
        }

        System.out.println("APROVADO: A peça " + produto.getNome() + " passou com 100% de sucesso nos testes!");
        produto.setStatus("Aprovado");
        return true;
    }

    @Override
    public String getTipo() {
        return "Estação de Testes e Validações";
    }
}