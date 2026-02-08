# Lab 03 – Replicar fluxo Navita Connect no Kind (GitHub + K8s local)

Este lab replica o **fluxo fim a fim** da aplicação **Navita Connect** (build → Config Server → deploy no Kubernetes) usando seu **GitHub pessoal** e um cluster **Kind** na sua máquina.

---

## 1. Visão do fluxo original (Navita)

O deploy do Navita Connect segue esta ordem:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│ 1. config-properties (Git)  →  connect.properties + connect-{perfil}.properties  │
│ 2. Config Server (Spring Cloud Config)  →  serve properties para a aplicação    │
│ 3. Build  →  Maven (navita-connect)  →  portal.jar                               │
│ 4. Imagem Docker  →  Dockerfile (portal.jar + jks + agent)  →  push OCIR         │
│ 5. Manifestos K8s (devops-config)  →  deployment, service, hpa, ingress           │
│ 6. Deploy no OKE  →  kubectl apply  →  pods com CLOUD_PROFILE  →  Config Server  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Runtime:** Cada pod inicia com `CLOUD_PROFILE` (ex.: `navita-hom`); a aplicação usa Spring Cloud Config Client para buscar `connect.properties` + `connect-{profile}.properties` no Config Server antes de subir.

Documentação completa: `nvt-repos/PROCEDIMENTO-TECNICO-DEPLOY-NAVITA-CONNECT.md`.

---

## 2. O que este lab replica (Kind + GitHub pessoal)

| Original (Navita)              | Replicado neste lab                          |
|-------------------------------|----------------------------------------------|
| navita-config-properties (Git)| `config-properties/` (connect + connect-kind)|
| Config Server (OKE)           | Config Server no Kind (perfil native + ConfigMap) |
| navita-connect build + OCIR    | Mock Connect (Spring Boot) ou sua build + `kind load` |
| devops-config (manifestos)    | `manifests/` (namespace, config-server, connect) |
| GitHub Actions → OKE          | GitHub Actions → Kind (self-hosted runner)   |

---

## 3. Estrutura do lab

```
03 - nvt-cnt/
├── README.md                    # Este arquivo
├── config-properties/           # Properties servidas pelo Config Server
│   ├── connect.properties
│   └── connect-kind.properties
├── manifests/                   # Kubernetes (Kind)
│   ├── namespace.yaml
│   ├── config-server/
│   │   ├── configmap.yaml       # Properties para perfil native
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── connect/
│       ├── deployment.yaml
│       └── service.yaml
├── mock-connect/                # App mínima (Config Client + /portal/ping)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/...
└── scripts/
    └── setup-kind.sh            # Cria cluster + namespace (se necessário)
```

O workflow `.github/workflows/pipeline-connect-kind.yml` fica na **raiz do repositório labs** e referencia esta pasta.

---

## 4. Ordem de execução (replicar na sua máquina)

### 4.1 Pré-requisitos

- **Docker** e **Kind** instalados
- **kubectl** configurado para o cluster Kind
- **GitHub pessoal** com o repositório `labs` (ou fork)
- (Opcional) **Self-hosted runner** na máquina onde o Kind está, para CI no GitHub

### 4.2 Passo a passo

1. **Criar cluster Kind e namespace**
   ```bash
   ./scripts/setup-kind.sh
   ```
   Ou manualmente: `kind create cluster`, `kubectl create namespace nvt-cnt`.

2. **Config Server primeiro**
   - O Config Server precisa estar no ar antes dos pods Connect.
   ```bash
   kubectl apply -f "03 - nvt-cnt/manifests/namespace.yaml"
   kubectl apply -f "03 - nvt-cnt/manifests/config-server/" -n nvt-cnt
   kubectl rollout status deployment/config-server -n nvt-cnt
   ```

3. **Build da aplicação (mock ou Connect real)**
   - **Mock (este lab):** o workflow ou você localmente:
     ```bash
     cd "03 - nvt-cnt/mock-connect"
     mvn -q package -DskipTests
     docker build -t connect:local .
     kind load docker-image connect:local
     ```
   - **Connect real:** no repositório `navita-connect`, build + imagem e depois:
     ```bash
     kind load docker-image <sua-imagem-connect>:<tag>
     ```

4. **Deploy da Connect**
   ```bash
   kubectl apply -f "03 - nvt-cnt/manifests/connect/" -n nvt-cnt
   kubectl rollout status deployment/connect -n nvt-cnt
   ```

5. **Verificação**
   ```bash
   kubectl get pods,svc -n nvt-cnt
   kubectl port-forward svc/connect 9090:9090 -n nvt-cnt
   # Acesse: http://localhost:9090/portal/ping
   ```

### 4.3 Via GitHub Actions (self-hosted runner)

- O workflow **pipeline-connect-kind** (em `.github/workflows/`) dispara em `workflow_dispatch` ou em push em `03 - nvt-cnt/**`.
- Ele faz: checkout → build mock → Docker build → `kind load docker-image` → `kubectl apply` dos manifestos.
- O runner deve estar na mesma máquina do Kind e com `kubectl` apontando para o cluster Kind.

---

## 5. Config Server (perfil native)

Neste lab o Config Server usa o perfil **native**: as properties vêm de um **ConfigMap** montado em `/config`, sem Git externo. Assim você não precisa de um repositório separado de config-properties no GitHub só para rodar.

- **Arquivos base:** `config-properties/connect.properties` e `connect-kind.properties`.
- Eles são embutidos no ConfigMap `manifests/config-server/configmap.yaml`.
- A aplicação Connect (ou mock) usa `CLOUD_PROFILE=kind` e recebe o merge `connect` + `connect-kind` do Config Server.

Para usar um **repositório Git** (como no fluxo Navita), troque o Config Server para imagem/config que use `spring.cloud.config.server.git.uri` e remova o uso do ConfigMap.

---

## 6. Uso com a aplicação Navita Connect real

Para subir a **Connect real** (código em `nvt-repos/navita-connect`):

1. **Config Server acessível** pela Connect (no Kind ou em outro host). Ajuste `bootstrap.properties` da Connect para a URL do Config Server (ex.: `http://config-server.nvt-cnt.svc.cluster.local:8888` se no mesmo cluster).
2. **Properties no Config Server:** use um repo Git com `connect.properties` e `connect-<perfil>.properties` (ex.: perfil `kind` ou `local-dev`), ou mantenha o ConfigMap com um perfil equivalente.
3. **Build da imagem** no repo navita-connect (Maven + Dockerfile do portal).
4. **Carregar no Kind:** `kind load docker-image <imagem-connect>:<tag>`.
5. **Manifests:** use os de `manifests/connect/` ajustando nome da imagem e `CLOUD_PROFILE` para o perfil que o Config Server serve.

---

## 7. Resumo

- **Fluxo replicado:** Properties → Config Server → Build (mock ou Connect) → Imagem → Kind → Deploy com CLOUD_PROFILE.
- **Diferenças:** Sem OCI/OCIR (imagem via `kind load`), sem Nexus, manifestos e config no próprio repo, Config Server em modo native com ConfigMap.
- Para **produção/homologação real**, use o procedimento completo em `PROCEDIMENTO-TECNICO-DEPLOY-NAVITA-CONNECT.md` no repositório `nvt-repos`.
