package br.unicamp.ic.mc322.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.w3c.dom.Document;

import br.unicamp.ic.mc322.exception.ArquivoInexistenteException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.service.EstadoPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;

/**
 * Salva a planta em um arquivo XML e a recupera dele.
 * <p>
 * Esta e a unica classe que o resto da aplicacao precisa conhecer da camada de persistencia.
 * Ela cuida do arquivo (existencia, leitura, gravacao) e traduz as falhas para as excecoes da
 * planta; a conversao entre objetos e XML fica em {@link EscritorXmlPlanta} e
 * {@link LeitorXmlPlanta}, e as classes de dominio nao sabem que o arquivo existe.
 * </p>
 * <p>
 * O arquivo usado pela aplicacao fica definido aqui ({@link #ARQUIVO_PADRAO}), de modo que a
 * interface apenas pede para salvar ou carregar. Os metodos que recebem um {@link Path} continuam
 * disponiveis para gravar em qualquer outro lugar, como a pasta temporaria dos testes.
 * </p>
 * <p>
 * Duas garantias protegem o sistema contra estados inconsistentes:
 * </p>
 * <ul>
 *   <li>o carregamento le e valida tudo antes de entregar uma planta nova; se algo falhar, nada
 *       e entregue e quem chamou continua com a planta que ja tinha;</li>
 *   <li>o salvamento grava primeiro em um arquivo temporario, na mesma pasta do destino, e so
 *       depois o move para o destino. Uma falha durante a gravacao deixa o arquivo anterior como
 *       estava. A troca final e atomica quando o sistema de arquivos aceita esse tipo de
 *       movimentacao; quando nao aceita, e feita uma substituicao comum, que nao tem a mesma
 *       garantia: uma interrupcao no exato momento da troca pode deixar o arquivo de destino
 *       incompleto.</li>
 * </ul>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public class PersistenciaPlanta {

    /** Arquivo em que a aplicacao guarda o estado da planta, relativo a pasta de execucao. */
    public static final Path ARQUIVO_PADRAO = Path.of("dados", "planta.xml");

    private final Path arquivoPadrao;

    /** Cria o servico de persistencia, que usa {@link #ARQUIVO_PADRAO} quando nenhum caminho e informado. */
    public PersistenciaPlanta() {
        this(ARQUIVO_PADRAO);
    }

    /**
     * Cria o servico de persistencia com outro arquivo como padrao.
     *
     * @param arquivoPadrao arquivo usado por {@link #salvar(GerenciadorProducao)} e {@link #carregar()}
     * @throws IllegalArgumentException se o caminho for nulo
     */
    public PersistenciaPlanta(Path arquivoPadrao) {
        if (arquivoPadrao == null) {
            throw new IllegalArgumentException("O caminho do arquivo padrao deve ser informado.");
        }
        this.arquivoPadrao = arquivoPadrao;
    }

    /** Consulta o arquivo usado quando nenhum caminho e informado.
     * @return caminho do arquivo padrao
     */
    public Path getArquivoPadrao() {
        return arquivoPadrao;
    }

    /**
     * Informa se ja existe algo no arquivo padrao, sem verificar se o conteudo e valido.
     * Um arquivo existente mas invalido so e apontado pelo {@link #carregar()}.
     *
     * @return {@code true} se o caminho do arquivo padrao existir
     */
    public boolean existeEstadoSalvo() {
        return Files.exists(arquivoPadrao);
    }

    /**
     * Grava o estado atual da planta no arquivo padrao.
     *
     * @param planta planta a ser salva
     * @throws IllegalArgumentException se a planta for nula
     * @throws PersistenciaException se o arquivo nao puder ser gravado
     * @see #salvar(GerenciadorProducao, Path)
     */
    public void salvar(GerenciadorProducao planta) {
        salvar(planta, arquivoPadrao);
    }

    /**
     * Le o arquivo padrao e reconstroi uma planta nova a partir dele.
     *
     * @return planta reconstruida
     * @throws ArquivoInexistenteException se o arquivo nao existir
     * @throws DadosInvalidosException se o arquivo estiver vazio, mal formado ou contrariar as regras do dominio
     * @throws PersistenciaException se o arquivo nao puder ser lido
     * @see #carregar(Path)
     */
    public GerenciadorProducao carregar() {
        return carregar(arquivoPadrao);
    }

    /**
     * Grava o estado atual da planta no arquivo, substituindo o conteudo anterior.
     * As pastas que faltarem no caminho sao criadas.
     *
     * @param planta planta a ser salva
     * @param arquivo caminho do arquivo XML
     * @throws IllegalArgumentException se a planta ou o caminho forem nulos
     * @throws PersistenciaException se o arquivo nao puder ser gravado
     */
    public void salvar(GerenciadorProducao planta, Path arquivo) {
        if (planta == null || arquivo == null) {
            throw new IllegalArgumentException("A planta e o caminho do arquivo devem ser informados.");
        }

        EstadoPlanta estado = planta.obterEstado();
        byte[] conteudo;
        try {
            conteudo = EscritorXmlPlanta.gerar(estado);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Nao foi possivel salvar a planta: " + e.getMessage(), e);
        }

        Path destino = arquivo.toAbsolutePath();
        Path temporario = null;
        try {
            Files.createDirectories(destino.getParent());
            temporario = Files.createTempFile(destino.getParent(), destino.getFileName().toString(), ".tmp");
            Files.write(temporario, conteudo);
            moverParaDestino(temporario, destino);
        } catch (IOException e) {
            IOException falhaNaLimpeza = apagarTemporario(temporario);
            String mensagem = "Nao foi possivel gravar o arquivo " + arquivo + ": " + descrever(e);
            if (falhaNaLimpeza != null) {
                mensagem += " (alem disso, o arquivo temporario " + temporario + " nao pode ser removido: "
                        + descrever(falhaNaLimpeza) + ")";
            }
            PersistenciaException falha = new PersistenciaException(mensagem, e);
            if (falhaNaLimpeza != null) {
                falha.addSuppressed(falhaNaLimpeza);
            }
            throw falha;
        }
    }

    /**
     * Le o arquivo e reconstroi uma planta nova a partir dele.
     * <p>
     * O conteudo e totalmente lido e validado antes de a planta ser criada. A planta devolvida
     * e independente de qualquer outra em memoria: quem chama deve passar a usar a nova e
     * descartar a anterior, sem combinar as duas.
     * </p>
     *
     * @param arquivo caminho do arquivo XML
     * @return planta reconstruida
     * @throws IllegalArgumentException se o caminho for nulo
     * @throws ArquivoInexistenteException se o arquivo nao existir
     * @throws DadosInvalidosException se o arquivo estiver vazio, mal formado ou contrariar as regras do dominio
     * @throws PersistenciaException se o arquivo nao puder ser lido
     */
    public GerenciadorProducao carregar(Path arquivo) {
        if (arquivo == null) {
            throw new IllegalArgumentException("O caminho do arquivo deve ser informado.");
        }
        if (!Files.exists(arquivo)) {
            throw new ArquivoInexistenteException("Nao existe arquivo de persistencia em: " + arquivo);
        }
        if (!Files.isRegularFile(arquivo)) {
            throw new PersistenciaException("O caminho " + arquivo + " nao e um arquivo.");
        }

        byte[] conteudo;
        try {
            conteudo = Files.readAllBytes(arquivo);
        } catch (IOException e) {
            throw new PersistenciaException("Nao foi possivel ler o arquivo " + arquivo + ": " + descrever(e), e);
        }
        if (new String(conteudo, StandardCharsets.UTF_8).isBlank()) {
            throw new DadosInvalidosException("O arquivo de persistencia esta vazio: " + arquivo);
        }

        Document documento = LeitorXmlPlanta.analisar(conteudo);
        try {
            return GerenciadorProducao.restaurar(LeitorXmlPlanta.ler(documento));
        } catch (PersistenciaException e) {
            throw e;
        } catch (IllegalArgumentException | PlantaException e) {
            throw new DadosInvalidosException("Os dados do arquivo " + arquivo
                    + " sao incompativeis com as regras da planta: " + e.getMessage(), e);
        }
    }

    /** Troca o destino pelo temporario, de forma atomica quando o sistema de arquivos permitir. */
    private static void moverParaDestino(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Tenta remover o arquivo temporario de uma gravacao que falhou.
     * A falha da limpeza nao substitui a falha original da gravacao: e devolvida para quem
     * chamou, que a anexa ao erro principal.
     *
     * @param temporario arquivo a remover, ou {@code null} se ele nem chegou a ser criado
     * @return a falha ao remover o arquivo, ou {@code null} se a remocao foi bem sucedida
     */
    static IOException apagarTemporario(Path temporario) {
        if (temporario == null) {
            return null;
        }
        try {
            Files.deleteIfExists(temporario);
            return null;
        } catch (IOException e) {
            return e;
        }
    }

    private static String descrever(IOException e) {
        return (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
    }
}
