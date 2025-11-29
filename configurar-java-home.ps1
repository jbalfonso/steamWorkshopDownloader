# Script para configurar JAVA_HOME permanentemente en Windows
# Debe ejecutarse como Administrador

$javaHome = "C:\Program Files\Java\jdk-21"

Write-Host "Configurando JAVA_HOME a: $javaHome" -ForegroundColor Green

# Configurar JAVA_HOME en Variables de Usuario
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "User")

# Agregar JAVA_HOME\bin al PATH si no está ya
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$javaBinPath = "$javaHome\bin"

if ($currentPath -notlike "*$javaBinPath*") {
    $newPath = "$javaBinPath;$currentPath"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "Agregado $javaBinPath al PATH" -ForegroundColor Green
} else {
    Write-Host "$javaBinPath ya está en el PATH" -ForegroundColor Yellow
}

Write-Host "`nJAVA_HOME configurado correctamente!" -ForegroundColor Green
Write-Host "Por favor, cierra y vuelve a abrir todas las terminales/IDEs para que los cambios surtan efecto." -ForegroundColor Yellow

