# Fabrica de Scanners Automotivos
## Modelo: KTS 590

## Alunos:
### Eduardo Fernandes Sales Pontes - 270802
### Icaro Kwiatkowski do Amaral - 186535

Projeto da disciplina MC322 (Programação Orientada a Objetos, UNICAMP): simulação, pelo terminal, do controle de uma fábrica de scanners automotivos KTS 590. O operador registra demandas, compra matéria-prima, escolhe a estratégia de produção, fabrica lotes e acompanha o estado das máquinas.

Ao longo das tarefas, o projeto passou por três etapas principais:

- **Tarefa 4:** build com Gradle e documentação de todo o código com Javadoc, junto com o tratamento de exceções do sistema.
- **Tarefa 5:** composição entre cada máquina e as suas partes (motor, sensor e atuadores) e agrupamento das máquinas em uma linha de produção.
- **Tarefa 6 (esta versão):** persistência do estado da planta em XML, com recuperação dos objetos em outra execução, testes automatizados com JUnit 5 e organização do código em pacotes.

## Compilação, execução e testes

```
./gradlew build     # compila, executa os testes e monta o projeto
./gradlew run       # abre a aplicação interativa no terminal
./gradlew test      # executa apenas os testes automatizados
./gradlew javadoc   # gera a documentação em build/docs/javadoc
```

No Windows, use o arquivo `gradlew.bat`: `.\gradlew.bat build`, `.\gradlew.bat run`, `.\gradlew.bat test` e `.\gradlew.bat javadoc`.

O `build.gradle` declara o grupo (`br.unicamp.ic.mc322`) e a versão (`6.0`) do projeto e fixa UTF-8 tanto na compilação quanto na execução (`run`).

O relatório HTML dos testes é gerado em `build/reports/tests/test/index.html` e lista cada teste aprovado ou reprovado. Os testes não usam o teclado nem o terminal e não precisam que a aplicação tenha sido executada antes.

## Organização do projeto

```
src/
  main/java/br/unicamp/ic/mc322/
    Main.java            Interface de terminal (menus)
    model/               Entidades do domínio: Demanda, MateriaPrima, LinhaDeProducao, Cenario, StatusDemanda, Auditavel
      maquina/           Maquina e subclasses, e as partes da composição (Motor, Atuador, SensorDeTemperatura)
      produto/           Produto e subclasses (CarcacaSuperior, CarcacaInferior, ProtecaoLateral)
    service/             GerenciadorProducao (coordena a planta), EstadoPlanta, PlantaPadrao
    strategy/            Padrão Strategy: estratégias de seleção de demandas
    persistence/         Leitura e escrita do XML (PersistenciaPlanta e classes de apoio)
    exception/           Exceções da planta, incluindo as de persistência
  test/java/br/unicamp/ic/mc322/
    (testes JUnit 5, em pacotes que espelham os de main)
```

As dependências seguem uma direção só: `persistence` conhece `service` e `model`, mas as classes de `model` e `service` não conhecem arquivos nem XML.

## Tarefa 5: Composição, Fases de Construção e Acoplamento

Nesta etapa, a máquina passou a ser o "Todo" de uma composição. Cada `Maquina` cria no seu construtor um `Motor` (1:1), um `SensorDeTemperatura` (1:1) e uma lista de `Atuador` (1:N), todos herdando de `ComponenteMaquina`. As máquinas ficam agrupadas em uma `LinhaDeProducao` (agregação), cuja lista é protegida com `Collections.unmodifiableList()`.

- O Gerenciador e o Main falam apenas com a linha e com as máquinas (Lei de Deméter); nenhuma parte é exposta.
- A auditoria percorre Linha > Máquina > Partes e exibe um relatório hierárquico e indentado.
- O estado das partes afeta o desempenho: motor e sensor gastos aumentam a chance de falha da máquina e atuadores gastos deixam um risco extra de defeito nas peças.
- O menu principal ganhou a opção `[ 7 ] Linha de Producao`, com o fluxo da linha, a auditoria estrutural e a manutenção.
- O diagrama UML está em `uml_tarefa5.png` (fonte editável em `uml_tarefa5.drawio`).

## Tarefa 6: Persistência de Objetos e Testes Automatizados com JUnit

Nesta etapa, o estado da planta passou a poder ser salvo em um arquivo XML e recuperado em outra execução, o código foi reorganizado em pacotes e foi criada uma suíte de testes com JUnit 5.

### Como a persistência funciona

- **Salvar** (`[ 8 ]` no menu): grava o estado inteiro da planta em `dados/planta.xml`, na pasta em que a aplicação é executada. Salvar de novo atualiza o arquivo. A gravação é feita primeiro em um arquivo temporário, na mesma pasta, e só depois ele substitui o anterior; assim, uma falha durante a gravação deixa o último estado salvo como estava. A troca final é atômica quando o sistema de arquivos aceita esse tipo de movimentação; quando não aceita, é feita uma substituição comum, que não tem a mesma garantia (veja Limitações). Se o temporário de uma gravação que falhou não puder ser removido, essa segunda falha é informada junto com a principal, e não escondida.
- **Carregar** (`[ 9 ]` no menu): lê e valida o arquivo inteiro antes de qualquer mudança. Só se tudo estiver correto o estado atual é substituído pelo recuperado (nunca combinado com ele). Se algo falhar, a planta em uso continua exatamente como estava e o arquivo não é alterado.
- **Início da aplicação**: se já existe um arquivo salvo, a aplicação pergunta se o operador quer `[ 1 ]` recuperar a planta salva ou `[ 2 ]` iniciar uma planta nova. A planta nova não apaga nem altera o arquivo anterior: ele só é substituído se o operador salvar depois (`[ 8 ]` ou ao encerrar). Se o arquivo existir mas estiver inválido e o operador escolher recuperá-lo, a aplicação mostra o motivo, mantém o arquivo como está e inicia a planta padrão. Se o arquivo ainda não existe (primeira execução), isso é tratado como uma situação normal: a aplicação avisa, pergunta o cenário e inicia a planta padrão (`PlantaPadrao`).
- **Encerrar** (`[ 0 ]`): pergunta se o estado deve ser salvo. Encerrar sem salvar não apaga nem altera o que já estava salvo. Se o salvamento escolhido falhar, o programa não fecha e mostra o erro.
- **Validar**: a estrutura do XML (elementos e atributos obrigatórios, tipos dos valores, nomes de cenário, status, estratégia e tipos conhecidos) é conferida pelo leitor. As regras do domínio são conferidas pelas próprias classes recriadas (os construtores e métodos de `Demanda`, `MateriaPrima`, `Maquina`, `Produto` e `GerenciadorProducao` lançam exceção para dados inválidos, e o armazém só aceita peças com status de aprovadas), de modo que um objeto recuperado respeita as mesmas invariantes de um objeto criado normalmente.

### Separação de responsabilidades

| Classe | Papel |
|---|---|
| `PersistenciaPlanta` | Única porta de entrada: `salvar(planta)` e `carregar()` usam o arquivo padrão (`ARQUIVO_PADRAO`, que é `dados/planta.xml`) definido nesta classe, e `existeEstadoSalvo()` diz se ele existe. As versões `salvar(planta, arquivo)` e `carregar(arquivo)` trabalham com qualquer caminho, como a pasta temporária dos testes. Cuida do arquivo, da gravação segura e da tradução das falhas para exceções da planta. |
| `EscritorXmlPlanta` | Converte o estado da planta em XML. |
| `LeitorXmlPlanta` | Converte o XML em estado da planta e valida o que lê. |
| `RegistroDeTipos` | Relaciona o nome gravado no arquivo com a classe concreta (máquinas, produtos e estratégias). |
| `TagsXml` | Nomes dos elementos e atributos, compartilhados por escritor e leitor (sem texto repetido). |
| `EstadoPlanta` (em `service`) | Conjunto dos dados que definem o estado da planta. É o único contato entre o gerenciador e a persistência: `obterEstado()` entrega esses dados e `GerenciadorProducao.restaurar(...)` reconstrói uma planta nova a partir deles. As listas são cópias somente para leitura, mas rasas: os objetos dentro delas são os mesmos da planta (veja Limitações). |

As classes de domínio não sabem que existe um arquivo. Para ser recuperada, cada uma só oferece o que já é natural a ela: `Demanda.restaurar(...)`, `Maquina.getSaudeDasPartes()` e `Maquina.restaurarSaudeDasPartes(...)`.

### A composição na recuperação

O arquivo não guarda as partes das máquinas como objetos independentes. Para cada `<maquina>`, o leitor cria a máquina pelo tipo gravado (`Injetora`, `BracoEncaixotador` ou `EstacaoTestes`) e é o construtor dela que cria o motor, o sensor e os atuadores, exatamente como na execução normal. Só depois a saúde gravada de cada parte é devolvida, pelo nome, e a máquina confere se as partes do arquivo são exatamente as que ela possui (nome, quantidade, sem repetição, saúde entre 0 e 100). Nenhuma parte é criada fora da máquina nem fica referenciada em dois lugares. A configuração gravada (capacidade, custo de operação e probabilidade de falha) é comparada com a que o código define para aquele tipo de máquina no cenário do arquivo; se for diferente, o arquivo é recusado como incompatível.

### Estrutura do XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<planta versao="1">
    <cenario>IDEAL</cenario>
    <orcamento>22100.0</orcamento>
    <estrategia>Linha de Montagem</estrategia>
    <contadorLotes>1</contadorLotes>
    <totalRejeitados>0</totalRejeitados>
    <materiaPrima id="ABS000">
        <nome>Plastico ABS</nome>
        <quantidade>5100</quantidade>
        <unidade>g</unidade>
        <custoPorUnidade>0.5</custoPorUnidade>
    </materiaPrima>
    <linhaDeProducao nome="Linha Principal">
        <maquina nome="Injetora CNC Alpha" tipo="Injetora">
            <capacidadeMaxima>5000</capacidadeMaxima>
            <custoOperacao>500.0</custoOperacao>
            <probabilidadeFalha>0.075</probabilidadeFalha>
            <componentes>
                <componente nome="Motor" saude="98.0"/>
                <componente nome="Sensor" saude="100.0"/>
                <componente nome="Atuador 1" saude="97.2"/>
                <componente nome="Atuador 2" saude="100.0"/>
            </componentes>
        </maquina>
        ...
    </linhaDeProducao>
    <catalogo>
        <molde id="MOLDE-TOP" tipo="Carcaca Superior"/>
        ...
    </catalogo>
    <demandas>
        <demanda status="PENDENTE" tipo="Carcaca Superior">
            <quantidade>0</quantidade>
            <custoUnitario>800.0</custoUnitario>
        </demanda>
        ...
    </demandas>
    <armazem>
        <produto id="LOTE-001-MOLDE-TOP-1" lote="LOTE-001" tipo="Carcaca Superior">
            <status>Aprovado</status>
            <probabilidadeFalha>0.09</probabilidadeFalha>
        </produto>
        ...
    </armazem>
</planta>
```

O atributo `versao` do elemento raiz permite recusar, com uma mensagem clara, arquivos de um formato que a aplicação não conhece.

### Tratamento de erros de persistência

Todas as falhas viram exceções da hierarquia `PlantaException` (a mesma tratada pelos menus, que mostram a mensagem e voltam ao menu sem encerrar o programa):

| Situação | Exceção |
|---|---|
| Arquivo inexistente | `ArquivoInexistenteException` |
| Arquivo vazio, XML mal formado, estrutura diferente da esperada, valores que não são números, tipos desconhecidos | `DadosInvalidosException` |
| Dados que contrariam o domínio (orçamento ou estoque negativo, saúde fora de 0 a 100, demanda CONCLUIDA com peças pendentes, demanda ou peça de um produto que não está no catálogo, peça no armazém que não está aprovada (status desconhecido ou de reprovação), lote maior que o contador, máquina com configuração diferente da esperada, itens repetidos) | `DadosInvalidosException` |
| Falha de leitura ou de escrita (permissão, pasta que não pode ser criada, caminho que não é arquivo) | `PersistenciaException` |

A leitura do XML rejeita declarações `DOCTYPE` e entidades externas.

## Escopo e Limitações

### Dados persistidos versus não persistidos

**Persistidos (obrigatórios pelo enunciado):**

- **Estoque de matéria-prima:** id, nome, quantidade, unidade e custo por unidade. Sem o saldo, a planta recuperada produziria ou compraria de forma diferente da original.
- **Máquinas e subcomponentes (composição):** tipo, nome, configuração (capacidade, custo de operação, probabilidade de falha) e a saúde exata (com casas decimais) de cada parte (motor, sensor e cada atuador). A saúde é o que de fato varia com o uso e decide se uma máquina está quebrada, precisa de manutenção ou falha mais.
- **Demandas e seus estados:** tipo do produto, quantidade ainda pendente, `StatusDemanda` e custo unitário estimado. Todas as demandas são salvas, inclusive as concluídas e canceladas, na ordem em que foram registradas, porque a demanda mais recente de cada tipo é a que representa o pedido atual. EM_PRODUCAO é um estado transitório, que só existe enquanto um lote está na linha, e o sistema não guarda até onde a produção chegou; por isso, uma demanda salva como EM_PRODUCAO é recuperada como PENDENTE, para que o lote seja avaliado de novo por inteiro (o mesmo que `atualizarQuantidade` já faz com uma demanda em andamento). Como o menu só permite salvar com a produção parada, isso só ocorre em arquivos feitos à mão ou gerados por outro código.

**Persistidos (opcionais, escolhidos pela dupla):**

- **Orçamento atual e cenário:** o orçamento é parte do estado financeiro, e o cenário define o orçamento inicial e os fatores de falha e desgaste com que as máquinas foram criadas.
- **Estratégia de produção ativa:** gravada pelo nome; evita que o operador precise escolhê-la de novo. É opcional no arquivo (nenhuma estratégia definida é um estado válido).
- **Catálogo de produtos (moldes):** id e tipo de cada molde, necessários para ligar demandas e peças ao produto correto.
- **Armazém:** cada peça aprovada (status `Aprovado` ou `Aprovado Indevidamente`, que são os únicos com que uma peça chega ao armazém), com id, tipo, lote, status e probabilidade de falha acumulada. A qualidade e o consumo de matéria-prima não são gravados, pois são fixos por tipo de produto e vêm do próprio molde.
- **Contador de lotes e total de peças reprovadas:** o contador impede que, depois de recuperar a planta, um novo lote reutilize o código de um lote que já está no armazém; o total de reprovadas mantém o histórico exibido nos relatórios.
- **Nome da linha de produção:** identifica a linha na auditoria.

**Mantidos apenas em memória (não persistidos), e por quê:**

- **Se a máquina está ligada ou desligada:** a linha só liga as máquinas durante um lote e sempre as desliga ao terminar, mesmo se o lote for interrompido. Como o estado só pode ser salvo pelo menu, com a produção parada, toda máquina está desligada naquele momento, e as máquinas recuperadas começam desligadas.
- **Geradores de números aleatórios (`Random`) das máquinas e das partes:** os sorteios não têm estado relevante para o domínio e não são reprodutíveis; cada objeto recriado cria o seu.
- **Constantes de cada classe (taxa de desgaste das partes, rótulos de condição, tempos de produção, quantidade de matéria-prima e qualidade de cada produto):** estão no código, e o construtor as define sempre. Gravá-las criaria uma segunda fonte para o mesmo dado, com risco de divergir. A configuração das máquinas é gravada, mas apenas para ser conferida na leitura.
- **Peças reprovadas na inspeção, individualmente:** são descartadas pela produção; só o total é guardado.
- **Contador estático `Produto.getTotalProdutosFabricados()`:** conta os objetos de produto criados na execução atual (inclusive os recriados ao carregar) e não faz parte do estado da planta.
- **Relatórios e totais exibidos nos menus:** são calculados a partir dos dados persistidos.

### Limitações

- Existe um único arquivo de estado (`dados/planta.xml`, definido em `PersistenciaPlanta.ARQUIVO_PADRAO`); não há histórico de versões nem vários perfis de salvamento. Iniciar uma planta nova não apaga o arquivo, mas salvá-la depois o substitui.
- O salvamento é manual (opção `[ 8 ]` ou ao encerrar). Não há salvamento automático depois de cada operação.
- O arquivo é legível, mas editá-lo à mão exige respeitar as regras do domínio: o que contrariar essas regras é recusado inteiro, sem aproveitamento parcial.
- Cada tipo de máquina, produto e estratégia precisa estar registrado em `RegistroDeTipos` para poder ser persistido. Um tipo novo, sem registro, faz o salvamento falhar com uma mensagem, sem alterar o arquivo anterior.
- O formato tem versão 1. Não há conversão automática entre versões.
- A garantia de preservar o último arquivo salvo depende de o sistema de arquivos aceitar movimentação atômica. Quando não aceita, a substituição é comum e uma interrupção (queda de energia, por exemplo) no exato momento da troca pode deixar o arquivo incompleto. Não há cópia de segurança automática.
- `EstadoPlanta` não é uma cópia profunda: as listas são cópias, mas os objetos dentro delas são os da planta em uso. A persistência só os lê, e `restaurar` monta uma planta nova com eles. Uma versão mais isolada usaria objetos de transferência só com valores simples (textos, números e enumerações), e não foi feita porque exigiria refazer a leitura, a escrita e os testes sem ser requisito da tarefa.
- A suíte de testes não percorre os menus da interface de terminal, que dependem de entrada pelo teclado; cobre apenas a escolha feita ao iniciar (`MainTest`), e a lógica que os menus chamam é coberta pelas classes de domínio, de serviço e de persistência.

### Explicação da suíte de testes

Os testes usam JUnit 5 e ficam em `src/test/java`, em pacotes que espelham os de `src/main/java`. Cada teste monta os dados de que precisa (`@BeforeEach` ou dentro do próprio teste), não depende da ordem de execução e não usa entrada pelo teclado. Os testes de persistência usam `@TempDir`: cada teste recebe uma pasta temporária própria, apagada ao final, e nunca toca em `dados/planta.xml`. Como as máquinas sorteiam falhas e desgaste, os testes de produção verificam apenas o que vale para qualquer sorteio (consumo de recursos, contabilidade do lote, estado final da demanda), e a quebra de uma máquina é provocada desgastando-a até quebrar, e não esperando por sorte.

| Grupo | Classe de teste | O que avalia |
|---|---|---|
| Regras de negócio | `DemandaTest` | Criação válida e inválida, transições de `StatusDemanda` (iniciar, atualizar, aprovar, cancelar, atender), cálculo de matéria-prima e viabilidade financeira, e as mesmas regras na recuperação (`Demanda.restaurar`). |
| Regras de negócio | `MateriaPrimaTest` | Criação, consumo, entrada de estoque, limite numérico e consulta de disponibilidade. |
| Regras de negócio | `ProdutoTest` | Consumo, qualidade e tipo de cada produto, acúmulo e limite da probabilidade de falha, criação de novas unidades, lote e status, e quais status contam como peça aprovada (`estaAprovado`). |
| Regras de negócio | `MaquinaTest` | Composição (cada máquina cria suas partes), ligar e desligar, desgaste, quebra, reparo, relatório em cascata, cenário e restauração validada da saúde das partes. |
| Regras de negócio | `LinhaDeProducaoTest` | Agregação (adicionar, remover, ordem, lista somente leitura), custo por peça, ligar e processar, localização de máquina quebrada e relatório hierárquico. |
| Regras de negócio | `EstrategiaProducaoTest` | O padrão Strategy: cada estratégia escolhe a demanda correta, e entradas inválidas são recusadas. |
| Regras de negócio | `GerenciadorProducaoTest` | Planta padrão por cenário, registro de demandas, execução da produção e sua contabilidade (orçamento, matéria-prima, lote, estado da demanda), compra, reparo, `restaurar` (inclusive a recusa de peças no armazém que não foram aprovadas) e `obterEstado`. |
| Interface | `MainTest` | A escolha feita ao iniciar a aplicação: sem arquivo salvo vai direto ao cenário; com arquivo, recuperar devolve a planta salva e iniciar uma nova não altera o arquivo; entrada inválida é pedida de novo; arquivo salvo inválido gera aviso e deixa o arquivo como estava. |
| Persistência | `PersistenciaPlantaTest` | Salvar gera XML bem formado com as seções esperadas e sem temporários; carregar devolve estoque, orçamento, estratégia, máquinas com a saúde exata de cada parte, demandas em todos os status (uma salva como EM_PRODUCAO volta como PENDENTE e pode ser produzida), catálogo e armazém; o carregamento substitui o estado sem duplicar nem combinar; salvar de novo atualiza o arquivo; a numeração de lotes continua depois de recuperar; nomes com acentos sobrevivem; o arquivo padrão é usado por `salvar()` e `carregar()` sem caminho e não impede o uso de outro. |
| Situações de erro | `PersistenciaErrosTest` | Arquivo inexistente, vazio, só com espaços, texto que não é XML, XML truncado ou sem fechamento, `DOCTYPE`; elementos e atributos ausentes ou repetidos; valores não numéricos; cenário, estratégia, status e tipos desconhecidos; dados incompatíveis com o domínio (valores negativos, saúde fora de 0 a 100, partes faltando ou repetidas, configuração de máquina diferente, demanda concluída com peças, demanda ou peça fora do catálogo, peça no armazém com status desconhecido ou de reprovação, lote incoerente, risco fora de 0 a 1); falhas de leitura e escrita, incluindo a falha ao remover o arquivo temporário; arquivo padrão ausente (que conta como sem estado salvo) ou inválido (que existe, mas não é carregado); e a garantia de que, em caso de falha, a planta em uso, o arquivo e o último salvamento ficam intactos. |
| Situações de erro | Demais classes | Cada classe acima também verifica as exceções específicas do seu domínio (por exemplo `RecursoInsuficienteException` para estoque insuficiente, `OrcamentoInsuficienteException`, `MaquinaIndisponivelException` e `DemandaInvalidaException`), conferindo o tipo da exceção e que o estado não foi alterado pela operação recusada. |

`UtilidadesDeTeste` reúne um apoio usado por mais de uma classe (desgastar uma máquina até quebrar).
