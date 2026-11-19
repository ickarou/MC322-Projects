package br.unicamp.ic.mc322.model;

import estudai.excecoes.DadoInvalidoException;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

import java.time.LocalTime;
import java.util.UUID;

/** Um horário semanal de aula de uma disciplina (F10, F11). */
public class HorarioAula implements PersistivelCSV, Comparable<HorarioAula> {
    private String id;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String sala;
    private Disciplina disciplina;

    public HorarioAula(DiaSemana dia, LocalTime ini, LocalTime fim, String sala) throws DadoInvalidoException {
        this.id = UUID.randomUUID().toString();
        editarHorario(dia, ini, fim, sala);
    }

    public void editarHorario(DiaSemana dia, LocalTime ini, LocalTime fim, String sala) throws DadoInvalidoException {
        if (dia == null || ini == null || fim == null) {
            throw new DadoInvalidoException("Informe o dia, a hora de início e a hora de fim da aula.");
        }
        if (!fim.isAfter(ini)) {
            throw new DadoInvalidoException("A aula precisa terminar depois de começar.");
        }
        this.diaSemana = dia;
        this.horaInicio = ini;
        this.horaFim = fim;
        this.sala = sala == null ? "" : sala.trim();
    }

    /** Dois horários conflitam quando caem no mesmo dia e os intervalos se sobrepõem. */
    public boolean conflitaCom(HorarioAula outro) {
        return outro != null && outro != this
                && diaSemana == outro.diaSemana
                && horaInicio.isBefore(outro.horaFim)
                && outro.horaInicio.isBefore(horaFim);
    }

    @Override
    public int compareTo(HorarioAula o) {
        int c = diaSemana.compareTo(o.diaSemana);
        return c != 0 ? c : horaInicio.compareTo(o.horaInicio);
    }

    public String getId() {
        return id;
    }

    public void restaurarId(String id) {
        this.id = id;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public String getSala() {
        return sala;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    @Override
    public String toCSV() {
        return Csv.juntar(id, diaSemana.name(), horaInicio, horaFim, sala);
    }

    @Override
    public String toString() {
        return diaSemana.getNome() + " " + horaInicio + "–" + horaFim + (sala.isEmpty() ? "" : " (" + sala + ")");
    }
}
