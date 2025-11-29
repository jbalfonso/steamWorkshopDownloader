@echo off
echo Compilando y ejecutando Steam Workshop Downloader con Java 21...
echo.

REM Configurar Java 21 y Maven
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "MAVEN_HOME=D:\Users\Juan\apache-maven-3.9.11"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

REM Verificar que Maven esté disponible
"%MAVEN_HOME%\bin\mvn.cmd" -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven no encontrado en %MAVEN_HOME%
    echo Por favor, verifica que Maven esté instalado correctamente.
    pause
    exit /b 1
)

REM Verificar que Java 21 esté disponible
"%JAVA_HOME%\bin\java.exe" -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java 21 no encontrado en %JAVA_HOME%
    echo Por favor, verifica que Java 21 esté instalado correctamente.
    pause
    exit /b 1
)

echo Java 21 detectado:
"%JAVA_HOME%\bin\java.exe" -version
echo.
echo Maven detectado:
"%MAVEN_HOME%\bin\mvn.cmd" -version
echo.

REM Compilar el proyecto
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Error al compilar el proyecto
    pause
    exit /b 1
)

REM Ejecutar la aplicación
echo.
echo Iniciando la aplicacion...
call mvn exec:java

pause

