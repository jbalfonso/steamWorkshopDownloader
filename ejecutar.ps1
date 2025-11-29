# Script para compilar y ejecutar la aplicación
Write-Host "Compilando y ejecutando Steam Workshop Downloader..." -ForegroundColor Green
Write-Host ""

# Compilar el proyecto
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al compilar el proyecto" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host ""
Write-Host "Iniciando la aplicación..." -ForegroundColor Green
mvn exec:java

