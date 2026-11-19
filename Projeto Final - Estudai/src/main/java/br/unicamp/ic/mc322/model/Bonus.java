package br.unicamp.ic.mc322.model;

import estudai.atividades.Avaliavel;
import estudai.excecoes.NotaInvalidaException;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

/**
 * Bônus de nota (F26). Com alvo, soma na nota daquela avaliação;
 * sem alvo (null), soma na nota final da disciplina. As notas nunca passam de 10.
 */
public class Bonus implements PersistivelCSV {
    private final double valor;
    private final Avaliavel alvo;

    public Bonus(double valor, Avaliavel alvo) throws NotaInvalidaException {
        if (valor <= 0 || valor > 10) {
            throw new NotaInvalidaException("O bônus deve ser maior que 0 e no máximo 10.");
        }
        this.valor = valor;
        this.alvo = alvo;
    }

    public boolean aplicaNaNotaFinal() {
        return alvo == null;
    }

    public double getValor() {
        return valor;
    }

    public Avaliavel getAlvo() {
        return alvo;
    }

    /** A avaliação é ligada pela sigla, que é única na disciplina. */
    @Override
    public String toCSV() {
        return Csv.juntar(valor, alvo == null ? "" : alvo.getSigla());
    }
}

