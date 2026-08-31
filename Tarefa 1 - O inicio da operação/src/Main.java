/*Main.java
*
* última modificação: 31/08/2026
*
* Material para a disciplina MC322 - Programação orientada a objetos
*
*/

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        /*1. Matéria  Prima*/
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plastico ABS", 5000, "g", 200);

        System.out.println("Matéria Prima ID: " + plasticoABS.getId() + " - " + plasticoABS.getNome());
        System.out.println("Quantidade de Matéria: " + plasticoABS.getQuantidade() + " " + plasticoABS.getUnidade());

        Produto prod1 = new Produto("CARC_TOP", "Carcaca Superior KTS590", 250);
        Produto prod2 = new Produto("PROT_LAT", "Protecao Lateral KTS590", 200);
        Produto prod3 = new Produto("CARC_BOT", "Carcaca Inferior KTS590", 250);
    }
}
