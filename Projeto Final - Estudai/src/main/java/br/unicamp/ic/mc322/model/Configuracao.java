package br.unicamp.ic.mc322.model;

import estudai.excecoes.DadoInvalidoException;
import estudai.persistencia.Csv;
import estudai.persistencia.PersistivelCSV;

import java.nio.file.Path;

/** Configurações gerais do aplicativo (F41) e o semestre que estava ativo ao fechar. */
public class Configuracao implements PersistivelCSV {
    public static final double MEDIA_APROVACAO_PADRAO = 5.0;
    public static final double MEDIA_EXAME_PADRAO = 2.5;

    private int diasAntecedenciaAviso = 3;
    private double mediaMinimaAprovacao = MEDIA_APROVACAO_PADRAO;
    private double mediaMinimaExame = MEDIA_EXAME_PADRAO;
    private Path diretorioDados;
    private String idSemestreAtivo;

    public Configuracao() {
        this.diretorioDados = Path.of(System.getProperty("user.home"), "estudai-dados");
    }

    public int getDiasAntecedenciaAviso() {
        return diasAntecedenciaAviso;
    }

    public void setDiasAntecedenciaAviso(int dias) throws DadoInvalidoException {
        if (dias < 0 || dias > 60) {
            throw new DadoInvalidoException("A antecedência dos avisos deve ficar entre 0 e 60 dias.");
        }
        this.diasAntecedenciaAviso = dias;
    }

    public double getMediaMinimaAprovacao() {
        return mediaMinimaAprovacao;
    }

    public double getMediaMinimaExame() {
        return mediaMinimaExame;
    }

    public Path getDiretorioDados() {
        return diretorioDados;
    }

    public void setDiretorioDados(Path diretorioDados) {
        this.diretorioDados = diretorioDados;
    }

    public String getIdSemestreAtivo() {
        return idSemestreAtivo;
    }

    public void setIdSemestreAtivo(String idSemestreAtivo) {
        this.idSemestreAtivo = idSemestreAtivo;
    }

    @Override
    public String toCSV() {
        return Csv.juntar(diasAntecedenciaAviso, mediaMinimaAprovacao, mediaMinimaExame, idSemestreAtivo);
    }

    /** Usado pela persistência ao ler config.csv. */
    public void restaurar(int dias, double aprovacao, double exame, String idAtivo) {
        this.diasAntecedenciaAviso = dias;
        this.mediaMinimaAprovacao = aprovacao;
        this.mediaMinimaExame = exame;
        this.idSemestreAtivo = idAtivo == null || idAtivo.isBlank() ? null : idAtivo;
    }
}
