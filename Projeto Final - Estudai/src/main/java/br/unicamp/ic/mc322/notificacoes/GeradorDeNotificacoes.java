package br.unicamp.ic.mc322.notificacoes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Quem sabe gerar as próprias notificações (atividades e disciplinas) (F42). */
public interface GeradorDeNotificacoes {
    List<Notificacao> gerarNotificacoes(LocalDate hoje, LocalTime agora, int diasAntecedencia);
}
