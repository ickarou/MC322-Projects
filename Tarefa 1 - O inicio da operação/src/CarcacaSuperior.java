/*Subclasse Carcaça Superior
*
* Tarefa 2
*
* última modificação: 10/09/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class CarcacaSuperior extends Produto{

    public CarcacaSuperior(String id) {
        super(id, "Carcaça Superior KTS590", 300, 0.9f);
    }

    /*Métodos da classe principal*/
    @Override
    public void processar() {
        System.out.println("Injetando " + getNome() + "!");
        aumentarProbabilidadeFalha(0.05f); 
    }

    @Override
    public int calcularTempoProducao(){
        return 150;
    }

    @Override
    public String getTipo(){
        return "Carcaça Superior";
    }
}
