# Microservico de cadastro Linketinder

Servico independente, implementado em Groovy e Gradle, para cadastrar candidatos e empresas nas tabelas PostgreSQL ja existentes. A aplicacao usa o servidor HTTP do JDK, JSON do Groovy e JDBC; nao usa framework web nem cria ou altera tabelas/colunas.

## Esquema e mapeamento utilizados

Os comandos SQL foram alinhados aos DAOs/models compartilhados da aplicacao principal e conferidos no banco `LinkeTinder_BD`:

- `candidato`: `cpf`, `nome`, `email`, `pais`, `estado`, `cep`, `descricao`, `idade`.
- `empresa`: `cnpj`, `nome`, `email`, `pais`, `estado`, `cep`, `descricao`.
- `competencia`: `id_competencia`, `nome`.
- `candidato_competencia`: `cpf_candidato`, `id_competencia`.
- `empresa_competencia`: `cnpj_empresa`, `id_competencia`.

O cadastro de candidato recebe e persiste `idade`, como no formulario, no model e na tabela existentes. `nome` recebe o nome completo, pois a tabela tem apenas essa coluna.

As competencias recebidas como lista de nomes sao inseridas em `competencia` ou reutilizadas quando o nome ja existe. O microservico entao cria os vinculos na tabela correspondente ao perfil. O cadastro do perfil, as competencias novas e os vinculos sao gravados em uma unica transacao.

**Senha:** as tabelas, DAOs e models consultados nao apresentam coluna de senha. Por isso, o microservico nao persiste nem promete autenticar senhas; enviar `senha` no JSON nao a armazena. Nenhuma alteracao de banco e realizada.

## Dependencias

- Groovy 6.0.0 (Gradle).
- PostgreSQL JDBC 42.7.14 (runtime).
- JUnit Jupiter 6.0.0 (testes).
- Java 17 ou superior.

## Organizacao MVC

- `controller/ControladorCadastro.groovy`: recebe as operacoes de cadastro e encaminha ao servico.
- `model/`: regras de cadastro, validacao e persistencia PostgreSQL (repositorio).
- `view/ManipuladorHttpCadastro.groovy`: recebe HTTP/JSON, traduz os endpoints para chamadas do controlador e devolve as respostas.
- `AplicacaoCadastro.groovy`: inicializa e conecta as camadas.

## Configuracao e execucao

O JDBC URL padrao e `jdbc:postgresql://localhost:5432/LinkeTinder_BD`, correspondente a conexao explorada no projeto. Configure `DB_USER` e `DB_PASSWORD` no ambiente; `DB_URL` e opcional para sobrescrever a URL. `PORT` e opcional (padrao `8081`). `FRONTEND_ORIGIN` define a origem exata do frontend (esquema, host e porta); como a origem informada para o frontend local e `http://localhost:5173`, esse e o valor padrao. Sobrescreva a variavel se a view rodar em outro endereco.

```bash
export DB_USER='seu_usuario'
export DB_PASSWORD='sua_senha'
# Opcional: FRONTEND_ORIGIN='http://localhost:5173'
./gradlew run
```

O banco e o usuario precisam existir previamente. O usuario precisa inserir nas tabelas de perfil e de vinculo e ter permissao de insercao, consulta e atualizacao em `competencia` (o `ON CONFLICT` reutiliza competencias existentes). As credenciais sao lidas do ambiente; nao as coloque no codigo nem no controle de versao.

## Endpoints

`POST /api/candidatos`

```json
{
  "cpf": "12345678900",
  "nome": "Batatiudo Exemplo",
  "idade": 29,
  "email": "batatiudo@example.com",
  "pais": "Brasil",
  "estado": "SP",
  "cep": "01000000",
  "descricao": "Perfil de candidato",
  "competencias": ["Java", "Groovy"]
}
```

`POST /api/empresas`

```json
{
  "cnpj": "12345678000199",
  "nome": "Tapetes Exemplo",
  "email": "contato@example.com",
  "pais": "Brasil",
  "estado": "SP",
  "cep": "01000000",
  "descricao": "Fabricante de tapetes",
  "competencias": ["Producao", "Logistica"]
}
```

Ambos exigem `Content-Type: application/json`. Sucesso retorna `201 Created` com `tipo` e `identificador`. Erros retornam JSON no formato `{"mensagem":"..."}`. JSON invalido retorna `400`, validacao `422`, conflito de unicidade PostgreSQL `409`, metodo invalido `405`, endpoint inexistente `404` e falhas internas `500`.

## Exploracao da integracao com frontend

O fluxo explorado e um `fetch` do formulario para o endpoint HTTP, enviando JSON. O servidor habilita CORS somente para a origem configurada em `FRONTEND_ORIGIN` (padrao `http://localhost:5173`); `OPTIONS` permite a verificacao previa do navegador. Sobrescreva a configuracao se a origem da view mudar:

```javascript
const resposta = await fetch('http://localhost:8081/api/candidatos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(dadosDoFormulario)
});

if (!resposta.ok) {
  throw new Error((await resposta.json()).mensagem);
}
```

Para empresas, use `/api/empresas` e envie `cnpj` no lugar de `cpf`/`idade`. Envie `competencias` como lista de nomes para ambos os endpoints. O projeto principal do frontend nao esta neste repositorio; a origem configurada em `FRONTEND_ORIGIN` deve ser a origem real servida pelo frontend.

## Testes

```bash
./gradlew test
```

A suite cobre regras de cadastro/validacao, respostas HTTP e CORS em servidor local, alem dos comandos SQL, parametros JDBC e fechamento dos recursos usando um driver de teste. Os testes nao inserem registros no PostgreSQL configurado.
