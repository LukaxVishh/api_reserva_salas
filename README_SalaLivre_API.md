# SalaLivre API

API REST em Java para gerenciamento de salas e reservas, evoluída com integração externa via ViaCEP e comunicação assíncrona com RabbitMQ.

O projeto simula um sistema de reserva de salas onde o usuário pode cadastrar salas, criar reservas e buscar salas disponíveis mais próximas a partir de um CEP informado.

---

## 1. Objetivo do Projeto

O objetivo do projeto é desenvolver uma API REST completa em Java, aplicando os principais conceitos trabalhados na disciplina:

1. APIs REST;
2. manipulação de JSON;
3. consumo de API externa;
4. persistência de dados com JDBC;
5. mensageria com RabbitMQ;
6. arquitetura em camadas;
7. comunicação assíncrona entre sistemas;
8. regras de negócio reais, evitando um CRUD vazio.

---

## 2. Tema do Projeto

O tema escolhido é:

**Sistema de Reserva de Salas com Busca por Proximidade via CEP**

A API permite o cadastro de salas, criação de reservas e consulta de salas disponíveis. Como evolução da versão anterior, o sistema passa a utilizar o CEP das salas e o CEP informado pelo usuário para simular uma busca por salas mais próximas disponíveis.

A integração com API externa será feita utilizando o **ViaCEP**, que será responsável por buscar dados de endereço a partir de um CEP.

---

## 3. Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring Web
- JDBC
- MySQL
- RabbitMQ
- Docker / Podman
- Maven
- ViaCEP
- Bruno para testes de API

---

## 4. Arquitetura do Projeto

O projeto segue uma arquitetura em camadas:

```text
Controller -> Service -> Repository -> Banco de Dados
```

Também serão adicionadas camadas auxiliares para integração externa e mensageria:

```text
Controller -> Service -> Client externo
Controller -> Service -> Publisher RabbitMQ
Consumer RabbitMQ -> Repository
```

### Responsabilidade de cada camada

#### Controller

Responsável por receber as requisições HTTP, validar parâmetros básicos e retornar respostas JSON.

Exemplo:

```text
SalaController
ReservaController
LocalizacaoController
SalaDisponibilidadeController
```

#### Service

Responsável pelas regras de negócio da aplicação.

Exemplo:

```text
SalaService
ReservaService
LocalizacaoService
SalaDisponibilidadeService
DistanciaService
```

#### Repository

Responsável pelo acesso ao banco de dados utilizando JDBC.

Exemplo:

```text
SalaRepository
ReservaRepository
EventoSistemaRepository
```

#### Client externo

Responsável pelo consumo de APIs externas.

Exemplo:

```text
ViaCepClient
```

#### Messaging

Responsável por publicar e consumir mensagens no RabbitMQ.

Exemplo:

```text
EventPublisher
EventConsumer
RabbitMqConfig
```

---

## 5. Funcionalidades Atuais da API

A primeira versão do projeto já possui as seguintes funcionalidades:

### 5.1 Salas

- Listar todas as salas;
- Buscar sala por ID;
- Criar sala;
- Atualizar sala;
- Remover sala.

### 5.2 Reservas

- Listar todas as reservas;
- Buscar reserva por ID;
- Listar reservas de uma sala;
- Criar reserva;
- Atualizar reserva;
- Remover reserva.

### 5.3 Regras já existentes de reserva

A API já possui regras de negócio importantes:

- não permite reserva em data passada;
- não permite horário inicial maior ou igual ao horário final;
- só permite reservas entre 07:00 e 23:00;
- limita a duração máxima da reserva a 4 horas;
- não permite reservar salas inativas;
- não permite conflito de horário na mesma sala;
- calcula automaticamente o status da reserva;
- não permite remover reserva em andamento.

---

## 6. Nova Proposta de Evolução

A nova proposta é adicionar localização às salas e permitir que o usuário busque salas disponíveis próximas de um CEP informado.

A funcionalidade principal será:

```http
GET /salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

Essa rota deverá:

1. receber o CEP de origem do usuário;
2. validar o CEP;
3. consultar o ViaCEP;
4. buscar salas ativas cadastradas no banco;
5. verificar quais salas estão disponíveis na data e horário informados;
6. calcular uma proximidade simulada entre o CEP do usuário e o CEP da sala;
7. ordenar as salas da mais próxima para a mais distante;
8. retornar o resultado em JSON.

---

## 7. API Externa Utilizada

A API externa escolhida é o **ViaCEP**.

### 7.1 Objetivo do uso do ViaCEP

O ViaCEP será utilizado para buscar informações de endereço a partir de um CEP.

Exemplo de chamada externa:

```http
GET https://viacep.com.br/ws/01001000/json/
```

Exemplo de resposta:

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP",
  "ibge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

### 7.2 Uso dentro do sistema

O ViaCEP será usado em dois momentos principais:

#### 1. Consulta direta de CEP

Endpoint para testar e demonstrar a integração externa:

```http
GET /localizacao/cep/{cep}
```

Exemplo:

```http
GET /localizacao/cep/01001000
```

Resposta esperada:

```json
{
  "cep": "01001000",
  "logradouro": "Praça da Sé",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

#### 2. Cadastro ou atualização de endereço da sala

Ao informar um CEP para uma sala, a API poderá buscar os dados no ViaCEP e preencher automaticamente:

- logradouro;
- bairro;
- cidade;
- UF.

O usuário deverá informar manualmente apenas dados complementares, como:

- número;
- complemento.

---

## 8. Regras de Negócio Novas

### 8.1 Validação de CEP

O sistema deverá validar:

- CEP é obrigatório;
- CEP deve conter exatamente 8 números;
- CEP não pode conter letras;
- CEP inexistente no ViaCEP deve retornar erro;
- resposta do ViaCEP com `"erro": true` deve ser tratada como CEP inválido.

Exemplo de erro:

```json
{
  "erro": "CEP inválido ou não encontrado."
}
```

---

### 8.2 Sala com endereço

A entidade `Sala` será evoluída para conter informações de endereço.

Campos novos:

```text
cep
logradouro
bairro
cidade
uf
numero
complemento
```

A sala poderá ser cadastrada com endereço para participar da busca por proximidade.

---

### 8.3 Busca de salas disponíveis próximas

A busca de salas próximas deverá considerar:

- CEP de origem válido;
- data obrigatória;
- hora inicial obrigatória;
- hora final obrigatória;
- horário dentro do funcionamento da API;
- salas ativas;
- salas sem conflito de reserva no período;
- salas com CEP cadastrado.

A API não deverá retornar:

- salas inativas;
- salas sem CEP;
- salas já reservadas no horário solicitado;
- salas com dados de localização incompletos.

---

### 8.4 Cálculo simples de proximidade

Para manter o projeto simples, o cálculo de proximidade será uma simulação baseada nos dados do CEP.

Como o ViaCEP não retorna latitude e longitude, a API poderá usar uma regra simples:

1. mesma cidade e mesmo bairro: muito próximo;
2. mesma cidade: próximo;
3. mesmo estado: distância média;
4. estados diferentes: distante.

Além disso, será calculado um valor aproximado em quilômetros para ordenar os resultados.

Exemplo de classificação:

```text
MUITO_PROXIMA
PROXIMA
MEDIA
DISTANTE
```

Exemplo de retorno:

```json
{
  "salaId": 1,
  "nome": "Sala 101",
  "bloco": "A",
  "capacidade": 40,
  "temProjetor": true,
  "cep": "01310930",
  "logradouro": "Avenida Paulista",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "uf": "SP",
  "distanciaAproximadaKm": 3.8,
  "classificacaoProximidade": "PROXIMA"
}
```

---

## 9. RabbitMQ

O RabbitMQ será utilizado para comunicação assíncrona entre partes do sistema.

A API principal publicará eventos em filas, e um consumidor será responsável por processar esses eventos e salvar registros de auditoria no banco de dados.

---

### 9.1 Eventos propostos

#### Evento de reserva criada

Quando uma reserva for criada com sucesso, será enviada uma mensagem para o RabbitMQ.

Fila sugerida:

```text
sala-livre.reservas.criadas
```

Payload exemplo:

```json
{
  "tipo": "RESERVA_CRIADA",
  "reservaId": 10,
  "salaId": 1,
  "nomeResponsavel": "Lucas",
  "data": "2026-06-10",
  "horaInicio": "14:00",
  "horaFim": "16:00"
}
```

---

#### Evento de busca de salas próximas

Quando o usuário fizer uma busca por salas próximas disponíveis, será enviada uma mensagem para o RabbitMQ.

Fila sugerida:

```text
sala-livre.buscas.localizacao
```

Payload exemplo:

```json
{
  "tipo": "BUSCA_SALAS_PROXIMAS",
  "cepOrigem": "01001000",
  "data": "2026-06-10",
  "horaInicio": "14:00",
  "horaFim": "16:00",
  "quantidadeResultados": 3
}
```

---

### 9.2 Consumidor de mensagens

O consumidor deverá ler as mensagens das filas e salvar os eventos em uma tabela de auditoria.

Tabela sugerida:

```sql
CREATE TABLE eventos_sistema (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Essa tabela servirá para demonstrar que as mensagens foram processadas de forma assíncrona.

---

## 10. Alterações no Banco de Dados

### 10.1 Alteração na tabela de salas

A tabela `salas` deverá receber novos campos:

```sql
ALTER TABLE salas
ADD COLUMN cep VARCHAR(8),
ADD COLUMN logradouro VARCHAR(150),
ADD COLUMN bairro VARCHAR(100),
ADD COLUMN cidade VARCHAR(100),
ADD COLUMN uf VARCHAR(2),
ADD COLUMN numero VARCHAR(20),
ADD COLUMN complemento VARCHAR(100);
```

Na criação inicial do banco, a tabela poderá ficar assim:

```sql
CREATE TABLE salas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    bloco VARCHAR(50) NOT NULL,
    capacidade INT NOT NULL,
    tem_projetor BOOLEAN NOT NULL,
    ativa BOOLEAN NOT NULL,
    cep VARCHAR(8),
    logradouro VARCHAR(150),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    numero VARCHAR(20),
    complemento VARCHAR(100)
);
```

---

### 10.2 Nova tabela de eventos

```sql
CREATE TABLE eventos_sistema (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 11. Endpoints da API

### 11.1 Salas

#### Listar salas

```http
GET /salas
```

#### Buscar sala por ID

```http
GET /salas/{id}
```

#### Criar sala

```http
POST /salas
```

Exemplo de body:

```json
{
  "nome": "Sala 201",
  "bloco": "B",
  "capacidade": 35,
  "temProjetor": true,
  "ativa": true,
  "cep": "01310930",
  "numero": "1000",
  "complemento": "2º andar"
}
```

#### Atualizar sala

```http
PUT /salas/{id}
```

#### Remover sala

```http
DELETE /salas/{id}
```

---

### 11.2 Reservas

#### Listar reservas

```http
GET /reservas
```

#### Buscar reserva por ID

```http
GET /reservas/{id}
```

#### Listar reservas de uma sala

```http
GET /reservas/sala/{salaId}
```

#### Criar reserva

```http
POST /reservas
```

Exemplo de body:

```json
{
  "nomeResponsavel": "Lucas Antonio",
  "descricao": "Aula de desenvolvimento de sistemas",
  "data": "2026-06-10",
  "horaInicio": "14:00",
  "horaFim": "16:00",
  "salaId": 1
}
```

#### Atualizar reserva

```http
PUT /reservas/{id}
```

#### Remover reserva

```http
DELETE /reservas/{id}
```

---

### 11.3 Localização

#### Consultar CEP no ViaCEP

```http
GET /localizacao/cep/{cep}
```

Exemplo:

```http
GET /localizacao/cep/01001000
```

---

### 11.4 Salas disponíveis próximas

#### Buscar salas próximas disponíveis

```http
GET /salas/disponiveis/proximas
```

Parâmetros:

```text
cep
data
horaInicio
horaFim
```

Exemplo:

```http
GET /salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

Resposta esperada:

```json
[
  {
    "salaId": 1,
    "nome": "Sala 101",
    "bloco": "A",
    "capacidade": 40,
    "temProjetor": true,
    "cep": "01310930",
    "logradouro": "Avenida Paulista",
    "bairro": "Bela Vista",
    "cidade": "São Paulo",
    "uf": "SP",
    "distanciaAproximadaKm": 3.8,
    "classificacaoProximidade": "PROXIMA"
  }
]
```

---

## 12. Organização das Filas RabbitMQ

### Exchange sugerida

```text
sala-livre.exchange
```

### Filas sugeridas

```text
sala-livre.reservas.criadas
sala-livre.buscas.localizacao
```

### Routing keys sugeridas

```text
reserva.criada
busca.localizacao
```

---

## 13. Estrutura de Pacotes Sugerida

```text
src/main/java/com/salalivre/api
├── client
│   └── ViaCepClient.java
├── config
│   ├── DatabaseConfig.java
│   └── RabbitMqConfig.java
├── controller
│   ├── SalaController.java
│   ├── ReservaController.java
│   ├── LocalizacaoController.java
│   └── SalaDisponibilidadeController.java
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── RecursoNaoEncontradoException.java
│   ├── ReservaConflitanteException.java
│   └── CepInvalidoException.java
├── messaging
│   ├── EventPublisher.java
│   ├── EventConsumer.java
│   ├── ReservaCriadaEvent.java
│   └── BuscaSalasProximasEvent.java
├── model
│   ├── Sala.java
│   ├── Reserva.java
│   ├── StatusReserva.java
│   ├── EnderecoViaCep.java
│   ├── SalaDisponivelProximaResponse.java
│   └── EventoSistema.java
├── repository
│   ├── SalaRepository.java
│   ├── ReservaRepository.java
│   └── EventoSistemaRepository.java
├── service
│   ├── SalaService.java
│   ├── ReservaService.java
│   ├── LocalizacaoService.java
│   ├── SalaDisponibilidadeService.java
│   └── DistanciaService.java
└── SalaLivreApiApplication.java
```

---

## 14. Divisão da Equipe

O projeto será desenvolvido por 5 integrantes.

Cada desenvolvedor ficará responsável por uma parte específica, mas todas as partes devem se integrar no final.

---

## Desenvolvedor 1 — Integração com ViaCEP e Serviço de Localização

### Responsabilidade principal

Implementar a integração com a API externa ViaCEP.

Essa parte atende diretamente ao requisito de consumo de API externa.

---

### Tarefas

#### 1. Criar o model de resposta do ViaCEP

Criar a classe:

```text
model/EnderecoViaCep.java
```

Campos sugeridos:

```java
private String cep;
private String logradouro;
private String complemento;
private String bairro;
private String localidade;
private String uf;
private Boolean erro;
```

Observação:

O ViaCEP retorna o nome da cidade no campo `localidade`.

---

#### 2. Criar o client do ViaCEP

Criar a classe:

```text
client/ViaCepClient.java
```

Responsabilidades:

- montar a URL do ViaCEP;
- fazer a requisição HTTP;
- converter a resposta JSON para objeto Java;
- tratar erro de comunicação;
- retornar o endereço encontrado.

Exemplo de URL:

```text
https://viacep.com.br/ws/{cep}/json/
```

---

#### 3. Criar o service de localização

Criar a classe:

```text
service/LocalizacaoService.java
```

Responsabilidades:

- validar se o CEP possui 8 dígitos;
- remover caracteres como hífen e ponto;
- chamar o `ViaCepClient`;
- verificar se o ViaCEP retornou erro;
- lançar exceção se o CEP for inválido;
- retornar os dados de endereço para outras partes da aplicação.

---

#### 4. Criar exceção para CEP inválido

Criar a classe:

```text
exception/CepInvalidoException.java
```

Mensagem sugerida:

```text
CEP inválido ou não encontrado.
```

---

#### 5. Atualizar o GlobalExceptionHandler

Adicionar tratamento para:

```text
CepInvalidoException
```

HTTP sugerido:

```text
400 Bad Request
```

Resposta:

```json
{
  "erro": "CEP inválido ou não encontrado."
}
```

---

#### 6. Criar controller de localização

Criar a classe:

```text
controller/LocalizacaoController.java
```

Endpoint:

```http
GET /localizacao/cep/{cep}
```

Exemplo:

```http
GET /localizacao/cep/01001000
```

---

### Entregáveis do Desenvolvedor 1

Ao final, devem existir:

```text
client/ViaCepClient.java
model/EnderecoViaCep.java
service/LocalizacaoService.java
controller/LocalizacaoController.java
exception/CepInvalidoException.java
```

Também deve ser possível testar:

```http
GET /localizacao/cep/01001000
```

---

## Desenvolvedor 2 — Evolução da Entidade Sala com Endereço

### Responsabilidade principal

Adicionar endereço às salas e garantir que os dados sejam persistidos corretamente no banco usando JDBC.

Essa parte atende ao requisito de persistência com JDBC e prepara a base para a busca de salas próximas.

---

### Tarefas

#### 1. Alterar a tabela `salas`

Atualizar o arquivo:

```text
db/init/01_schema.sql
```

Adicionar os campos:

```sql
cep VARCHAR(8),
logradouro VARCHAR(150),
bairro VARCHAR(100),
cidade VARCHAR(100),
uf VARCHAR(2),
numero VARCHAR(20),
complemento VARCHAR(100)
```

---

#### 2. Atualizar os dados iniciais

Atualizar o arquivo:

```text
db/init/02_seed.sql
```

As salas iniciais devem possuir CEPs reais para facilitar os testes.

Exemplo:

```sql
INSERT INTO salas 
(nome, bloco, capacidade, tem_projetor, ativa, cep, logradouro, bairro, cidade, uf, numero, complemento)
VALUES
('Sala 101', 'A', 40, true, true, '01001000', 'Praça da Sé', 'Sé', 'São Paulo', 'SP', '100', '1º andar'),
('Sala 102', 'A', 30, true, true, '01310930', 'Avenida Paulista', 'Bela Vista', 'São Paulo', 'SP', '1000', '2º andar'),
('Sala Lab 1', 'B', 25, false, false, '20040002', 'Rua da Quitanda', 'Centro', 'Rio de Janeiro', 'RJ', '50', 'Laboratório');
```

---

#### 3. Atualizar o model `Sala`

Atualizar a classe:

```text
model/Sala.java
```

Adicionar os campos:

```java
private String cep;
private String logradouro;
private String bairro;
private String cidade;
private String uf;
private String numero;
private String complemento;
```

Criar getters e setters.

---

#### 4. Atualizar o `SalaRepository`

Atualizar:

```text
repository/SalaRepository.java
```

Alterar os métodos:

- listar;
- buscar por ID;
- criar;
- atualizar.

Todos devem considerar os novos campos de endereço.

---

#### 5. Atualizar o `SalaService`

Atualizar:

```text
service/SalaService.java
```

Responsabilidades novas:

- validar CEP quando informado;
- usar `LocalizacaoService` para buscar endereço;
- preencher automaticamente logradouro, bairro, cidade e UF;
- manter número e complemento informados pelo usuário;
- salvar sala com endereço completo.

---

#### 6. Atualizar o `SalaController`

Atualizar os endpoints existentes para aceitar os novos campos no JSON.

Principalmente:

```http
POST /salas
PUT /salas/{id}
```

---

### Entregáveis do Desenvolvedor 2

Ao final, deve ser possível cadastrar uma sala com CEP:

```json
{
  "nome": "Sala 301",
  "bloco": "C",
  "capacidade": 50,
  "temProjetor": true,
  "ativa": true,
  "cep": "01001000",
  "numero": "200",
  "complemento": "3º andar"
}
```

E a API deve salvar também:

```text
logradouro
bairro
cidade
uf
```

---

## Desenvolvedor 3 — Busca de Salas Disponíveis Próximas

### Responsabilidade principal

Implementar a principal feature nova do projeto:

```http
GET /salas/disponiveis/proximas
```

Essa parte representa a regra de negócio mais importante da nova versão.

---

### Tarefas

#### 1. Criar response específico

Criar a classe:

```text
model/SalaDisponivelProximaResponse.java
```

Campos sugeridos:

```java
private Integer salaId;
private String nome;
private String bloco;
private Integer capacidade;
private Boolean temProjetor;
private String cep;
private String logradouro;
private String bairro;
private String cidade;
private String uf;
private Double distanciaAproximadaKm;
private String classificacaoProximidade;
```

---

#### 2. Criar service de distância

Criar a classe:

```text
service/DistanciaService.java
```

Responsabilidades:

- comparar o endereço de origem com o endereço da sala;
- gerar uma classificação de proximidade;
- calcular uma distância aproximada simples.

Regra sugerida:

```text
Mesmo bairro e mesma cidade: 2 km
Mesma cidade: 8 km
Mesmo estado: 80 km
Estados diferentes: 500 km
```

Classificações sugeridas:

```text
MUITO_PROXIMA
PROXIMA
MEDIA
DISTANTE
```

---

#### 3. Criar service de disponibilidade

Criar a classe:

```text
service/SalaDisponibilidadeService.java
```

Responsabilidades:

- validar CEP;
- validar data;
- validar hora inicial;
- validar hora final;
- reaproveitar regras de horário já existentes;
- buscar endereço de origem pelo ViaCEP;
- buscar salas ativas no banco;
- ignorar salas sem CEP;
- verificar conflito de reserva;
- calcular proximidade;
- ordenar resultado por distância aproximada;
- retornar lista de salas disponíveis próximas.

---

#### 4. Reutilizar regras de conflito

O projeto já possui lógica de conflito no `ReservaRepository`.

O Desenvolvedor 3 deverá reaproveitar ou criar método auxiliar para verificar se existe conflito para uma sala em determinada data e horário.

Regra de conflito:

```text
Uma sala está indisponível se já houver uma reserva na mesma data em horário sobreposto.
```

Exemplo:

```text
Reserva existente: 14:00 até 16:00
Busca solicitada: 15:00 até 17:00
Resultado: conflito
```

Exemplo sem conflito:

```text
Reserva existente: 14:00 até 16:00
Busca solicitada: 16:00 até 18:00
Resultado: sem conflito
```

---

#### 5. Criar controller

Criar a classe:

```text
controller/SalaDisponibilidadeController.java
```

Endpoint:

```http
GET /salas/disponiveis/proximas
```

Parâmetros:

```text
cep
data
horaInicio
horaFim
```

Exemplo:

```http
GET /salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

---

#### 6. Publicar evento de busca

Após realizar a busca com sucesso, publicar um evento no RabbitMQ.

Essa parte poderá depender do Desenvolvedor 4.

Evento:

```json
{
  "tipo": "BUSCA_SALAS_PROXIMAS",
  "cepOrigem": "01001000",
  "data": "2026-06-10",
  "horaInicio": "14:00",
  "horaFim": "16:00",
  "quantidadeResultados": 3
}
```

---

### Entregáveis do Desenvolvedor 3

Ao final, deve ser possível chamar:

```http
GET /salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

E receber uma lista ordenada da sala mais próxima para a mais distante.

---

## Desenvolvedor 4 — RabbitMQ e Auditoria de Eventos

### Responsabilidade principal

Implementar a comunicação assíncrona com RabbitMQ.

Essa parte atende diretamente ao requisito de mensageria e comunicação assíncrona entre sistemas.

---

### Tarefas

#### 1. Adicionar dependência do RabbitMQ

No `pom.xml`, adicionar:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

#### 2. Atualizar o Docker Compose

Adicionar o serviço do RabbitMQ no `docker-compose.yml`.

Exemplo:

```yaml
rabbitmq:
  image: rabbitmq:3-management
  container_name: sala_livre_rabbitmq
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

Painel web:

```text
http://localhost:15672
```

Usuário:

```text
guest
```

Senha:

```text
guest
```

---

#### 3. Criar configuração do RabbitMQ

Criar:

```text
config/RabbitMqConfig.java
```

Configurar:

- exchange;
- filas;
- bindings;
- routing keys.

Nomes sugeridos:

```text
Exchange:
sala-livre.exchange

Filas:
sala-livre.reservas.criadas
sala-livre.buscas.localizacao

Routing keys:
reserva.criada
busca.localizacao
```

---

#### 4. Criar publisher

Criar:

```text
messaging/EventPublisher.java
```

Responsável por publicar mensagens no RabbitMQ.

Métodos sugeridos:

```java
public void publicarReservaCriada(ReservaCriadaEvent event)

public void publicarBuscaSalasProximas(BuscaSalasProximasEvent event)
```

---

#### 5. Criar eventos

Criar:

```text
messaging/ReservaCriadaEvent.java
messaging/BuscaSalasProximasEvent.java
```

---

#### 6. Publicar evento ao criar reserva

Alterar:

```text
service/ReservaService.java
```

Após criar a reserva com sucesso, publicar evento:

```text
RESERVA_CRIADA
```

Importante:

A reserva só deve ser publicada depois de ser salva com sucesso no banco.

---

#### 7. Publicar evento ao buscar salas próximas

Alterar:

```text
service/SalaDisponibilidadeService.java
```

Após realizar a busca, publicar evento:

```text
BUSCA_SALAS_PROXIMAS
```

---

#### 8. Criar tabela de eventos

Atualizar:

```text
db/init/01_schema.sql
```

Adicionar:

```sql
CREATE TABLE eventos_sistema (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### 9. Criar model de evento

Criar:

```text
model/EventoSistema.java
```

Campos:

```java
private Integer id;
private String tipo;
private String payload;
private LocalDateTime criadoEm;
```

---

#### 10. Criar repository de eventos

Criar:

```text
repository/EventoSistemaRepository.java
```

Método principal:

```java
public EventoSistema salvar(EventoSistema evento)
```

---

#### 11. Criar consumer

Criar:

```text
messaging/EventConsumer.java
```

Responsável por consumir mensagens das filas e salvar no banco.

Fluxo:

```text
RabbitMQ -> EventConsumer -> EventoSistemaRepository -> eventos_sistema
```

---

### Entregáveis do Desenvolvedor 4

Ao final:

- RabbitMQ deve subir junto com a aplicação;
- reserva criada deve gerar mensagem;
- busca de salas próximas deve gerar mensagem;
- consumer deve consumir mensagens;
- mensagens devem ser salvas na tabela `eventos_sistema`.

---

## Desenvolvedor 5 — Documentação, Bruno e Validação Final

### Responsabilidade principal

Organizar a documentação, os testes de API e validar o fluxo completo do projeto.

Essa parte é essencial para a entrega e para a defesa presencial.

---

### Tarefas

#### 1. Atualizar README

Manter este README atualizado conforme a implementação real for sendo concluída.

Se algum endpoint, nome de fila ou regra mudar durante o desenvolvimento, o README deve ser ajustado.

---

#### 2. Atualizar coleção Bruno

Criar ou atualizar requisições para testar:

##### Localização

```text
GET /localizacao/cep/01001000
GET /localizacao/cep/00000000
GET /localizacao/cep/abc
```

##### Salas com endereço

```text
POST /salas
GET /salas
PUT /salas/{id}
```

##### Reservas

```text
POST /reservas
GET /reservas
DELETE /reservas/{id}
```

##### Busca de salas próximas

```text
GET /salas/disponiveis/proximas
```

Testar cenários:

- CEP válido;
- CEP inválido;
- horário válido;
- horário inválido;
- sala disponível;
- sala com conflito;
- sala inativa;
- salas ordenadas por proximidade.

---

#### 3. Criar documentação de exemplos

Criar uma pasta opcional:

```text
docs/
```

Com arquivos como:

```text
docs/exemplos-endpoints.md
docs/fluxo-rabbitmq.md
docs/regras-negocio.md
```

---

#### 4. Validar Docker/Podman

Garantir que o projeto sobe corretamente com:

```bash
./scripts/start.sh
```

ou, se usado Docker Compose diretamente:

```bash
docker compose up
```

Validar:

- API;
- MySQL;
- RabbitMQ.

---

#### 5. Preparar roteiro da defesa

Organizar uma explicação simples do fluxo:

1. cadastrar uma sala com CEP;
2. consultar CEP no ViaCEP;
3. criar uma reserva;
4. buscar salas próximas disponíveis;
5. mostrar evento no RabbitMQ;
6. mostrar evento salvo no banco.

---

### Entregáveis do Desenvolvedor 5

Ao final, devem existir:

- README atualizado;
- coleção Bruno atualizada;
- exemplos de testes;
- roteiro de defesa;
- validação final do projeto completo.

---

## 15. Ordem Recomendada de Desenvolvimento

Para evitar conflitos e facilitar a integração, a equipe deve seguir esta ordem:

### Etapa 1 — Banco e Sala com Endereço

Responsável principal:

```text
Desenvolvedor 2
```

Antes de avançar, garantir que:

- tabela `salas` possui campos de endereço;
- model `Sala` foi atualizado;
- repository salva e lista os novos campos.

---

### Etapa 2 — Integração ViaCEP

Responsável principal:

```text
Desenvolvedor 1
```

Antes de avançar, garantir que:

```http
GET /localizacao/cep/01001000
```

funciona corretamente.

---

### Etapa 3 — Cadastro de Sala com CEP

Responsáveis principais:

```text
Desenvolvedor 1
Desenvolvedor 2
```

Antes de avançar, garantir que:

- ao cadastrar uma sala com CEP, o endereço é preenchido;
- CEP inválido retorna erro;
- sala é salva corretamente no banco.

---

### Etapa 4 — Busca de Salas Disponíveis Próximas

Responsável principal:

```text
Desenvolvedor 3
```

Antes de avançar, garantir que:

- endpoint recebe CEP, data e horários;
- busca salas ativas;
- remove salas com conflito;
- calcula proximidade;
- ordena resultado.

---

### Etapa 5 — RabbitMQ

Responsável principal:

```text
Desenvolvedor 4
```

Antes de avançar, garantir que:

- RabbitMQ sobe com Docker;
- eventos são publicados;
- eventos são consumidos;
- eventos são salvos no banco.

---

### Etapa 6 — Testes e Documentação Final

Responsável principal:

```text
Desenvolvedor 5
```

Antes de entregar, garantir que:

- README está atualizado;
- Bruno possui todos os testes;
- fluxo completo funciona;
- equipe sabe apresentar a solução.

---

## 16. Regras de Negócio Consolidadas

### Salas

- nome é obrigatório;
- bloco é obrigatório;
- capacidade é obrigatória;
- capacidade deve ser maior que zero;
- `temProjetor` é obrigatório;
- `ativa` é obrigatório;
- CEP, quando informado, deve ser válido;
- endereço da sala pode ser preenchido automaticamente via ViaCEP.

---

### Reservas

- nome do responsável é obrigatório;
- descrição é obrigatória;
- data é obrigatória;
- hora inicial é obrigatória;
- hora final é obrigatória;
- sala é obrigatória;
- data não pode ser passada;
- hora inicial deve ser menor que hora final;
- reserva deve estar entre 07:00 e 23:00;
- duração máxima é de 4 horas;
- não é permitido reservar sala inativa;
- não é permitido conflito de horário na mesma sala;
- status é calculado automaticamente;
- reserva em andamento não pode ser deletada.

---

### Salas próximas disponíveis

- CEP de origem é obrigatório;
- CEP de origem deve existir no ViaCEP;
- data é obrigatória;
- hora inicial é obrigatória;
- hora final é obrigatória;
- somente salas ativas podem ser retornadas;
- somente salas sem conflito podem ser retornadas;
- somente salas com CEP cadastrado podem ser retornadas;
- resultado deve ser ordenado por proximidade simulada.

---

## 17. Como Executar o Projeto

### Subir aplicação com Docker/Podman

Usar o script existente:

```bash
./scripts/start.sh
```

Ou subir manualmente:

```bash
docker compose up
```

### Acessar a API

```text
http://localhost:8080
```

### Acessar o RabbitMQ

```text
http://localhost:15672
```

Usuário:

```text
guest
```

Senha:

```text
guest
```

---

## 18. Exemplos de Testes

### Consultar CEP

```http
GET http://localhost:8080/localizacao/cep/01001000
```

---

### Criar sala com CEP

```http
POST http://localhost:8080/salas
Content-Type: application/json
```

```json
{
  "nome": "Sala 301",
  "bloco": "C",
  "capacidade": 45,
  "temProjetor": true,
  "ativa": true,
  "cep": "01001000",
  "numero": "200",
  "complemento": "3º andar"
}
```

---

### Criar reserva

```http
POST http://localhost:8080/reservas
Content-Type: application/json
```

```json
{
  "nomeResponsavel": "Lucas Antonio",
  "descricao": "Aula de sistemas integrados",
  "data": "2026-06-10",
  "horaInicio": "14:00",
  "horaFim": "16:00",
  "salaId": 1
}
```

---

### Buscar salas próximas disponíveis

```http
GET http://localhost:8080/salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

---

## 19. Fluxo Principal da Demonstração

Durante a defesa, a equipe pode demonstrar o seguinte fluxo:

1. subir o projeto com MySQL e RabbitMQ;
2. consultar um CEP usando o endpoint de localização;
3. cadastrar uma sala com CEP;
4. criar uma reserva para uma sala;
5. buscar salas disponíveis próximas a um CEP;
6. mostrar que a sala reservada não aparece se houver conflito;
7. mostrar que o resultado vem ordenado por proximidade;
8. acessar o RabbitMQ;
9. mostrar que eventos foram publicados;
10. consultar a tabela `eventos_sistema` e mostrar os eventos consumidos.

---

## 20. Melhorias Futuras

Algumas melhorias possíveis para versões futuras:

- usar latitude e longitude reais com outra API externa;
- calcular distância real entre dois pontos;
- adicionar autenticação de usuários;
- diferenciar usuário comum e administrador;
- criar histórico de reservas por usuário;
- enviar e-mail de confirmação;
- adicionar testes automatizados com JUnit;
- migrar JDBC puro para JdbcTemplate ou JPA;
- melhorar tratamento de erros;
- criar paginação nos endpoints de listagem.

---

## 21. Resumo dos Requisitos Atendidos

| Requisito | Como o projeto atende |
|---|---|
| API REST | Endpoints de salas, reservas, localização e disponibilidade |
| JSON | Requisições e respostas em JSON |
| API externa | Integração com ViaCEP |
| JDBC | Repositories com acesso manual ao MySQL |
| RabbitMQ | Eventos de reserva e busca de localização |
| Arquitetura em camadas | Controller, Service, Repository, Client e Messaging |
| Comunicação assíncrona | Producer e Consumer RabbitMQ |
| Regra de negócio real | Validação de CEP, conflito de reservas, disponibilidade e proximidade |
| Não é CRUD vazio | Possui cálculos, validações, classificação e integração externa |

---

## 22. Conclusão

O projeto SalaLivre API evolui uma API de reserva de salas para um sistema integrado com API externa e mensageria.

A nova versão mantém a simplicidade da implementação, mas atende aos requisitos principais do trabalho:

- possui regras de negócio reais;
- consome uma API externa;
- persiste dados com JDBC;
- utiliza RabbitMQ;
- mantém arquitetura em camadas;
- permite uma divisão clara de tarefas entre os integrantes da equipe.

A funcionalidade principal da nova versão é a busca de salas disponíveis próximas a partir de um CEP informado pelo usuário.
