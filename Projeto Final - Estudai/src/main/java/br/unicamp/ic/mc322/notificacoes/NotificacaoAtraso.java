package br.unicamp.ic.mc322.notificacoes;

import java.time.LocalDate;

public class NotificacaoAtraso extends Notificacao {
    private final String tituloAtividade;
    private final String nomeDisciplina;
    private final LocalDate prazoVencido;

    public NotificacaoAtraso(String idAtividade, String tituloAtividade, String nomeDisciplina, LocalDate prazoVencido) {
        super(idAtividade, prazoVencido, 3);
        this.tituloAtividade = tituloAtividade;
        this.nomeDisciplina = nomeDisciplina;
        this.prazoVencido = prazoVencido;
    }

    @Override
    public String gerarMensagem() {
        return tituloAtividade + " (" + nomeDisciplina + ") venceu em " + prazoVencido.format(DATA) + ".";
    }

    @Override
    public String getTipoNotificacao() {
        return "Atraso";
    }
}
