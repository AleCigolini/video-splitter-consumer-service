<#
Gera kubernetes/Secret.yaml a partir do arquivo .env em texto plano.
Uso:
  powershell -ExecutionPolicy Bypass -File scripts/secret-from-env.ps1 [-EnvPath .env] [-SecretPath kubernetes/Secret.yaml]
#>
param(
  [string]$EnvPath = '.env',
  [string]$SecretPath = 'kubernetes/Secret.yaml'
)

if (!(Test-Path $EnvPath)) {
  Write-Error "Arquivo $EnvPath não encontrado"; exit 1
}

$lines = Get-Content $EnvPath | Where-Object { $_ -and -not ($_ -match '^#') -and ($_ -match '=') }
$kvs = @{}
foreach ($l in $lines) {
  $parts = $l -split '=',2
  $key = $parts[0].Trim()
  $val = $parts[1]
  if ($val.StartsWith('"') -and $val.EndsWith('"')) { $val = $val.Substring(1, $val.Length-2) }
  if ($key -eq 'KAFKA_SASL_JAAS_CONFIG') {
    # Reverter escape usado no docker-compose (username="$$ConnectionString") para a forma original (username="$ConnectionString")
    $val = $val -replace 'username="\$\$ConnectionString"','username="$ConnectionString"'
  }
  # Se contiver caracteres especiais de YAML, encapsular em single quotes
  if ($val -match '[:";#]') {
    $val = $val -replace "'", "''"  # Escapar single quote duplicando
    $val = "'$val'"
  }
  $kvs[$key] = $val
}

$yaml = @()
$yaml += 'apiVersion: v1'
$yaml += 'kind: Secret'
$yaml += 'metadata:'
$yaml += '  name: video-splitter-secret'
$yaml += 'type: Opaque'
$yaml += 'stringData:'
foreach ($k in $kvs.Keys) {
  $yaml += "  $k: $($kvs[$k])"
}

$dir = Split-Path $SecretPath -Parent
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
$yaml | Set-Content -Encoding UTF8 $SecretPath
Write-Host "Secret gerado em $SecretPath"
