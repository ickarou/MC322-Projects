package br.unicamp.ic.mc322.biblioteca;

import br.unicamp.ic.mc322.exceptions.ArquivoPdfInvalidoException;

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

    /**
     * Remove a subpasta e apaga do disco os PDFs copiados para ela.
     * Apaga tudo o que conseguir. Se algum arquivo não puder ser apagado (no Windows, por exemplo,
     * um PDF aberto no leitor), a pasta sai da biblioteca mesmo assim e a exceção lista o que ficou no disco.
     */
    public void removerSubpasta(PastaBiblioteca p) throws ArquivoPdfInvalidoException {
        if (!subpastas.remove(p)) {
            return;
        }
        if (!Files.exists(p.caminhoAtual)) {
            return;
        }
        List<Path> naoApagados = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(p.caminhoAtual)) {
            for (Path f : arquivos.sorted(Comparator.reverseOrder()).toList()) {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    naoApagados.add(f);
                }
            }
        } catch (IOException e) {
            naoApagados.add(p.caminhoAtual);
        }
        // Só arquivos interessam ao usuário; as pastas que os contêm também ficam, é claro.
        naoApagados.removeIf(Files::isDirectory);
        if (!naoApagados.isEmpty() || Files.exists(p.caminhoAtual)) {
            throw new ArquivoPdfInvalidoException("Alguns arquivos não puderam ser apagados (talvez estejam abertos "
                    + "em outro programa). Feche-os e apague a pasta " + p.caminhoAtual + " manualmente."
                    + (naoApagados.isEmpty() ? "" : " Arquivos: " + naoApagados.stream()
                            .map(f -> f.getFileName().toString()).toList()));
        }
    }

    /**
     * Renomeia a subpasta "p" (no disco e na árvore), levando junto os PDFs e as subpastas dela.
     * Se já existir uma pasta com o novo nome, o conteúdo é juntado a ela. Devolve a pasta com o novo nome.
     * Diretórios antigos só são apagados se ficarem vazios, então nenhum arquivo do usuário se perde.
     */
    public PastaBiblioteca renomearSubpasta(PastaBiblioteca p, String novoNome) {
        if (!subpastas.contains(p)) {
            throw new IllegalArgumentException("A pasta " + p + " não está dentro de " + this + ".");
        }
        if (p.nome.equals(nomeSeguro(novoNome))) {
            return p;
        }
        PastaBiblioteca destino = criarSubpasta(novoNome);
        if (destino == p) {
            return p;   // só mudou maiúscula/minúscula: no Windows é a mesma pasta
        }
        try {
            p.moverConteudoPara(destino);
            apagarDiretoriosVazios(p.caminhoAtual);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível mover a pasta " + p.caminhoAtual + " para " + destino.caminhoAtual, e);
        }
        subpastas.remove(p);
        return destino;
    }

    private void moverConteudoPara(PastaBiblioteca destino) throws IOException {
        Files.createDirectories(destino.caminhoAtual);
        for (MaterialBiblioteca m : new ArrayList<>(materiais)) {
            Path antigo = m.getCaminhoArquivoCopia();
            Path novo = destino.destinoLivre(antigo.getFileName().toString());
            if (Files.exists(antigo)) {
                Files.move(antigo, novo);
            }
            m.atualizarCaminhoCopia(novo);
            materiais.remove(m);
            destino.materiais.add(m);
        }
        for (PastaBiblioteca sub : new ArrayList<>(subpastas)) {
            sub.moverConteudoPara(destino.criarSubpasta(sub.nome));
        }
        subpastas.clear();
    }

    /** Apaga o diretório e os subdiretórios que estiverem vazios, de baixo para cima. */
    private static void apagarDiretoriosVazios(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> filhos = Files.list(dir)) {
            for (Path f : filhos.toList()) {
                apagarDiretoriosVazios(f);
            }
        }
        try (Stream<Path> resto = Files.list(dir)) {
            if (resto.findAny().isEmpty()) {
                Files.delete(dir);
            }
        }
    }

    /**
     * F47: valida o PDF, copia para esta pasta e cria o material do tipo pedido.
     * Detalhes de cada tipo (autor, número da lista...) podem ser preenchidos depois pelos setters.
     */
    public MaterialBiblioteca adicionarMaterial(Path pdf, TipoMaterial tipo, String titulo)
            throws ArquivoPdfInvalidoException {
        if (tipo == null) {
            throw new ArquivoPdfInvalidoException("Escolha o tipo do material.");
        }
        MaterialBiblioteca.validarPdf(pdf);
        Path destino = destinoLivre(nomeSeguro(pdf.getFileName().toString()));
        try {
            Files.createDirectories(caminhoAtual);
            Files.copy(pdf, destino, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new ArquivoPdfInvalidoException("Não foi possível copiar " + pdf.getFileName() + " para a biblioteca.");
        }
        MaterialBiblioteca m = switch (tipo) {
            case LIVRO -> new Livro(titulo, destino, "", 0);
            case APOSTILA -> new Apostila(titulo, destino, "");
            case LISTA -> new ListaExercicios(titulo, destino, 0, false);
        };
        m.setCaminhoArquivoOriginal(pdf);
        materiais.add(m);
        return m;
    }

    /** Usado pela persistência. */
    public void adicionarMaterial(MaterialBiblioteca m) {
        materiais.add(m);
    }

    /**
     * F50. Apaga a cópia primeiro e só depois tira o material da lista: se o arquivo não puder ser
     * apagado (PDF aberto no leitor, no Windows), o material continua na biblioteca e nada fica pela metade.
     */
    public void removerMaterial(MaterialBiblioteca m) throws ArquivoPdfInvalidoException {
        if (!materiais.contains(m)) {
            return;
        }
        m.apagarCopiaDisco();
        materiais.remove(m);
    }

    /** F48: filtro por tipo; null lista tudo. */
    public List<MaterialBiblioteca> listarMateriais(TipoMaterial filtroTipo) {
        List<MaterialBiblioteca> lista = new ArrayList<>();
        for (MaterialBiblioteca m : materiais) {
            if (filtroTipo == null || m.getTipoMaterial() == filtroTipo) {
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
