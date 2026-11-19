$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$host.UI.RawUI.WindowTitle = "Android Optimizer - Conectar"
Write-Host ""
Write-Host "ANDROID OPTIMIZER - CONFIGURACAO AUTOMATICA" -ForegroundColor Cyan
Write-Host "Ferramentas oficiais do Android. Nenhum comando para digitar."
Write-Host ""
$root = Join-Path $env:LOCALAPPDATA "AndroidOptimizer"
$adb = Join-Path $root "platform-tools\adb.exe"
try {
  if (-not (Test-Path $adb)) {
    Write-Host "[1/3] Baixando ferramentas oficiais do Google..."
    New-Item -ItemType Directory -Force -Path $root | Out-Null
    $zip = Join-Path $root "platform-tools.zip"
    Invoke-WebRequest -Uri "https://dl.google.com/android/repository/platform-tools-latest-windows.zip" -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath $root -Force
    Remove-Item $zip -Force -ErrorAction SilentlyContinue
  }
  if (-not (Test-Path $adb)) { throw "Nao foi possivel preparar o ADB." }
  Write-Host "[2/3] Conecte o celular com CABO USB DE DADOS."
  Write-Host "No telefone: ative DEPURACAO USB e toque PERMITIR quando aparecer."
  Write-Host "Aguardando o celular (ate 3 minutos)..."
  & $adb start-server | Out-Null
  $deadline = (Get-Date).AddMinutes(3)
  $serial = $null
  $last = ""
  while ((Get-Date) -lt $deadline) {
    $lines = @(& $adb devices 2>&1)
    $devices = @($lines | Where-Object { $_ -match '^([^\s]+)\s+device\s*$' -and $_ -notmatch '^(\d+\.){3}\d+:\d+' })
    if ($devices.Count -gt 0) {
      $serial = ($devices[0] -split '\s+')[0]
      break
    }
    $unauthorized = @($lines | Where-Object { $_ -match '\s+unauthorized\s*$' })
    $hint = if ($unauthorized.Count -gt 0) {
      "Autorize a depuracao USB na tela do celular."
    } else {
      "Aguardando USB. Desbloqueie o celular, confira o cabo e a depuracao USB."
    }
    if ($hint -ne $last) { Write-Host $hint -ForegroundColor Yellow; $last = $hint }
    Start-Sleep -Seconds 3
  }
  if (-not $serial) {
    throw "Celular nao autorizado ou nao detectado. Confira o cabo, a depuracao USB e o driver Motorola."
  }
  Write-Host "Celular autorizado!" -ForegroundColor Green
  $model = (& $adb -s $serial shell getprop ro.product.model 2>&1) -join ""
  Write-Host "Aparelho: $model"
  $address = (& $adb -s $serial shell ip -4 addr show wlan0 2>&1) -join " "
  $match = [regex]::Match($address, 'inet\s+(\d{1,3}(?:\.\d{1,3}){3})/')
  if ($match.Success) {
    Set-Content -Path (Join-Path $root "last-ip.txt") -Value $match.Groups[1].Value
  }
  Write-Host "[3/3] Preparando a conexao local..."
  $response = @(& $adb -s $serial tcpip 5555 2>&1)
  if ($LASTEXITCODE -ne 0) { throw "Nao foi possivel ativar a conexao ADB: $response" }
  Write-Host ""
  Write-Host "PRONTO!" -ForegroundColor Green
  Write-Host "Volte ao Android Optimizer no celular e toque VERIFICAR CONEXAO."
  Write-Host "Se o celular pedir outra autorizacao de depuracao, toque PERMITIR."
  Write-Host ""
  Write-Host "ATENCAO: a depuracao pela rede fica ativa ate reiniciar ou desativar."
  Write-Host "Use apenas em uma rede Wi-Fi confiavel. Para encerrar, execute Desconectar-Windows.cmd."
} catch {
  Write-Host ""
  Write-Host "A conexao nao foi concluida." -ForegroundColor Red
  Write-Host $_.Exception.Message -ForegroundColor Yellow
  Write-Host "Feche esta janela, confira o celular e execute o assistente novamente."
}
