package br.unicamp.ic.mc322.notificacoes;

import estudai.modelo.HorarioAula;

import java.time.LocalDate;
import java.time.LocalTime;

public class NotificacaoProximaAula extends Notificacao {
    private final String nomeDisciplina;
    private final LocalTime horario;
    private final String sala;

    public NotificacaoProximaAula(String codigoDisciplina, String nomeDisciplina, HorarioAula aula, LocalDate hoje) {
        super(codigoDisciplina + "#" + aula.getId(), hoje, 1);
        this.nomeDisciplina = nomeDisciplina;
        this.horario = aula.getHoraInicio();
        this.sala = aula.getSala();
    }

    @Override
    public String gerarMensagem() {
        return "Aula de " + nomeDisciplina + " hoje às " + horario + (sala.isEmpty() ? "" : ", sala " + sala) + ".";
    }

    @Override
    public String getTipoNotificacao() {
        return "Próxima aula";
    }
}
