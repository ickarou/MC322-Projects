

package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;

/** Prova. O checklist guarda os tópicos a revisar. Pode ser substitutiva de outra prova. 
 * A substitutiva não tem peso próprio (fica com 0): na média, a nota dela usa o peso da prova substituida.
*/
public class Prova extends AtividadeAvaliativa {
    private String conteudo;
    private Prova provaSubstituida;

    public Prova(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, String sigla, double peso, String conteudo)
            throws PrazoInvalidoException, NotaInvalidaException, DadoInvalidoException {
        super(titulo, prazo, prio, d, sigla, peso);
        this.conteudo = conteudo == null ? "" : conteudo.trim();
    }

    /** Marca esta prova como substitutiva de "p" e zera o peso dela. Com null, deixa de ser substitutiva
     *  (o peso continua 0 até ser definido com setPeso).
     *  Recusa ciclos e cadeias: "p" não pode ser substitutiva, ninguém pode estar substituindo esta prova 
     * e "p" não pode já ter outra substitutiva.
     */
    public void tornarSubstitutiva(Prova p) {
        if (p == null) {
            this.provaSubstituida = null;
            return;
        }
        if (p == this) {
            throw new IllegalArgumentException("Uma prova não pode substituir a si mesma.");
        }
        if (p.getDisciplina() != disciplina) {
            throw new IllegalArgumentException("A prova substituída precisa ser da mesma disciplina.");
        }
        if (p.isSubstitutiva()) {
            throw new IllegalArgumentException("A prova " + p.getSigla() + " já é substitutiva e não pode ser substituída.");
        }
        for (Atividade a : disciplina.getAtividades()) {
            if (a instanceof Prova outra && outra != this) {
                if (outra.provaSubstituida == this) {
                    throw new IllegalArgumentException("A prova " + sigla + " já é substituída pela "
                            + outra.getSigla() + " e não pode ser substitutiva.");
                }
                if (outra.provaSubstituida == p) {
                    throw new IllegalArgumentException("A prova " + p.getSigla() + " já tem a substitutiva "
                            + outra.getSigla() + ".");
                }
            }
        }
        this.provaSubstituida = p;
        this.peso = 0;
    }

    public boolean isSubstitutiva() {
        return provaSubstituida != null;
    }

    /** A substitutiva usa o peso da prova substituída, então o peso próprio dela precisa ser 0. */
    @Override
    public void setPeso(double peso) throws NotaInvalidaException {
        if (isSubstitutiva() && peso != 0) {
            throw new NotaInvalidaException("A substitutiva usa o peso da prova " + provaSubstituida.getSigla() + ".");
        }
        super.setPeso(peso);
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
        return camposAvaliacao(conteudo, isSubstitutiva(), provaSubstituida == null ? "" : provaSubstituida.getSigla());
    }
}

