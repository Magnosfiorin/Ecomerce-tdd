# Como Rodar o Projeto

## Opção A (Via Terminal na pasta Back):
```
cd Back
.\mvnw.cmd compile exec:java
```
## Opção B (Via Script na Raiz):

Dê um duplo clique ou execute no terminal:

```
.\run-backend.bat
```

## Para Rodar os Testes:
```
cd Back
.\mvnw.cmd test
```

Ao iniciar, você pode abrir o navegador ou Postman em http://localhost:8080/api/health para ver o status da API e da conexão com o banco.

## Deploy do backend na Vercel

O backend pode ser publicado como container usando `Dockerfile.vercel`.

1. No projeto do backend na Vercel, configure **Root Directory** como `Back`.
2. Faça um novo deploy. A Vercel detectará `Dockerfile.vercel` e executará o servidor Java.
3. Em **Settings → Environment Variables**, configure `DB_URL`, `DB_USER` e `DB_PASSWORD` com as credenciais do PostgreSQL de produção. Não coloque essas credenciais no repositório.
4. Depois do deploy, confirme que `https://ecomerce-tdd-back.vercel.app/api/health` retorna JSON com `"status": "UP"`.

O servidor usa a variável `PORT` fornecida pela Vercel. O banco precisa aceitar conexões de saída do container.