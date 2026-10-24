/**
 * Motor da máquina (parte 1:1).
 * <p>
 * Regra própria: um motor gasto deixa a máquina menos confiável. A chance de falha
 * da máquina é multiplicada por até 2 conforme o motor se desgasta. Quando quebra,
 * a máquina inteira para.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class Motor extends ComponenteMaquina {
 
    /** Cria um motor novo, com saúde máxima. */
    public Motor() {
        super("Motor", "Aquecendo", "Superaquecido", 1.0f);
    }
 
    /** @return multiplicador entre 1 e 2 sobre a chance de falha da máquina */
    @Override
    public float calcularFatorDeFalha() {
        return 1.0f + getNivelDeDesgaste();
    }
}