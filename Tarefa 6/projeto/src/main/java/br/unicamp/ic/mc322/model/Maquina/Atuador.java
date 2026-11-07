/**
 * Atuador da máquina (parte 1:N).
 * <p>
 * Cada atuador é identificado pela sua posição na máquina e se desgasta de forma
 * independente dos demais. Regra própria: um atuador gasto perde precisão e deixa
 * um risco extra de defeito, de até 4%, em cada peça processada. Se um deles
 * quebra, a máquina inteira para.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class Atuador extends ComponenteMaquina {

    /**
     * Cria um atuador novo, com saúde máxima.
     *
     * @param numero posição do atuador dentro da máquina, a partir de 1
     * @throws IllegalArgumentException se o número for menor que 1
     */
    public Atuador(int numero) {
        super(nomeDoAtuador(numero), "Impreciso", "Travando", 1.2f);
    }

    /**
     * Monta o nome de identificação do atuador.
     *
     * @param numero posição do atuador, a partir de 1
     * @return nome no formato "Atuador N"
     * @throws IllegalArgumentException se o número for menor que 1
     */
    private static String nomeDoAtuador(int numero) {
        if (numero < 1) {
            throw new IllegalArgumentException("O numero do atuador deve ser maior ou igual a 1.");
        }
        return "Atuador " + numero;
    }

    /** @return risco de defeito, entre 0 e 0,04, somado à peça a cada uso */
    @Override
    public float calcularRiscoParaPeca() {
        return 0.04f * getNivelDeDesgaste();
    }
}
