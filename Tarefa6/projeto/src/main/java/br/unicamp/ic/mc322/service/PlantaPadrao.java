package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.BracoEncaixotador;
import br.unicamp.ic.mc322.model.maquina.EstacaoTestes;
import br.unicamp.ic.mc322.model.maquina.Injetora;
import br.unicamp.ic.mc322.model.produto.CarcacaInferior;
import br.unicamp.ic.mc322.model.produto.CarcacaSuperior;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.ProtecaoLateral;
import br.unicamp.ic.mc322.strategy.EstrategiaLinhaDeMontagem;

/**
 * Monta a planta com os dados padrão do projeto.
 * <p>
 * É a planta usada na primeira execução, quando ainda não existe um arquivo de
 * persistência, e também a base dos testes automatizados.
 * </p>
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public final class PlantaPadrao {

    /** Classe utilitária, não é instanciada. */
    private PlantaPadrao() {
    }

    /**
     * Cria a planta padrão: estoque de plástico ABS, a linha com injetora, braço
     * encaixotador e estação de testes, os três moldes de produto e uma demanda vazia
     * para cada um deles.
     *
     * @param cenario cenário de operação da planta
     * @return planta nova, com a estratégia Linha de Montagem ativa
     * @throws IllegalArgumentException se o cenário for nulo
     */
    public static GerenciadorProducao criar(Cenario cenario) {
        MateriaPrima plasticoABS = new MateriaPrima("ABS000", "Plastico ABS", 5000, "g", 0.50f);
        GerenciadorProducao gerenciador = new GerenciadorProducao(plasticoABS, cenario);
        gerenciador.setEstrategia(new EstrategiaLinhaDeMontagem());

        gerenciador.registrarMaquina(new Injetora("Injetora CNC Alpha", cenario));
        gerenciador.registrarMaquina(new BracoEncaixotador("Braco Robotico KUKA", cenario));
        gerenciador.registrarMaquina(new EstacaoTestes("Estacao de Diagnostico ESI", cenario));

        Produto[] moldes = {
            new CarcacaSuperior("MOLDE-TOP"),
            new CarcacaInferior("MOLDE-BOT"),
            new ProtecaoLateral("MOLDE-LAT")
        };
        for (Produto molde : moldes) {
            gerenciador.registrarDemanda(new Demanda(molde.getTipo(), 0), molde);
        }
        return gerenciador;
    }
}
