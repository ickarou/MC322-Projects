package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.exceptions.PrazoInvalidoException;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;

import java.time.LocalDate;

/** Leitura: não tem nota. O progresso é páginas lidas ÷ total; com todas lidas, ela está concluída. */
public class Leitura extends Atividade {
    private int totalPaginas;
    private int paginasLidas;

    public Leitura(String titulo, LocalDate prazo, Prioridade prio, Disciplina d, int total, int lidas)
            throws PrazoInvalidoException, DadoInvalidoException {
        super(titulo, prazo, prio, d);
        validarPaginas(total, lidas);
        this.totalPaginas = total;
        this.paginasLidas = lidas;
    }

    private static void validarPaginas(int total, int lidas) throws DadoInvalidoException {
        if (total <= 0) {
            throw new DadoInvalidoException("O total de páginas deve ser maior que zero.");
        }
        if (lidas < 0 || lidas > total) {
            throw new DadoInvalidoException("Páginas lidas deve ficar entre 0 e " + total + ".");
        }
    }

    /** F19 */
    public void atualizarPaginasLidas(int lidas) throws DadoInvalidoException {
        validarPaginas(totalPaginas, lidas);
        this.paginasLidas = lidas;
    }

    public void setTotalPaginas(int total) throws DadoInvalidoException {
        if (total > 0 && paginasLidas > total) {
            throw new DadoInvalidoException("O total não pode ser menor que as páginas já lidas.");
        }
        validarPaginas(total, paginasLidas);
        this.totalPaginas = total;
    }

    /** Com todas as páginas lidas, a leitura terminou, mesmo sem o usuário marcar. */
    @Override
    public boolean estaEncerrada() {
        return super.estaEncerrada() || paginasLidas == totalPaginas;
    }

    @Override
    protected double calcularProgressoParcial() {
        return (double) paginasLidas / totalPaginas;
    }

    @Override
    public TipoAtividade getTipoAtividade() {
        return TipoAtividade.LEITURA;
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
