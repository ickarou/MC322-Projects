package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;
import br.unicamp.ic.mc322.model.StatusAtividade;
import br.unicamp.ic.mc322.notificacoes.GeradorDeNotificacoes;
import br.unicamp.ic.mc322.notificacoes.Notificacao;
import br.unicamp.ic.mc322.notificacoes.NotificacaoAtraso;
import br.unicamp.ic.mc322.notificacoes.NotificacaoPrazoProximo;
import br.unicamp.ic.mc322.persistencia.Csv;
import br.unicamp.ic.mc322.persistencia.PersistivelCSV;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base das atividades (F12 a F22). Cada tipo calcula o próprio progresso.
 *
 * O status não é guardado: só a decisão do usuário de concluir a atividade é guardada.
 * PENDENTE, EM_ANDAMENTO e ATRASADA são calculados a partir do prazo e do progresso,
 * então nunca ficam desatualizados.
 *
 * Para criar uma atividade nova, use os métodos cadastrar... da Disciplina: eles conferem as
 * regras do cadastro (F14) e já ligam a atividade à disciplina. Os construtores são usados
 * diretamente só pela persistência.
 */
public abstract class Atividade implements PersistivelCSV, GeradorDeNotificacoes {
    private String id;
    private String titulo;
    private LocalDate prazo;
    private Prioridade prioridade;
    private boolean concluida;
    private final Disciplina disciplina;

    protected Atividade(String titulo, LocalDate prazo, Prioridade prio, Disciplina d)
            throws PrazoInvalidoException, DadoInvalidoException {
        if (d == null) {
            throw new DadoInvalidoException("A atividade precisa pertencer a uma disciplina.");
        }
        this.id = UUID.randomUUID().toString();
        this.disciplina = d;
        aplicarDadosBasicos(titulo, prazo, prio);
    }

    /** F15. O prazo precisa existir e, se a disciplina tem semestre, cair dentro dele. */
    public void editarDadosBasicos(String titulo, LocalDate prazo, Prioridade prio)
            throws PrazoInvalidoException, DadoInvalidoException {
        aplicarDadosBasicos(titulo, prazo, prio);
    }

    private void aplicarDadosBasicos(String titulo, LocalDate prazo, Prioridade prio)
            throws PrazoInvalidoException, DadoInvalidoException {
        if (titulo == null || titulo.isBlank()) {
            throw new DadoInvalidoException("Informe o título da atividade.");
        }
        if (prazo == null) {
            throw new PrazoInvalidoException("Informe o prazo da atividade.");
        }
        if (disciplina.getSemestre() != null && !disciplina.getSemestre().contem(prazo)) {
            throw new PrazoInvalidoException("O prazo " + prazo + " está fora das datas do semestre.");
        }
        this.titulo = titulo.trim();
        this.prazo = prazo;
        this.prioridade = prio == null ? Prioridade.MEDIA : prio;
    }

    /** Regra do cadastro (F14): uma atividade nova não pode ter prazo no passado. */
    public static void validarPrazoNovo(LocalDate prazo, LocalDate hoje) throws PrazoInvalidoException {
        if (prazo != null && prazo.isBefore(hoje)) {
            throw new PrazoInvalidoException("O prazo de uma atividade nova não pode estar no passado.");
        }
    }

    // ------------------------------------------------------------------ status e progresso

    public void concluir() {
        concluida = true;
    }

    /**
     * Desfaz o concluir(). A atividade continua encerrada se o próprio tipo diz que ela terminou
     * (avaliação com nota lançada, leitura com todas as páginas lidas).
     */
    public void reabrir() {
        concluida = false;
    }

    /** O usuário marcou a atividade como concluída. */
    public boolean isMarcadaComoConcluida() {
        return concluida;
    }

    /**
     * Atividade encerrada não fica atrasada nem gera avisos, e o progresso dela é 100%.
     * As subclasses acrescentam os próprios critérios (nota lançada, todas as páginas lidas).
     */
    public boolean estaEncerrada() {
        return concluida;
    }

    /** F20: status calculado na data informada. */
    public StatusAtividade getStatus(LocalDate hoje) {
        if (estaEncerrada()) {
            return StatusAtividade.CONCLUIDA;
        }
        if (prazo.isBefore(hoje)) {
            return StatusAtividade.ATRASADA;
        }
        return calcularProgresso() > 0 ? StatusAtividade.EM_ANDAMENTO : StatusAtividade.PENDENTE;
    }

    /** Status calculado hoje. */
    public StatusAtividade getStatus() {
        return getStatus(LocalDate.now());
    }

    /** F19: fração de 0 a 1. Atividade encerrada está sempre em 100%. */
    public final double calcularProgresso() {
        return estaEncerrada() ? 1 : calcularProgressoParcial();
    }

    /** Progresso próprio de cada tipo, de 0 a 1, para quando a atividade ainda não terminou. */
    protected abstract double calcularProgressoParcial();

    public abstract TipoAtividade getTipoAtividade();

    // ------------------------------------------------------------------ notificações

    /** Prazo vencido gera atraso; prazo dentro da antecedência gera prazo próximo. */
    @Override
    public List<Notificacao> gerarNotificacoes(LocalDate hoje, LocalTime agora, int diasAntecedencia) {
        List<Notificacao> lista = new ArrayList<>();
        if (estaEncerrada()) {
            return lista;
        }
        long dias = ChronoUnit.DAYS.between(hoje, prazo);
        if (dias < 0) {
            lista.add(new NotificacaoAtraso(id, titulo, disciplina.getNome(), prazo));
        } else if (dias <= diasAntecedencia) {
            lista.add(new NotificacaoPrazoProximo(id, titulo, disciplina.getNome(), prazo, (int) dias));
        }
        return lista;
    }

    // ------------------------------------------------------------------ persistência

    /** Campos próprios de cada tipo, gravados depois dos campos comuns. */
    protected abstract Object[] camposEspecificos();

    /**
     * Na coluna de status vai só o que o usuário decidiu: CONCLUIDA se ele marcou a atividade como
     * concluída, PENDENTE se não. Os outros status são calculados de novo ao abrir o programa.
     */
    @Override
    public final String toCSV() {
        StatusAtividade gravado = concluida ? StatusAtividade.CONCLUIDA : StatusAtividade.PENDENTE;
        Object[] base = {id, getTipoAtividade().name(), titulo, prazo, prioridade.name(), gravado.name()};
        return Csv.juntar(concatenar(base, camposEspecificos()));
    }

    /** Junta dois vetores de campos (usado para montar as linhas CSV). */
    protected static Object[] concatenar(Object[] inicio, Object... fim) {
        Object[] todos = new Object[inicio.length + fim.length];
        System.arraycopy(inicio, 0, todos, 0, inicio.length);
        System.arraycopy(fim, 0, todos, inicio.length, fim.length);
        return todos;
    }

    /** Usado pela persistência ao recarregar uma atividade salva. */
    public void restaurar(String id, boolean concluida) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O id da atividade não pode ser vazio.");
        }
        this.id = id;
        this.concluida = concluida;
    }

    // ------------------------------------------------------------------ acesso

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    @Override
    public String toString() {
        return titulo + " (" + getTipoAtividade() + ", " + prazo + ")";
    }
}
