package br.unicamp.ic.mc322.biblioteca;

import estudai.excecoes.ArquivoPdfInvalidoException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Pasta da biblioteca (F46 a F50). A biblioteca é única para todos os semestres e se organiza
 * em pastas semestre → disciplina, espelhadas em diretórios no disco.
 */
public class PastaBiblioteca {
    private final String nome;
    private final Path caminhoAtual;
    private final PastaBiblioteca pastaPai;
    private final List<PastaBiblioteca> subpastas = new ArrayList<>();
    private final List<MaterialBiblioteca> materiais = new ArrayList<>();

    public PastaBiblioteca(String nome, Path caminho, PastaBiblioteca pai) {
        this.nome = nome;
        this.caminhoAtual = caminho;
        this.pastaPai = pai;
    }

    public PastaBiblioteca abrirSubpasta(String nome) {
        String procurado = nomeSeguro(nome);
        for (PastaBiblioteca p : subpastas) {
            if (p.nome.equalsIgnoreCase(procurado)) {
                return p;
            }
        }
        return null;
    }

    /** Na raiz, continua na raiz. */
    public PastaBiblioteca voltarPastaPai() {
        return pastaPai == null ? this : pastaPai;
    }

    /** Cria (ou devolve, se já existe) uma subpasta, também no disco. */
    public PastaBiblioteca criarSubpasta(String nome) {
        PastaBiblioteca existente = abrirSubpasta(nome);
        if (existente != null) {
            return existente;
        }
        String seguro = nomeSeguro(nome);
        Path dir = caminhoAtual.resolve(seguro);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta " + dir, e);
        }
        PastaBiblioteca p = new PastaBiblioteca(seguro, dir, this);
        subpastas.add(p);
        return p;
    }

    /** Remove a subpasta e apaga do disco os PDFs copiados para ela. */
    public void removerSubpasta(PastaBiblioteca p) {
        if (!subpastas.remove(p)) {
            return;
        }
        if (Files.exists(p.caminhoAtual)) {
            try (Stream<Path> arquivos = Files.walk(p.caminhoAtual)) {
                for (Path f : arquivos.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(f);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Não foi possível apagar a pasta " + p.caminhoAtual, e);
            }
        }
    }

    /**
     * F47: valida o PDF, copia para esta pasta e cria o material do tipo pedido
     * (LIVRO, APOSTILA ou LISTA). Detalhes de cada tipo podem ser preenchidos depois.
     */
    public MaterialBiblioteca adicionarMaterial(Path pdf, String tipo, String titulo) throws ArquivoPdfInvalidoException {
        MaterialBiblioteca.validarPdf(pdf);
        Path destino = destinoLivre(nomeSeguro(pdf.getFileName().toString()));
        try {
            Files.createDirectories(caminhoAtual);
            Files.copy(pdf, destino, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new ArquivoPdfInvalidoException("Não foi possível copiar " + pdf.getFileName() + " para a biblioteca.");
        }
        String t = tipo == null ? "" : tipo.trim().toUpperCase();
        MaterialBiblioteca m = switch (t) {
            case "LIVRO" -> new Livro(titulo, destino, "", 0);
            case "APOSTILA" -> new Apostila(titulo, destino, "");
            case "LISTA" -> new ListaExercicios(titulo, destino, 0, false);
            default -> {
                try {
                    Files.deleteIfExists(destino);
                } catch (IOException ignorada) {
                    // a cópia órfã não impede o aviso ao usuário
                }
                throw new ArquivoPdfInvalidoException("Tipo de material desconhecido: " + tipo + ".");
            }
        };
        m.setCaminhoArquivoOriginal(pdf);
        materiais.add(m);
        return m;
    }

    /** Usado pela persistência. */
    public void adicionarMaterial(MaterialBiblioteca m) {
        materiais.add(m);
    }

    /** F50 */
    public void removerMaterial(MaterialBiblioteca m) throws ArquivoPdfInvalidoException {
        if (materiais.remove(m)) {
            m.apagarCopiaDisco();
        }
    }

    /** F48: filtro por tipo (LIVRO, APOSTILA, LISTA); vazio ou null lista tudo. */
    public List<MaterialBiblioteca> listarMateriais(String filtroTipo) {
        List<MaterialBiblioteca> lista = new ArrayList<>();
        for (MaterialBiblioteca m : materiais) {
            if (filtroTipo == null || filtroTipo.isBlank() || m.getTipoMaterial().equalsIgnoreCase(filtroTipo)) {
                lista.add(m);
            }
        }
        return lista;
    }

    /** Caminho "lógico" a partir da raiz, ex.: "2026 – 4º semestre/MC322". A raiz é "". */
    public String getCaminhoLogico() {
        if (pastaPai == null) {
            return "";
        }
        String pai = pastaPai.getCaminhoLogico();
        return pai.isEmpty() ? nome : pai + "/" + nome;
    }

    /** Encontra (criando se preciso) a pasta de um caminho lógico. */
    public PastaBiblioteca resolverCaminho(String caminhoLogico) {
        PastaBiblioteca p = this;
        if (caminhoLogico == null || caminhoLogico.isBlank()) {
            return p;
        }
        for (String parte : caminhoLogico.split("/")) {
            p = p.criarSubpasta(parte);
        }
        return p;
    }

    private Path destinoLivre(String nomeArquivo) {
        Path destino = caminhoAtual.resolve(nomeArquivo);
        String base = nomeArquivo.replaceFirst("(?i)\\.pdf$", "");
        int n = 2;
        while (Files.exists(destino)) {
            destino = caminhoAtual.resolve(base + " (" + n++ + ").pdf");
        }
        return destino;
    }

    /**
     * Nome que funciona como diretório em qualquer sistema: sem acentos, sem caracteres
     * proibidos no Windows e só com ASCII. Ex.: "2026 – 4º semestre" vira "2026 - 4o semestre".
     */
    static String nomeSeguro(String nome) {
        String s = nome.replace('–', '-').replace('—', '-').replace('º', 'o').replace('ª', 'a');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replaceAll("[\\\\/:*?\"<>|]", "-").replaceAll("[^\\x20-\\x7E]", "");
        return s.trim();
    }

    public String getNome() {
        return nome;
    }

    public Path getCaminhoAtual() {
        return caminhoAtual;
    }

    public PastaBiblioteca getPastaPai() {
        return pastaPai;
    }

    public List<PastaBiblioteca> getSubpastas() {
        return Collections.unmodifiableList(subpastas);
    }

    public List<MaterialBiblioteca> getMateriais() {
        return Collections.unmodifiableList(materiais);
    }

    @Override
    public String toString() {
        return nome;
    }
}
