package br.unicamp.ic.mc322.persistencia;

/** Tudo o que é gravado em arquivo sabe se converter em uma linha CSV. */
public interface PersistivelCSV {
    String toCSV();
}
