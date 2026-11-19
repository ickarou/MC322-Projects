package br.unicamp.ic.mc322.model;

import estudai.excecoes.DadoInvalidoException;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

import java.time.LocalDate;
import java.util.UUID;

/** Uma falta registrada: data e quantas aulas (horas-aula) foram perdidas (F32). */
public class RegistroFalta implements PersistivelCSV {
    private String id;
    private final LocalDate data;
    private final int numeroAulas;

    public RegistroFalta(LocalDate data, int numeroAulas) throws DadoInvalidoException {
        if (data == null) {
            throw new DadoInvalidoException("Informe a data da falta.");
        }
        if (numeroAulas < 1 || numeroAulas > 12) {
            throw new DadoInvalidoException("O número de aulas perdidas deve ficar entre 1 e 12.");
        }
        this.id = UUID.randomUUID().toString();
        this.data = data;
        this.numeroAulas = numeroAulas;
    }

    public String getId() {
        return id;
    }

    public void restaurarId(String id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public int getNumeroAulas() {
        return numeroAulas;
    }

    @Override
    public String toCSV() {
        return Csv.juntar(id, data, numeroAulas);
    }
}
