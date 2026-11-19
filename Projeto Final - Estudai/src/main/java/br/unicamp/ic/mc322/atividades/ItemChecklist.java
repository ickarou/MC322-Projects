package br.unicamp.ic.mc322.atividades;

import br.unicamp.ic.mc322.exceptions.DadoInvalidoException;
import br.unicamp.ic.mc322.persistencia.Csv;
import br.unicamp.ic.mc322.persistencia.PersistivelCSV;

/** Uma etapa de trabalho ou um tópico de prova (F18). */
public class ItemChecklist implements PersistivelCSV {
    private final String descricao;
    private boolean concluido;

    public ItemChecklist(String descricao) throws DadoInvalidoException {
        if (descricao == null || descricao.isBlank()) {
            throw new DadoInvalidoException("Descreva o item do checklist.");
        }
        this.descricao = descricao.trim();
    }

    public void marcar() {
        concluido = true;
    }

    public void desmarcar() {
        concluido = false;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toCSV() {
        return Csv.juntar(descricao, concluido);
    }
}
