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

### Checkout

`POST /api/checkout` recebe o usuário, os dados de entrega/pagamento e os itens do carrinho:
envie o token Bearer retornado pelo login ou cadastro de cliente no cabeçalho `Authorization`.
O token expira em 8 horas e as sessões são mantidas em memória; reiniciar o backend encerra as sessões ativas.

```json
{
  "nome": "Cliente",
  "endereco": "Rua A, 10",
  "metodoPagamento": "PIX",
  "itens": [
    { "produtoId": 3, "quantidade": 2 }
  ]
}
```

O preço e o total são lidos/calculados no backend. Pedido, itens e baixa de estoque são
confirmados na mesma transação PostgreSQL; em caso de erro, a transação é revertida.
O status persistido e retornado é `CONCLUÍDO`.

O teste que acessa um PostgreSQL real é opt-in e deve usar um banco descartável:

```powershell
$env:RUN_POSTGRES_CHECKOUT_TESTS = "true"
$env:CHECKOUT_TEST_DB_URL = "jdbc:postgresql://localhost:5432/ecommerce_test"
$env:CHECKOUT_TEST_DB_USER = "postgres"
$env:CHECKOUT_TEST_DB_PASSWORD = "sua-senha"
.\mvnw.cmd test
```

Ao iniciar, você pode abrir o navegador ou Postman em http://localhost:8080/api/health para ver o status da API e da conexão com o banco.

## Deploy do backend na Vercel

O backend pode ser publicado como container usando `Dockerfile.vercel`.

1. No projeto do backend na Vercel, configure **Root Directory** como `Back`.
2. Faça um novo deploy. A Vercel detectará `Dockerfile.vercel` e executará o servidor Java.
3. Em **Settings → Environment Variables**, a URL da integração Neon pode aparecer como `bd_DATABASE_URL`; o backend também aceita `DATABASE_URL` e `DB_URL`. Configure o valor no projeto Back e ambiente Production. Não coloque credenciais no repositório.
4. Depois do deploy, confirme que `https://ecomerce-tdd-back-steel.vercel.app/api/health` retorna JSON com `"status": "UP"`.

O servidor usa a variável `PORT` fornecida pela Vercel. O banco precisa aceitar conexões de saída do container.