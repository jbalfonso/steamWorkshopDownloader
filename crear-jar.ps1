# Script PowerShell para crear JAR con Java 21
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Creando JAR ejecutable con Java 21" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurar rutas
$JAVA_HOME = "C:\Program Files\Java\jdk-21"
$MAVEN_HOME = "D:\Users\Juan\apache-maven-3.9.11"
$MAVEN_BIN = "$MAVEN_HOME\bin"
$JAVA_BIN = "$JAVA_HOME\bin"

# Agregar al PATH
$env:PATH = "$JAVA_BIN;$MAVEN_BIN;$env:PATH"

Write-Host "[1/4] Verificando Java 21..." -ForegroundColor Yellow
$javaVersion = & "$JAVA_BIN\java.exe" -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Java 21 no encontrado en $JAVA_HOME" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}
Write-Host "[OK] Java 21 encontrado" -ForegroundColor Green
Write-Host ""

Write-Host "[2/4] Verificando Maven..." -ForegroundColor Yellow
if (-not (Test-Path "$MAVEN_BIN\mvn.cmd")) {
    Write-Host "[ERROR] Maven no encontrado en $MAVEN_HOME" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}
$mavenVersion = & "$MAVEN_BIN\mvn.cmd" -version 2>&1 | Select-Object -First 1
Write-Host "[OK] Maven encontrado: $mavenVersion" -ForegroundColor Green
Write-Host ""

Write-Host "[3/4] Compilando y empaquetando proyecto..." -ForegroundColor Yellow
Write-Host "Esto puede tardar varios minutos la primera vez..." -ForegroundColor Gray
Write-Host ""

& "$MAVEN_BIN\mvn.cmd" clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Fallo al crear el JAR" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}
Write-Host ""

Write-Host "[4/4] Verificando JAR creado..." -ForegroundColor Yellow
$jarPath = "target\steam-workshop-downloader-1.0.0.jar"
if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length / 1MB
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "[EXITO] JAR creado correctamente!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "Ubicacion: $jarPath" -ForegroundColor White
    Write-Host "Tamaño: $([math]::Round($jarSize, 2)) MB" -ForegroundColor White
    Write-Host ""
    Write-Host "Para ejecutar, usa: .\ejecutar-jar.bat" -ForegroundColor Cyan
    Write-Host "O: & '$JAVA_BIN\java.exe' -jar $jarPath" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "[ERROR] JAR no encontrado en target\" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Read-Host "Presiona Enter para salir"

