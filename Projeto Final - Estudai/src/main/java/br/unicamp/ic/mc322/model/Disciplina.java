package br.unicamp.ic.mc322.model;

import estudai.atividades.Atividade;
import estudai.atividades.AtividadeAvaliativa;
import estudai.atividades.Avaliavel;
import estudai.atividades.Prova;
import estudai.biblioteca.PastaBiblioteca;
import estudai.excecoes.AtividadeNaoEncontradaException;
import estudai.excecoes.ConflitoDeHorarioException;
import estudai.excecoes.DadoInvalidoException;
import estudai.excecoes.FormulaInvalidaException;
import estudai.excecoes.NotaInvalidaException;
import estudai.excecoes.PrazoInvalidoException;
import estudai.medias.MediaAritmetica;
import estudai.medias.NotaSimulada;
import estudai.medias.RegraMedia;
import estudai.notificacoes.GeradorDeNotificacoes;
import estudai.notificacoes.Notificacao;
import estudai.notificacoes.NotificacaoProximaAula;
import estudai.notificacoes.NotificacaoRiscoFaltas;
import estudai.notificacoes.NotificacaoSugestaoTrancamento;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Uma disciplina cursada no semestre (F6 a F11, F24 a F34).
 * Compõe horários, atividades, faltas e bônus; usa uma RegraMedia (Strategy) para calcular a média.
 */
public class Disciplina implements PersistivelCSV, GeradorDeNotificacoes {
    /** 1 crédito = 15 horas-aula; o limite de faltas é 25% da carga horária. */
    public static final int HORAS_POR_CREDITO = 15;
    public static final double FRACAO_LIMITE_FALTAS = 0.25;
    /** A partir desta fração do limite, o status de faltas vira ATENCAO. */
    public static final double FRACAO_ATENCAO_FALTAS = 0.75;

    private String codigo;
    private String nome;
    private String professor;
    private int creditos;
    private boolean trancada;
    private Double notaExame;
    private final List<HorarioAula> horarios = new ArrayList<>();
    private final List<Atividade> atividades = new ArrayList<>();
    private final List<RegistroFalta> faltas = new ArrayList<>();
    private PastaBiblioteca pastaDisciplina;
    private final List<Bonus> bonus = new ArrayList<>();
    private RegraMedia regraMedia = new MediaAritmetica();
    private Semestre semestre;

    public Disciplina(String codigo, String nome, String professor, int creditos) throws DadoInvalidoException {
        validar(codigo, nome, creditos);
        this.codigo = codigo.trim().toUpperCase();
        this.nome = nome.trim();
        this.professor = professor == null ? "" : professor.trim();
        this.creditos = creditos;
    }

    private static void validar(String codigo, String nome, int creditos) throws DadoInvalidoException {
        if (codigo == null || codigo.isBlank()) {
            throw new DadoInvalidoException("Informe o código da disciplina.");
        }
        if (nome == null || nome.isBlank()) {
            throw new DadoInvalidoException("Informe o nome da disciplina.");
        }
        if (creditos <= 0 || creditos > 30) {
            throw new DadoInvalidoException("O número de créditos deve ficar entre 1 e 30.");
        }
    }

    /** F7 */
    public void editarDados(String codigo, String nome, String professor, int creditos) throws DadoInvalidoException {
        validar(codigo, nome, creditos);
        String novoCodigo = codigo.trim().toUpperCase();
        if (semestre != null && !novoCodigo.equals(this.codigo)) {
            Disciplina outra = semestre.buscarDisciplina(novoCodigo);
            if (outra != null && outra != this) {
                throw new DadoInvalidoException("Já existe a disciplina " + novoCodigo + " neste semestre.");
            }
        }
        this.codigo = novoCodigo;
        this.nome = nome.trim();
        this.professor = professor == null ? "" : professor.trim();
        this.creditos = creditos;
    }

    /** F9: só é possível trancar até a data limite do semestre (se houver). */
    public void trancar(LocalDate hoje, LocalDate dataLimiteSemestre) throws PrazoInvalidoException {
        if (dataLimiteSemestre != null && hoje.isAfter(dataLimiteSemestre)) {
            throw new PrazoInvalidoException("O prazo de trancamento terminou em " + dataLimiteSemestre + ".");
        }
        this.trancada = true;
    }

    public void destrancar() {
        this.trancada = false;
    }

    // ------------------------------------------------------------------ horários

    /** F10 */
    public void adicionarHorario(HorarioAula h) throws ConflitoDeHorarioException {
        verificarConflito(h, null);
        h.setDisciplina(this);
        horarios.add(h);
    }

    /** F11: edita um horário conferindo conflito com as outras aulas. */
    public void editarHorario(HorarioAula h, DiaSemana dia, LocalTime ini, LocalTime fim, String sala)
            throws DadoInvalidoException, ConflitoDeHorarioException {
        HorarioAula provisorio = new HorarioAula(dia, ini, fim, sala);
        verificarConflito(provisorio, h);
        h.editarHorario(dia, ini, fim, sala);
    }

    private void verificarConflito(HorarioAula novo, HorarioAula ignorar) throws ConflitoDeHorarioException {
        boolean conflito;
        if (semestre != null) {
            conflito = semestre.verificarConflitoHorario(novo, ignorar);
        } else {
            conflito = horarios.stream().anyMatch(h -> h != ignorar && h.conflitaCom(novo));
        }
        if (conflito) {
            throw new ConflitoDeHorarioException("O horário " + novo + " colide com outra aula do semestre.");
        }
    }

    public void removerHorario(HorarioAula h) {
        horarios.remove(h);
    }

    // ------------------------------------------------------------------ atividades

    /**
     * F14: liga uma atividade à disciplina. Nas avaliações, a sigla precisa ser única
     * e a soma dos pesos não pode passar de 100.
     */
    public void adicionarAtividade(Atividade a) throws NotaInvalidaException, DadoInvalidoException {
        if (a.getDisciplina() != this) {
            throw new DadoInvalidoException("A atividade pertence a outra disciplina.");
        }
        if (a instanceof AtividadeAvaliativa av) {
            if (buscarAvaliacao(av.getSigla()) != null) {
                throw new DadoInvalidoException("A sigla " + av.getSigla() + " já é usada nesta disciplina.");
            }
            if (somaPesos(null) + av.getPeso() > 100.0 + 1e-9) {
                throw new NotaInvalidaException("A soma dos pesos das avaliações passaria de 100.");
            }
        }
        atividades.add(a);
    }

    /** Soma dos pesos das avaliações, ignorando uma delas (usado ao trocar um peso). */
    public double somaPesos(AtividadeAvaliativa ignorar) {
        double soma = 0;
        for (Atividade a : atividades) {
            if (a instanceof AtividadeAvaliativa av && av != ignorar && !isSubstitutiva(av)) {
                soma += av.getPeso();
            }
        }
        return soma;
    }

    /** F16 */
    public void removerAtividade(String id) throws AtividadeNaoEncontradaException {
        Atividade a = buscarAtividade(id);
        atividades.remove(a);
        bonus.removeIf(b -> b.getAlvo() == a);
        for (Atividade outra : atividades) {
            if (outra instanceof Prova p && p.getProvaSubstituida() == a) {
                p.tornarSubstitutiva(null);
            }
        }
    }

    public Atividade buscarAtividade(String id) throws AtividadeNaoEncontradaException {
        for (Atividade a : atividades) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        throw new AtividadeNaoEncontradaException("Atividade não encontrada na disciplina " + codigo + ".");
    }

    public AtividadeAvaliativa buscarAvaliacao(String sigla) {
        for (Atividade a : atividades) {
            if (a instanceof AtividadeAvaliativa av && av.getSigla().equalsIgnoreCase(sigla)) {
                return av;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ faltas

    /** F32: a data precisa estar dentro do semestre. */
    public RegistroFalta registrarFalta(LocalDate data, int numAulas) throws DadoInvalidoException {
        if (semestre != null && data != null && !semestre.contem(data)) {
            throw new DadoInvalidoException("A data da falta está fora do semestre.");
        }
        RegistroFalta f = new RegistroFalta(data, numAulas);
        faltas.add(f);
        return f;
    }

    /** Usado pela persistência. */
    public void adicionarFalta(RegistroFalta f) {
        faltas.add(f);
    }

    public void removerFalta(RegistroFalta f) {
        faltas.remove(f);
    }

    /** F34: 25% de (créditos × 15) horas-aula. Ex.: 4 créditos → 15. */
    public int calcularLimiteFaltas() {
        return (int) Math.floor(creditos * HORAS_POR_CREDITO * FRACAO_LIMITE_FALTAS);
    }

    public int calcularTotalFaltasUsadas() {
        return faltas.stream().mapToInt(RegistroFalta::getNumeroAulas).sum();
    }

    public int calcularFaltasRestantes() {
        return Math.max(0, calcularLimiteFaltas() - calcularTotalFaltasUsadas());
    }

    /** F35 */
    public StatusFaltas obterStatusFaltas() {
        int usadas = calcularTotalFaltasUsadas();
        int limite = calcularLimiteFaltas();
        if (usadas > limite) {
            return StatusFaltas.REPROVADO_POR_FALTA;
        }
        if (usadas >= FRACAO_ATENCAO_FALTAS * limite) {
            return StatusFaltas.ATENCAO;
        }
        return StatusFaltas.OK;
    }

    // ------------------------------------------------------------------ notas e médias

    /** F26: bônus numa avaliação desta disciplina, ou na nota final se alvo for null. */
    public void adicionarBonus(double valor, Avaliavel alvo) throws NotaInvalidaException {
        if (alvo != null && !atividades.contains(alvo)) {
            throw new NotaInvalidaException("O bônus precisa apontar para uma avaliação desta disciplina.");
        }
        bonus.add(new Bonus(valor, alvo));
    }

    public void adicionarBonus(Bonus b) {
        bonus.add(b);
    }

    public void removerBonus(Bonus b) {
        bonus.remove(b);
    }

    /** F27: troca a regra de cálculo da média (Strategy). */
    public void setRegraMedia(RegraMedia regra) {
        if (regra == null) {
            throw new IllegalArgumentException("A regra de média não pode ser nula.");
        }
        this.regraMedia = regra;
    }

    /** F29 */
    public void lancarNotaExame(double nota) throws NotaInvalidaException {
        if (nota < 0 || nota > 10) {
            throw new NotaInvalidaException("A nota do exame deve ficar entre 0 e 10.");
        }
        this.notaExame = nota;
    }

    public void removerNotaExame() {
        this.notaExame = null;
    }

    private boolean isSubstitutiva(AtividadeAvaliativa av) {
        return av instanceof Prova p && p.isSubstitutiva();
    }

    /**
     * Avaliações que entram na média. A prova substitutiva não entra sozinha:
     * quando tem nota, a nota dela ocupa o lugar da prova que ela substitui.
     */
    public List<Avaliavel> obterAvaliacoesParaMedia() {
        List<Avaliavel> lista = new ArrayList<>();
        for (Atividade a : atividades) {
            if (!(a instanceof AtividadeAvaliativa av) || isSubstitutiva(av)) {
                continue;
            }
            Prova sub = substitutaDe(av);
            if (sub != null && sub.possuiNotaLancada()) {
                lista.add(new NotaSimulada(av.getSigla(), av.getPeso(), sub.getNota()));
            } else {
                lista.add(av);
            }
        }
        return lista;
    }

    private Prova substitutaDe(AtividadeAvaliativa av) {
        for (Atividade a : atividades) {
            if (a instanceof Prova p && p.isSubstitutiva() && p.getProvaSubstituida() == av) {
                return p;
            }
        }
        return null;
    }

    /** Siglas que uma fórmula personalizada pode usar. */
    public Set<String> getSiglasAvaliacoes() {
        Set<String> siglas = new LinkedHashSet<>();
        for (Avaliavel a : obterAvaliacoesParaMedia()) {
            siglas.add(a.getSigla());
        }
        return siglas;
    }

    /** F28: média com as notas lançadas (avaliação sem nota vale 0) e bônus. */
    public double calcularMediaAtual() throws FormulaInvalidaException {
        return regraMedia.calcularMedia(obterAvaliacoesParaMedia(), bonus);
    }

    /** Média final: se o aluno ficou de exame e a nota do exame foi lançada, aplica a regra de exame. */
    public double calcularMediaFinal() throws FormulaInvalidaException {
        return calcularMediaFinal(Configuracao.MEDIA_APROVACAO_PADRAO, Configuracao.MEDIA_EXAME_PADRAO);
    }

    public double calcularMediaFinal(double minAprovacao, double minExame) throws FormulaInvalidaException {
        double media = calcularMediaAtual();
        if (notaExame != null && media > minExame && media < minAprovacao) {
            return regraMedia.calcularMediaFinalComExame(media, notaExame);
        }
        return media;
    }

    /**
     * F30: nota que precisa tirar em cada avaliação ainda sem nota para chegar à média mínima.
     * 0 = já alcançou; maior que 10 (infinito) = não é mais possível.
     */
    public double calcularNotaNecessaria(double mediaMinima) throws FormulaInvalidaException {
        return regraMedia.calcularNotaNecessaria(obterAvaliacoesParaMedia(), bonus, mediaMinima);
    }

    public boolean possuiAvaliacoesPendentes() {
        return obterAvaliacoesParaMedia().stream().anyMatch(a -> !a.possuiNotaLancada());
    }

    /** F31: nem tirando 10 em tudo o que falta dá para chegar ao exame. */
    public boolean isReprovacaoInevitavel() {
        if (!possuiAvaliacoesPendentes()) {
            return false;
        }
        try {
            double necessaria = calcularNotaNecessaria(nextUp(Configuracao.MEDIA_EXAME_PADRAO));
            return necessaria > 10;
        } catch (FormulaInvalidaException e) {
            return false;
        }
    }

    private static double nextUp(double v) {
        return v + 1e-6;   // "maior que 2,5" vira "pelo menos 2,500001"
    }

    /** F28: situação atual. Devolve null se a fórmula personalizada não puder ser calculada. */
    public SituacaoDisciplina obterSituacaoAtual() {
        return obterSituacaoAtual(Configuracao.MEDIA_APROVACAO_PADRAO, Configuracao.MEDIA_EXAME_PADRAO);
    }

    public SituacaoDisciplina obterSituacaoAtual(double minAprovacao, double minExame) {
        if (trancada) {
            return SituacaoDisciplina.TRANCADA;
        }
        if (obterStatusFaltas() == StatusFaltas.REPROVADO_POR_FALTA) {
            return SituacaoDisciplina.REPROVADO_POR_FALTA;
        }
        double media;
        try {
            media = calcularMediaAtual();
        } catch (FormulaInvalidaException e) {
            return null;
        }
        if (media >= minAprovacao) {
            return SituacaoDisciplina.APROVADO;
        }
        if (possuiAvaliacoesPendentes()) {
            return isReprovacaoInevitavel() ? SituacaoDisciplina.REPROVACAO_INEVITAVEL : SituacaoDisciplina.EM_ANDAMENTO;
        }
        if (media > minExame) {
            if (notaExame == null) {
                return SituacaoDisciplina.EM_EXAME;
            }
            double comExame = regraMedia.calcularMediaFinalComExame(media, notaExame);
            return comExame >= minAprovacao ? SituacaoDisciplina.APROVADO : SituacaoDisciplina.REPROVADO;
        }
        return SituacaoDisciplina.REPROVADO;
    }

    // ------------------------------------------------------------------ notificações

    /** Gera as notificações da disciplina e das atividades dela. */
    @Override
    public List<Notificacao> gerarNotificacoes(LocalDate hoje, LocalTime agora, int dias) {
        List<Notificacao> lista = new ArrayList<>();
        if (trancada) {
            return lista;
        }
        for (Atividade a : atividades) {
            lista.addAll(a.gerarNotificacoes(hoje, agora, dias));
        }
        StatusFaltas st = obterStatusFaltas();
        if (st != StatusFaltas.OK) {
            LocalDate ultima = faltas.stream().map(RegistroFalta::getData).max(LocalDate::compareTo).orElse(hoje);
            lista.add(new NotificacaoRiscoFaltas(codigo, nome, calcularTotalFaltasUsadas(), calcularLimiteFaltas(), ultima));
        }
        DiaSemana diaHoje = DiaSemana.de(hoje.getDayOfWeek());
        HorarioAula proxima = null;
        for (HorarioAula h : horarios) {
            if (h.getDiaSemana() == diaHoje && h.getHoraInicio().isAfter(agora)
                    && (proxima == null || h.getHoraInicio().isBefore(proxima.getHoraInicio()))) {
                proxima = h;
            }
        }
        if (proxima != null) {
            lista.add(new NotificacaoProximaAula(codigo, nome, proxima, hoje));
        }
        LocalDate limite = semestre == null ? null : semestre.getDataLimiteTrancamento();
        boolean emRisco = isReprovacaoInevitavel() || st == StatusFaltas.REPROVADO_POR_FALTA;
        if (emRisco && limite != null && !hoje.isAfter(limite)) {
            lista.add(new NotificacaoSugestaoTrancamento(codigo, nome, limite));
        }
        return lista;
    }

    // ------------------------------------------------------------------ acesso

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getProfessor() {
        return professor;
    }

    public int getCreditos() {
        return creditos;
    }

    public boolean isTrancada() {
        return trancada;
    }

    public Double getNotaExame() {
        return notaExame;
    }

    public List<HorarioAula> getHorarios() {
        return Collections.unmodifiableList(horarios);
    }

    public List<Atividade> getAtividades() {
        return Collections.unmodifiableList(atividades);
    }

    public List<RegistroFalta> getFaltas() {
        return Collections.unmodifiableList(faltas);
    }

    public List<Bonus> getBonus() {
        return Collections.unmodifiableList(bonus);
    }

    public RegraMedia getRegraMedia() {
        return regraMedia;
    }

    public PastaBiblioteca getPastaDisciplina() {
        return pastaDisciplina;
    }

    public void setPastaDisciplina(PastaBiblioteca pasta) {
        this.pastaDisciplina = pasta;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    /** Usado pela persistência ao recarregar o estado. */
    public void restaurarEstado(boolean trancada, Double notaExame) {
        this.trancada = trancada;
        this.notaExame = notaExame;
    }

    @Override
    public String toCSV() {
        return Csv.juntar(codigo, nome, professor, creditos, trancada, notaExame);
    }

    @Override
    public String toString() {
        return codigo + " – " + nome;
    }
}
