/*Subclasse Proteção Lateral
*
* Tarefa 2
*
* última modificação: 10/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class ProtecaoLateral extends Produto {
    
    public ProtecaoLateral(String id) {
        super(id, "Proteção Lateral KTS590", 150, 0.5f);
    }

    /*Métodos da classe principal*/
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.01f); 
    }

    @Override
    public int calcularTempoProducao(){
        return 80;
    }

    @Override
    public String getTipo(){
        return "Baixa Qualidade";
    }
}

