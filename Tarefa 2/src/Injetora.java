/* Subclasse Injetora (Classe Máquina)
*
* Tarefa 2
*
* última modificação: 12/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Injetora extends Maquina {
    
    public Injetora(String nome) {
        super(nome, 5000, 0.15f, 500.00f);
    }

    /*Métodos da classe principal*/
    @Override 
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            System.out.println("A máquina " + getNome() + " está desligada. Ligue-a para iniciar a injeção.");
            return false;
        }

        if (estaQuebrada()) {
            System.out.println(getNome() + " está quebrada e não pode processar " + produto.getNome());
            return false;
        }

        produto.processar();

        if (verificarFalha()) { 
            produto.aumentarProbabilidadeFalha(0.05f); 
            System.out.println("AVISO: Ocorreu uma variação térmica no bico de injeção da " + getNome() + ". Probabilidade de defeito aumentada em " + produto.getNome() + ".");
        } else {
            System.out.println("Injeção de " + produto.getNome() + " realizada!");
        }

        produto.setStatus("Injetado com Sucesso");
        return true;
    }

    @Override
    public String getTipo() {
        return "Máquina Injetora de Plástico ABS";
    }
}