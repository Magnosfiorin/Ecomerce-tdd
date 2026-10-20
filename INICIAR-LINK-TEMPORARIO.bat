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

echo Verificando frontend e proxy da API...
powershell.exe -NoProfile -Command "try {$r=Invoke-WebRequest -Uri 'http://localhost:4173/api/health' -TimeoutSec 3; if ($r.StatusCode -eq 200) {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
if not errorlevel 1 (
    set "FRONTEND_PORT=4173"
    goto :frontend_ready
)

powershell.exe -NoProfile -Command "try {$r=Invoke-WebRequest -Uri 'http://localhost:3000/api/health' -TimeoutSec 3; if ($r.StatusCode -eq 200) {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
if not errorlevel 1 (
    set "FRONTEND_PORT=3000"
    goto :frontend_ready
)

echo ERRO: nenhum frontend com o proxy da API foi encontrado.
echo Inicie o Vite Preview na porta 4173 ou npm run dev na porta 3000.
echo Confira se http://localhost:4173/api/health ou
echo http://localhost:3000/api/health abre localmente.
goto :error

:frontend_ready
echo Frontend e proxy da API OK na porta %FRONTEND_PORT%.
echo.

where.exe cloudflared >nul 2>&1
if not errorlevel 1 (
    echo Iniciando o Cloudflare Tunnel para a porta %FRONTEND_PORT%...
    start "E-Commerce - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k "cloudflared tunnel --url http://localhost:%FRONTEND_PORT%"
    goto :success
)

if exist "%CLOUDFLARED_EXE%" (
    echo Iniciando o Cloudflare Tunnel para a porta %FRONTEND_PORT%...
    start "E-Commerce - Cloudflare Tunnel" /D "%ROOT%" cmd.exe /k ""%CLOUDFLARED_EXE%" tunnel --url http://localhost:%FRONTEND_PORT%"
    goto :success
)

echo ERRO: cloudflared nao encontrado no PATH nem em:
echo %CLOUDFLARED_EXE%
echo Instale o Cloudflare Tunnel ou coloque cloudflared no PATH.
goto :error

:success
echo.
echo Uma nova janela foi aberta para o Cloudflare Tunnel.
echo Copie e compartilhe o endereco HTTPS trycloudflare.com mostrado nela.
echo Mantenha abertos o PostgreSQL, backend, frontend e tunel.
echo Pressione qualquer tecla para fechar esta janela.
pause >nul
exit /b 0

:error
echo.
pause
exit /b 1
