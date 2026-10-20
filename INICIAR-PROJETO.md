# Iniciar o projeto e compartilhar um link temporário

Este guia inicia a aplicação completa neste computador e a disponibiliza
temporariamente pela internet com o Cloudflare Tunnel.

## Como a conexão funciona

```text
Navegador do visitante
        |
        v
Link https://...trycloudflare.com
        |
        v
Cloudflare Tunnel -> frontend Vite Preview (porta 4173)
        |
        +-- arquivos do site
        |
        +-- chamadas /api -> proxy Vite -> backend local (porta 8080)
                                             |
                                             v
                                      PostgreSQL local (porta 5432)
```

O visitante só precisa abrir o link: não precisa instalar o projeto nem iniciar
o backend. **Este computador**, porém, precisa permanecer ligado, conectado à
internet e com PostgreSQL, backend, frontend e túnel em execução. O link
`trycloudflare.com` é temporário e pode mudar quando o túnel for reiniciado.

## Pré-requisitos

- Windows, Node.js/npm e Java instalados.
- PostgreSQL instalado e iniciado.
- Cloudflare Tunnel disponível pelo comando `cloudflared`. Se ele não estiver
  no `PATH`, o executável `Web\img\cloudflared-windows-amd64.exe`, caso esteja
  presente no projeto, pode ser iniciado diretamente conforme o passo 4.
- Arquivo `.env` da raiz configurado com `DB_URL`, `DB_USER` e `DB_PASSWORD`
  válidos para o PostgreSQL local. **Não compartilhe nem publique esse arquivo.**

O backend usa o PostgreSQL local da porta 5432 por padrão. Se suas credenciais
forem diferentes, configure as variáveis no `.env` da raiz antes de iniciar.
Ao conectar, o backend inicializa as tabelas necessárias.

## Inicialização

Antes de abrir o link, inicie o PostgreSQL, o backend e o frontend. O arquivo
`INICIAR-LINK-TEMPORARIO.bat` **não inicia nem compila esses serviços**: ele
apenas verifica se estão respondendo e abre o Cloudflare Tunnel.

### 1. Inicie o PostgreSQL

```powershell
Get-Service -Name '*postgres*'
```

Se o serviço estiver parado, inicie-o pelo PowerShell como administrador,
substituindo pelo nome retornado acima:

```powershell
Start-Service -Name 'NOME-DO-SERVICO'
```

Não exponha a porta 5432 à internet; o túnel deve publicar somente o frontend.

### 2. Inicie o backend Java

Na raiz do repositório, execute `.\run-backend.bat` ou:

```powershell
cd C:\Users\magno\Documents\GitHub\Ecomerce-tdd
.\run-backend.bat
```

Espere o servidor iniciar. Confirme que a API e o banco estão conectados:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

O resultado esperado inclui `"status": "UP"` e
`"database": "CONECTADO"`. Se o status do banco estiver desconectado, confira
PostgreSQL e `DB_URL`, `DB_USER` e `DB_PASSWORD` no `.env` antes de continuar.

### 3. Gere e inicie o frontend de produção

Para compartilhar a versão de produção, entre na pasta `Web`, gere o build e
inicie o preview:

```powershell
cd C:\Users\magno\Documents\GitHub\Ecomerce-tdd\Web
npm run build
npm run preview -- --host 127.0.0.1 --port 4173 --strictPort
```

Para desenvolvimento, também é possível usar `npm run dev` na pasta `Web`;
nesse modo o frontend fica na porta 3000. O inicializador detecta tanto o
preview (porta 4173) quanto o servidor de desenvolvimento (porta 3000).

O frontend usa `/api` no mesmo domínio. O proxy do Vite encaminha as chamadas
para o backend em `http://localhost:8080`; por isso, quem acessa o link não
tenta conectar ao próprio computador.

Antes de abrir o túnel, confirme que `http://localhost:4173/api/health`
(preview) ou `http://localhost:3000/api/health` (desenvolvimento) retorna o
status esperado. Deixe o frontend em execução.

### 4. Inicie o túnel público

Com PostgreSQL, backend e frontend já ativos, dê duplo clique em
`INICIAR-LINK-TEMPORARIO.bat` na raiz ou execute:

```powershell
.\INICIAR-LINK-TEMPORARIO.bat
```

O script valida se o backend responde em `localhost:8080/api/health` com o
banco conectado. Depois verifica o proxy da API no frontend em `localhost:4173`
ou `localhost:3000` e inicia o túnel para a porta que estiver respondendo.
Se algum serviço estiver parado, ele informa o problema e termina sem ficar
aguardando indefinidamente.

Copie o endereço HTTPS `https://...trycloudflare.com` exibido na janela do
túnel e compartilhe-o. **Não feche essa janela.** A rota pública
`https://...trycloudflare.com/api/health` deve responder com API e banco
conectados.

## Durante a apresentação

- Mantenha abertas as janelas do backend, do frontend (Preview ou
  desenvolvimento) e do túnel.
- Mantenha o PostgreSQL iniciado.
- Envie aos visitantes apenas o link HTTPS do túnel.
- Se o túnel for reiniciado e o endereço mudar, compartilhe o novo link. Com
  esta configuração, não é necessário alterar o endereço da API nem gerar
  outro build apenas porque o domínio mudou.
- Use apenas contas e dados fictícios. Quem tiver o link poderá acessar a
  aplicação enquanto ela estiver publicada.
- O backend guarda sessões em memória. Reiniciar o backend encerra as sessões
  ativas, então os usuários precisarão entrar novamente.
- Se a recuperação de senha por e-mail for usada, o `APP_BASE_URL` no `.env`
  precisa apontar para o endereço público atual do túnel; links enviados antes
  de uma mudança de endereço podem deixar de funcionar.

## Encerrar

Pressione `Ctrl+C` nas janelas do Cloudflare Tunnel, do frontend e do backend.
O PostgreSQL pode continuar em execução como serviço do Windows.

## Se algo não funcionar

1. `http://localhost:8080/api/health` falha: confira a janela do backend, o
   PostgreSQL e o `.env`.
2. A API local funciona, mas `/api/health` no frontend falha:
   confirme que o Vite Preview está em execução e que seu proxy `/api` aponta
   para `http://localhost:8080` em `Web\vite.config.js`. Se estiver usando
   `npm run dev`, teste `http://localhost:3000/api/health`.
3. Os endereços locais funcionam, mas o link público falha: confirme que o
   túnel aponta para a mesma porta do frontend em execução e que as janelas
   do backend, frontend e túnel continuam abertas.
4. O site abre, mas indica a API indisponível: abra a rota pública
   `/api/health`. Se ela não responder corretamente, investigue o proxy ou o
   backend; se responder, recarregue o site.
