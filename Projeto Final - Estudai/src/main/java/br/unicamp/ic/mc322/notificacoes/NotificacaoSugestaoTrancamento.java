package br.unicamp.ic.mc322.notificacoes;

import java.time.LocalDate;

public class NotificacaoSugestaoTrancamento extends Notificacao {
    private final String nomeDisciplina;
    private final LocalDate dataLimiteTrancamento;

    public NotificacaoSugestaoTrancamento(String codigoDisciplina, String nomeDisciplina, LocalDate dataLimiteTrancamento) {
        super(codigoDisciplina, dataLimiteTrancamento, 2);
        this.nomeDisciplina = nomeDisciplina;
        this.dataLimiteTrancamento = dataLimiteTrancamento;
    }

    @Override
    public String gerarMensagem() {
        return "A aprovação em " + nomeDisciplina + " não é mais possível. Dá para trancar até "
                + dataLimiteTrancamento.format(DATA) + ".";
    }

    @Override
    public String getTipoNotificacao() {
        return "Sugestão de trancamento";
    }
}
