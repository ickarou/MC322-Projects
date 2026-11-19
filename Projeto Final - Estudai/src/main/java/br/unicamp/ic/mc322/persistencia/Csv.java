package br.unicamp.ic.mc322.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para montar e separar linhas CSV.
 * Separador ';'. Barra invertida, ';' e quebras de linha dentro dos campos são escapados.
 */
public final class Csv {
    public static final char SEP = ';';

    private Csv() {
    }

    public static String juntar(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEP);
            }
            sb.append(escapar(campos[i] == null ? "" : String.valueOf(campos[i])));
        }
        return sb.toString();
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\;").replace("\n", "\\n").replace("\r", "");
    }

    public static String[] separar(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\\' && i + 1 < linha.length()) {
                char prox = linha.charAt(++i);
                atual.append(prox == 'n' ? '\n' : prox);
            } else if (c == SEP) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(new String[0]);
    }

    // ---- conversões usadas na leitura (campo vazio vira null) ----

    public static LocalDate data(String s) {
        return s == null || s.isBlank() ? null : LocalDate.parse(s);
    }

    public static LocalTime hora(String s) {
        return s == null || s.isBlank() ? null : LocalTime.parse(s);
    }

    public static Double numeroOuNulo(String s) {
        return s == null || s.isBlank() ? null : Double.parseDouble(s);
    }
}
