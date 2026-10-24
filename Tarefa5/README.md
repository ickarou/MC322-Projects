# Fabrica de Scanners Automotivos
## Modelo: KTS 590
#
## Alunos:
### Eduardo Fernandes Sales Pontes - 270802
### Icaro Kwiatkowski do Amaral - 186535

Nesta etapa da tarefa, importamos a estruturação de código desenvolvida nas atividades anteriores através da implementação do Gradle, que é uma ferramenta para a automação de compilação nos projetos.

Além disso, com o crescimento das funcionalidades da planta industrial (incluindo diversas classes, interfaces, lógicas de gerenciamento de finanças e de estoque, cenários de operação entre outras aplicações elaboradas anteriormente), fez-se necessário tratar da documentação do projeto, como um todo, mediante o uso do javadoc. Dessa forma, buscamos padronizar as informações referentes às classes do sistema, tratando rigorosamente das excessões de funcionamento.

## Tarefa 5: Composição, Fases de Construção e Acoplamento

Nesta etapa, a máquina passou a ser o "Todo" de uma composição. Cada `Maquina` cria no seu construtor um `Motor` (1:1), um `SensorDeTemperatura` (1:1) e uma lista de `Atuador` (1:N), todos herdando de `ComponenteMaquina`. As máquinas ficam agrupadas em uma `LinhaDeProducao` (agregação), cuja lista é protegida com `Collections.unmodifiableList()`.

- O Gerenciador e o Main falam apenas com a linha e com as máquinas (Lei de Deméter); nenhuma parte é exposta.
- A auditoria percorre Linha > Máquina > Partes e exibe um relatório hierárquico e indentado.
- O estado das partes afeta o desempenho: motor e sensor gastos aumentam a chance de falha da máquina e atuadores gastos deixam um risco extra de defeito nas peças.
- O menu principal ganhou a opção `[ 7 ] Linha de Producao`, com o fluxo da linha, a auditoria estrutural e a manutenção.
- O diagrama UML está em `uml_tarefa5.png` (fonte editável em `uml_tarefa5.drawio`).

### Compilação e execução

```
./gradlew build
./gradlew run
```

