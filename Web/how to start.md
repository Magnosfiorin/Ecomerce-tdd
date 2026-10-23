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

O teste E2E cria um cliente e um produto pela API e conclui uma compra. Mantenha o backend
na porta 8080 com um PostgreSQL de teste configurado; os dados de pedido permanecem nesse banco.
Para usar outra URL da API, defina `CYPRESS_API_URL` no Cypress e configure `Cypress.env('apiUrl')`
no `cypress.config.js`.

### ou interativo:
```
npm run cypress:open
```
Ambos os projetos (Back na porta 8080 e Web na porta 3000) estão prontos, integrados e configurados para o fluxo de TDD dos 5 integrantes!

## Teste de Performance da rota de produtos (JMeter)

Com o backend e o PostgreSQL de teste em execução:

```sh
jmeter -n -t Web/jmeter/produtos.jmx -l resultados-jmeter.jtl -e -o relatorio-jmeter
```

O cenário simula 20 usuários concorrentes por 60 segundos e valida HTTP 200 e a propriedade
`produtos` na resposta. O relatório HTML inclui o percentil 95. Para apontar para outra API:

```sh
jmeter -n -t Web/jmeter/produtos.jmx -Japi_host=localhost -Japi_port=8080 -Japi_protocol=http -l resultados-jmeter.jtl -e -o relatorio-jmeter
```

## Deploy do frontend na Vercel

1. No projeto Web na Vercel, configure **Root Directory** como `Web` e faça um novo deploy.
2. Em **Settings → Environment Variables**, configure `VITE_API_BASE_URL` como `https://ecomerce-tdd-back-steel.vercel.app/api` para os ambientes **Production**, **Preview** e **Development** que desejar.
3. Faça um novo deploy do frontend depois de adicionar ou alterar a variável: o Vite incorpora esse valor durante o build.

Sem `VITE_API_BASE_URL`, o frontend usa `/api` no mesmo domínio; o proxy do `vite.config.js` encaminha as chamadas para `http://localhost:8080` tanto em desenvolvimento quanto no `vite preview`. Se `.env` definir `VITE_API_BASE_URL` com `localhost`, esse valor também é convertido para `/api`, para que visitantes pelo túnel não tentem acessar o próprio computador. Para a Vercel com API remota, configure `VITE_API_BASE_URL` com o endereço HTTPS da API e publique um novo build após alterá-la.