package br.unicamp.ic.mc322.medias;

import estudai.atividades.Avaliavel;
import estudai.excecoes.FormulaInvalidaException;
import estudai.medias.formula.Expressao;
import estudai.medias.formula.ParserDeFormula;
import estudai.modelo.Bonus;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Fórmula livre montada pelo usuário (F27), ex.: (P1 + 2*P2 + max(T1, T2)) / 4.
 * A fórmula de exame é opcional e usa as variáveis MEDIA e EXAME.
 */
public class MediaPersonalizada implements RegraMedia {
    private String formula;
    private Expressao arvore;
    private String formulaExame;
    private Expressao arvoreExame;

    public MediaPersonalizada(String formula, String formulaExame, Set<String> siglas) throws FormulaInvalidaException {
        ParserDeFormula parser = new ParserDeFormula();
        this.arvore = parser.interpretar(formula);
        this.formula = formula.trim();
        if (formulaExame != null && !formulaExame.isBlank()) {
            this.arvoreExame = parser.interpretar(formulaExame);
            this.formulaExame = formulaExame.trim();
            for (String v : arvoreExame.getVariaveis()) {
                if (!v.equals("MEDIA") && !v.equals("EXAME")) {
                    throw new FormulaInvalidaException("A fórmula de exame só pode usar MEDIA e EXAME (achei " + v + ").");
                }
            }
        }
        if (siglas != null) {
            validarRegra(siglas);
        }
    }

    /** Confere se todas as siglas usadas na fórmula existem na disciplina. */
    public void validarRegra(Set<String> siglas) throws FormulaInvalidaException {
        for (String v : arvore.getVariaveis()) {
            boolean existe = siglas.stream().anyMatch(s -> s.equalsIgnoreCase(v));
            if (!existe) {
                throw new FormulaInvalidaException("A sigla " + v + " não existe nesta disciplina.");
            }
        }
    }

    @Override
    public double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) throws FormulaInvalidaException {
        Map<String, Double> valores = new HashMap<>();
        for (Avaliavel a : avaliacoes) {
            valores.put(a.getSigla().toUpperCase(Locale.ROOT), RegraMedia.notaComBonus(a, bonus));
        }
        double media = RegraMedia.limitar(arvore.avaliar(valores));
        return RegraMedia.aplicarBonusFinal(media, bonus);
    }

    @Override
    public double calcularMediaFinalComExame(double media, double exame) {
        if (arvoreExame == null) {
            return RegraMedia.super.calcularMediaFinalComExame(media, exame);
        }
        try {
            return RegraMedia.limitar(arvoreExame.avaliar(Map.of("MEDIA", media, "EXAME", exame)));
        } catch (FormulaInvalidaException e) {
            return RegraMedia.super.calcularMediaFinalComExame(media, exame);
        }
    }

    @Override
    public double calcularNotaNecessaria(List<Avaliavel> avaliacoes, List<Bonus> bonus, double min)
            throws FormulaInvalidaException {
        return RegraMedia.buscarNotaNecessaria(this, avaliacoes, bonus, min);
    }

    public String getFormula() {
        return formula;
    }

    public String getFormulaExame() {
        return formulaExame;
    }

    @Override
    public String getDescricaoRegra() {
        return "Fórmula: " + formula;
    }

    @Override
    public String toString() {
        return getDescricaoRegra();
    }
}
