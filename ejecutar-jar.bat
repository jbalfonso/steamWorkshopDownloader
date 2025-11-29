@echo off
echo Ejecutando Steam Workshop Downloader con Java 21...
echo.

REM Configurar Java 21
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Verificar que el JAR existe
if not exist "target\steam-workshop-downloader-1.0.0.jar" (
    echo ERROR: JAR no encontrado en target\steam-workshop-downloader-1.0.0.jar
    echo Por favor, ejecuta primero: crear-jar.bat
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
echo Iniciando aplicacion...
echo.

REM Ejecutar el JAR con Java 21
"%JAVA_HOME%\bin\java.exe" -jar target\steam-workshop-downloader-1.0.0.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error al ejecutar la aplicacion
    pause
    exit /b 1
)

pause

