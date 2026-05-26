@echo off
cd /d "C:\UQ\Estructura de datos\ProyectoPropTech\utilidades"
echo Iniciando servidor PropTech...
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java
pause