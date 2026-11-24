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

/** Base das atividades (F12 a F22). Cada tipo calcula o próprio progresso. */
public abstract class Atividade implements PersistivelCSV, GeradorDeNotificacoes {
    protected String id;
    protected String titulo;
    protected LocalDate prazo;
    protected Prioridade prioridade;
    protected StatusAtividade status = StatusAtividade.PENDENTE;
    protected Disciplina disciplina;

    public Atividade(String titulo, LocalDate prazo, Prioridade prio, Disciplina d)
            throws PrazoInvalidoException, DadoInvalidoException {
        if (d == null) {
            throw new DadoInvalidoException("A atividade precisa pertencer a uma disciplina.");
        }
        this.id = UUID.randomUUID().toString();
        this.disciplina = d;
        // No construtor não chamamos verificarAtraso(): os campos das subclasses ainda não existem.
        aplicarDadosBasicos(titulo, prazo, prio);
    }

    /** F15. O prazo precisa existir e, se a disciplina tem semestre, cair dentro dele. */
    public void editarDadosBasicos(String titulo, LocalDate prazo, Prioridade prio)
            throws PrazoInvalidoException, DadoInvalidoException {
        aplicarDadosBasicos(titulo, prazo, prio);
        verificarAtraso(LocalDate.now());
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

    public void concluir() {
        status = StatusAtividade.CONCLUIDA;
    }

    public void reabrir() {
        status = StatusAtividade.PENDENTE;
        verificarAtraso(LocalDate.now());
    }

    /**
     * F20: atualiza o status pela data. Atividade não concluída com prazo vencido fica ATRASADA;
     * as outras ficam EM_ANDAMENTO (algum progresso) ou PENDENTE.
     * Atividade encerrada (concluída ou, nas avaliações, com nota lançada) fica sempre CONCLUIDA.
     */
    public boolean verificarAtraso(LocalDate hoje) {
        if (estaEncerrada()) {
            status = StatusAtividade.CONCLUIDA;
            return false;
        }
        boolean atrasada = prazo.isBefore(hoje);
        if (atrasada) {
            status = StatusAtividade.ATRASADA;
        } else {
            status = calcularProgresso() > 0 ? StatusAtividade.EM_ANDAMENTO : StatusAtividade.PENDENTE;
        }
        return atrasada;
    }

    /** Atividade encerrada não fica atrasada nem gera avisos. */
    protected boolean estaEncerrada() {
        return status == StatusAtividade.CONCLUIDA;
    }

    /** Fração de 0 a 1. */
    public abstract double calcularProgresso();

    public abstract String getTipoAtividade();

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

    /** Campos próprios de cada tipo, gravados depois dos campos comuns. */
    protected abstract Object[] camposEspecificos();

    @Override
    public final String toCSV() {
        Object[] base = {id, getTipoAtividade(), titulo, prazo, prioridade.name(), status.name()};
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

    public LocalDate getPrazo() {
        return prazo;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusAtividade getStatus() {
        return status;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    /** Usado pela persistência ao recarregar uma atividade salva. */
    public void restaurar(String id, StatusAtividade status) {
        this.id = id;
        this.status = status;
    }

    @Override
    public String toString() {
        return titulo + " (" + getTipoAtividade() + ", " + prazo + ")";
    }
}
