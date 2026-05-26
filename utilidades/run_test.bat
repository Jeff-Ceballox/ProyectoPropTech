@echo off
cd /d "%~dp0"
set CP=target/classes
for %%j in (target\*.jar) do set CP=%CP%;%%j
java -cp "%CP%" com.proptech.Main
timeout /t 5 /nobreak >nul
curl http://localhost:7070/api/diagnostico/usuarios 2>nul || echo "Servidor no responde"