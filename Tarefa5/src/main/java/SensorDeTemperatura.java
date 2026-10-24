/**
 * Sensor de temperatura da máquina (parte 1:1).
 * <p>
 * Regra própria: um sensor descalibrado deixa de acusar o superaquecimento a tempo,
 * então a chance de falha da máquina cresce em até 50% conforme ele se desgasta.
 * Quando quebra, a máquina inteira para.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class SensorDeTemperatura extends ComponenteMaquina {

    /** Cria um sensor novo, com saúde máxima. */
    public SensorDeTemperatura() {
        super("Sensor", "Descalibrado", "Defeituoso", 0.8f);
    }

    /** @return multiplicador entre 1 e 1,5 sobre a chance de falha da máquina */
    @Override
    public float calcularFatorDeFalha() {
        return 1.0f + (0.5f * getNivelDeDesgaste());
    }
}
