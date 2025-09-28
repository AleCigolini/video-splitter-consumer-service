![image](https://github.com/user-attachments/assets/c8996715-f174-4611-ab40-7c1d5ba35877)

# Video Splitter Consumer Service

## Variáveis de ambiente / Secrets (Local vs Kubernetes)
Este projeto usa variáveis relacionadas a Kafka (Event Hubs) e configurações internas:
- KAFKA_BOOTSTRAP_SERVERS
- KAFKA_SECURITY_PROTOCOL
- KAFKA_SASL_MECHANISM
- KAFKA_SASL_JAAS_CONFIG
- SEGMENT_TIME (não sensível)

No Kubernetes usamos `kubernetes/Secret.yaml` (chaves em `stringData`). No ambiente local (Docker Compose) usamos um arquivo `.env`.

## Limitação importante
O Docker Compose NÃO lê diretamente um `Secret.yaml` do Kubernetes. Precisamos converter para `.env` ou declarar manualmente em `docker-compose.yml`.

## Dependência: FFmpeg
A divisão de vídeo é feita invocando o binário `ffmpeg` pelo sistema operacional.

- No container Docker desta aplicação o FFmpeg já é instalado (ver `Dockerfile`). Portanto, ao usar `docker compose up` não é necessário configurar nada.
- Em execução local (sem Docker), é preciso ter o FFmpeg disponível no `PATH` ou configurar a propriedade `ffmpeg.binary`.

Opções para execução local:
- Instale o FFmpeg e adicione ao PATH do sistema.
- OU defina o caminho completo via variável de ambiente `FFMPEG_BINARY` (a aplicação lê `ffmpeg.binary=${FFMPEG_BINARY:ffmpeg}`):
  - Windows (cmd.exe, por usuário):
    ```bat
    setx FFMPEG_BINARY "C:\\ffmpeg\\bin\\ffmpeg.exe"
    ```
    Feche e reabra o terminal.
  - Windows (PowerShell, sessão atual):
    ```powershell
    $env:FFMPEG_BINARY = "C:\ffmpeg\bin\ffmpeg.exe"
    ```
  - Linux/macOS (bash/zsh):
    ```bash
    export FFMPEG_BINARY=/usr/bin/ffmpeg
    ```

Para verificar:
- Local: execute `ffmpeg -version` (ou o caminho que você configurou)
- No container: `docker compose exec app ffmpeg -version`

## Scripts auxiliares
Foram adicionados dois scripts PowerShell em `scripts/`:

1. `scripts/env-from-secret.ps1`
   - Converte `kubernetes/Secret.yaml` (stringData) em um arquivo `.env`.
   - Uso:
     ```powershell
     powershell -ExecutionPolicy Bypass -File scripts/env-from-secret.ps1 -SecretPath kubernetes/Secret.yaml -OutFile .env
     ```

2. `scripts/secret-from-env.ps1`
   - Gera `kubernetes/Secret.yaml` a partir de um `.env` (fonte de verdade local).
   - Uso:
     ```powershell
     powershell -ExecutionPolicy Bypass -File scripts/secret-from-env.ps1 -EnvPath .env -SecretPath kubernetes/Secret.yaml
     ```

## Fluxos sugeridos
### Fluxo A (recomendado)
1. Mantenha um `.env` local (NÃO commit em repositório público com chaves reais).
2. Gere o Secret para deploy:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/secret-from-env.ps1
   ```
3. Faça o deploy no cluster:
   ```bash
   kubectl apply -f kubernetes/Secret.yaml
   kubectl apply -f kubernetes/Deployment.yaml
   ```

### Fluxo B (a partir de um Secret existente)
1. Recebeu um `Secret.yaml` (com stringData) de outro time.
2. Converta para `.env`:
   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts/env-from-secret.ps1 -SecretPath kubernetes/Secret.yaml -OutFile .env
   ```
3. Rode localmente com Docker Compose.

## Executando com Docker Compose
O arquivo `docker-compose.yml` já referencia:
```yaml
env_file:
  - .env
```
Passos:
```bash
# 1. Gerar/atualizar .env (editar manualmente ou via script)
# 2. Build do app (gera target/quarkus-app)
# Se tiver Maven instalado: mvn -DskipTests package
# 3. Subir o stack
docker compose up -d --build
# 4. Logs
docker compose logs -f app
```
A aplicação lerá as variáveis do `.env` porque o Compose as injeta no contêiner.
