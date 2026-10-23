# Iniciar o projeto e compartilhar um link temporário

Este guia inicia a aplicação completa neste computador e a disponibiliza
temporariamente pela internet com o Cloudflare Tunnel.

## Como a conexão funciona

```text
Navegador do visitante
        |
        v
Links https://...trycloudflare.com (um por catálogo)
        |
        +-- túnel 1 -> preview catálogo normal (porta 4173)
        |                  |
        +-- túnel 2 -> preview catálogo B (porta 4174)
                           |
                           +-- chamadas /api -> proxy Vite
                                                -> backend local (8080)
                                                -> PostgreSQL local (5432)
```

O visitante só precisa abrir o link: não precisa instalar o projeto nem iniciar
o backend. **Este computador**, porém, precisa permanecer ligado, conectado à
internet e com PostgreSQL, backend, os dois frontends e os dois túneis em
execução. Cada link `trycloudflare.com` é temporário e pode mudar quando o
túnel correspondente for reiniciado. Ambos os catálogos usam a mesma API e o
mesmo banco de dados.

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

### 3. Gere e inicie os dois catálogos

Na raiz do repositório, execute:

```powershell
.\INICIAR-CATALOGOS.bat
```

O script gera os builds normal e B, inicia os previews em janelas separadas nas
portas 4173 e 4174 e aguarda os dois endpoints `/api/health` responderem. Se um
preview já estiver servindo o site e o proxy da API, ele é reutilizado. Se uma
porta estiver ocupada por outro programa, confira as janelas e libere a porta
manualmente; o script não encerra processos existentes.

Os endereços locais para conferência são `http://localhost:4173` (catálogo
normal) e `http://localhost:4174` (catálogo B). O backend Java e o PostgreSQL
devem estar ativos antes de iniciar os catálogos.

Se preferir executar os passos manualmente, use os comandos a seguir.

Os dois builds são necessários: o normal mantém o catálogo padrão; o outro
configura o Catalogo B como página inicial. Abra uma janela de PowerShell na
pasta `Web` e execute:

```powershell
cd C:\Users\magno\Documents\GitHub\Ecomerce-tdd\Web
npm run build
$env:VITE_CATALOGO_VARIANT='catalogo-b'
npm run build -- --outDir dist-catalogo-b
Remove-Item Env:VITE_CATALOGO_VARIANT
```

### 4. Inicie os dois frontends de produção

Abra **duas janelas de PowerShell** na pasta `Web`. Na primeira, inicie o
catálogo normal:

```powershell
cd C:\Users\magno\Documents\GitHub\Ecomerce-tdd\Web
npm run preview -- --host 127.0.0.1 --port 4173 --strictPort
```

Na segunda, inicie o Catalogo B usando o build alternativo:

```powershell
cd C:\Users\magno\Documents\GitHub\Ecomerce-tdd\Web
npm run preview -- --host 127.0.0.1 --port 4174 --strictPort --outDir dist-catalogo-b
```

Os dois frontends usam `/api` no próprio domínio. O proxy do Vite encaminha
as chamadas para o mesmo backend em `http://localhost:8080`; portanto, os
visitantes não precisam executar serviços localmente.

Antes de abrir os túneis, confira que ambos os endereços retornam o status
esperado:

```powershell
Invoke-RestMethod http://localhost:4173/api/health
Invoke-RestMethod http://localhost:4174/api/health
```

### 5. Inicie os dois túneis públicos

Com PostgreSQL, backend e os dois frontends já ativos, dê duplo clique em
`INICIAR-LINK-TEMPORARIO.bat` na raiz ou execute:

```powershell
.\INICIAR-LINK-TEMPORARIO.bat
```

O script valida o backend e o banco e depois verifica o proxy da API nos
frontends `localhost:4173` e `localhost:4174`. Se tudo responder, abre uma
janela de túnel para cada catálogo; se algo estiver parado, informa o problema
e termina sem ficar aguardando indefinidamente.

Copie os endereços HTTPS exibidos nas janelas e compartilhe separadamente:
um para o catálogo normal (porta 4173) e outro para o Catalogo B (porta 4174).
**Não feche as janelas dos túneis.** A rota `/api/health` de cada link deve
responder com a API e o banco conectados.

## Durante a apresentação

- Mantenha abertas as janelas do backend, dos dois previews e dos dois túneis.
- Mantenha o PostgreSQL iniciado.
- Envie a cada grupo o link correspondente ao catálogo desejado.
- Se um túnel for reiniciado e o endereço mudar, compartilhe o novo link desse
  catálogo. Não é necessário alterar o endereço da API nem gerar outro build
  apenas porque o domínio mudou.
- Use apenas contas e dados fictícios. Quem tiver o link poderá acessar a
  aplicação enquanto ela estiver publicada.
- O backend guarda sessões em memória. Reiniciar o backend encerra as sessões
  ativas, então os usuários precisarão entrar novamente.
- Se a recuperação de senha por e-mail for usada, o `APP_BASE_URL` no `.env`
  precisa apontar para o endereço público atual do túnel; links enviados antes
  de uma mudança de endereço podem deixar de funcionar.

## Encerrar

Pressione `Ctrl+C` nas duas janelas do Cloudflare Tunnel, nos dois previews e
no backend. O PostgreSQL pode continuar em execução como serviço do Windows.

## Se algo não funcionar

1. `http://localhost:8080/api/health` falha: confira a janela do backend, o
   PostgreSQL e o `.env`.
2. Um dos previews não abre o catálogo esperado: confirme que o build B foi
   gerado com `VITE_CATALOGO_VARIANT='catalogo-b'` e que o preview na porta
   4174 usa `dist-catalogo-b`.
3. A API local funciona, mas `/api/health` em um preview falha: confirme o
   proxy `/api` para `http://localhost:8080` em `Web\vite.config.js`.
4. Os previews locais funcionam, mas um link público falha: confirme que cada
   túnel aponta para a porta correta (4173 para o normal e 4174 para B) e que
   seus processos continuam ativos.
5. O site indica a API indisponível: abra `/api/health` no link desse catálogo.
   Se não responder, investigue o proxy ou o backend; se responder, recarregue
   a página.
