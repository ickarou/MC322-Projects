package br.unicamp.ic.mc322;

import br.unicamp.ic.mc322.atividades.Atividade;
import br.unicamp.ic.mc322.biblioteca.PastaBiblioteca;
import br.unicamp.ic.mc322.exceptions.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Configuracao;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Semestre;
import br.unicamp.ic.mc322.model.StatusAtividade;
import br.unicamp.ic.mc322.notificacoes.Notificacao;
import br.unicamp.ic.mc322.ordenacao.EstrategiaOrdenacaoAtividade;
import br.unicamp.ic.mc322.ordenacao.OrdenacaoPorPrazo;
import br.unicamp.ic.mc322.persistencia.PersistenciaCSV;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Fachada do sistema: guarda os semestres, o semestre ativo, a biblioteca, as configurações
 * e as notificações. É por aqui que a interface gráfica conversa com o modelo.
 */
public class EstudaiSistema {
    private final List<Semestre> semestres = new ArrayList<>();
    private Semestre semestreAtivo;
    private Configuracao configuracao = new Configuracao();
    private PastaBiblioteca bibliotecaRaiz;
    private final List<Notificacao> notificacoes = new ArrayList<>();
    private final Set<String> chavesNotificacoesLidas = new LinkedHashSet<>();
    private EstrategiaOrdenacaoAtividade ordenadorAtividades = new OrdenacaoPorPrazo();
    private final PersistenciaCSV persistencia;

    public EstudaiSistema(PersistenciaCSV persistencia) {
        this.persistencia = persistencia;
        this.bibliotecaRaiz = new PastaBiblioteca("Biblioteca", persistencia.getDiretorioBiblioteca(), null);
    }

    // =================================================================== arquivos

    /**
     * F38: carrega tudo. Se algumas linhas estavam com problema, os dados válidos ficam carregados
     * e a exceção lista o que foi pulado, para a interface avisar o usuário.
     */
    public void carregarDados() throws ArquivoCorrompidoException {
        persistencia.limparAvisos();
        persistencia.criarPastaEArquivosIniciais();
        configuracao = persistencia.carregarConfiguracao();
        semestres.clear();
        semestres.addAll(persistencia.carregarSemestres());
        chavesNotificacoesLidas.clear();
        chavesNotificacoesLidas.addAll(persistencia.carregarNotificacoesLidas());
        bibliotecaRaiz = persistencia.carregarBiblioteca();
        for (Semestre s : semestres) {
            for (Disciplina d : s.getDisciplinas()) {
                d.setPastaDisciplina(pastaDa(s, d));
            }
        }
        semestreAtivo = null;
        for (Semestre s : semestres) {
            if (s.getId().equals(configuracao.getIdSemestreAtivo())) {
                semestreAtivo = s;
            }
        }
        if (semestres.isEmpty()) {
            criarSemestreInicialEmBranco();
        } else if (semestreAtivo == null) {
            semestreAtivo = semestres.get(semestres.size() - 1);
        }
        atualizarNotificacoes();
        if (!persistencia.getAvisos().isEmpty()) {
            throw new ArquivoCorrompidoException("Algumas linhas dos arquivos de dados foram ignoradas:\n- "
                    + String.join("\n- ", persistencia.getAvisos()));
        }
    }

    /** F39 */
    public void salvarDados() throws ArquivoCorrompidoException {
        configuracao.setIdSemestreAtivo(semestreAtivo == null ? null : semestreAtivo.getId());
        persistencia.salvarTudo(semestres, configuracao, chavesNotificacoesLidas, bibliotecaRaiz);
    }

    // =================================================================== semestres

    /** F1 */
    public Semestre cadastrarSemestre(int ano, int num, LocalDate ini, LocalDate fim, LocalDate lim)
            throws PrazoInvalidoException, DadoInvalidoException {
        verificarDuplicado(ano, num, null);
        Semestre s = new Semestre(ano, num, ini, fim, lim);
        semestres.add(s);
        bibliotecaRaiz.criarSubpasta(s.getRotulo());
        if (semestreAtivo == null) {
            semestreAtivo = s;
        }
        return s;
    }

    /** F2. Se ano ou número mudarem, as disciplinas passam a usar uma pasta com o novo nome. */
    public void editarSemestre(Semestre s, int ano, int num, LocalDate ini, LocalDate fim, LocalDate lim)
            throws PrazoInvalidoException, DadoInvalidoException {
        verificarDuplicado(ano, num, s);
        String rotuloAntigo = s.getRotulo();
        s.editar(ano, num, ini, fim, lim);
        if (!rotuloAntigo.equals(s.getRotulo())) {
            for (Disciplina d : s.getDisciplinas()) {
                d.setPastaDisciplina(pastaDa(s, d));
            }
        }
    }

    private void verificarDuplicado(int ano, int num, Semestre ignorar) throws DadoInvalidoException {
        for (Semestre s : semestres) {
            if (s != ignorar && s.getAno() == ano && s.getNumeroSemestre() == num) {
                throw new DadoInvalidoException("Já existe o semestre " + s.getRotulo() + ".");
            }
        }
    }

    /** F3: remove o semestre, as disciplinas dele e a pasta da biblioteca. Sempre sobra um semestre ativo. */
    public void removerSemestre(Semestre s) {
        if (!semestres.remove(s)) {
            return;
        }
        PastaBiblioteca pasta = bibliotecaRaiz.abrirSubpasta(s.getRotulo());
        if (pasta != null) {
            bibliotecaRaiz.removerSubpasta(pasta);
        }
        if (semestreAtivo == s) {
            semestreAtivo = semestres.isEmpty() ? null : semestres.get(semestres.size() - 1);
        }
        if (semestres.isEmpty()) {
            criarSemestreInicialEmBranco();
        }
        atualizarNotificacoes();
    }

    /** F4 */
    public void alternarSemestreAtivo(Semestre s) {
        if (!semestres.contains(s)) {
            throw new IllegalArgumentException("Semestre desconhecido.");
        }
        semestreAtivo = s;
        atualizarNotificacoes();
    }

    /** F5: semestre "em branco" com valores padrão; o usuário completa depois pela F2. */
    public Semestre criarSemestreInicialEmBranco() {
        LocalDate hoje = LocalDate.now();
        int num = 1;
        while (existe(hoje.getYear(), num)) {
            num++;
        }
        try {
            Semestre s = new Semestre(hoje.getYear(), num, hoje, hoje.plusMonths(4), null);
            semestres.add(s);
            bibliotecaRaiz.criarSubpasta(s.getRotulo());
            semestreAtivo = s;
            return s;
        } catch (PrazoInvalidoException e) {
            throw new IllegalStateException("Valores padrão do semestre em branco inválidos", e);
        }
    }

    private boolean existe(int ano, int num) {
        return semestres.stream().anyMatch(s -> s.getAno() == ano && s.getNumeroSemestre() == num);
    }

    // =================================================================== disciplinas

    /** F6, criando também a pasta da disciplina na biblioteca. */
    public Disciplina cadastrarDisciplina(Semestre s, String cod, String nome, String prof, int cred)
            throws DadoInvalidoException {
        Disciplina d = s.cadastrarDisciplina(cod, nome, prof, cred);
        d.setPastaDisciplina(pastaDa(s, d));
        return d;
    }

    /** F8: remove a disciplina com tudo o que é dela, inclusive os PDFs. */
    public void removerDisciplina(Semestre s, Disciplina d) {
        s.removerDisciplina(d);
        PastaBiblioteca pasta = d.getPastaDisciplina();
        if (pasta != null && pasta.getPastaPai() != null) {
            pasta.getPastaPai().removerSubpasta(pasta);
        }
        atualizarNotificacoes();
    }

    private PastaBiblioteca pastaDa(Semestre s, Disciplina d) {
        return bibliotecaRaiz.criarSubpasta(s.getRotulo()).criarSubpasta(d.getCodigo());
    }

    // =================================================================== atividades

    /** Todas as atividades do semestre ativo, com status atualizado pela data e já ordenadas (F19 a F21). */
    public List<Atividade> listarAtividadesSemestreAtivo(boolean incluirTrancadas) {
        List<Atividade> lista = new ArrayList<>();
        if (semestreAtivo == null) {
            return lista;
        }
        LocalDate hoje = LocalDate.now();
        for (Disciplina d : semestreAtivo.getDisciplinas()) {
            if (d.isTrancada() && !incluirTrancadas) {
                continue;
            }
            for (Atividade a : d.getAtividades()) {
                a.verificarAtraso(hoje);
                lista.add(a);
            }
        }
        return ordenadorAtividades.ordenar(lista);
    }

    /** F22: filtros opcionais (null = não filtra). A busca procura no título, sem diferenciar maiúsculas. */
    public List<Atividade> filtrarAtividades(Disciplina disc, Class<? extends Atividade> tipo, StatusAtividade st,
                                             String busca, boolean incluirTrancadas) {
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        List<Atividade> resultado = new ArrayList<>();
        for (Atividade a : listarAtividadesSemestreAtivo(incluirTrancadas)) {
            if (disc != null && a.getDisciplina() != disc) {
                continue;
            }
            if (tipo != null && !tipo.isInstance(a)) {
                continue;
            }
            if (st != null && a.getStatus() != st) {
                continue;
            }
            if (!termo.isEmpty() && !a.getTitulo().toLowerCase(Locale.ROOT).contains(termo)) {
                continue;
            }
            resultado.add(a);
        }
        return resultado;
    }

    public void setOrdenadorAtividades(EstrategiaOrdenacaoAtividade est) {
        if (est == null) {
            throw new IllegalArgumentException("A estratégia de ordenação não pode ser nula.");
        }
        this.ordenadorAtividades = est;
    }

    public EstrategiaOrdenacaoAtividade getOrdenadorAtividades() {
        return ordenadorAtividades;
    }

    // =================================================================== notificações

    /** F42: gera de novo as notificações do semestre ativo, mantendo marcadas as já lidas. */
    public void atualizarNotificacoes() {
        atualizarNotificacoes(LocalDate.now(), LocalTime.now());
    }

    public void atualizarNotificacoes(LocalDate hoje, LocalTime agora) {
        Map<String, Notificacao> unicas = new LinkedHashMap<>();
        if (semestreAtivo != null) {
            for (Disciplina d : semestreAtivo.getDisciplinas()) {
                for (Notificacao n : d.gerarNotificacoes(hoje, agora, configuracao.getDiasAntecedenciaAviso())) {
                    if (chavesNotificacoesLidas.contains(n.getChaveUnica())) {
                        n.marcarComoLida();
                    }
                    unicas.putIfAbsent(n.getChaveUnica(), n);
                }
            }
        }
        notificacoes.clear();
        notificacoes.addAll(unicas.values());
    }

    /** F43: por urgência (mais urgente primeiro) ou por data (mais próxima primeiro). */
    public List<Notificacao> listarNotificacoes(boolean porUrgencia) {
        List<Notificacao> lista = new ArrayList<>(notificacoes);
        if (porUrgencia) {
            Collections.sort(lista);
        } else {
            lista.sort(Comparator.comparing(Notificacao::getDataGeracao));
        }
        return lista;
    }

    public int contarNotificacoesNaoLidas() {
        return (int) notificacoes.stream().filter(n -> !n.isLida()).count();
    }

    /** F44: marca como lida e grava, para não voltar a aparecer. */
    public void dispensarNotificacao(Notificacao n) throws ArquivoCorrompidoException {
        n.marcarComoLida();
        chavesNotificacoesLidas.add(n.getChaveUnica());
        salvarDados();
    }

    /** F45 */
    public void limparTodasNotificacoes() throws ArquivoCorrompidoException {
        for (Notificacao n : notificacoes) {
            n.marcarComoLida();
            chavesNotificacoesLidas.add(n.getChaveUnica());
        }
        salvarDados();
    }

    // =================================================================== acesso

    public List<Semestre> getSemestres() {
        return Collections.unmodifiableList(semestres);
    }

    public Semestre getSemestreAtivo() {
        return semestreAtivo;
    }

    public Configuracao getConfiguracao() {
        return configuracao;
    }

    public PastaBiblioteca getBibliotecaRaiz() {
        return bibliotecaRaiz;
    }

    public PersistenciaCSV getPersistencia() {
        return persistencia;
    }
}
