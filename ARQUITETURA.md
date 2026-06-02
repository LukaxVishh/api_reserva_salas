# Como o projeto funciona — SalaLivre API

Visão técnica das funcionalidades implementadas: integração com a API externa ViaCep,
mensageria com RabbitMQ e busca de salas disponíveis por proximidade geográfica.

---

## 1. Visão geral da estrutura

```
src/main/java/com/salalivre/api/
├── client/          ViaCepClient.java           — chama a API externa ViaCep
├── config/          RabbitMqConfig.java          — configura exchange, filas e bindings
├── controller/      LocalizacaoController.java   — expõe /localizacao/cep/{cep}
│                    SalaDisponibilidadeController — expõe /salas/disponiveis/proximas
│                    SalaController.java           — CRUD de salas (com CEP opcional)
│                    ReservaController.java        — CRUD de reservas
├── messaging/       EventPublisher.java          — publica eventos no RabbitMQ
│                    EventConsumer.java            — consome e persiste eventos
│                    ReservaCriadaEvent.java       — payload do evento de reserva
│                    BuscaSalasProximasEvent.java  — payload do evento de busca
├── model/           Sala.java                    — inclui campos de endereço (cep, logradouro…)
│                    EnderecoViaCep.java           — resposta mapeada do ViaCep
│                    EventoSistema.java            — registro de auditoria no banco
├── repository/      EventoSistemaRepository.java — grava em eventos_sistema
├── service/         LocalizacaoService.java      — normaliza CEP e chama ViaCepClient
│                    SalaService.java             — ao salvar sala com CEP, chama ViaCep
│                    SalaDisponibilidadeService   — busca + filtra + ordena por distância
│                    DistanciaService.java        — calcula distância e classificação
│                    ReservaService.java          — ao criar reserva, publica evento
└── exception/       GlobalExceptionHandler.java  — mapeia exceções para códigos HTTP
```

Banco de dados (MySQL):
```
salas            — dados das salas + campos de endereço adicionados recentemente
reservas         — reservas com status calculado dinamicamente
eventos_sistema  — log de auditoria dos eventos RabbitMQ
```

---

## 2. API Externa: ViaCep

### O que é

ViaCep (`https://viacep.com.br`) é uma API pública brasileira que retorna o endereço
completo a partir de um CEP. A integração é usada em dois pontos do sistema:

- Ao **criar ou atualizar uma sala** com CEP informado → o endereço é preenchido automaticamente.
- Na **busca de salas disponíveis próximas** → o CEP do usuário é convertido em endereço
  para calcular a distância até cada sala.

### Caminho de execução

```
Requisição HTTP
      │
      ▼
LocalizacaoController  GET /localizacao/cep/{cep}
      │
      ▼
LocalizacaoService.buscarEnderecoPorCep(cep)
  1. normalizarCep(cep)      — remove traço, valida exatamente 8 dígitos
  2. ViaCepClient.buscar()   — GET https://viacep.com.br/ws/{cep}/json/
  3. se endereco.erro == true → lança CepInvalidoException (400)
  4. retorna EnderecoViaCep preenchido
      │
      ▼
Resposta JSON com: cep, logradouro, bairro, cidade, uf
```

### Validações em LocalizacaoService

| Entrada            | Erro lançado                    | HTTP |
|--------------------|---------------------------------|------|
| CEP nulo/vazio     | "O CEP é obrigatório."          | 400  |
| Menos/mais de 8 dígitos | "O CEP deve conter exatamente 8 números." | 400 |
| CEP com formato correto mas inexistente | "CEP inválido ou não encontrado." | 400 |
| Falha de rede      | "Erro ao consultar o ViaCEP."   | 400  |

### Como a sala usa o ViaCep

Em `SalaService.salvar()` e `SalaService.atualizar()`:

```java
// Se o CEP foi informado no body da requisição:
EnderecoViaCep endereco = localizacaoService.buscarEnderecoPorCep(sala.getCep());
sala.setCep(endereco.getCep());
sala.setLogradouro(endereco.getLogradouro());
sala.setBairro(endereco.getBairro());
sala.setCidade(endereco.getCidade());
sala.setUf(endereco.getUf());
// numero e complemento vêm do body da requisição
```

O CEP é opcional — se não for informado, a sala é salva sem endereço e não aparece
nos resultados de `/salas/disponiveis/proximas`.

### Arquivos relevantes

| Arquivo | Caminho |
|---------|---------|
| Cliente HTTP | `client/ViaCepClient.java` |
| Serviço de normalização e validação | `service/LocalizacaoService.java` |
| Endpoint público | `controller/LocalizacaoController.java` |
| Modelo de resposta | `model/EnderecoViaCep.java` |
| Exceção customizada | `exception/CepInvalidoException.java` |

---

## 3. RabbitMQ

### O que foi implementado

O sistema publica **eventos de auditoria** no RabbitMQ em duas situações:

1. Quando uma reserva é criada com sucesso → `ReservaCriadaEvent`
2. Quando alguém busca salas disponíveis próximas → `BuscaSalasProximasEvent`

Esses eventos são **consumidos pela própria aplicação** e gravados na tabela
`eventos_sistema` no MySQL — funcionando como um log de auditoria.

### Topologia (RabbitMqConfig.java)

```
                    ┌─────────────────────────────┐
                    │   Exchange (Topic)           │
                    │   sala-livre.exchange        │
                    └──────────┬──────────────────┘
                               │
           ┌───────────────────┴────────────────────┐
           │ routing key:                           │ routing key:
           │ reserva.criada                         │ busca.localizacao
           ▼                                        ▼
┌─────────────────────────┐          ┌───────────────────────────────┐
│ Queue                   │          │ Queue                         │
│ sala-livre.reservas     │          │ sala-livre.buscas.localizacao │
│ .criadas                │          │                               │
└───────────┬─────────────┘          └───────────────┬───────────────┘
            │                                        │
            └──────────────────┬─────────────────────┘
                               ▼
                        EventConsumer.java
                    (consome ambas as filas)
                               │
                               ▼
                    eventos_sistema (MySQL)
```

**Tipo de exchange:** `TopicExchange` — permite roteamento por padrão de chave.
Neste projeto as chaves são exatas, mas o TopicExchange permite expandir no futuro
com wildcards (ex: `reserva.*`).

**Filas duráveis (`durable: true`):** os eventos sobrevivem a um restart do RabbitMQ.

### Publicação de eventos (EventPublisher.java)

O `EventPublisher` usa o `RabbitTemplate` configurado com um `Jackson2JsonMessageConverter`,
então os objetos Java são serializados automaticamente para JSON no momento do envio.

```java
// Chamado em ReservaService.salvar() após persistir no banco:
eventPublisher.publicarReservaCriada(new ReservaCriadaEvent(
    salva.getId(), salva.getSalaId(), salva.getNomeResponsavel(),
    salva.getData(), salva.getHoraInicio(), salva.getHoraFim()
));

// Chamado em SalaDisponibilidadeService ao final da busca:
// (publicação implícita via EventPublisher.publicarBuscaSalasProximas)
```

Se o RabbitMQ estiver indisponível no momento da publicação, o erro é **capturado e
logado** — a operação principal (criar reserva / buscar salas) não falha por causa disso.

### Consumo e auditoria (EventConsumer.java)

```java
@RabbitListener(queues = "sala-livre.reservas.criadas")
public void consumirReservaCriada(ReservaCriadaEvent event) {
    // deserializado automaticamente do JSON pelo Jackson2JsonMessageConverter
    persistir(event.getTipo(), event);   // grava em eventos_sistema
}
```

O `EventConsumer` roda em uma **thread separada** gerenciada pelo Spring AMQP.
Por isso o `EventoSistemaRepository` sincroniza o acesso à `Connection` JDBC
(que é um objeto compartilhado e não é thread-safe):

```java
synchronized (connection) {
    // INSERT INTO eventos_sistema ...
}
```

### Estrutura dos eventos

**ReservaCriadaEvent** — payload gravado em `eventos_sistema.payload`:
```json
{
  "tipo": "RESERVA_CRIADA",
  "reservaId": 1,
  "salaId": 1,
  "nomeResponsavel": "João Silva",
  "data": "2026-06-10",
  "horaInicio": "14:00:00",
  "horaFim": "16:00:00"
}
```

**BuscaSalasProximasEvent** — payload gravado em `eventos_sistema.payload`:
```json
{
  "tipo": "BUSCA_SALAS_PROXIMAS",
  "cepOrigem": "01001000",
  "data": "2026-06-10",
  "horaInicio": "14:00:00",
  "horaFim": "16:00:00",
  "quantidadeResultados": 2
}
```

### Como verificar os eventos no banco

```bash
# Abre shell no MySQL via podman:
podman compose exec mysql mysql -uroot -prootroot salalivre

# Dentro do MySQL:
SELECT id, tipo, criado_em, payload FROM eventos_sistema ORDER BY id DESC LIMIT 10;
```

### Arquivos relevantes

| Arquivo | Caminho |
|---------|---------|
| Configuração (exchange, filas, converter) | `config/RabbitMqConfig.java` |
| Publicação | `messaging/EventPublisher.java` |  
| Consumo | `messaging/EventConsumer.java` |
| Payload evento de reserva | `messaging/ReservaCriadaEvent.java` |
| Payload evento de busca | `messaging/BuscaSalasProximasEvent.java` |
| Modelo de auditoria | `model/EventoSistema.java` |
| Repositório de auditoria | `repository/EventoSistemaRepository.java` |

---

## 4. Busca de salas disponíveis próximas

### Endpoint

```
GET /salas/disponiveis/proximas?cep=01001000&data=2026-06-10&horaInicio=14:00&horaFim=16:00
```

Todos os parâmetros são obrigatórios. Retorna lista ordenada por distância crescente.

### Algoritmo (SalaDisponibilidadeService)

```
1. Valida que horaFim > horaInicio  →  se não, retorna []
2. Chama ViaCep com o CEP de origem  →  obtém cidade/bairro/UF do usuário
3. Para cada sala no banco:
   a. Ignora salas inativas
   b. Ignora salas sem CEP cadastrado
   c. Verifica conflito de horário via ReservaRepository.buscarConflitos()
   d. Calcula distância via DistanciaService
   e. Adiciona à lista de resultado
4. Ordena pelo distanciaAproximadaKm crescente
5. Retorna a lista
```

### Cálculo de distância (DistanciaService)

A distância é **aproximada** — baseada em comparação de texto entre cidade/bairro/UF,
não em coordenadas geográficas reais:

| Condição                         | Distância | Classificação  |
|----------------------------------|-----------|----------------|
| Mesma cidade E mesmo bairro      | 2 km      | MUITO_PROXIMA  |
| Mesma cidade, bairros diferentes | 8 km      | PROXIMA        |
| Mesmo estado (UF)                | 80 km     | MEDIA          |
| Estados diferentes               | 500 km    | DISTANTE       |

A comparação usa `equalsIgnoreCase` entre `origem.getLocalidade()` (retornado pelo
ViaCep) e `sala.getCidade()` (armazenado no banco). As salas do seed estão em
São Paulo (SP) e Rio de Janeiro (RJ).

### Integração com RabbitMQ

Ao final de `buscarSalasProximasDisponiveis()`, o serviço publica um
`BuscaSalasProximasEvent` com o CEP de origem e a quantidade de resultados retornados.

---

## 5. Banco de dados

### Tabelas

**salas** — campos adicionados recentemente (requer recriar os containers se o volume
foi criado antes dessas colunas existirem):
```sql
cep         VARCHAR(8)    -- somente dígitos, ex: "01001000"
logradouro  VARCHAR(150)
bairro      VARCHAR(100)
cidade      VARCHAR(100)
uf          VARCHAR(2)    -- ex: "SP"
numero      VARCHAR(20)   -- informado pelo usuário, não pelo ViaCep
complemento VARCHAR(100)  -- informado pelo usuário, não pelo ViaCep
```

**eventos_sistema** — tabela de auditoria criada junto com a integração RabbitMQ:
```sql
id        INT        auto_increment
tipo      VARCHAR(50) -- "RESERVA_CRIADA" ou "BUSCA_SALAS_PROXIMAS"
payload   TEXT        -- JSON do evento
criado_em TIMESTAMP   -- preenchido automaticamente pelo MySQL
```

### Seed (02_seed.sql)

Três salas pré-cadastradas com endereço:
- **Sala 101** — Praça da Sé, Sé, São Paulo/SP — ativa
- **Sala 102** — Av. Paulista, Bela Vista, São Paulo/SP — ativa
- **Sala Lab 1** — Rua da Quitanda, Centro, Rio de Janeiro/RJ — **inativa**

A Sala Lab 1 nunca aparece nos resultados de `/salas/disponiveis/proximas` por ser inativa.

---

## 6. Configuração por ambiente

O `application.properties` define os valores padrão (localhost). Quando rodando via
`podman compose`, o `docker-compose.yml` sobrescreve via variáveis de ambiente:

| Propriedade                   | Local (application.properties) | Docker (docker-compose.yml) |
|-------------------------------|---------------------------------|-----------------------------|
| `spring.datasource.url`       | `jdbc:mysql://localhost:3306/…` | `jdbc:mysql://mysql:3306/…` |
| `spring.rabbitmq.host`        | `localhost`                     | `rabbitmq`                  |
| `server.port`                 | `8080`                          | `8080` (mapeado para 8081)  |

A API fica acessível em `http://localhost:8081` quando rodando via podman compose.

---

## 7. Aviso: bug na classificação de distância

Ao testar com CEP `01001000` (Praça da Sé, São Paulo/SP), as salas de São Paulo
aparecem com classificação `MEDIA` (80 km) em vez de `PROXIMA` ou `MUITO_PROXIMA`.

**Causa provável:** `DistanciaService` compara `origem.getLocalidade()` com
`sala.getCidade()`. O campo `localidade` em `EnderecoViaCep` tem a anotação
`@JsonProperty(access = WRITE_ONLY)` — dependendo de como o Jackson acessa o campo
(diretamente ou via setter), o `setLocalidade()` pode não ser chamado, o que faz
`getLocalidade()` retornar `null` enquanto `getCidade()` retorna "São Paulo".

Com `getLocalidade()` retornando `null`, `safe(null)` retorna `""`, que não bate
com `"São Paulo"` — mas a UF (`"SP"`) ainda bate, resultando em `MEDIA`.

**Correção sugerida:** trocar `origem.getLocalidade()` por `origem.getCidade()`
nas duas comparações em `DistanciaService`, já que `cidade` é sempre populado
(tanto pelo setter como diretamente).
