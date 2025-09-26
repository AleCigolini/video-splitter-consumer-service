<#
Converte kubernetes/Secret.yaml (stringData) em um arquivo .env para uso local com docker-compose.
Uso:
  powershell -ExecutionPolicy Bypass -File scripts/env-from-secret.ps1 -SecretPath kubernetes/Secret.yaml -OutFile .env
#>
param(
  [string]$SecretPath = 'kubernetes/Secret.yaml',
  [string]$OutFile = '.env'
)

if (!(Test-Path $SecretPath)) { Write-Error "Secret não encontrado: $SecretPath"; exit 1 }

$inStringData = $false
$envLines = @()
Get-Content $SecretPath | ForEach-Object {
  $line = $_
  if ($line -match '^stringData:') { $inStringData = $true; return }
  if ($inStringData) {
    if ($line -match '^[^\s]') { $inStringData = $false }
    elseif ($line -match '^\s{2,}([A-Z0-9_]+):\s*(.*)$') {
      $key = $matches[1]
      $raw = $matches[2]
      # Remove comentários inline
      $raw = ($raw -split '\s+#')[0].Trim()
      # Remove aspas externas simples ou duplas se existirem
      if (($raw.StartsWith('"') -and $raw.EndsWith('"')) -or ($raw.StartsWith("'") -and $raw.EndsWith("'"))) {
        $raw = $raw.Substring(1, $raw.Length-2)
      }
      # Reescapar qualquer CRLF
      $raw = $raw -replace "`r", ''
      if ($key -eq 'KAFKA_SASL_JAAS_CONFIG') {
        # Escapar $ConnectionString -> $$ConnectionString para docker compose não interpolar
        $raw = $raw -replace 'username="\$ConnectionString"','username="$$ConnectionString"'
      }
      $envLines += "$key=$raw"
    }
  }
}

if ($envLines.Count -eq 0) { Write-Error 'Nenhuma chave encontrada em stringData.'; exit 2 }
$envLines | Set-Content -Encoding UTF8 $OutFile
Write-Host "Arquivo .env gerado: $OutFile"
