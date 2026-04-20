# Gerador de QR Code API

API backend em Spring Boot para gerar QR Codes em PNG, armazenar os arquivos em um bucket privado no Amazon S3 e retornar uma URL presignada temporaria para download ou preview seguro no frontend.

## Visao Geral

Este projeto foi construido com foco em arquitetura backend, integracao com servicos externos e preparo para deploy real.

Fluxo principal:

1. O cliente envia um texto para a API.
2. O backend gera o QR Code em memoria usando ZXing.
3. O arquivo PNG e enviado para o Amazon S3.
4. A API retorna uma URL presignada com tempo de expiracao.

Essa abordagem evita expor o bucket publicamente e mantem o controle de acesso no backend.

## Stack

- Java 17
- Spring Boot 4.0.5
- Spring Web
- Spring Validation
- Spring Actuator
- Google ZXing 3.5.2
- AWS SDK for Java v2
- Docker
- Maven Wrapper

## Arquitetura

O projeto segue uma separacao simples e intencional de responsabilidades:

- `controller`: recebe a requisicao HTTP e devolve a resposta
- `dto`: define os contratos de entrada, saida e erro
- `service`: concentra a regra de geracao do QR Code
- `ports`: abstrai o servico de armazenamento
- `infrastructure`: implementa a integracao real com o S3
- `config`: centraliza configuracoes como CORS
- `GlobalExceptionHandler`: padroniza erros 400 e 500

Decisoes arquiteturais principais:

- Validacao com `@Valid`, `@NotBlank` e `@Size` na borda da aplicacao
- `QrCodeGeneratorService` stateless para facilitar teste e escalabilidade
- `StoragePort` para desacoplar regra de negocio da infraestrutura AWS
- Bucket privado com presigned URL para entregar seguranca sem complicar o frontend
- `DefaultCredentialsProvider` para compatibilidade com ambiente local, Docker e cloud

## Endpoint

### `POST /qrcode`

Gera um QR Code a partir do texto informado, faz upload da imagem para o S3 e retorna os metadados do arquivo gerado.

### Request

```json
{
  "text": "https://github.com/seu-usuario"
}
```

### Regras de validacao

- `text` e obrigatorio
- `text` nao pode estar vazio
- `text` nao pode ultrapassar 500 caracteres

### Response `200 OK`

```json
{
  "downloadUrl": "https://seu-bucket.s3.sa-east-1.amazonaws.com/arquivo.png?...",
  "expiresInSeconds": 900,
  "fileName": "9d7d9d1d-4e5a-4a41-9f22-1d9f2c5f0b01.png",
  "contentType": "image/png"
}
```

### Response `400 Bad Request`

```json
{
  "timeStamp": "2026-04-20T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/qrcode",
  "fieldErrors": {
    "text": "O texto e obrigatorio"
  }
}
```

### Response `500 Internal Server Error`

```json
{
  "timeStamp": "2026-04-20T12:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Internal server error",
  "path": "/qrcode",
  "fieldErrors": null
}
```

### Exemplo com `curl`

```bash
curl -X POST http://localhost:8080/qrcode \
  -H "Content-Type: application/json" \
  -d '{"text":"https://github.com/seu-usuario"}'
```

## Variaveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
AWS_REGION=sa-east-1
AWS_S3_BUCKET_NAME=seu-bucket
AWS_S3_PRESIGNED_DURATION_MINUTES=15
AWS_ACCESS_KEY_ID=sua_access_key
AWS_SECRET_ACCESS_KEY=sua_secret_key
```

Observacoes:

- `PORT` possui fallback para `8080`
- `CORS_ALLOWED_ORIGINS` deve apontar para o dominio real do frontend em producao
- As credenciais AWS precisam existir no processo do backend ou no container
- O bucket deve permanecer privado
- Use `.env.example` como referencia de configuracao local sem expor segredos

## Como Executar

### Localmente

Executar os testes:

```bash
./mvnw test
```

Subir a aplicacao:

```bash
./mvnw spring-boot:run
```

Validar health check:

```bash
curl http://localhost:8080/actuator/health
```

### Com Docker

Build da imagem:

```bash
docker build -t gerador-qrcode .
```

Execucao do container:

```bash
docker run --rm -p 8080:8080 --env-file .env gerador-qrcode
```

## Deploy no Render

Este repositório ja inclui um arquivo [`render.yaml`](./render.yaml) para publicar a API como Web Service usando o `Dockerfile` da raiz.

### O que o Render vai usar

- runtime `docker`
- health check em `GET /actuator/health`
- porta via variavel `PORT` com valor `10000`
- deploy automatico por commit

### Variaveis que voce precisa configurar no Render

Configure estas variaveis no servico antes do deploy final:

- `AWS_REGION`
- `AWS_S3_BUCKET_NAME`
- `AWS_S3_PRESIGNED_DURATION_MINUTES`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `CORS_ALLOWED_ORIGINS`

Observacoes importantes:

- `CORS_ALLOWED_ORIGINS` deve conter a URL publica do frontend, por exemplo `https://seu-front.netlify.app`
- o Render injeta `PORT` automaticamente para Web Services, e a aplicacao ja esta preparada para usar esse valor
- nao suba credenciais reais para o Git; preencha os segredos no painel do Render

### Passo a passo

1. Suba este repositorio para GitHub, GitLab ou Bitbucket.
2. No Render, clique em `New +` e escolha `Blueprint` se quiser usar o `render.yaml`.
3. Conecte o repositório e confirme a criacao do servico `gerador-qrcode-api`.
4. No painel do servico, preencha as variaveis de ambiente faltantes.
5. Rode o primeiro deploy.
6. Quando o deploy terminar, valide:

```bash
curl https://SEU-SERVICO.onrender.com/actuator/health
```

7. Depois teste o endpoint principal:

```bash
curl -X POST https://SEU-SERVICO.onrender.com/qrcode \
  -H "Content-Type: application/json" \
  -d '{"text":"https://github.com/seu-usuario"}'
```

### Se preferir criar manualmente no painel

Voce tambem pode criar um `Web Service` manualmente no Render com estes parametros:

- Runtime: `Docker`
- Dockerfile path: `./Dockerfile`
- Docker build context: `.`
- Health Check Path: `/actuator/health`
- Auto-Deploy: `On Commit`

Depois, adicione as mesmas variaveis de ambiente listadas acima.

## Fluxo de Consumo no Frontend

Fluxo recomendado:

1. O frontend envia o texto para `POST /qrcode`.
2. O backend devolve `downloadUrl`, `expiresInSeconds`, `fileName` e `contentType`.
3. O frontend usa a `downloadUrl` imediatamente para preview ou download.
4. A URL nao deve ser tratada como permanente, porque expira.

Importante:

- O Postman testa apenas a API HTTP
- As credenciais AWS precisam existir no ambiente do backend
- Dependendo da forma de consumo da `downloadUrl`, pode ser necessario configurar CORS tambem no S3

## Estrategia de Testes

O projeto possui:

- teste de controller para proteger o contrato HTTP
- teste de service para proteger a regra de aplicacao

Essa separacao ajuda a manter a API evolutiva sem quebrar o consumo pelo frontend.

## Checklist Final de Deploy

Antes do deploy, valide:

- bucket S3 privado criado
- credenciais AWS disponiveis no ambiente de execucao
- `AWS_REGION` configurado corretamente
- `AWS_S3_BUCKET_NAME` configurado corretamente
- `CORS_ALLOWED_ORIGINS` apontando para o frontend real
- `./mvnw test` executando com sucesso
- `docker build` executando com sucesso
- `POST /qrcode` funcionando no ambiente local ou container
- `GET /actuator/health` respondendo corretamente
- `.env` ignorado no Git
- `.dockerignore` presente na raiz do projeto

## Valor Tecnico do Projeto

Este projeto demonstra:

- modelagem de contrato HTTP
- validacao de entrada
- tratamento global de excecoes
- arquitetura em camadas
- uso de porta e adaptador
- integracao com AWS S3
- seguranca com presigned URL
- empacotamento com Docker
- preocupacao com deploy real
