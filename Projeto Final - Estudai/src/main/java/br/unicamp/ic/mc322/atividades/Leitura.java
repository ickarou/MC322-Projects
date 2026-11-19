package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;

/** Leitura: não tem nota. O progresso é páginas lidas ÷ total. */
public class Leitura extends Atividade {
    private int totalPaginas;
    private int paginasLidas;

    public Leitura(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, int total, int lidas)
            throws PrazoInvalidoException, DadoInvalidoException {
        super(titulo, prazo, prio, d);
        setTotalPaginas(total);
        atualizarPaginasLidas(lidas);
    }

    /** F19 */
    public void atualizarPaginasLidas(int lidas) throws DadoInvalidoException {
        if (lidas < 0 || lidas > totalPaginas) {
            throw new DadoInvalidoException("Páginas lidas deve ficar entre 0 e " + totalPaginas + ".");
        }
        this.paginasLidas = lidas;
        verificarAtraso(LocalDate.now());
    }

    public void setTotalPaginas(int total) throws DadoInvalidoException {
        if (total <= 0) {
            throw new DadoInvalidoException("O total de páginas deve ser maior que zero.");
        }
        if (paginasLidas > total) {
            throw new DadoInvalidoException("O total não pode ser menor que as páginas já lidas.");
        }
        this.totalPaginas = total;
    }

    @Override
    public double calcularProgresso() {
        return totalPaginas == 0 ? 0 : (double) paginasLidas / totalPaginas;
    }

    @Override
    public String getTipoAtividade() {
        return "LEITURA";
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public int getPaginasLidas() {
        return paginasLidas;
    }

    @Override
    protected Object[] camposEspecificos() {
        return new Object[]{totalPaginas, paginasLidas};
    }
}

