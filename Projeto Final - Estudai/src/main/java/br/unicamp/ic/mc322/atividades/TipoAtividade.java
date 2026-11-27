package br.unicamp.ic.mc322.atividades;

/** Tipos de atividade. O nome de cada constante é o que vai para o CSV. */
public enum TipoAtividade {
    PROVA("Prova"),
    TRABALHO("Trabalho"),
    LEITURA("Leitura");

    private final String nome;

    TipoAtividade(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
