@echo off
setlocal
title Link temporario do E-Commerce

set "ROOT=%~dp0"
set "CLOUDFLARED_EXE=%ROOT%Web\img\cloudflared-windows-amd64.exe"

echo ================================================
echo  Iniciar link temporario do E-Commerce
echo ================================================
echo.
echo Este script nao inicia nem compila o projeto.
echo Inicie antes o PostgreSQL, o backend e o frontend.
echo.

echo Verificando backend e PostgreSQL em localhost:8080...
powershell.exe -NoProfile -Command "try {$h=Invoke-RestMethod -Uri 'http://localhost:8080/api/health' -TimeoutSec 3; if ($h.status -eq 'UP' -and $h.database -eq 'CONECTADO') {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
if errorlevel 1 (
    echo ERRO: backend ou PostgreSQL indisponivel/desconectado.
    echo Inicie o PostgreSQL e o backend, depois confira:
    echo http://localhost:8080/api/health
    goto :error
)
echo Backend e PostgreSQL OK.
echo.

echo Verificando os dois frontends e seus proxies da API...
powershell.exe -NoProfile -Command "try {$r=Invoke-WebRequest -Uri 'http://localhost:4173/api/health' -TimeoutSec 3; if ($r.StatusCode -eq 200) {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
if errorlevel 1 (
    echo ERRO: o catalogo normal nao responde em http://localhost:4173.
    echo Inicie o preview normal na porta 4173 antes de abrir o tunel.
    goto :error
)

powershell.exe -NoProfile -Command "try {$r=Invoke-WebRequest -Uri 'http://localhost:4174/api/health' -TimeoutSec 3; if ($r.StatusCode -eq 200) {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
if errorlevel 1 (
    echo ERRO: o catalogo B nao responde em http://localhost:4174.
    echo Inicie o preview do Catalogo B na porta 4174 antes de abrir o tunel.
    goto :error
)
echo Os dois frontends e proxies da API estao OK.
echo.

where.exe cloudflared >nul 2>&1
if not errorlevel 1 (
    echo Iniciando dois Cloudflare Tunnels...
    start "Catalogo normal - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k "cloudflared tunnel --url http://localhost:4173"
    start "Catalogo B - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k "cloudflared tunnel --url http://localhost:4174"
    goto :success
)

if exist "%CLOUDFLARED_EXE%" (
    echo Iniciando dois Cloudflare Tunnels...
    start "Catalogo normal - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k ""%CLOUDFLARED_EXE%" tunnel --url http://localhost:4173"
    start "Catalogo B - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k ""%CLOUDFLARED_EXE%" tunnel --url http://localhost:4174"
    goto :success
)

echo ERRO: cloudflared nao encontrado no PATH nem em:
echo %CLOUDFLARED_EXE%
echo Instale o Cloudflare Tunnel ou coloque cloudflared no PATH.
goto :error

:success
echo.
echo Uma nova janela foi aberta para o Cloudflare Tunnel.
echo Copie os dois enderecos HTTPS trycloudflare.com mostrados nas janelas:
echo um para o catalogo normal e outro para o Catalogo B.
echo Mantenha abertos o PostgreSQL, backend, os dois frontends e os tuneis.
echo Pressione qualquer tecla para fechar esta janela.
pause >nul
exit /b 0

:error
echo.
pause
exit /b 1
