@echo off
echo ========================================
echo Creando ejecutable EXE con jpackage
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
    echo ERROR: Java 21 no encontrado
    pause
    exit /b 1
)

echo.
echo [2/4] Compilando proyecto...
call "%MAVEN_BIN%\mvn.cmd" clean package -DskipTests
if errorlevel 1 (
    echo Error al compilar
    pause
    exit /b 1
)

echo.
echo [3/4] Preparando directorio para jpackage...
if exist "target\jpackage-input" rmdir /s /q "target\jpackage-input"
mkdir "target\jpackage-input"
copy "target\steam-workshop-downloader-1.0.0.jar" "target\jpackage-input\"

echo.
echo [4/4] Creando ejecutable EXE...
"%JAVA_BIN%\jpackage.exe" ^
    --input "target\jpackage-input" ^
    --name "SteamWorkshopDownloader" ^
    --main-jar steam-workshop-downloader-1.0.0.jar ^
    --main-class com.steamworkshop.downloader.SteamWorkshopDownloader ^
    --type exe ^
    --dest "target\dist" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "Steam Workshop Downloader" ^
    --win-shortcut ^
    --app-version "1.0.0" ^
    --vendor "Steam Workshop Downloader"

if errorlevel 1 (
    echo.
    echo Error al crear el EXE. Verifica que jpackage esté disponible en Java 21.
    pause
    exit /b 1
)

echo.
echo ========================================
echo EXE creado exitosamente!
echo Ubicacion: target\dist\SteamWorkshopDownloader.exe
echo ========================================
echo.
echo Puedes ejecutar el EXE con un simple doble clic.
echo.
pause

