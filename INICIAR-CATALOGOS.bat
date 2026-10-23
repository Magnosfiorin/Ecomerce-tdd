@echo off
setlocal
title Iniciar catalogos do E-Commerce

set "ROOT=%~dp0"
set "WEB=%ROOT%Web"

echo ================================================
echo  Gerar e iniciar os dois catalogos
echo ================================================
echo.

where.exe npm >nul 2>&1
if errorlevel 1 (
    echo ERRO: npm nao encontrado no PATH.
    goto :error
)

echo Gerando o build do catalogo normal...
pushd "%WEB%"
set "VITE_CATALOGO_VARIANT="
call npm run build
set "BUILD_EXIT=%ERRORLEVEL%"
if not "%BUILD_EXIT%"=="0" (
    popd
    echo ERRO: falha ao gerar o build do catalogo normal.
    goto :error
)

echo.
echo Gerando o build do Catalogo B...
set "VITE_CATALOGO_VARIANT=catalogo-b"
call npm run build -- --outDir dist-catalogo-b
set "BUILD_EXIT=%ERRORLEVEL%"
set "VITE_CATALOGO_VARIANT="
if not "%BUILD_EXIT%"=="0" (
    popd
    echo ERRO: falha ao gerar o build do Catalogo B.
    goto :error
)
popd

echo.
echo Verificando se os previews ja estao respondendo...
call :check_preview 4173
if errorlevel 1 (
    echo Iniciando o catalogo normal na porta 4173...
    start "E-Commerce - Catalogo normal" /D "%WEB%" cmd.exe /k "npm run preview -- --host 127.0.0.1 --port 4173 --strictPort"
) else (
    echo Catalogo normal ja esta ativo na porta 4173; reutilizando.
)

call :check_preview 4174
if errorlevel 1 (
    echo Iniciando o Catalogo B na porta 4174...
    start "E-Commerce - Catalogo B" /D "%WEB%" cmd.exe /k "npm run preview -- --host 127.0.0.1 --port 4174 --strictPort --outDir dist-catalogo-b"
) else (
    echo Catalogo B ja esta ativo na porta 4174; reutilizando.
)

echo.
echo Aguardando os previews e os proxies da API iniciarem...
set /a ATTEMPT=0
:wait_for_previews
call :check_preview 4173
if errorlevel 1 goto :wait_again
call :check_preview 4174
if errorlevel 1 goto :wait_again

echo.
echo Os dois catalogos estao prontos:
echo   Normal: http://localhost:4173
echo   Catalogo B: http://localhost:4174
echo.
echo O backend e o PostgreSQL devem estar ativos para os dados da API.
echo Para criar os links publicos, execute INICIAR-LINK-TEMPORARIO.bat.
echo Mantenha abertas as janelas dos previews.
pause
exit /b 0

:wait_again
set /a ATTEMPT+=1
if %ATTEMPT% GEQ 20 (
    echo ERRO: um ou ambos os previews nao responderam em 40 segundos.
    echo Verifique as janelas abertas e se as portas 4173 e 4174 estao livres.
    echo A verificacao tambem exige que /api/health responda pelo proxy local.
    goto :error
)
timeout /t 2 /nobreak >nul
goto :wait_for_previews

:check_preview
powershell.exe -NoProfile -Command "$base='http://127.0.0.1:%~1'; try {$page=Invoke-WebRequest -Uri $base -TimeoutSec 2; $health=Invoke-WebRequest -Uri ($base+'/api/health') -TimeoutSec 2; if ($page.StatusCode -eq 200 -and $health.StatusCode -eq 200) {exit 0}; exit 1} catch {exit 1}" >nul 2>&1
exit /b %ERRORLEVEL%

:error
echo.
pause
exit /b 1
