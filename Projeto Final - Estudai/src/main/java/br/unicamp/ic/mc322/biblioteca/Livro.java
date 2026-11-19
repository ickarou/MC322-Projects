package br.unicamp.ic.mc322.biblioteca;

import java.nio.file.Path;

public class Livro extends MaterialBiblioteca {
    private String autor;
    private int edicao;

    public Livro(String titulo, Path copia, String autor, int ed) {
        super(titulo, copia);
        this.autor = autor == null ? "" : autor.trim();
        this.edicao = ed;
    }

    @Override
    public String getDescricaoDetalhada() {
        String s = autor.isEmpty() ? titulo : titulo + ", de " + autor;
        return edicao > 0 ? s + " (" + edicao + "ª ed.)" : s;
    }

    @Override
    public String getTipoMaterial() {
        return "LIVRO";
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor == null ? "" : autor.trim();
    }

    public int getEdicao() {
        return edicao;
    }

    public void setEdicao(int edicao) {
        this.edicao = edicao;
    }

    @Override
    protected Object[] camposEspecificos() {
        return new Object[]{autor, edicao};
    }
}
