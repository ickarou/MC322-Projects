/* Subclasse Braço Encaixotador (Classe Máquina)
*
* Tarefa 2
*
* última modificação: 12/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class BracoEncaixotador extends Maquina{
    
    public BracoEncaixotador(String nome){
        super(nome, 1000, 0.10f, 100.00f);
    }

    /*Métodos da classe principal*/
    @Override 
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            System.out.println("O " + getNome() + " está desligado. Ligue-o para iniciar o empacotamento");
            return false;
        }

        if (estaQuebrada()) {
            System.out.println(getNome() + " está quebrada e não pode processar " + produto.getNome());
            return false;
        }

        if (verificarFalha()) { 
            produto.aumentarProbabilidadeFalha(0.10f); 
            System.out.println("AVISO: Ocorreu um problema nas juntas do " + getNome() + ". Probabilidade de defeito aumentada em " + produto.getNome() + ".");
        } else {
            System.out.println(produto.getNome() + " empacotado!");
        }

        produto.setStatus("Empacotamento realizado");
        return true;
    }

    @Override
    public String getTipo() {
        return "Braço Robótico Empacotador";
    }
}
