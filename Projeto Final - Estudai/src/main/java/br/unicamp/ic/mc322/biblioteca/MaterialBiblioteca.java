package br.unicamp.ic.mc322.biblioteca;

import estudai.excecoes.ArquivoPdfInvalidoException;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

/** Base dos materiais da biblioteca (F47 a F50). O PDF é copiado para a pasta do programa. */
public abstract class MaterialBiblioteca implements PersistivelCSV {
    private static final byte[] ASSINATURA_PDF = "%PDF".getBytes(StandardCharsets.US_ASCII);

    protected String id;
    protected String titulo;
    protected Path caminhoArquivoOriginal;
    protected Path caminhoArquivoCopia;
    protected LocalDate dataAdicao;

    public MaterialBiblioteca(String titulo, Path caminhoCopia) {
        this.id = UUID.randomUUID().toString();
        this.titulo = titulo == null || titulo.isBlank() ? caminhoCopia.getFileName().toString() : titulo.trim();
        this.caminhoArquivoCopia = caminhoCopia;
        this.dataAdicao = LocalDate.now();
    }

    /** Confere se o arquivo existe, termina em .pdf e começa com a assinatura %PDF. */
    public static void validarPdf(Path arquivo) throws ArquivoPdfInvalidoException {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            throw new ArquivoPdfInvalidoException("Arquivo não encontrado: " + arquivo);
        }
        if (!arquivo.getFileName().toString().toLowerCase().endsWith(".pdf")) {
            throw new ArquivoPdfInvalidoException("O arquivo precisa ter a extensão .pdf.");
        }
        try (InputStream in = Files.newInputStream(arquivo)) {
            byte[] inicio = in.readNBytes(ASSINATURA_PDF.length);
            if (!Arrays.equals(inicio, ASSINATURA_PDF)) {
                throw new ArquivoPdfInvalidoException("O arquivo " + arquivo.getFileName() + " não é um PDF válido.");
            }
        } catch (IOException e) {
            throw new ArquivoPdfInvalidoException("Não foi possível ler " + arquivo.getFileName() + ".");
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
            throw new ArquivoPdfInvalidoException("Não foi possível apagar a cópia de " + titulo + ".");
        }
    }

    public abstract String getDescricaoDetalhada();

    public abstract String getTipoMaterial();

    protected abstract Object[] camposEspecificos();

    @Override
    public final String toCSV() {
        Object[] base = {id, getTipoMaterial(), titulo, caminhoArquivoCopia, caminhoArquivoOriginal, dataAdicao};
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

    public Path getCaminhoArquivoCopia() {
        return caminhoArquivoCopia;
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
