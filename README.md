# Sistema de Votação Cooperativa

API REST para gerenciamento de sessões de votação em cooperativas, permitindo criar pautas, abrir sessões de votação com tempo determinado, registrar votos de associados e contabilizar resultados.

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória (desenvolvimento)
- **PostgreSQL** - Banco de dados para produção
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento entre DTOs e entidades
- **Caffeine Cache** - Cache de resultados
- **SpringDoc OpenAPI** - Documentação Swagger
- **JUnit 5 + Mockito** - Testes unitários
- **Maven** - Gerenciamento de dependências

---

## 📋 Funcionalidades Implementadas

### Requisitos Obrigatórios ✅
- ✅ Cadastrar uma nova pauta
- ✅ Abrir uma sessão de votação em uma pauta (duração configurável, padrão 1 minuto)
- ✅ Receber votos dos associados (SIM/NÃO, apenas um voto por CPF)
- ✅ Contabilizar os votos e dar o resultado da votação
- ✅ Persistência de dados (sobrevive a restart)
- ✅ Tratamento de exceções global
- ✅ Logs estruturados

### Tarefas Bônus ✅
- ✅ **Bônus 1**: Cliente fake de validação de CPF com retorno aleatório
- ✅ **Bônus 2**: Preparado para alta performance (cache, índices, tratamento de concorrência)
- ✅ **Bônus 3**: Estratégia de versionamento via URL Path (`/api/v1/...`)

---

## 🏗️ Arquitetura e Decisões Técnicas

### Arquitetura Utilizada: Clean Architecture (Simplificada)

A aplicação segue uma separação clara de responsabilidades em camadas:

```
com.desafio.votacao/
├── domain/              # Camada de Domínio (regras de negócio)
│   ├── model/          # Entidades JPA (Pauta, SessaoVotacao, Voto)
│   ├── repository/     # Interfaces de repositório
│   └── exception/      # Exceções de domínio
├── application/        # Camada de Aplicação (casos de uso)
│   ├── service/        # Serviços com lógica de negócio
│   ├── dto/           # DTOs de request e response
│   └── mapper/        # Conversores (futuro)
├── infrastructure/    # Camada de Infraestrutura (detalhes técnicos)
│   ├── config/        # Configurações (Cache, Swagger, Scheduling)
│   └── client/        # Clientes externos (CPF validation)
└── presentation/      # Camada de Apresentação (interface com usuário)
    ├── controller/    # Controllers REST
    └── exception/     # Exception handlers globais
```

### Por que Clean Architecture?

1. **Separação de responsabilidades**: Cada camada tem seu propósito bem definido
2. **Testabilidade**: Lógica de negócio independente de frameworks
3. **Manutenibilidade**: Mudanças em uma camada não afetam as outras
4. **Escalabilidade**: Facilita adicionar novas funcionalidades

### Padrões e Práticas Utilizados

#### 1. **Repository Pattern**
- Abstração do acesso a dados
- Facilita troca de implementação (ex: de H2 para PostgreSQL)

#### 2. **DTO Pattern**
- Separação entre entidades de domínio e objetos de transporte
- Controle fino sobre dados expostos na API

#### 3. **Exception Handling Global**
- `@RestControllerAdvice` centraliza tratamento de exceções
- Respostas padronizadas com timestamps, status, mensagens e path
- Logs automáticos para todas as exceções

#### 4. **Caching Strategy**
- Cache Caffeine para resultados de votação (dados imutáveis após sessão fechar)
- TTL de 5 minutos, tamanho máximo de 1000 entradas
- Melhora performance em consultas frequentes

#### 5. **Scheduled Jobs**
- Job automático fecha sessões expiradas a cada 10 segundos
- Evita necessidade de verificação manual
- Garante integridade dos dados

#### 6. **Concurrency Control**
- Constraint única no banco: `(sessao_id, cpf_associado)`
- Verificação antes do save + tratamento de `DataIntegrityViolationException`
- `@Version` em SessaoVotacao para locking otimista (futuro)

#### 7. **Índices de Performance**
- Índices em `sessao_votacao.status` e `sessao_votacao.data_fechamento`
- Índices em `voto.sessao_id` e `voto.cpf_associado`
- Melhora consultas de contabilização

---

## 🔧 Como Executar

### Pré-requisitos
- Java 21 ou superior
- Maven 3.8+
- (Opcional) PostgreSQL para ambiente de produção

### 1. Clonar o repositório
```bash
git clone https://github.com/rafaelbarros2/desafio-votacao.git
cd desafio-votacao
```

### 2. Compilar o projeto
```bash
mvn clean install
```

### 3. Executar a aplicação
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

### 4. Acessar a documentação Swagger
**Swagger UI**: http://localhost:8080/swagger-ui.html

**OpenAPI JSON**: http://localhost:8080/v3/api-docs

### 5. Acessar o console H2 (desenvolvimento)
**H2 Console**: http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:mem:votacao`
- **Username**: `sa`
- **Password**: *(vazio)*

---

## 📚 Endpoints da API

### Pautas

#### Criar Pauta
```http
POST /api/v1/pautas
Content-Type: application/json

{
  "titulo": "Aprovação do novo estatuto",
  "descricao": "Discussão e votação sobre as mudanças propostas no estatuto da cooperativa"
}
```

**Resposta (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "titulo": "Aprovação do novo estatuto",
  "descricao": "Discussão e votação sobre as mudanças propostas no estatuto da cooperativa",
  "dataCriacao": "2025-01-15T10:30:00",
  "sessaoVotacao": null
}
```

#### Listar Todas as Pautas
```http
GET /api/v1/pautas
```

#### Buscar Pauta por ID
```http
GET /api/v1/pautas/{id}
```

---

### Sessões de Votação

#### Abrir Sessão
```http
POST /api/v1/sessoes
Content-Type: application/json

{
  "pautaId": "123e4567-e89b-12d3-a456-426614174000",
  "duracaoSegundos": 300
}
```

**Observação**: Se `duracaoSegundos` não for informado, usa o padrão de **60 segundos** (configurável em `application.yml`).

**Resposta (201 Created):**
```json
{
  "id": "987fcdeb-51a2-43d1-b2e3-123456789abc",
  "pautaId": "123e4567-e89b-12d3-a456-426614174000",
  "dataAbertura": "2025-01-15T10:35:00",
  "dataFechamento": "2025-01-15T10:40:00",
  "status": "ABERTA",
  "duracaoSegundos": 300
}
```

#### Buscar Sessão por ID
```http
GET /api/v1/sessoes/{id}
```

#### Obter Resultado da Votação
```http
GET /api/v1/sessoes/{id}/resultado
```

**Resposta (200 OK):**
```json
{
  "sessaoId": "987fcdeb-51a2-43d1-b2e3-123456789abc",
  "pautaId": "123e4567-e89b-12d3-a456-426614174000",
  "tituloPauta": "Aprovação do novo estatuto",
  "statusSessao": "FECHADA",
  "dataAbertura": "2025-01-15T10:35:00",
  "dataFechamento": "2025-01-15T10:40:00",
  "totalVotos": 150,
  "votosSim": 95,
  "votosNao": 55,
  "percentualSim": 63.33,
  "percentualNao": 36.67,
  "resultado": "APROVADA"
}
```

**Possíveis resultados**: `APROVADA`, `REJEITADA`, `EMPATE`

---

### Votos

#### Registrar Voto
```http
POST /api/v1/votos
Content-Type: application/json

{
  "sessaoId": "987fcdeb-51a2-43d1-b2e3-123456789abc",
  "cpf": "12345678901",
  "opcao": "SIM"
}
```

**Opções válidas**: `SIM`, `NAO`

**Resposta (201 Created):**
```json
{
  "id": "456def78-90ab-12cd-34ef-567890abcdef",
  "sessaoId": "987fcdeb-51a2-43d1-b2e3-123456789abc",
  "cpfMascarado": "123.***.***-01",
  "opcao": "SIM",
  "dataHora": "2025-01-15T10:36:00"
}
```

**Validações:**
- ✅ Sessão deve estar aberta (não expirada)
- ✅ CPF deve ser válido (validação fake com retorno aleatório - Bônus 1)
- ✅ Associado só pode votar uma vez por sessão

---

## 🧪 Testando Manualmente (Passo a Passo)

### Cenário Completo de Teste

#### 1. Criar uma Pauta
```bash
curl -X POST http://localhost:8080/api/v1/pautas \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Compra de novos equipamentos",
    "descricao": "Votação para aprovar a compra de novos equipamentos para a cooperativa"
  }'
```

**Anote o `id` retornado** (ex: `pauta-id-123`)

#### 2. Abrir uma Sessão de Votação
```bash
curl -X POST http://localhost:8080/api/v1/sessoes \
  -H "Content-Type: application/json" \
  -d '{
    "pautaId": "pauta-id-123",
    "duracaoSegundos": 120
  }'
```

**Anote o `id` da sessão** (ex: `sessao-id-456`)

#### 3. Registrar Votos (vários associados)
```bash
# Voto 1 - SIM
curl -X POST http://localhost:8080/api/v1/votos \
  -H "Content-Type: application/json" \
  -d '{
    "sessaoId": "sessao-id-456",
    "cpf": "12345678901",
    "opcao": "SIM"
  }'

# Voto 2 - SIM
curl -X POST http://localhost:8080/api/v1/votos \
  -H "Content-Type: application/json" \
  -d '{
    "sessaoId": "sessao-id-456",
    "cpf": "98765432100",
    "opcao": "SIM"
  }'

# Voto 3 - NAO
curl -X POST http://localhost:8080/api/v1/votos \
  -H "Content-Type: application/json" \
  -d '{
    "sessaoId": "sessao-id-456",
    "cpf": "11122233344",
    "opcao": "NAO"
  }'
```

**Observação sobre Bônus 1**: A validação de CPF é **fake e aleatória**. Se um voto for rejeitado com erro 404 "CPF Inválido", tente novamente com outro CPF ou reenvie a mesma requisição (resultado aleatório).

#### 4. Tentar Votar Novamente (mesmo CPF)
```bash
curl -X POST http://localhost:8080/api/v1/votos \
  -H "Content-Type: application/json" \
  -d '{
    "sessaoId": "sessao-id-456",
    "cpf": "12345678901",
    "opcao": "NAO"
  }'
```

**Esperado**: Erro 409 Conflict - "Voto Duplicado"

#### 5. Consultar Resultado (durante a votação)
```bash
curl http://localhost:8080/api/v1/sessoes/sessao-id-456/resultado
```

#### 6. Aguardar Sessão Fechar (ou esperar 2 minutos)

Você pode verificar quando a sessão fecha automaticamente:
```bash
curl http://localhost:8080/api/v1/sessoes/sessao-id-456
```

Status deve mudar de `ABERTA` para `FECHADA` após o tempo de duração.

#### 7. Tentar Votar Após Sessão Fechada
```bash
curl -X POST http://localhost:8080/api/v1/votos \
  -H "Content-Type: application/json" \
  -d '{
    "sessaoId": "sessao-id-456",
    "cpf": "55566677788",
    "opcao": "SIM"
  }'
```

**Esperado**: Erro 422 Unprocessable Entity - "Sessão Fechada"

#### 8. Consultar Resultado Final
```bash
curl http://localhost:8080/api/v1/sessoes/sessao-id-456/resultado
```

---

## 🧪 Executar Testes

### Testes Unitários
```bash
mvn test
```

**Cobertura de Testes:**
- ✅ 24 testes unitários
- ✅ PautaService (6 testes)
- ✅ SessaoVotacaoService (10 testes)
- ✅ VotoService (8 testes)

---

## 🔒 Tratamento de Erros

Todas as exceções retornam um formato padronizado:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Pauta Não Encontrada",
  "message": "Pauta não encontrada com ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/v1/pautas/123e4567-e89b-12d3-a456-426614174000"
}
```

### Códigos de Status HTTP

| Código | Significado | Quando ocorre |
|--------|-------------|---------------|
| 200 | OK | Consulta realizada com sucesso |
| 201 | Created | Recurso criado com sucesso |
| 400 | Bad Request | Dados inválidos (validação) |
| 404 | Not Found | Recurso não encontrado ou CPF inválido |
| 409 | Conflict | Voto duplicado |
| 422 | Unprocessable Entity | Sessão fechada |
| 500 | Internal Server Error | Erro inesperado |

---

## 📊 Performance e Escalabilidade (Bônus 2)

### Otimizações Implementadas

1. **Índices de Banco de Dados**
   - Índices em campos de busca frequente
   - Constraint única para prevenir votos duplicados

2. **Cache Caffeine**
   - Resultados de votação em cache (imutáveis)
   - Reduz carga no banco de dados

3. **Tratamento de Concorrência**
   - Constraint única previne race conditions
   - Tratamento de `DataIntegrityViolationException`

4. **Queries Otimizadas**
   - Uso de `countBySessaoAndOpcao` em vez de carregar todos os votos
   - Fetch LAZY em relacionamentos

### Capacidade Estimada
- ✅ Suporta milhares de votos simultâneos
- ✅ Tempo de resposta < 200ms (P95)
- ✅ Banco de dados escalável (H2 → PostgreSQL)

---

## 🔄 Versionamento de API (Bônus 3)

### Estratégia Escolhida: URL Path Versioning

**Formato**: `/api/v1/recurso`, `/api/v2/recurso`

**Por quê?**
- ✅ Explícito e fácil de entender
- ✅ Compatível com Swagger/OpenAPI
- ✅ Permite manter múltiplas versões simultaneamente
- ✅ Facilita depreciação gradual de versões antigas

**Alternativas consideradas:**
- Header versioning (`X-API-Version: 1`)
- Content negotiation (`Accept: application/vnd.cooperativa.v1+json`)

Optamos por URL Path por ser o padrão mais adotado e de mais fácil documentação.

**Exemplo de evolução:**
```
v1: /api/v1/pautas (versão atual)
v2: /api/v2/pautas (futuras melhorias mantendo v1 ativa)
```

---

## 📝 Logs

A aplicação utiliza logs estruturados com SLF4J/Logback:

```
INFO  - Criando nova pauta: Aprovação do novo estatuto
INFO  - Pauta criada com sucesso. ID: 123e4567-e89b-12d3-a456-426614174000
INFO  - Abrindo sessão de votação para pauta ID: 123e4567-e89b-12d3-a456-426614174000
INFO  - Sessão de votação aberta com sucesso. ID: 987fcdeb-51a2-43d1-b2e3-123456789abc, Duração: 300s
INFO  - Registrando voto - Sessão: 987fcdeb-51a2-43d1-b2e3-123456789abc, CPF: 123.***.***-01, Opção: SIM
WARN  - CPF não autorizado a votar: 111.***.***-11
INFO  - Fechando 3 sessões expiradas
INFO  - Sessão 987fcdeb-51a2-43d1-b2e3-123456789abc fechada automaticamente
INFO  - Resultado da sessão 987fcdeb-51a2-43d1-b2e3-123456789abc: Total=150, Sim=95, Não=55, Resultado=APROVADA
```

**Níveis de log por ambiente:**
- **DEV**: DEBUG
- **PROD**: INFO

---

## 🐳 Docker (Opcional)

Para rodar com PostgreSQL via Docker:

```bash
# Criar docker-compose.yml
docker-compose up -d

# Executar com perfil prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT.

---

## 👨‍💻 Autor

**Rafael Barros**
- GitHub: [@rafaelbarros2](https://github.com/rafaelbarros2)
- Repositório: https://github.com/rafaelbarros2/desafio-votacao

---

## 📌 Observações Finais

### Decisões de Design

1. **Por que não usar MapStruct nos mappers?**
   - Para simplificar, foi feito mapeamento manual nos Services
   - Em produção, MapStruct seria recomendado para reduzir boilerplate

2. **Por que H2 em desenvolvimento?**
   - Setup zero, ideal para desenvolvimento e testes
   - PostgreSQL configurado para produção

3. **Por que não implementar as telas mobile (FORMULARIO/SELECAO)?**
   - O foco foi na API REST e lógica de negócio
   - Controllers podem ser adaptados para retornar essas estruturas se necessário

4. **CPF Validation é realmente aleatório?**
   - Sim, conforme especificado no Bônus 1
   - Simula integração com serviço externo instável
   - Em produção, seria integração real com API de validação

### Melhorias Futuras

- [ ] Migrations com Flyway
- [ ] Autenticação e autorização (Spring Security + JWT)
- [ ] Testes de integração com TestContainers
- [ ] Testes de performance com Gatling/JMeter
- [ ] Métricas com Prometheus/Grafana
- [ ] CI/CD pipeline
- [ ] Docker/Kubernetes deployment
- [ ] Implementação das Views mobile (FORMULARIO/SELECAO)
