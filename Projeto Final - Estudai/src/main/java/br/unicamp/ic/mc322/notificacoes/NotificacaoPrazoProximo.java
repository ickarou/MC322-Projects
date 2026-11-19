package br.unicamp.ic.mc322.notificacoes;

import java.time.LocalDate;

public class NotificacaoPrazoProximo extends Notificacao {
    private final String tituloAtividade;
    private final String nomeDisciplina;
    private final LocalDate prazo;
    private final int diasRestantes;

    public NotificacaoPrazoProximo(String idAtividade, String tituloAtividade, String nomeDisciplina,
                                   LocalDate prazo, int diasRestantes) {
        super(idAtividade, prazo, diasRestantes == 0 ? 3 : diasRestantes <= 2 ? 2 : 1);
        this.tituloAtividade = tituloAtividade;
        this.nomeDisciplina = nomeDisciplina;
        this.prazo = prazo;
        this.diasRestantes = diasRestantes;
    }

    @Override
    public String gerarMensagem() {
        String quando = diasRestantes == 0 ? "hoje" : diasRestantes == 1 ? "amanhã" : "em " + diasRestantes + " dias";
        return tituloAtividade + " (" + nomeDisciplina + ") vence " + quando + " (" + prazo.format(DATA) + ").";
    }

    @Override
    public String getTipoNotificacao() {
        return "Prazo próximo";
    }
}
