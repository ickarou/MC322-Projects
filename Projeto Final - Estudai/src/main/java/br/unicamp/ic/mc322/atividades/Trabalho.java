package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.NotaInvalidaException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;

/** Trabalho. O checklist guarda as etapas. */
public class Trabalho extends AtividadeAvaliativa {
    private boolean emGrupo;
    private String localEntrega;

    public Trabalho(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, String sigla, double peso,
                    boolean emGrupo, String local)
            throws PrazoInvalidoException, NotaInvalidaException, DadoInvalidoException {
        super(titulo, prazo, prio, d, sigla, peso);
        this.emGrupo = emGrupo;
        this.localEntrega = local == null ? "" : local.trim();
    }

    public boolean isEmGrupo() {
        return emGrupo;
    }

    public void setEmGrupo(boolean emGrupo) {
        this.emGrupo = emGrupo;
    }

    public String getLocalEntrega() {
        return localEntrega;
    }

    public void setLocalEntrega(String localEntrega) {
        this.localEntrega = localEntrega == null ? "" : localEntrega.trim();
    }

    @Override
    public TipoAtividade getTipoAtividade() {
        return TipoAtividade.TRABALHO;
    }

    @Override
    protected Object[] camposEspecificos() {
        return camposAvaliacao(emGrupo, localEntrega);
    }
}
