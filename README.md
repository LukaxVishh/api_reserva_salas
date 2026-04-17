# SalaLivre API

API REST desenvolvida com Java e Spring Boot para gerenciamento de salas e reservas, com persistência em MySQL via JDBC.

Este projeto foi criado como trabalho acadêmico para praticar:

- Comunicação via HTTP
- Manipulação de JSON
- Separação de responsabilidades em camadas
- Regras de negócio reais
- Persistência de dados com JDBC

---

## 1) Objetivo do Projeto

A SalaLivre API permite:

- Cadastrar salas
- Consultar salas disponíveis
- Criar, consultar, atualizar e remover reservas
- Evitar conflitos de horários entre reservas

O sistema simula um cenário real para:

- Faculdades
- Escolas
- Empresas
- Laboratórios
- Espaços de estudo

---

## 2) Tema Escolhido

Sistema de reserva de salas.

Motivos da escolha:

- Diferente de CRUDs genéricos
- Simples de modelar
- Bom para aplicar regras de negócio
- Fácil de evoluir em grupo

---

## 3) Tecnologias Utilizadas

- Java 17
- Spring Boot
- Spring Web
- JDBC
- MySQL
- Maven
- Jackson (serialização e desserialização JSON)

---

## 4) Estrutura do Projeto

```text
src/main/java/com/salalivre/api
├── config
├── controller
├── exception
├── model
├── repository
├── service
└── SalaLivreApiApplication.java
```

### Responsabilidade de cada camada

- controller: recebe requisições HTTP e devolve respostas JSON
- model: representa as entidades do sistema
- repository: acesso ao banco de dados com JDBC
- service: lógica de negócio
- exception: exceções personalizadas e tratamento global
- config: configurações da aplicação e banco

---

## 5) Entidades do Sistema

### Sala

Representa uma sala que pode ser reservada.

Campos:

- id
- nome
- bloco
- capacidade
- temProjetor
- ativa

### Reserva

Representa uma reserva vinculada a uma sala.

Campos:

- id
- nomeResponsavel
- descricao
- data
- horaInicio
- horaFim
- status
- salaId

### Relacionamento

- Uma sala possui várias reservas
- Uma reserva pertence a uma sala
- Relação: Sala 1 : N Reserva

---

## 6) Banco de Dados

Banco utilizado: MySQL  
Nome do banco: salalivre

### Tabela salas

```sql
CREATE TABLE salas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    bloco VARCHAR(50) NOT NULL,
    capacidade INT NOT NULL,
    tem_projetor BOOLEAN NOT NULL,
    ativa BOOLEAN NOT NULL
);
```

### Tabela reservas

```sql
CREATE TABLE reservas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome_responsavel VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    data_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    status VARCHAR(30) NOT NULL,
    sala_id INT NOT NULL,
    CONSTRAINT fk_reserva_sala FOREIGN KEY (sala_id) REFERENCES salas(id)
);
```

---

## 7) Regras de Negócio

### Regras planejadas

- Não permitir reservas sobrepostas para a mesma sala no mesmo dia
- Validar que hora de início seja menor que hora de fim
- Limitar duração máxima da reserva
- Permitir reserva apenas em salas ativas
- Validar campos obrigatórios
- Classificar automaticamente o status da reserva

### Status de reserva previstos

- AGENDADA
- EM_ANDAMENTO
- FINALIZADA

---

## 8) Endpoints da API

### Tabela de status por endpoint

| Método | Endpoint | Descrição | Status |
| --- | --- | --- | --- |
| GET | /salas | Lista todas as salas | Implementada |
| GET | /salas/{id} | Busca uma sala por ID | Planejada |
| POST | /salas | Cadastra uma nova sala | Planejada |
| PUT | /salas/{id} | Atualiza uma sala existente | Planejada |
| DELETE | /salas/{id} | Remove uma sala | Planejada |
| GET | /reservas | Lista todas as reservas | Planejada |
| GET | /reservas/{id} | Busca uma reserva por ID | Planejada |
| POST | /reservas | Cria uma nova reserva | Planejada |
| PUT | /reservas/{id} | Atualiza uma reserva existente | Planejada |
| DELETE | /reservas/{id} | Remove uma reserva | Planejada |
| GET | /salas/{id}/reservas | Lista reservas de uma sala | Planejada |
| GET | /salas/disponiveis?data=AAAA-MM-DD&horaInicio=HH:mm&horaFim=HH:mm | Lista salas disponíveis por intervalo | Planejada |

---

## 9) Exemplos de Requisição

### GET /salas

```http
GET http://localhost:8080/salas
```

### Exemplo de resposta

```json
[
  {
    "id": 1,
    "nome": "Sala 101",
    "bloco": "Bloco A",
    "capacidade": 30,
    "temProjetor": true,
    "ativa": true
  },
  {
    "id": 2,
    "nome": "Sala 102",
    "bloco": "Bloco A",
    "capacidade": 20,
    "temProjetor": false,
    "ativa": true
  }
]
```

### Exemplo de JSON para criar sala

```json
{
  "nome": "Sala 301",
  "bloco": "Bloco C",
  "capacidade": 35,
  "temProjetor": true,
  "ativa": true
}
```

### Exemplo de JSON para criar reserva

```json
{
  "nomeResponsavel": "Carlos",
  "descricao": "Grupo de estudos de POO",
  "data": "2026-04-20",
  "horaInicio": "14:00",
  "horaFim": "16:00",
  "status": "AGENDADA",
  "salaId": 1
}
```

---

## 10) Tratamento de Exceções

Exceções previstas:

- RecursoNaoEncontradoException
- ReservaConflitanteException

Componente de tratamento global:

- GlobalExceptionHandler

Objetivo:

- Retornar erros padronizados e claros para o cliente da API

### Padrão sugerido para respostas de erro

```json
{
  "timestamp": "2026-04-17T10:30:00",
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Sala com ID 99 não foi encontrada",
  "caminho": "/salas/99"
}
```

### Exemplos de códigos HTTP esperados

- 400: requisição inválida (campos obrigatórios, horário inválido)
- 404: recurso não encontrado (sala ou reserva inexistente)
- 409: conflito de reserva (sobreposição de horário)
- 500: erro interno inesperado

---

## 11) Status Atual do Desenvolvimento

### Já implementado

- Estrutura inicial do projeto em camadas
- Configuração Maven com dependências necessárias
- Conexão com MySQL
- Banco de dados criado
- Tabelas salas e reservas criadas
- Classe principal da aplicação
- Primeira rota funcional: GET /salas

### Em andamento

- CRUD completo de Sala
- CRUD completo de Reserva
- Exceções personalizadas em uso real
- Regras de negócio completas
- Validações adicionais
- Tratamento de erros mais detalhado
- Documentação final das rotas

---

## 12) Como Executar o Projeto

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
```

### 2. Abrir o projeto na IDE

Opções sugeridas:

- VS Code
- IntelliJ IDEA
- Eclipse

### 3. Criar o banco no MySQL

```sql
CREATE DATABASE salalivre;
USE salalivre;
```

### 4. Criar as tabelas

Execute os comandos SQL deste README para as tabelas salas e reservas.

### 5. Configurar o application.properties

```properties
spring.application.name=api-reserva-salas
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/salalivre?useSSL=false&serverTimezone=America/Sao_Paulo
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 6. Rodar a aplicação

Execute a classe principal:

SalaLivreApiApplication

### 7. Testar endpoint inicial

```http
GET http://localhost:8080/salas
```

---

## 13) Requisitos Acadêmicos Atendidos

Até o momento, o projeto demonstra:

- Uso de Java
- Uso de classes e objetos
- Encapsulamento
- Uso de construtores
- Uso de collections (List)
- Comunicação HTTP
- Manipulação de JSON
- Uso de Controllers
- Separação em camadas
- Persistência com JDBC
- Uso de Repository Pattern
- Separação entre acesso a dados e lógica de negócio

---

## 14) Divisão Sugerida do Grupo

- Integrante 1: configuração inicial, banco, estrutura de pastas e classe principal
- Integrante 2: CRUD de Sala
- Integrante 3: CRUD de Reserva
- Integrante 4: regras de negócio, validações e exceções
- Integrante 5: README, documentação de rotas, testes em Postman/Insomnia e integração final

---

## 15) Checklist Final para Apresentação

Use esta lista antes da entrega/apresentação:

- Aplicação sobe sem erros
- Conexão com banco funciona
- Tabelas criadas corretamente
- Endpoint GET /salas respondendo
- CRUD de salas validado
- CRUD de reservas validado
- Regra de conflito de horário validada
- Tratamento de exceções retornando JSON padronizado
- README atualizado com endpoints e exemplos
- Testes manuais no Postman ou Insomnia registrados