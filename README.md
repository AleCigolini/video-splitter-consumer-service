# Hackaton - Video Splitter Consumer Service

## Arquitetura

![image](https://github.com/user-attachments/assets/c8996715-f174-4611-ab40-7c1d5ba35877)
Considerando o uso da clean archtecture foi pensada da seguinte maneira:
- As camadas presentation/infrasctructre equivalem a Framework & Drivers, sendo a presentation responsável por capturar a entrada do usuário e a infrastrucutre pela comunicação com camadas externas.
- A camada Application contempla as camadas Application Business Roles e Interface Adapters.
- A camda de Entities representa a camada Domain.

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

## Cobertura Sonar
<img width="2041" height="956" alt="image" src="https://github.com/user-attachments/assets/f5ed48f6-5332-411c-b7ce-416214742965" />

## Arquitetura Infraestrutura

### Diagrama de Fluxo
![Comunicao_Kubernetes](https://github.com/user-attachments/assets/ff35b655-1385-4738-b50d-7eb09826ff20)
- Dentro do Resource Group techchallenge-rg, há um IP Público que acessa o APIM (Azure API Management)
- Quando acessado e havendo configuração de suas políticas realiza a chamada para a function.
- O Ingress Controller então roteia as requisições para os diferentes serviços internos a depender da URI chamada, utilizando a comunicação via Cluster IP.
- As aplicações java se comunicam com seus respectivos databases utilizando a comunicação via Cluster IP.
  Obs: Para saber mais sobre o recurso Standard_B2S: https://learn.microsoft.com/pt-br/azure/virtual-machines/sizes/general-purpose/bv1-series?tabs=sizebasic

### Diagrama de Componente
![Arquitetura_Kubernetes](https://github.com/user-attachments/assets/8c5c551b-f5d1-4f37-833c-bb082a6d6594)
O cluster k8s-fiap é configurado com dois namespaces principais, cada um com funções específicas:
- default: Namespace onde as aplicações principais são implantadas e gerenciadas, contendo os PODs:
    - java-app-*: microsserviço presente no cluster.
        - Ingress: Configurado para gerenciar o tráfego de entrada direcionado à aplicação Java.
        - Cluster IP: Endereço IP interno para comunicação dentro do cluster.
        - Deployment: Gerencia a implantação e a escalabilidade da aplicação Java.
        - Secret: Armazena dados sensíveis, como chaves de API ou credenciais usadas pela aplicação.
        - Horizontal Pod Autoscaler (HPA): Configurado para escalar automaticamente o número de réplicas do pod com base na utilização de CPU.
        - Configuração do HPA:
            - Mínimo de 1 e máximo de 3 réplicas.
            - Escala a partir da métrica de uso de CPU atingir 70%.
        - Role HPA: Define as permissões necessárias para que o HPA acesse métricas do cluster (como CPU e memória) para tomar decisões de escalabilidade.
- ingress-basic: é responsável por gerenciar o tráfego externo e rotear as requisições para os serviços no namespace default.
    - ingress-nginx-controller: Executa o controlador NGINX Ingress, que atua como ponto de entrada para requisições externas e roteia o tráfego para os serviços apropriados no namespace default.
        - Ingress: Define as regras de roteamento para requisições externas (por exemplo, rotear requisições para o serviço do java-app).
        - Service: Expõe o controlador NGINX internamente no cluster.
        - Endpoint: Mapeia os endpoints para os serviços internos.
        - Deployment: Gerencia a implantação do controlador NGINX.
        - ConfigMap: Armazena configurações do NGINX, como limites de requisições, timeouts e outras opções de personalização.
        - Secret: Armazena informações sensíveis, como certificados TLS para habilitar HTTPS.    
          *Os arquivos de configuração do Kubernetes (em formato .yml) estão organizados no diretório kubernetes/, que contém os recursos descritos no diagrama.
