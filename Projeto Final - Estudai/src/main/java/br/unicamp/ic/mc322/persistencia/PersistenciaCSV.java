package br.unicamp.ic.mc322.persistencia;

import br.unicamp.ic.mc322.atividades.Atividade;
import br.unicamp.ic.mc322.atividades.AtividadeAvaliativa;
import br.unicamp.ic.mc322.atividades.ItemChecklist;
import br.unicamp.ic.mc322.atividades.Prova;
import br.unicamp.ic.mc322.biblioteca.MaterialBiblioteca;
import br.unicamp.ic.mc322.biblioteca.PastaBiblioteca;
import br.unicamp.ic.mc322.exceptions.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exceptions.EstudaiException;
import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;
import br.unicamp.ic.mc322.medias.MediaPersonalizada;
import br.unicamp.ic.mc322.model.Bonus;
import br.unicamp.ic.mc322.model.Configuracao;
import br.unicamp.ic.mc322.model.DiaSemana;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.HorarioAula;
import br.unicamp.ic.mc322.model.RegistroFalta;
import br.unicamp.ic.mc322.model.Semestre;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Lê e grava os dados em arquivos CSV, um por tipo de entidade (F38 a F40).
 * Linhas com problema são puladas e registradas em getAvisos(): o programa carrega o que conseguir.
 */
public class PersistenciaCSV {
    static final String CONFIG = "config.csv";
    static final String SEMESTRES = "semestres.csv";
    static final String DISCIPLINAS = "disciplinas.csv";
    static final String REGRAS = "regras.csv";
    static final String HORARIOS = "horarios.csv";
    static final String FALTAS = "faltas.csv";
    static final String ATIVIDADES = "atividades.csv";
    static final String CHECKLIST = "checklist.csv";
    static final String BONUS = "bonus.csv";
    static final String LIDAS = "notificacoes_lidas.csv";
    static final String BIBLIOTECA = "biblioteca.csv";
    static final String PASTA_PDFS = "biblioteca";

    private static final List<String> ARQUIVOS = List.of(CONFIG, SEMESTRES, DISCIPLINAS, REGRAS, HORARIOS, FALTAS,
            ATIVIDADES, CHECKLIST, BONUS, LIDAS, BIBLIOTECA);

    private final Path diretorioBase;
    private final FabricaEntidadesCSV fabrica = new FabricaEntidadesCSV();
    private final List<String> avisos = new ArrayList<>();

    public PersistenciaCSV(Path diretorioBase) {
        this.diretorioBase = diretorioBase;
    }

    /** F40: cria a pasta de dados e os arquivos vazios na primeira execução. */
    public void criarPastaEArquivosIniciais() {
        try {
            Files.createDirectories(getDiretorioBiblioteca());
            for (String nome : ARQUIVOS) {
                Path f = diretorioBase.resolve(nome);
                if (!Files.exists(f)) {
                    Files.createFile(f);
                }
            }
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Não foi possível criar a pasta de dados em " + diretorioBase, e);
        }
    }

    public Path getDiretorioBase() {
        return diretorioBase;
    }

    public Path getDiretorioBiblioteca() {
        return diretorioBase.resolve(PASTA_PDFS);
    }

    public List<String> getAvisos() {
        return Collections.unmodifiableList(avisos);
    }

    public void limparAvisos() {
        avisos.clear();
    }

    // =================================================================== gravação

    /** F39 */
    public void salvarTudo(List<Semestre> semestres, Configuracao cfg, Set<String> lidas, PastaBiblioteca raiz)
            throws ArquivoCorrompidoException {
        List<String> lSem = new ArrayList<>();
        List<String> lDisc = new ArrayList<>();
        List<String> lRegra = new ArrayList<>();
        List<String> lHor = new ArrayList<>();
        List<String> lFal = new ArrayList<>();
        List<String> lAtiv = new ArrayList<>();
        List<String> lChk = new ArrayList<>();
        List<String> lBon = new ArrayList<>();
        for (Semestre s : semestres) {
            lSem.add(s.toCSV());
            for (Disciplina d : s.getDisciplinas()) {
                String chave = Csv.juntar(s.getId(), d.getCodigo());
                lDisc.add(Csv.juntar(s.getId()) + Csv.SEP + d.toCSV());
                lRegra.add(chave + Csv.SEP + fabrica.regraParaCSV(d.getRegraMedia()));
                for (HorarioAula h : d.getHorarios()) {
                    lHor.add(chave + Csv.SEP + h.toCSV());
                }
                for (RegistroFalta f : d.getFaltas()) {
                    lFal.add(chave + Csv.SEP + f.toCSV());
                }
                for (Atividade a : d.getAtividades()) {
                    lAtiv.add(chave + Csv.SEP + a.toCSV());
                    if (a instanceof AtividadeAvaliativa av) {
                        for (ItemChecklist item : av.getChecklist()) {
                            lChk.add(Csv.juntar(a.getId()) + Csv.SEP + item.toCSV());
                        }
                    }
                }
                for (Bonus b : d.getBonus()) {
                    lBon.add(chave + Csv.SEP + b.toCSV());
                }
            }
        }
        List<String> lBib = new ArrayList<>();
        coletarMateriais(raiz, lBib);

        gravar(CONFIG, List.of(cfg.toCSV()));
        gravar(SEMESTRES, lSem);
        gravar(DISCIPLINAS, lDisc);
        gravar(REGRAS, lRegra);
        gravar(HORARIOS, lHor);
        gravar(FALTAS, lFal);
        gravar(ATIVIDADES, lAtiv);
        gravar(CHECKLIST, lChk);
        gravar(BONUS, lBon);
        List<String> lLidas = new ArrayList<>();
        for (String k : lidas) {
            lLidas.add(Csv.juntar(k));
        }
        gravar(LIDAS, lLidas);
        gravar(BIBLIOTECA, lBib);
    }

    private void coletarMateriais(PastaBiblioteca pasta, List<String> linhas) {
        if (pasta == null) {
            return;
        }
        for (MaterialBiblioteca m : pasta.getMateriais()) {
            linhas.add(Csv.juntar(pasta.getCaminhoLogico()) + Csv.SEP + m.toCSV());
        }
        for (PastaBiblioteca sub : pasta.getSubpastas()) {
            coletarMateriais(sub, linhas);
        }
    }

    /** Grava num arquivo temporário e depois troca: se algo falhar, o arquivo antigo continua inteiro. */
    private void gravar(String nome, List<String> linhas) throws ArquivoCorrompidoException {
        Path destino = diretorioBase.resolve(nome);
        Path temp = diretorioBase.resolve(nome + ".tmp");
        try {
            Files.createDirectories(diretorioBase);
            Files.write(temp, linhas, StandardCharsets.UTF_8);
            Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ArquivoCorrompidoException("Não foi possível gravar " + nome + ": " + e.getMessage());
        }
    }

    // =================================================================== leitura

    /** Uma linha lida, com o número dela no arquivo (para os avisos). */
    private record Linha(int numero, String[] campos, String bruta) {
    }

    private List<Linha> ler(String nome) throws ArquivoCorrompidoException {
        Path f = diretorioBase.resolve(nome);
        List<Linha> linhas = new ArrayList<>();
        if (!Files.exists(f)) {
            return linhas;
        }
        try {
            List<String> todas = Files.readAllLines(f, StandardCharsets.UTF_8);
            for (int i = 0; i < todas.size(); i++) {
                if (!todas.get(i).isBlank()) {
                    linhas.add(new Linha(i + 1, Csv.separar(todas.get(i)), todas.get(i)));
                }
            }
        } catch (IOException e) {
            throw new ArquivoCorrompidoException("Não foi possível ler " + nome + ": " + e.getMessage());
        }
        return linhas;
    }

    private void avisar(String arquivo, Linha l, String motivo) {
        avisos.add(arquivo + ", linha " + l.numero() + ": " + motivo);
    }

    /** Remove os n primeiros campos (as chaves do "pai") e devolve o resto como linha CSV. */
    private static String resto(Linha l, int n) {
        String[] r = new String[l.campos().length - n];
        System.arraycopy(l.campos(), n, r, 0, r.length);
        return Csv.juntar((Object[]) r);
    }

    public Configuracao carregarConfiguracao() throws ArquivoCorrompidoException {
        Configuracao cfg = new Configuracao();
        cfg.setDiretorioDados(diretorioBase);
        for (Linha l : ler(CONFIG)) {
            try {
                String[] c = l.campos();
                cfg.restaurar(Integer.parseInt(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2]),
                        c.length > 3 ? c[3] : null);
            } catch (RuntimeException e) {
                avisar(CONFIG, l, "configuração inválida, usando os valores padrão");
            }
            break;
        }
        return cfg;
    }

    public Set<String> carregarNotificacoesLidas() throws ArquivoCorrompidoException {
        Set<String> lidas = new LinkedHashSet<>();
        for (Linha l : ler(LIDAS)) {
            lidas.add(l.campos()[0]);
        }
        return lidas;
    }

    /** F38: recria semestres, disciplinas e tudo o que pertence a elas. */
    public List<Semestre> carregarSemestres() throws ArquivoCorrompidoException {
        Map<String, Semestre> semestres = new LinkedHashMap<>();
        for (Linha l : ler(SEMESTRES)) {
            try {
                String[] c = l.campos();
                Semestre s = new Semestre(Integer.parseInt(c[1]), Integer.parseInt(c[2]), Csv.data(c[3]),
                        Csv.data(c[4]), Csv.data(c[5]));
                s.restaurarId(c[0]);
                semestres.put(c[0], s);
            } catch (EstudaiException | RuntimeException e) {
                avisar(SEMESTRES, l, "semestre inválido (" + e.getMessage() + ")");
            }
        }

        // As disciplinas são montadas por completo antes de entrar no semestre,
        // para que validações de prazo e de conflito não descartem dados antigos.
        Map<String, Disciplina> disciplinas = new LinkedHashMap<>();
        Map<String, String> semestreDe = new HashMap<>();
        for (Linha l : ler(DISCIPLINAS)) {
            try {
                String[] c = l.campos();
                if (!semestres.containsKey(c[0])) {
                    avisar(DISCIPLINAS, l, "semestre inexistente");
                    continue;
                }
                Disciplina d = new Disciplina(c[1], c[2], c[3], Integer.parseInt(c[4]));
                d.restaurarEstado(Boolean.parseBoolean(c[5]), Csv.numeroOuNulo(c[6]));
                String chave = Csv.juntar(c[0], d.getCodigo());
                disciplinas.put(chave, d);
                semestreDe.put(chave, c[0]);
            } catch (EstudaiException | RuntimeException e) {
                avisar(DISCIPLINAS, l, "disciplina inválida (" + e.getMessage() + ")");
            }
        }

        for (Linha l : ler(REGRAS)) {
            Disciplina d = disciplinaDa(l, disciplinas, REGRAS);
            if (d != null) {
                try {
                    d.restaurarRegraMedia(fabrica.criarRegraMediaDeCSV(resto(l, 2)));
                } catch (ArquivoCorrompidoException e) {
                    avisar(REGRAS, l, e.getMessage());
                }
            }
        }

        for (Linha l : ler(HORARIOS)) {
            Disciplina d = disciplinaDa(l, disciplinas, HORARIOS);
            if (d != null) {
                try {
                    String[] c = l.campos();
                    HorarioAula h = new HorarioAula(DiaSemana.valueOf(c[3]), Csv.hora(c[4]), Csv.hora(c[5]), c[6]);
                    h.restaurarId(c[2]);
                    d.adicionarHorario(h);
                } catch (EstudaiException | RuntimeException e) {
                    avisar(HORARIOS, l, "horário inválido (" + e.getMessage() + ")");
                }
            }
        }

        for (Linha l : ler(FALTAS)) {
            Disciplina d = disciplinaDa(l, disciplinas, FALTAS);
            if (d != null) {
                try {
                    String[] c = l.campos();
                    RegistroFalta f = new RegistroFalta(Csv.data(c[3]), Integer.parseInt(c[4]));
                    f.restaurarId(c[2]);
                    d.adicionarFalta(f);
                } catch (EstudaiException | RuntimeException e) {
                    avisar(FALTAS, l, "falta inválida (" + e.getMessage() + ")");
                }
            }
        }

        Map<String, Atividade> atividades = new HashMap<>();
        // LinkedHashMap: as substitutivas são religadas na ordem do arquivo, sempre do mesmo jeito.
        Map<Prova, String> substitutas = new LinkedHashMap<>();
        for (Linha l : ler(ATIVIDADES)) {
            Disciplina d = disciplinaDa(l, disciplinas, ATIVIDADES);
            if (d == null) {
                continue;
            }
            try {
                String linhaAtividade = resto(l, 2);
                Atividade a = fabrica.criarAtividadeDeCSV(linhaAtividade, d);
                String siglaSub = fabrica.siglaSubstituidaDeCSV(linhaAtividade);
                if (a instanceof Prova p && !siglaSub.isBlank()) {
                    // A substitutiva não tem peso próprio. Zerar antes de adicionar evita que ela conte
                    // na soma dos pesos (arquivos antigos podem ter um peso gravado para ela).
                    p.setPeso(0);
                    substitutas.put(p, siglaSub);
                }
                d.adicionarAtividade(a);
                atividades.put(a.getId(), a);
            } catch (EstudaiException | RuntimeException e) {
                avisar(ATIVIDADES, l, e.getMessage());
            }
        }
        for (Map.Entry<Prova, String> e : substitutas.entrySet()) {
            Prova sub = e.getKey();
            AtividadeAvaliativa alvo = sub.getDisciplina().buscarAvaliacao(e.getValue());
            if (!(alvo instanceof Prova p) || p == sub) {
                avisos.add(ATIVIDADES + ": a substitutiva " + sub.getSigla() + " aponta para a prova "
                        + e.getValue() + ", que não existe; ela foi mantida como prova comum.");
                continue;
            }
            try {
                sub.tornarSubstitutiva(p);
            } catch (IllegalArgumentException ex) {
                avisos.add(ATIVIDADES + ": " + ex.getMessage() + " A " + sub.getSigla()
                        + " foi mantida como prova comum.");
            }
        }

        for (Linha l : ler(CHECKLIST)) {
            Atividade a = atividades.get(l.campos()[0]);
            if (!(a instanceof AtividadeAvaliativa av)) {
                avisar(CHECKLIST, l, "atividade inexistente");
                continue;
            }
            try {
                ItemChecklist item = new ItemChecklist(l.campos()[1]);
                if (Boolean.parseBoolean(l.campos()[2])) {
                    item.marcar();
                }
                av.adicionarItem(item);
            } catch (EstudaiException | RuntimeException e) {
                avisar(CHECKLIST, l, "item inválido (" + e.getMessage() + ")");
            }
        }

        for (Linha l : ler(BONUS)) {
            Disciplina d = disciplinaDa(l, disciplinas, BONUS);
            if (d == null) {
                continue;
            }
            try {
                String[] c = l.campos();
                double valor = Double.parseDouble(c[2]);
                AtividadeAvaliativa alvo = c.length > 3 && !c[3].isBlank() ? d.buscarAvaliacao(c[3]) : null;
                if (c.length > 3 && !c[3].isBlank() && alvo == null) {
                    avisar(BONUS, l, "avaliação " + c[3] + " inexistente");
                    continue;
                }
                d.adicionarBonus(new Bonus(valor, alvo));
            } catch (EstudaiException | RuntimeException e) {
                avisar(BONUS, l, "bônus inválido (" + e.getMessage() + ")");
            }
        }

        // Com as atividades carregadas, confere se as fórmulas personalizadas usam siglas que existem.
        // A fórmula é mantida mesmo assim (para o usuário corrigir), mas fica o aviso.
        for (Disciplina d : disciplinas.values()) {
            if (d.getRegraMedia() instanceof MediaPersonalizada mp) {
                try {
                    mp.validarRegra(d.getSiglasAvaliacoes());
                } catch (FormulaInvalidaException ex) {
                    avisos.add(REGRAS + ": fórmula de " + d.getCodigo() + " – " + ex.getMessage());
                }
            }
        }

        for (Map.Entry<String, Disciplina> e : disciplinas.entrySet()) {
            try {
                semestres.get(semestreDe.get(e.getKey())).adicionarDisciplina(e.getValue());
            } catch (EstudaiException ex) {
                avisos.add(DISCIPLINAS + ": " + ex.getMessage());
            }
        }
        return new ArrayList<>(semestres.values());
    }

    private Disciplina disciplinaDa(Linha l, Map<String, Disciplina> disciplinas, String arquivo) {
        if (l.campos().length < 2) {
            avisar(arquivo, l, "linha incompleta");
            return null;
        }
        Disciplina d = disciplinas.get(Csv.juntar(l.campos()[0], l.campos()[1]));
        if (d == null) {
            avisar(arquivo, l, "disciplina inexistente");
        }
        return d;
    }

    /**
     * Recria a árvore de pastas a partir dos diretórios no disco (assim pastas vazias também voltam)
     * e põe cada material na pasta registrada em biblioteca.csv.
     */
    public PastaBiblioteca carregarBiblioteca() throws ArquivoCorrompidoException {
        Path dir = getDiretorioBiblioteca();
        PastaBiblioteca raiz = new PastaBiblioteca("Biblioteca", dir, null);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> caminhos = Files.walk(dir)) {
                for (Path p : caminhos.filter(Files::isDirectory).sorted().toList()) {
                    if (!p.equals(dir)) {
                        raiz.resolverCaminho(dir.relativize(p).toString().replace('\\', '/'));
                    }
                }
            } catch (IOException e) {
                throw new ArquivoCorrompidoException("Não foi possível ler a pasta da biblioteca: " + e.getMessage());
            }
        }
        for (Linha l : ler(BIBLIOTECA)) {
            try {
                MaterialBiblioteca m = fabrica.criarMaterialDeCSV(resto(l, 1));
                raiz.resolverCaminho(l.campos()[0]).adicionarMaterial(m);
            } catch (ArquivoCorrompidoException e) {
                avisar(BIBLIOTECA, l, e.getMessage());
            }
        }
        return raiz;
    }
}
