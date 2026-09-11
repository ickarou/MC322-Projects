/*Subclasse Carcaça Inferior
*
* Tarefa 2
*
* última modificação: 10/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class CarcacaInferior extends Produto {
    
    public CarcacaInferior(String id) {
        super(id, "Carcaça Inferior KTS590", 200, 0.7f);
    }

    /*Métodos da classe principal*/
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.03f); 
    }

    @Override
    public int calcularTempoProducao(){
        return 120;
    }

    @Override
    public String getTipo(){
        return "Qualidade Média";
    }
}
