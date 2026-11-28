package br.unicamp.ic.mc322.biblioteca;

import java.nio.file.Path;

public class ListaExercicios extends MaterialBiblioteca {
    private int numeroLista;
    private boolean possuiGabarito;

    public ListaExercicios(String titulo, Path copia, int num, boolean gab) {
        super(titulo, copia);
        this.numeroLista = num;
        this.possuiGabarito = gab;
    }

    @Override
    public String getDescricaoDetalhada() {
        String s = numeroLista > 0 ? "Lista " + numeroLista + " – " + getTitulo() : getTitulo();
        return possuiGabarito ? s + " (com gabarito)" : s;
    }

    @Override
    public TipoMaterial getTipoMaterial() {
        return TipoMaterial.LISTA;
    }

    public int getNumeroLista() {
        return numeroLista;
    }

    public void setNumeroLista(int numeroLista) {
        this.numeroLista = numeroLista;
    }

    public boolean isPossuiGabarito() {
        return possuiGabarito;
    }

    public void setPossuiGabarito(boolean possuiGabarito) {
        this.possuiGabarito = possuiGabarito;
    }

    @Override
    protected Object[] camposEspecificos() {
        return new Object[]{numeroLista, possuiGabarito};
    }
}
