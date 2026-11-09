package br.unicamp.ic.mc322.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;

/**
 * Conjunto dos dados que definem o estado de uma {@link GerenciadorProducao}.
 * <p>
 * É o único ponto de contato entre a planta e a camada de persistência: o gerenciador
 * entrega o seu estado por meio de {@link GerenciadorProducao#obterEstado()} e aceita um
 * estado de volta em {@link GerenciadorProducao#restaurar(EstadoPlanta)}. Assim, as classes
 * de domínio não precisam conhecer arquivos nem XML, e quem lê o arquivo não precisa
 * conhecer os atributos internos do gerenciador.
 * </p>
 * <p>
 * As listas são cópias somente para leitura, mas as cópias são rasas: os objetos dentro delas
 * (máquinas, demandas, matéria-prima e produtos) são os mesmos da planta, e não cópias. Por
 * isso o estado serve para ser lido na hora de salvar e para montar uma planta nova, mas não
 * é uma foto independente: quem o recebe não deve alterar esses objetos.
 * </p>
 *
 * @param cenario cenário de operação da planta
 * @param orcamento orçamento disponível
 * @param estrategia estratégia ativa de produção, ou {@code null} se não houver nenhuma
 * @param contadorLotes quantidade de lotes já produzidos, usada para gerar o próximo código
 * @param totalRejeitados peças reprovadas na inspeção desde o início
 * @param materiaPrima estoque da matéria-prima
 * @param nomeLinha nome da linha de produção
 * @param maquinas máquinas da linha, na ordem do fluxo
 * @param catalogo modelos de produto cadastrados
 * @param demandas demandas registradas, da mais antiga para a mais recente
 * @param armazem peças aprovadas que estão no armazém
 *
 * @author Icaro Amaral
 * @author Eduardo Pontes
 */
public record EstadoPlanta(
        Cenario cenario,
        float orcamento,
        EstrategiaProducao estrategia,
        int contadorLotes,
        int totalRejeitados,
        MateriaPrima materiaPrima,
        String nomeLinha,
        List<Maquina> maquinas,
        List<Produto> catalogo,
        List<Demanda> demandas,
        List<Produto> armazem) {

    /**
     * Guarda cópias somente para leitura das listas informadas.
     *
     * @throws IllegalArgumentException se alguma das listas for nula
     */
    public EstadoPlanta {
        maquinas = copiar(maquinas, "maquinas");
        catalogo = copiar(catalogo, "catalogo");
        demandas = copiar(demandas, "demandas");
        armazem = copiar(armazem, "armazem");
    }

    private static <T> List<T> copiar(List<T> lista, String nome) {
        if (lista == null) {
            throw new IllegalArgumentException("A lista de " + nome + " deve ser informada.");
        }
        return Collections.unmodifiableList(new ArrayList<>(lista));
    }
}
