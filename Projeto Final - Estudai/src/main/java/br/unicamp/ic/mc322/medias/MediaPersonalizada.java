package br.unicamp.ic.mc322.medias;

import br.unicamp.ic.mc322.atividades.Avaliavel;
import br.unicamp.ic.mc322.exceptions.FormulaInvalidaException;
import br.unicamp.ic.mc322.medias.formula.Expressao;
import br.unicamp.ic.mc322.medias.formula.ParserDeFormula;
import br.unicamp.ic.mc322.model.Bonus;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Fórmula livre montada pelo usuário (F27), ex.: (P1 + 2*P2 + max(T1, T2)) / 4.
 * A fórmula de exame é opcional, usa as variáveis MEDIA e EXAME e precisa usar EXAME.
 * Imutável: para mudar a fórmula, crie outra MediaPersonalizada.
 */
public final class MediaPersonalizada implements RegraMedia {
    private static final String VAR_MEDIA = "MEDIA";
    private static final String VAR_EXAME = "EXAME";

    private final String formula;
    private final Expressao arvore;
    private final String formulaExame;
    private final Expressao arvoreExame;

    /**
     * Interpreta as fórmulas. Se "siglas" não for null, confere também se a fórmula só usa siglas
     * que existem (a Disciplina confere de novo em setRegraMedia).
     */
    public MediaPersonalizada(String formula, String formulaExame, Set<String> siglas) throws FormulaInvalidaException {
        ParserDeFormula parser = new ParserDeFormula();
        this.arvore = parser.interpretar(formula);
        this.formula = formula.trim();
        if (arvore.getVariaveis().isEmpty()) {
            throw new FormulaInvalidaException("A fórmula precisa usar pelo menos uma avaliação (ex.: P1).");
        }
        if (formulaExame != null && !formulaExame.isBlank()) {
            this.arvoreExame = parser.interpretar(formulaExame);
            this.formulaExame = formulaExame.trim();
            for (String v : arvoreExame.getVariaveis()) {
                if (!v.equals(VAR_MEDIA) && !v.equals(VAR_EXAME)) {
                    throw new FormulaInvalidaException("A fórmula de exame só pode usar MEDIA e EXAME (achei " + v + ").");
                }
            }
            if (!arvoreExame.getVariaveis().contains(VAR_EXAME)) {
                throw new FormulaInvalidaException("A fórmula de exame precisa usar a nota do EXAME.");
            }
        } else {
            this.arvoreExame = null;
            this.formulaExame = null;
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

    /** A fórmula usa esta sigla? (usado para não deixar remover uma avaliação que a fórmula usa) */
    public boolean usaSigla(String sigla) {
        return sigla != null && arvore.getVariaveis().contains(sigla.toUpperCase(Locale.ROOT));
    }

    @Override
    public double calcularMedia(List<Avaliavel> avaliacoes, List<Bonus> bonus) throws FormulaInvalidaException {
        Map<String, Double> valores = new HashMap<>();
        for (Avaliavel a : avaliacoes) {
            valores.put(a.getSigla().toUpperCase(Locale.ROOT), CalculoMedia.notaComBonus(a, bonus));
        }
        double media = CalculoMedia.limitar(arvore.avaliar(valores));
        return CalculoMedia.aplicarBonusFinal(media, bonus);
    }

    /** Sem fórmula de exame, usa a regra padrão. Com fórmula, um erro de cálculo é avisado em vez de ignorado. */
    @Override
    public double calcularMediaFinalComExame(double media, double exame) throws FormulaInvalidaException {
        if (arvoreExame == null) {
            return RegraMedia.super.calcularMediaFinalComExame(media, exame);
        }
        return CalculoMedia.limitar(arvoreExame.avaliar(Map.of(VAR_MEDIA, media, VAR_EXAME, exame)));
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
