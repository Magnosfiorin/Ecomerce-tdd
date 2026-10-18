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

## Deploy do frontend na Vercel

1. No projeto Web na Vercel, configure **Root Directory** como `Web` e faça um novo deploy.
2. Em **Settings → Environment Variables**, configure `VITE_API_BASE_URL` como `https://ecomerce-tdd-back-steel.vercel.app/api` para os ambientes **Production**, **Preview** e **Development** que desejar.
3. Faça um novo deploy do frontend depois de adicionar ou alterar a variável: o Vite incorpora esse valor durante o build.

O frontend também usa essa URL como padrão nos builds de produção. Em desenvolvimento local, continua usando `http://localhost:8080/api`. A API precisa estar publicada e responder em `/api/health` antes de testar as chamadas do site.