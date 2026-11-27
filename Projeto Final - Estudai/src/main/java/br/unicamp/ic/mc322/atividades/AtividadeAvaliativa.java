package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Atividade que recebe nota: guarda sigla, peso, nota e o checklist (etapas ou tópicos).
 * O progresso é a fração do checklist concluída. Com nota lançada, a avaliação está encerrada.
 */
public abstract class AtividadeAvaliativa extends Atividade implements Avaliavel {
    /** A sigla é usada nas fórmulas: começa com letra e só tem letras, números ou _. */
    private static final Pattern FORMATO_SIGLA = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    private final String sigla;
    private double peso;
    private Double nota;
    private final List<ItemChecklist> checklist = new ArrayList<>();

    protected AtividadeAvaliativa(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, String sigla,
                                  double peso)
            throws PrazoInvalidoException, NotaInvalidaException, DadoInvalidoException {
        super(titulo, prazo, prio, d);
        if (sigla == null || !FORMATO_SIGLA.matcher(sigla.trim()).matches()) {
            throw new DadoInvalidoException("Sigla inválida: use letras e números, começando por letra (ex.: P1, T2).");
        }
        validarPeso(peso);
        this.sigla = sigla.trim().toUpperCase(Locale.ROOT);
        this.peso = peso;
    }

    /** Escrito como !(dentro do intervalo) para que NaN também seja recusado. */
    private static void validarPeso(double peso) throws NotaInvalidaException {
        if (!(peso >= 0 && peso <= 100)) {
            throw new NotaInvalidaException("O peso deve ficar entre 0 e 100.");
        }
    }

    // ------------------------------------------------------------------ nota e peso

    @Override
    public String getSigla() {
        return sigla;
    }

    /** F24. Com a nota lançada, a avaliação passa a CONCLUIDA. */
    public void lancarNota(double nota) throws NotaInvalidaException {
        if (!(nota >= 0 && nota <= 10)) {
            throw new NotaInvalidaException("A nota deve ficar entre 0 e 10.");
        }
        this.nota = nota;
    }

    /** Sem a nota, o status volta a ser calculado pelo prazo, a menos que o usuário tenha concluído a atividade. */
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
    public void setPeso(double peso) throws NotaInvalidaException {
        validarPeso(peso);
        Disciplina d = getDisciplina();
        if (d.getAtividades().contains(this) && d.somaPesos(this) + peso > 100.0 + 1e-9) {
            throw new NotaInvalidaException("A soma dos pesos das avaliações passaria de 100.");
        }
        this.peso = peso;
    }

    /** Para a Prova substitutiva, cujo peso é sempre 0. Zerar nunca quebra a soma dos pesos. */
    protected final void zerarPeso() {
        this.peso = 0;
    }

    /** Avaliação com nota lançada já aconteceu: não conta como atrasada. */
    @Override
    public boolean estaEncerrada() {
        return super.estaEncerrada() || nota != null;
    }

    // ------------------------------------------------------------------ checklist (F18)

    public void adicionarItem(String descricao) throws DadoInvalidoException {
        checklist.add(new ItemChecklist(descricao));
    }

    /** Usado pela persistência. */
    public void adicionarItem(ItemChecklist item) {
        if (item == null) {
            throw new IllegalArgumentException("O item do checklist não pode ser nulo.");
        }
        checklist.add(item);
    }

    public void removerItem(int indice) throws DadoInvalidoException {
        checklist.remove(item(indice));
    }

    public void alternarItem(int indice, boolean concluido) throws DadoInvalidoException {
        ItemChecklist item = item(indice);
        if (concluido) {
            item.marcar();
        } else {
            item.desmarcar();
        }
    }

    public void editarItem(int indice, String descricao) throws DadoInvalidoException {
        item(indice).setDescricao(descricao);
    }

    private ItemChecklist item(int indice) throws DadoInvalidoException {
        if (indice < 0 || indice >= checklist.size()) {
            throw new DadoInvalidoException("Item " + indice + " não existe no checklist.");
        }
        return checklist.get(indice);
    }

    public List<ItemChecklist> getChecklist() {
        return Collections.unmodifiableList(checklist);
    }

    /** Itens concluídos ÷ total; sem itens, 0 (o progresso vira 100% quando a avaliação é encerrada). */
    @Override
    protected double calcularProgressoParcial() {
        if (checklist.isEmpty()) {
            return 0;
        }
        long feitos = checklist.stream().filter(ItemChecklist::isConcluido).count();
        return (double) feitos / checklist.size();
    }

    // ------------------------------------------------------------------ persistência

    /** Campos comuns às avaliações; as subclasses acrescentam os seus. */
    protected Object[] camposAvaliacao(Object... extras) {
        return concatenar(new Object[]{sigla, peso, nota}, extras);
    }
}
