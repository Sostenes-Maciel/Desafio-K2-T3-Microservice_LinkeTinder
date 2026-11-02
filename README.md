# Microservico de cadastro Linketinder

Servico independente, implementado em Groovy e Gradle, para cadastrar candidatos e empresas nas tabelas PostgreSQL ja existentes. A aplicacao usa o servidor HTTP do JDK, JSON do Groovy e JDBC; nao usa framework web nem cria ou altera tabelas/colunas.

## Esquema e mapeamento utilizados

Os comandos SQL foram alinhados aos DAOs/models compartilhados da aplicacao principal e conferidos no banco `LinkeTinder_BD`:

- `candidato`: `cpf`, `nome`, `email`, `pais`, `estado`, `cep`, `descricao`, `idade`.
- `empresa`: `cnpj`, `nome`, `email`, `pais`, `estado`, `cep`, `descricao`.

O cadastro de candidato recebe `dataNascimento` no formato `AAAA-MM-DD` e calcula `idade`, o campo persistido pelo DAO existente. `nome` recebe o nome completo, pois a tabela tem apenas essa coluna.

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

O JDBC URL padrao e `jdbc:postgresql://localhost:5432/LinkeTinder_BD`, correspondente a conexao explorada no projeto. Configure `DB_USER` e `DB_PASSWORD` no ambiente; `DB_URL` e opcional para sobrescrever a URL. `PORT` e opcional (padrao `8081`) e `FRONTEND_ORIGIN` e opcional (padrao `http://localhost:5173`).

```bash
export DB_USER='seu_usuario'
export DB_PASSWORD='sua_senha'
./gradlew run
```

O banco e o usuario precisam existir previamente e ter permissao de insercao nas tabelas. As credenciais sao lidas do ambiente; nao as coloque no codigo nem no controle de versao.

## Endpoints

`POST /api/candidatos`

```json
{
  "cpf": "12345678900",
  "nome": "Batatiudo Exemplo",
  "dataNascimento": "2000-01-15",
  "email": "batatiudo@example.com",
  "pais": "Brasil",
  "estado": "SP",
  "cep": "01000000",
  "descricao": "Perfil de candidato"
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
  "descricao": "Fabricante de tapetes"
}
```

Ambos exigem `Content-Type: application/json`. Sucesso retorna `201 Created` com `tipo` e `identificador`. JSON invalido retorna `400`, validacao `422`, conflito de unicidade PostgreSQL `409`, metodo invalido `405`, endpoint inexistente `404` e falhas internas `500`.

## Exploracao da integracao com frontend

O fluxo explorado e um `fetch` do formulario para o endpoint HTTP, enviando JSON. O servidor habilita CORS somente para a origem configurada em `FRONTEND_ORIGIN`; `OPTIONS` permite a verificacao previa do navegador. Ajuste essa origem para o endereco real da view:

```javascript
const resposta = await fetch('http://localhost:8081/api/candidatos', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(dadosDoFormulario)
});

if (!resposta.ok) {
  throw new Error((await resposta.json()).erro);
}
```

Para empresas, use `/api/empresas` e envie `cnpj` no lugar de `cpf`/`dataNascimento`. Este repositorio nao contem o frontend da aplicacao principal; o exemplo documenta o ponto de integracao a ser conectado pela view, sem alteracao no frontend.

## Testes

```bash
./gradlew test
```

A suite cobre regras de cadastro/validacao, respostas HTTP e CORS em servidor local, alem dos comandos SQL, parametros JDBC e fechamento dos recursos usando um driver de teste. Os testes nao inserem registros no PostgreSQL configurado.
