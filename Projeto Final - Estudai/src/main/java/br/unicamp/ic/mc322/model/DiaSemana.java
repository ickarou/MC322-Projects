package br.unicamp.ic.mc322.model;

import java.time.DayOfWeek;

public enum DiaSemana {
    SEGUNDA("Segunda", DayOfWeek.MONDAY),
    TERCA("Terça", DayOfWeek.TUESDAY),
    QUARTA("Quarta", DayOfWeek.WEDNESDAY),
    QUINTA("Quinta", DayOfWeek.THURSDAY),
    SEXTA("Sexta", DayOfWeek.FRIDAY),
    SABADO("Sábado", DayOfWeek.SATURDAY);

    private final String nome;
    private final DayOfWeek diaJava;

    DiaSemana(String nome, DayOfWeek diaJava) {
        this.nome = nome;
        this.diaJava = diaJava;
    }

    public String getNome() {
        return nome;
    }

    public DayOfWeek getDiaJava() {
        return diaJava;
    }

    /** Converte um DayOfWeek do Java. Domingo não tem aula e devolve null. */
    public static DiaSemana de(DayOfWeek dia) {
        for (DiaSemana d : values()) {
            if (d.diaJava == dia) {
                return d;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return nome;
    }
}
