package br.unicamp.ic.mc322.biblioteca;

import java.nio.file.Path;

public class Apostila extends MaterialBiblioteca {
    private String topicoReferencia;

    public Apostila(String titulo, Path copia, String topico) {
        super(titulo, copia);
        this.topicoReferencia = topico == null ? "" : topico.trim();
    }

    @Override
    public String getDescricaoDetalhada() {
        return topicoReferencia.isEmpty() ? getTitulo() : getTitulo() + " – " + topicoReferencia;
    }

    @Override
    public TipoMaterial getTipoMaterial() {
        return TipoMaterial.APOSTILA;
    }

    public String getTopicoReferencia() {
        return topicoReferencia;
    }

    public void setTopicoReferencia(String topico) {
        this.topicoReferencia = topico == null ? "" : topico.trim();
    }

    @Override
    protected Object[] camposEspecificos() {
        return new Object[]{topicoReferencia};
    }
}
