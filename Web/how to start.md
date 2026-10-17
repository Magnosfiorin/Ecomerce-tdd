## Como Rodar o Frontend
# Opção A (Via Terminal na pasta Web):
```
cd Web
npm run dev
```
O servidor estará acessível em: http://localhost:3000

# Opção B (Via Script na Raiz do Repositório):

Dê um duplo clique ou execute no terminal:
```
.\run-frontend.bat
```

## Para Rodar o Teste E2E (Cypress):
```
cd Web
npm run cypress:run
```
### ou interativo:
```
npm run cypress:open
```
Ambos os projetos (Back na porta 8080 e Web na porta 3000) estão prontos, integrados e configurados para o fluxo de TDD dos 5 integrantes!