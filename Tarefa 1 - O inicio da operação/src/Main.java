/*Main.java
*
* última modificação: 31/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

public class Main {
    public static void main(String[] args) {

        /*1. Matéria  Prima*/
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plastico ABS", 100, "kg", 15);

        System.out.println("Matéria Prima ID: " + plasticoABS.getId() + " - " + plasticoABS.getNome());
        System.out.println("Quantidade de Matéria: " + plasticoABS.getQuantidade() + " " + plasticoABS.getUnidade());

        Produto prod1 = new Produto("CARC_TOP", "Carcaca Superior KTS590", 0)
    }
}
