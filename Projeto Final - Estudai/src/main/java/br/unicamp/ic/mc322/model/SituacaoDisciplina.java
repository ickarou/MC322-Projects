package br.unicamp.ic.mc322.model;

/**
 * Situação do aluno na disciplina (F28).
 * EM_ANDAMENTO: ainda há avaliações sem nota e a aprovação continua possível.
 */
public enum SituacaoDisciplina {
    EM_ANDAMENTO, APROVADO, EM_EXAME, REPROVADO, REPROVADO_POR_FALTA, REPROVACAO_INEVITAVEL, TRANCADA
}
