package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;

/** Prova. O checklist guarda os tópicos a revisar. Pode ser substitutiva de outra prova. */
public class Prova extends AtividadeAvaliativa {
    private String conteudo;
    private boolean substitutiva;
    private Prova provaSubstituida;

    public Prova(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, String sigla, double peso, String conteudo)
            throws PrazoInvalidoException, NotaInvalidaException, DadoInvalidoException {
        super(titulo, prazo, prio, d, sigla, peso);
        this.conteudo = conteudo == null ? "" : conteudo.trim();
    }

    /** Marca esta prova como substitutiva de "p". Com null, deixa de ser substitutiva. */
    public void tornarSubstitutiva(Prova p) {
        if (p == this) {
            throw new IllegalArgumentException("Uma prova não pode substituir a si mesma.");
        }
        if (p != null && p.getDisciplina() != disciplina) {
            throw new IllegalArgumentException("A prova substituída precisa ser da mesma disciplina.");
        }
        this.provaSubstituida = p;
        this.substitutiva = p != null;
    }

    public boolean isSubstitutiva() {
        return substitutiva;
    }

    public Prova getProvaSubstituida() {
        return provaSubstituida;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo == null ? "" : conteudo.trim();
    }

    @Override
    public String getTipoAtividade() {
        return "PROVA";
    }

    @Override
    protected Object[] camposEspecificos() {
        return camposAvaliacao(conteudo, substitutiva, provaSubstituida == null ? "" : provaSubstituida.getSigla());
    }
}

