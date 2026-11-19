package br.unicamp.ic.mc322.notificacoes;

import java.time.LocalDate;

public class NotificacaoRiscoFaltas extends Notificacao {
    private final String nomeDisciplina;
    private final int faltasUsadas;
    private final int limiteFaltas;

    /** A chave inclui o total de faltas: uma falta nova gera um aviso novo, mesmo que o anterior tenha sido lido. */
    public NotificacaoRiscoFaltas(String codigoDisciplina, String nomeDisciplina, int faltasUsadas, int limiteFaltas,
                                  LocalDate ultimaFalta) {
        super(codigoDisciplina + "#" + faltasUsadas, ultimaFalta, faltasUsadas > limiteFaltas ? 3 : 2);
        this.nomeDisciplina = nomeDisciplina;
        this.faltasUsadas = faltasUsadas;
        this.limiteFaltas = limiteFaltas;
    }

    @Override
    public String gerarMensagem() {
        if (faltasUsadas > limiteFaltas) {
            return nomeDisciplina + ": " + faltasUsadas + " faltas, acima do limite de " + limiteFaltas + ".";
        }
        return nomeDisciplina + ": " + faltasUsadas + " de " + limiteFaltas + " faltas usadas. Restam "
                + (limiteFaltas - faltasUsadas) + ".";
    }

    @Override
    public String getTipoNotificacao() {
        return "Risco por faltas";
    }
}

