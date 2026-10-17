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