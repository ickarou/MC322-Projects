package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;
import br.unicamp.ic.mc322.model.StatusAtividade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Atividade que recebe nota: guarda sigla, peso, nota e o checklist (etapas ou tópicos).
 * O progresso é a fração do checklist concluída.
 */
public abstract class AtividadeAvaliativa extends Atividade implements Avaliavel {
    /** A sigla é usada nas fórmulas: começa com letra e só tem letras, números ou _. */
    private static final Pattern FORMATO_SIGLA = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    protected String sigla;
    protected double peso;
    protected Double nota;
    protected final List<ItemChecklist> checklist = new ArrayList<>();

    public AtividadeAvaliativa(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, String sigla, double peso)
            throws PrazoInvalidoException, NotaInvalidaException, DadoInvalidoException {
        super(titulo, prazo, prio, d);
        if (sigla == null || !FORMATO_SIGLA.matcher(sigla.trim()).matches()) {
            throw new DadoInvalidoException("Sigla inválida: use letras e números, começando por letra (ex.: P1, T2).");
        }
        validarPeso(peso);
        this.sigla = sigla.trim().toUpperCase();
        this.peso = peso;
    }

    /** Escrito como !(dentro do intervalo) para que NaN tambem seja recusado */
    private static void validarPeso(double peso) throws NotaInvalidaException {
        if (!(peso >= 0 && peso <= 100)) {
            throw new NotaInvalidaException("O peso deve ficar entre 0 e 100.");
        }
    }

    @Override
    public String getSigla() {
        return sigla;
    }

    /** F24: Avaliação com nota lançada já aconteceu, então passa a CONCLUIDA */
    @Override
    public void lancarNota(double nota) throws NotaInvalidaException {
        if (!(nota >= 0 && nota <= 10)) {
            throw new NotaInvalidaException("A nota deve ficar entre 0 e 10.");
        }
        this.nota = nota;
        concluir();
    }

    /** A atividade continua CONCLUIDA (ela aconteceu); para reabrir, use reabrir(). */
    @Override
    public void removerNota() {
        this.nota = null;
    }

    @Override
    public Double getNota() {
        return nota;
    }

    @Override
    public double getPeso() {
        return peso;
    }

    /** F14: a soma dos pesos da disciplina não pode passar de 100. */
    @Override
    public void setPeso(double peso) throws NotaInvalidaException {
        validarPeso(peso);
        if (disciplina.getAtividades().contains(this) && disciplina.somaPesos(this) + peso > 100.0 + 1e-9) {
            throw new NotaInvalidaException("A soma dos pesos das avaliações passaria de 100.");
        }
        this.peso = peso;
    }

    @Override
    public boolean possuiNotaLancada() {
        return nota != null;
    }

    /** Avaliação com nota lançada já aconteceu: não conta como atrasada. */
    @Override
    protected boolean estaEncerrada() {
        return super.estaEncerrada() || nota != null;
    }

    // ---- checklist (F18) ----

    public void adicionarItem(String descricao) throws DadoInvalidoException {
        checklist.add(new ItemChecklist(descricao));
        verificarAtraso(LocalDate.now());
    }

    /** Usado pela persistência. */
    public void adicionarItem(ItemChecklist item) {
        checklist.add(item);
    }

    public void removerItem(int indice) {
        checklist.remove(indice);
        verificarAtraso(LocalDate.now());
    }

    public void alternarItem(int indice, boolean concluido) {
        ItemChecklist item = checklist.get(indice);
        if (concluido) {
            item.marcar();
        } else {
            item.desmarcar();
        }
        verificarAtraso(LocalDate.now());
    }

    public List<ItemChecklist> getChecklist() {
        return Collections.unmodifiableList(checklist);
    }

    /** F19: itens concluídos ÷ total. Sem itens, vale 1 se a atividade foi concluída e 0 se não. */
    @Override
    public double calcularProgresso() {
        if (checklist.isEmpty()) {
            return status == StatusAtividade.CONCLUIDA ? 1 : 0;
        }
        long feitos = checklist.stream().filter(ItemChecklist::isConcluido).count();
        return (double) feitos / checklist.size();
    }

    /** Campos comuns às avaliações; as subclasses acrescentam os seus. */
    protected Object[] camposAvaliacao(Object... extras) {
        Object[] base = {sigla, peso, nota};
        Object[] todos = new Object[base.length + extras.length];
        System.arraycopy(base, 0, todos, 0, base.length);
        System.arraycopy(extras, 0, todos, base.length, extras.length);
        return todos;
    }
}

