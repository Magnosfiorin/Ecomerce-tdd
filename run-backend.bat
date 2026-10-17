@echo off
echo Iniciando o Backend E-Commerce TDD...
cd Back
call .\mvnw.cmd compile exec:java
pause
