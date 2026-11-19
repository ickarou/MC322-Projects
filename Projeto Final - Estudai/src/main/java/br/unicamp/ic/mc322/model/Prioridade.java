package br.unicamp.ic.mc322.model;

/** Prioridade de uma atividade. O peso é usado na ordenação por prioridade. */
public enum Prioridade {
    BAIXA(1), MEDIA(2), ALTA(3);

    private final int peso;

    Prioridade(int peso) {
        this.peso = peso;
    }

    public int getPeso() {
        return peso;
    }
}
