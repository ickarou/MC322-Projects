package br.unicamp.ic.mc322.notificacoes;

import estudai.persistencia.PersistivelCSV;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base das notificações (F42 a F45). A chave única (tipo + item + data) evita gerar a mesma
 * notificação duas vezes e permite lembrar quais já foram lidas.
 */
public abstract class Notificacao implements Comparable<Notificacao>, PersistivelCSV {
    protected static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM");

    protected String chaveUnica;
    protected LocalDateTime dataGeracao;
    protected boolean lida;
    protected int nivelUrgencia;

    public Notificacao(String itemRef, LocalDate data, int urgencia) {
        this.chaveUnica = getClass().getSimpleName() + ":" + itemRef + ":" + data;
        this.dataGeracao = data.atStartOfDay();
        this.nivelUrgencia = urgencia;
    }

    public abstract String gerarMensagem();

    public abstract String getTipoNotificacao();

    public void marcarComoLida() {
        lida = true;
    }

    public boolean isLida() {
        return lida;
    }

    public String getChaveUnica() {
        return chaveUnica;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public int getNivelUrgencia() {
        return nivelUrgencia;
    }

    /** Mais urgente primeiro; empate decidido pela data (mais próxima primeiro). */
    @Override
    public int compareTo(Notificacao outra) {
        int c = Integer.compare(outra.nivelUrgencia, nivelUrgencia);
        return c != 0 ? c : dataGeracao.compareTo(outra.dataGeracao);
    }

    /** Só a chave é gravada: as notificações são geradas de novo a cada abertura. */
    @Override
    public String toCSV() {
        return chaveUnica;
    }

    @Override
    public String toString() {
        return getTipoNotificacao() + ": " + gerarMensagem();
    }
}
