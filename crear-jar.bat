@echo off
echo ========================================
echo Creando JAR ejecutable con Java 21
echo ========================================
echo.

REM Configurar rutas
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "MAVEN_HOME=D:\Users\Juan\apache-maven-3.9.11"
set "MAVEN_BIN=%MAVEN_HOME%\bin"
set "JAVA_BIN=%JAVA_HOME%\bin"

REM Agregar al PATH
set "PATH=%JAVA_BIN%;%MAVEN_BIN%;%PATH%"

echo [1/4] Verificando Java 21...
"%JAVA_BIN%\java.exe" -version
if errorlevel 1 (
    echo [ERROR] Java 21 no encontrado en %JAVA_HOME%
    pause
    exit /b 1
)
echo [OK] Java 21 encontrado
echo.

echo [2/4] Verificando Maven...
"%MAVEN_BIN%\mvn.cmd" -version >nul 2>&1
set MAVEN_CHECK=%ERRORLEVEL%
if %MAVEN_CHECK% NEQ 0 (
    echo [ERROR] Maven no encontrado en %MAVEN_HOME%
    pause
    exit /b 1
)
echo [OK] Maven encontrado
echo.

echo [3/4] Compilando y empaquetando proyecto...
echo Esto puede tardar varios minutos la primera vez...
echo.
call "%MAVEN_BIN%\mvn.cmd" clean package
if errorlevel 1 (
    echo.
    echo [ERROR] Fallo al crear el JAR
    pause
    exit /b 1
)
echo.

echo [4/4] Verificando JAR creado...
if exist "target\steam-workshop-downloader-1.0.0.jar" (
    echo.
    echo ========================================
    echo [EXITO] JAR creado correctamente!
    echo ========================================
    echo Ubicacion: target\steam-workshop-downloader-1.0.0.jar
    echo.
    echo Para ejecutar, usa: ejecutar-jar.bat
    echo O: "%JAVA_BIN%\java.exe" -jar target\steam-workshop-downloader-1.0.0.jar
    echo.
) else (
    echo [ERROR] JAR no encontrado en target\
    pause
    exit /b 1
)

pause
