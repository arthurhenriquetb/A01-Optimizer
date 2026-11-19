$ErrorActionPreference = "Stop"
$host.UI.RawUI.WindowTitle = "Android Optimizer - Desconectar"
$root = Join-Path $env:LOCALAPPDATA "AndroidOptimizer"
$adb = Join-Path $root "platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
  Write-Host "O ADB nao foi encontrado neste computador."
  exit
}
$ipFile = Join-Path $root "last-ip.txt"
if (Test-Path $ipFile) {
  $ip = (Get-Content $ipFile -ErrorAction SilentlyContinue | Select-Object -First 1).Trim()
  if ($ip -match '^(\d{1,3}\.){3}\d{1,3}$') {
    & $adb connect "${ip}:5555" | Out-Null
    & $adb -s "${ip}:5555" usb
  }
}
Write-Host ""
Write-Host "Se a conexao nao foi encerrada, reinicie o celular."
Write-Host "Para maior seguranca, desative DEPURACAO USB nas opcoes do desenvolvedor."
