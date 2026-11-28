package br.unicamp.ic.mc322.persistencia;

import br.unicamp.ic.mc322.atividades.Atividade;
import br.unicamp.ic.mc322.atividades.AtividadeAvaliativa;
import br.unicamp.ic.mc322.atividades.Leitura;
import br.unicamp.ic.mc322.atividades.Prova;
import br.unicamp.ic.mc322.atividades.TipoAtividade;
import br.unicamp.ic.mc322.atividades.Trabalho;
import br.unicamp.ic.mc322.biblioteca.Apostila;
import br.unicamp.ic.mc322.biblioteca.ListaExercicios;
import br.unicamp.ic.mc322.biblioteca.Livro;
import br.unicamp.ic.mc322.biblioteca.MaterialBiblioteca;
import br.unicamp.ic.mc322.exceptions.ArquivoCorrompidoException;
import br.unicamp.ic.mc322.exceptions.EstudaiException;
import br.unicamp.ic.mc322.medias.MediaAritmetica;
import br.unicamp.ic.mc322.medias.MediaPersonalizada;
import br.unicamp.ic.mc322.medias.MediaPonderada;
import br.unicamp.ic.mc322.medias.RegraMedia;
import br.unicamp.ic.mc322.model.Disciplina;
import br.unicamp.ic.mc322.model.Prioridade;
import br.unicamp.ic.mc322.model.StatusAtividade;

import java.nio.file.Path;

/**
 * Factory: lê uma linha CSV e recria o objeto do tipo certo (subclasse de Atividade,
 * de MaterialBiblioteca ou implementação de RegraMedia).
 */
public class FabricaEntidadesCSV {

    public FabricaEntidadesCSV() {
    }

    /**
     * Campos: id;tipo;titulo;prazo;prioridade;status; depois os de cada tipo.
     * Do status só importa se é CONCLUIDA (o usuário marcou como concluída); o resto é recalculado.
     * PROVA: sigla;peso;nota;conteudo;substitutiva;siglaSubstituida
     * TRABALHO: sigla;peso;nota;emGrupo;localEntrega
     * LEITURA: totalPaginas;paginasLidas
     */
    public Atividade criarAtividadeDeCSV(String linha, Disciplina d) throws ArquivoCorrompidoException {
        String[] c = Csv.separar(linha);
        try {
            TipoAtividade tipo = TipoAtividade.valueOf(c[1]);
            String titulo = c[2];
            java.time.LocalDate prazo = Csv.data(c[3]);
            Prioridade prio = Prioridade.valueOf(c[4]);
            StatusAtividade status = StatusAtividade.valueOf(c[5]);
            Atividade a = switch (tipo) {
                case PROVA -> new Prova(titulo, prazo, prio, d, c[6], Double.parseDouble(c[7]), c[9]);
                case TRABALHO -> new Trabalho(titulo, prazo, prio, d, c[6], Double.parseDouble(c[7]),
                        Boolean.parseBoolean(c[9]), c[10]);
                case LEITURA -> new Leitura(titulo, prazo, prio, d, Integer.parseInt(c[6]), Integer.parseInt(c[7]));
            };
            if (a instanceof AtividadeAvaliativa av) {
                Double nota = Csv.numeroOuNulo(c[8]);
                if (nota != null) {
                    av.lancarNota(nota);
                }
            }
            a.restaurar(c[0], status == StatusAtividade.CONCLUIDA);
            return a;
        } catch (EstudaiException | RuntimeException e) {
            throw new ArquivoCorrompidoException("atividade inválida (" + e.getMessage() + ")");
        }
    }

    /** Sigla da prova substituída, guardada na linha de uma PROVA (vazia se não for substitutiva). */
    public String siglaSubstituidaDeCSV(String linha) {
        String[] c = Csv.separar(linha);
        return c.length > 11 && TipoAtividade.PROVA.name().equals(c[1]) && Boolean.parseBoolean(c[10]) ? c[11] : "";
    }

    /**
     * Campos: id;tipo;titulo;copia;original;dataAdicao; depois os de cada tipo.
     * LIVRO: autor;edicao — APOSTILA: topico — LISTA: numero;gabarito
     */
    public MaterialBiblioteca criarMaterialDeCSV(String linha) throws ArquivoCorrompidoException {
        String[] c = Csv.separar(linha);
        try {
            Path copia = Path.of(c[3]);
            MaterialBiblioteca m = switch (c[1]) {
                case "LIVRO" -> new Livro(c[2], copia, c[6], Integer.parseInt(c[7]));
                case "APOSTILA" -> new Apostila(c[2], copia, c[6]);
                case "LISTA" -> new ListaExercicios(c[2], copia, Integer.parseInt(c[6]), Boolean.parseBoolean(c[7]));
                default -> throw new ArquivoCorrompidoException("tipo de material desconhecido: " + c[1]);
            };
            if (!c[4].isBlank()) {
                m.setCaminhoArquivoOriginal(Path.of(c[4]));
            }
            m.restaurar(c[0], Csv.data(c[5]));
            return m;
        } catch (ArquivoCorrompidoException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ArquivoCorrompidoException("material inválido (" + e.getMessage() + ")");
        }
    }

    /** Campos: tipo;formula;formulaExame. tipo = ARITMETICA, PONDERADA ou PERSONALIZADA. */
    public RegraMedia criarRegraMediaDeCSV(String linha) throws ArquivoCorrompidoException {
        String[] c = Csv.separar(linha);
        try {
            return switch (c[0]) {
                case "ARITMETICA" -> new MediaAritmetica();
                case "PONDERADA" -> new MediaPonderada();
                // As siglas são conferidas pela PersistenciaCSV depois que as atividades são carregadas.
                case "PERSONALIZADA" -> new MediaPersonalizada(c[1], c.length > 2 ? c[2] : null, null);
                default -> throw new ArquivoCorrompidoException("regra de média desconhecida: " + c[0]);
            };
        } catch (ArquivoCorrompidoException e) {
            throw e;
        } catch (EstudaiException | RuntimeException e) {
            throw new ArquivoCorrompidoException("regra de média inválida (" + e.getMessage() + ")");
        }
    }

    /** Caminho inverso de criarRegraMediaDeCSV. */
    public String regraParaCSV(RegraMedia regra) {
        if (regra instanceof MediaPersonalizada p) {
            return Csv.juntar("PERSONALIZADA", p.getFormula(), p.getFormulaExame());
        }
        if (regra instanceof MediaPonderada) {
            return Csv.juntar("PONDERADA");
        }
        return Csv.juntar("ARITMETICA");
    }
}
