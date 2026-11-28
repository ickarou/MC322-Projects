package br.unicamp.ic.mc322.biblioteca;

import br.unicamp.ic.mc322.exceptions.ArquivoPdfInvalidoException;
import br.unicamp.ic.mc322.persistencia.Csv;
import br.unicamp.ic.mc322.persistencia.PersistivelCSV;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Base dos materiais da biblioteca (F47 a F50). O PDF é copiado para a pasta do programa.
 * No CSV vai só o nome do arquivo da cópia: o caminho completo vem da pasta onde o material está,
 * então a pasta de dados pode ser movida sem perder os PDFs.
 */
public abstract class MaterialBiblioteca implements PersistivelCSV {
    private String id;
    private String titulo;
    private Path caminhoArquivoOriginal;
    private Path caminhoArquivoCopia;
    private LocalDate dataAdicao;

    protected MaterialBiblioteca(String titulo, Path caminhoCopia) {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo == null || titulo.isBlank() ? caminhoCopia.getFileName().toString() : titulo.trim();
        this.caminhoArquivoCopia = caminhoCopia;
        this.dataAdicao = LocalDate.now();
    }

    /** Confere se o arquivo existe e tem a extensão .pdf. */
    public static void validarPdf(Path arquivo) throws ArquivoPdfInvalidoException {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            throw new ArquivoPdfInvalidoException("Arquivo não encontrado: " + arquivo);
        }
        if (!arquivo.getFileName().toString().toLowerCase().endsWith(".pdf")) {
            throw new ArquivoPdfInvalidoException("O arquivo precisa ter a extensão .pdf.");
        }
    }

    /** F49: abre a cópia no leitor de PDF padrão do computador. */
    public void abrirNoLeitor() throws ArquivoPdfInvalidoException {
        if (!Files.isRegularFile(caminhoArquivoCopia)) {
            throw new ArquivoPdfInvalidoException("O arquivo de " + titulo + " foi apagado ou movido.");
        }
        try {
            Desktop.getDesktop().open(caminhoArquivoCopia.toFile());
        } catch (IOException | UnsupportedOperationException e) {
            throw new ArquivoPdfInvalidoException("Não foi possível abrir o PDF neste computador.");
        }
    }

    /** F50: apaga a cópia guardada pelo programa (o arquivo original do usuário fica intacto). */
    public void apagarCopiaDisco() throws ArquivoPdfInvalidoException {
        try {
            Files.deleteIfExists(caminhoArquivoCopia);
        } catch (IOException e) {
            throw new ArquivoPdfInvalidoException("Não foi possível apagar a cópia de " + titulo
                    + ". Se o PDF estiver aberto em outro programa, feche-o e tente de novo.");
        }
    }

    public abstract String getDescricaoDetalhada();

    public abstract TipoMaterial getTipoMaterial();

    protected abstract Object[] camposEspecificos();

    @Override
    public final String toCSV() {
        Object[] base = {id, getTipoMaterial().name(), titulo, caminhoArquivoCopia.getFileName(), caminhoArquivoOriginal,
                dataAdicao};
        Object[] extra = camposEspecificos();
        Object[] todos = new Object[base.length + extra.length];
        System.arraycopy(base, 0, todos, 0, base.length);
        System.arraycopy(extra, 0, todos, base.length, extra.length);
        return Csv.juntar(todos);
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    /** Título vazio volta a ser o nome do arquivo. */
    public void setTitulo(String titulo) {
        this.titulo = titulo == null || titulo.isBlank() ? caminhoArquivoCopia.getFileName().toString() : titulo.trim();
    }

    public Path getCaminhoArquivoCopia() {
        return caminhoArquivoCopia;
    }

    /** Usado pela PastaBiblioteca depois de mover a cópia para outra pasta. */
    void atualizarCaminhoCopia(Path novo) {
        this.caminhoArquivoCopia = novo;
    }

    public Path getCaminhoArquivoOriginal() {
        return caminhoArquivoOriginal;
    }

    public void setCaminhoArquivoOriginal(Path original) {
        this.caminhoArquivoOriginal = original;
    }

    public LocalDate getDataAdicao() {
        return dataAdicao;
    }

    /** Usado pela persistência. */
    public void restaurar(String id, LocalDate dataAdicao) {
        this.id = id;
        this.dataAdicao = dataAdicao;
    }

    @Override
    public String toString() {
        return titulo + " (" + getTipoMaterial() + ")";
    }
}