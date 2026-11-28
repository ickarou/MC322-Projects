package br.unicamp.ic.mc322.biblioteca;

/** Tipos de material da biblioteca. O nome de cada constante é o que vai para o CSV. */
public enum TipoMaterial {
    LIVRO("Livro"),
    APOSTILA("Apostila"),
    LISTA("Lista de exercícios");

    private final String nome;

    TipoMaterial(String nome) {
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
