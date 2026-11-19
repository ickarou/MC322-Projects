package br.unicamp.ic.mc322;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.service.PlantaPadrao;

/**
 * Escolha feita no inicio da aplicacao: recuperar o estado salvo ou comecar uma planta nova.
 * <p>
 * Cada teste responde ao menu com um roteiro de entrada e usa a pasta temporaria do JUnit,
 * de modo que o arquivo real da aplicacao nunca e tocado. O que o menu escreve no terminal
 * e capturado para nao poluir a saida dos testes.
 * </p>
 */
@DisplayName("Main: inicio da aplicacao")
class MainTest {

    @TempDir
    Path pasta;

    private PrintStream saidaOriginal;
    private ByteArrayOutputStream saida;
    private Path arquivo;
    private PersistenciaPlanta persistencia;

    @BeforeEach
    void preparar() {
        saidaOriginal = System.out;
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
    }

    @AfterEach
    void restaurarSaida() {
        System.setOut(saidaOriginal);
    }

    private GerenciadorProducao iniciar(String entrada) {
        return Main.iniciarPlanta(new Scanner(entrada), persistencia);
    }

    private String textoMostrado() {
        return saida.toString(StandardCharsets.UTF_8);
    }

    /** Salva uma planta apocaliptica com estoque alterado, facil de distinguir de uma planta nova. */
    private byte[] salvarPlantaReconhecivel() throws IOException {
        GerenciadorProducao salva = PlantaPadrao.criar(Cenario.APOCALIPTICO);
        salva.comprarMateriaPrima(123);
        persistencia.salvar(salva);
        return Files.readAllBytes(arquivo);
    }

    @Test
    void semEstadoSalvoCriaAPlantaPadraoDoCenarioEscolhidoSemPerguntarSobreRecuperar() {
        GerenciadorProducao planta = iniciar("2\n");

        assertEquals("Apocaliptico", planta.getNomeCenarioAtivo());
        assertFalse(textoMostrado().contains("ESTADO SALVO ENCONTRADO"));
        assertFalse(Files.exists(arquivo), "Iniciar nao deve criar o arquivo");
    }

    @Test
    void comEstadoSalvoOOperadorPodeRecuperarAPlanta() throws IOException {
        salvarPlantaReconhecivel();

        GerenciadorProducao planta = iniciar("1\n");

        assertTrue(textoMostrado().contains("ESTADO SALVO ENCONTRADO"));
        assertEquals("Apocaliptico", planta.getNomeCenarioAtivo());
        assertEquals(5000 + 123, planta.getMateriaPrima().getQuantidade());
    }

    @Test
    void comEstadoSalvoOOperadorPodeIniciarUmaPlantaNovaSemMexerNoArquivo() throws IOException {
        byte[] antes = salvarPlantaReconhecivel();

        GerenciadorProducao planta = iniciar("2\n1\n");

        assertEquals("Ideal", planta.getNomeCenarioAtivo());
        assertEquals(5000, planta.getMateriaPrima().getQuantidade());
        assertArrayEquals(antes, Files.readAllBytes(arquivo), "O arquivo so muda se o operador salvar depois");
    }

    @Test
    void entradaInvalidaNaEscolhaEPedidaDeNovo() throws IOException {
        salvarPlantaReconhecivel();

        GerenciadorProducao planta = iniciar("7\nabc\n1\n");

        assertEquals("Apocaliptico", planta.getNomeCenarioAtivo());
        assertTrue(textoMostrado().contains("[ERRO]"));
    }

    @Test
    void arquivoSalvoInvalidoGeraAvisoIniciaPlantaPadraoEFicaIntacto() throws IOException {
        Files.writeString(arquivo, "isto nao e um XML", StandardCharsets.UTF_8);
        byte[] antes = Files.readAllBytes(arquivo);

        GerenciadorProducao planta = iniciar("1\n1\n");

        assertEquals("Ideal", planta.getNomeCenarioAtivo());
        assertTrue(textoMostrado().contains("[AVISO]"));
        assertArrayEquals(antes, Files.readAllBytes(arquivo), "Um arquivo invalido nao pode ser alterado nem apagado");
    }
}
