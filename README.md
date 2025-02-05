# Cashew

Esse projeto foi criado para resolução do [Desafio Técnico](https://caju.notion.site/Desafio-T-cnico-para-fazer-em-casa-218d49808fe14a4189c3ca664857de72) da Caju.

## Índice

- [Decisões técnicas](#decisões-técnicas)
- [Ferramentas](#ferramentas)
- [Executando a aplicação](#executando-a-aplicação)
- [Comandos](#comandos)
    + [Build](#Build)
    + [Testes](#testes)
    + [Gerar arquivo jar](#gerar-arquivo-jar)
    + [Lint](#lint)
- [L4. Questão aberta](#L4-questão-aberta)

## Decisões técnicas

A solução foi pensada, de forma funcional, no morfismo e tranformação das estruturas.

Foi desenvolvida uma API Rest que representa as operações de transações e todos os efeitos colaterias da mesma.
Não estava claro no enunciado, porém para facilitar a utilização das funcionalidades foram criados endpoints para 
visualizar e, em alguns casos cadastro, das entidades conta(Account), transação(Transaction) e carteira(Wallet). 

Iniciando a solução cira as do banco de dados e as populam para o necessário operacional.

---

## Ferramentas

Segue a lista das principais ferramentas utilizadas no desenvolvimento:

- [Kotlin](https://github.com/JetBrains/kotlin) - Linguagem de programação multiparadigma usada na constução da
  aplicação.
- [Kotlin-test](https://kotlinlang.org/api/core/kotlin-test/) - Biblioteca de testes que fornece anotações para marcar
  funções de teste e um conjunto de funções utilitárias para realizar asserções em testes, independentemente da
  estrutura de teste usada.
- [arrow-core](https://arrow-kt.io/) Biblioteca funcional que fornece as principais construções do mundo FP.
- [ktlint](https://github.com/pinterest/ktlint) Lint.
- [Kotlinx-serialization-json](https://github.com/Kotlin/kotlinx.serialization) - Plugin do compilador para serialização
  de classes e suporte a vários formatos de serialização.
- [Gradle](https://gradle.org/) - Ferramenta de build.

Foram usadas bibliotecas padrões do ambiente Kotlin/Java para a suíte de testes, serialização de objetos, lint e
ambiente de Build.

---

## Executando a aplicação

É necessário a instalção do Java e adição do mesmo nas variáveis de ambiente para que o Gradle funcione plenamente.
A partir da raiz do projeto. Para comodidade e rápida utilização, o arquivo capital-gains-1.0.jar está na raiz da pasta
do projeto.

Para rodar a aplicação:

```console
java -jar capital-gains-1.0.jar "nome-arquivo-input.txt"
```

ou

```console
./gradlew build
java -jar ./build/libs/capital-gains-1.0.jar "input.txt"
```

somado pelo comando:

```console
java -jar ./build/libs/capital-gains-1.0.jar "input.txt"
```

Uma opção é adicionar nas variáveis de ambiente para ficar mais transparente o comando. Não coberto nas instruções pois
foi avaliado como desnecessário para a solução.

---

## Comandos

### Build

```console
./gradlew build
```

### Testes

```console
./gradlew tests
```

### Gerar arquivo jar

```console
./gradlew jar
```

Output: _build/libs/capital-gains-1.0.jar_

### Lint

Para rodar o lint temos as opções via linha de comando:

```console
./gradlew ktlintCheck
```

```console
./gradlew ktlintFormat
```

O primeiro analisa as regras pré estabelecidas e o segundo, além da análise, modifica os arquivos caso necessário
baseado nas regras.

---

### L4 Questão aberta
Nas descrição do desafio foi levantada a questão reproduzida abaixo: 

"A seguir está uma questão aberta sobre um recurso importante de um autorizador completo (que você não precisa implementar, apenas discuta da maneira que achar adequada, como texto, diagramas, etc.).

- Transações simultâneas: dado que o mesmo cartão de crédito pode ser utilizado em diferentes serviços online, existe uma pequena mas existente probabilidade de ocorrerem duas transações ao mesmo tempo. O que você faria para garantir que apenas uma transação por conta fosse processada em um determinado momento? Esteja ciente do fato de que todas as solicitações de transação são síncronas e devem ser processadas rapidamente (menos de 100 ms), ou a transação atingirá o timeout."

Segue resposta:

Separando soluções para a pergunta em tópicos.

- Método 1 - Validação:

As transações são registradas em uma tabela no banco de dados.
Toda nova transação será revisada chegando a existentência da mesma, indo ao banco de dados na tabela anteriormente citada.
Um cenário de inundadação de requisição expõe o problema desse método. Solicitações simultâneas, com o mesmo payload, 
em questão de frações de segundos, aumenta a possibilidade de registro duplicado da transação no sistema.
A janela de tempo para recuperar as informações no baco de dados é o suficiente para possibilitar a criação
acidental de registros indesejados.

- Método 1 - Locking:

A implementação é bastante semelhante ao método acima, no entanto, em vez de um banco do dados transacional tabelar, 
poderíamos optar por armazenar em memória, como o Redis. Infelizmente, mesmo com esse método, ainda existe possibilidade
acidentalmente da criação registros indesejados. Consultas na memória, que é mais veloz que a consulta no banco de
dados, ainda são lenta o suficiente para registrar duplicatas.

- Método 3 - Queuing:

A ideia aqui é enfileirar todas as solicitações recebidas em uma fila e um consumidor processar as solicitações em determinado ritmo. Assim daria para validar e processar cada solicitação recebida adequadamente.
O problema com esse método é que aumentamos a complexidade da nossa solução. 

- Método 4: Tabelas de banco de dados com UNIQUE constrain:

A ideia é modelar sua tabela no banco de dados com algum campo identificador com UNIQUE constrain. O banco dedados nos
ajuda não salvando registros com o mesmo identificador, garantindo a atomicidade da transação.
O problema com esse método é que quanto mais distribuídos esses dados estão, aumenta a complexidade. Por exemplo, 
se já é utilizada alguma estratégia de sharding na  sua base de dados, é necessário soluções para adequação 
arquitetônica, como identificador único universal para identificar as transações.

Dados os métodos com seus trade-offs, em cenários de microserviços eu iria com o método 3. 
Caso fosse algo mais monolítico e em uma escala mais inicial, o método 4 poderia se encaixar como solução.    
