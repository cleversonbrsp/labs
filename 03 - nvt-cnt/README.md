# Lab 03 – Fluxo de deploy no Kind (GitHub + K8s local)

Este lab replica um **fluxo fim a fim** típico de aplicação enterprise: build → Config Server → deploy no Kubernetes. Usa aplicação de exemplo **portal-demo**, seu **GitHub** e um cluster **Kind** na sua máquina.

---

## Índice

1. [Visão do fluxo de referência](#1-visão-do-fluxo-de-referência)
2. [O que este lab replica](#2-o-que-este-lab-replica-kind--github-pessoal)
3. [Estrutura do lab](#3-estrutura-do-lab)
4. [Ordem de execução e como acessar](#4-ordem-de-execução-replicar-na-sua-máquina)
5. [Config Server (perfil native)](#5-config-server-perfil-native)
6. [Uso com outra aplicação](#6-uso-com-outra-aplicação-sua-build)
7. [Conceitos didáticos](#7-conceitos-didáticos)
8. [De onde veio este modelo](#8-de-onde-veio-este-modelo)
9. [Resumo](#9-resumo)
10. [Alterações recentes](#10-alterações-recentes)

---

## 1. Visão do fluxo de referência

Um fluxo típico de deploy com Config Server segue esta ordem:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│ 1. config-properties (Git)  →  app.properties + app-{perfil}.properties        │
│ 2. Config Server (Spring Cloud Config)  →  serve properties para a aplicação  │
│ 3. Build  →  Maven  →  portal.jar                                               │
│ 4. Imagem Docker  →  Dockerfile (portal.jar + jks + agent)  →  push registry     │
│ 5. Manifestos K8s  →  deployment, service, hpa, ingress                           │
│ 6. Deploy no cluster  →  kubectl apply  →  pods com CLOUD_PROFILE  →  Config Server │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Runtime:** Cada pod inicia com `CLOUD_PROFILE`; a aplicação usa Spring Cloud Config Client para buscar as properties no Config Server antes de subir.

---

## 2. O que este lab replica (Kind + GitHub pessoal)

| Fluxo de referência            | Replicado neste lab                                  |
|-------------------------------|------------------------------------------------------|
| config-properties (Git)      | `config-properties/` (portal-demo + portal-demo-kind)|
| Config Server (cluster)      | Config Server no Kind (perfil native + ConfigMap)    |
| Build app + registry          | portal-demo (Spring Boot) + `kind load`              |
| Manifestos K8s (repo separado)| `manifests/` (namespace lab-portal, config-server, portal-demo) |
| Pipeline → cluster            | GitHub Actions → Kind (self-hosted runner)           |

---

## 3. Estrutura do lab

```
03 - nvt-cnt/
├── README.md                    # Este arquivo (documentação centralizada)
├── config-properties/           # Properties servidas pelo Config Server
│   ├── portal-demo.properties
│   └── portal-demo-kind.properties
├── manifests/                   # Kubernetes (Kind)
│   ├── namespace.yaml           # lab-portal
│   ├── config-server/
│   │   ├── configmap.yaml       # Properties para perfil native
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── connect/                 # deployment + service da portal-demo
│       ├── deployment.yaml
│       └── service.yaml
├── mock-connect/                # App portal-demo (Config Client + /portal/ping)
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
   Ou manualmente: `kind create cluster`, `kubectl create namespace lab-portal`.

2. **Config Server primeiro**
   - O Config Server precisa estar no ar antes dos pods da aplicação.
   ```bash
   kubectl apply -f "03 - nvt-cnt/manifests/namespace.yaml"
   kubectl apply -f "03 - nvt-cnt/manifests/config-server/" -n lab-portal
   kubectl rollout status deployment/config-server -n lab-portal
   ```

3. **Build da aplicação (portal-demo)**
   - O workflow ou você localmente:
     ```bash
     cd "03 - nvt-cnt/mock-connect"
     mvn -q package -DskipTests
     docker build -t portal-demo:local .
     kind load docker-image portal-demo:local
     ```

4. **Deploy da aplicação**
   ```bash
   kubectl apply -f "03 - nvt-cnt/manifests/connect/" -n lab-portal
   kubectl rollout status deployment/portal-demo -n lab-portal
   ```

5. **Verificação**
   ```bash
   kubectl get pods,svc -n lab-portal
   ```

### 4.3 Como acessar a aplicação

O Service da aplicação é **ClusterIP** (só acessível dentro do cluster). Para acessar do seu navegador:

1. Em um terminal, deixe o port-forward ativo:
   ```bash
   kubectl port-forward svc/portal-demo 9090:9090 -n lab-portal
   ```
2. Abra no navegador:
   - **Status (ping):** http://localhost:9090/portal/ping  
   - **Base da aplicação:** http://localhost:9090/portal  

A resposta de `/portal/ping` deve incluir `lab.profile: kind` e `lab.env: kind-cluster` (config vinda do Config Server). Para encerrar o acesso, use `Ctrl+C` no terminal do port-forward.

### 4.4 Via GitHub Actions (self-hosted runner)

- O workflow **pipeline-connect-kind** (em `.github/workflows/`) dispara em `workflow_dispatch` ou em push em `03 - nvt-cnt/**`.
- Ele faz: checkout → build mock → Docker build → `kind load docker-image` → `kubectl apply` → **`kubectl rollout restart deployment/portal-demo`** → rollout status.
- O runner deve estar na mesma máquina do Kind e com `kubectl` apontando para o cluster Kind.

**Por que `rollout restart`?** A imagem usada é sempre `portal-demo:local`. O Kubernetes só recria os pods quando o **template do Deployment** muda (ex.: nome da imagem). Como o nome não muda, os pods antigos continuariam rodando com o container antigo mesmo após um novo `kind load`. O `rollout restart` força a criação de novos pods, que passam a usar a imagem recém-carregada.

**Sem queda (zero downtime):** o rollout usa a estratégia padrão **RollingUpdate**. O Kubernetes sobe o pod novo, espera ele ficar Ready (probes) e só então encerra o antigo. Enquanto o novo sobe, o antigo segue atendendo; quando o antigo sai, o novo já está pronto.

---

## 5. Config Server (perfil native)

### O que é e para que serve?

O **Config Server** (Spring Cloud Config Server) é um **serviço separado** que centraliza a **configuração** (properties) das suas aplicações. Em vez de cada app carregar tudo de um arquivo ou variáveis de ambiente fixas no deploy, a app pergunta ao Config Server: “qual é a minha config?”. O Config Server responde com o arquivo de properties correspondente (por nome da aplicação e perfil, ex.: `portal-demo` + perfil `kind`).

**Para que serve aqui no lab:**

1. **Separar config do código:** as properties (`server.port`, `context-path`, etc.) ficam em `config-properties/` e no ConfigMap; a aplicação **portal-demo** não tem esses valores “hardcoded” no JAR.
2. **Um lugar para todos os ambientes:** o Config Server serve `portal-demo.properties` (base) e `portal-demo-kind.properties` (perfil kind). Em produção você teria `portal-demo-prod.properties` no mesmo esquema.
3. **Trocar config sem rebuild:** em um fluxo com Git como fonte, você altera as properties no repositório de config; na próxima subida (ou refresh), a app busca a config nova no Config Server — sem precisar rebuildar a imagem da aplicação.

**Resumo:** Config Server = “servidor de configuração”. A aplicação, ao subir, chama o Config Server (URL no `bootstrap.properties`), informa o nome (`portal-demo`) e o perfil (ex.: `kind` via `CLOUD_PROFILE`). O Config Server devolve as properties; a app usa essas properties para configurar porta, context-path, etc.

---

Neste lab o Config Server usa o perfil **native**: as properties vêm de um **ConfigMap** montado em `/config`, sem Git externo. Assim você não precisa de um repositório separado de config-properties no GitHub só para rodar.

- **Arquivos base:** `config-properties/portal-demo.properties` e `portal-demo-kind.properties`.
- Eles são embutidos no ConfigMap `manifests/config-server/configmap.yaml`.
- A aplicação portal-demo usa `CLOUD_PROFILE=kind` e recebe o merge das duas properties do Config Server.

Para usar um **repositório Git** em vez do ConfigMap, configure o Config Server com `spring.cloud.config.server.git.uri`.

---

## 6. Uso com outra aplicação (sua build)

Para subir **sua própria aplicação** no mesmo fluxo:

1. **Config Server acessível** (no Kind ou em outro host). Ajuste `bootstrap.properties` da sua app para a URL do Config Server (ex.: `http://config-server.lab-portal.svc.cluster.local:8888` se no mesmo cluster).
2. **Properties no Config Server:** repo Git com `{application}.properties` e `{application}-<perfil>.properties`, ou ConfigMap com perfil equivalente.
3. **Build da imagem** (Maven + Dockerfile).
4. **Carregar no Kind:** `kind load docker-image <sua-imagem>:<tag>`.
5. **Manifests:** use os de `manifests/connect/` ajustando nome da imagem e `CLOUD_PROFILE`.

---

## 7. Conceitos didáticos

### 7.1 De onde vem o `springcloud/configserver` e por quê?

- **Origem:** imagem do **Docker Hub** ([hub.docker.com/r/springcloud/configserver](https://hub.docker.com/r/springcloud/configserver)), publicada pela organização springcloud.
- **Conteúdo:** aplicação Java pré-buildada que é um **Spring Cloud Config Server** — entrega configuração (`.properties` ou `.yml`) para outras aplicações via HTTP.
- **Por que usamos:** em um fluxo enterprise o Config Server pode ser uma app própria. Aqui usamos uma **imagem pronta** para não buildar e manter um segundo app Java. Ela serve config por **application name** e **profile** e suporta o perfil **native** (lê arquivos do sistema de arquivos, no nosso caso o ConfigMap em `/config`).

### 7.2 O que é `tcpSocket` e por que usamos aqui?

- **O que é:** **tcpSocket** é um tipo de **probe** do Kubernetes. Probes verificam: startup (o container já subiu?), readiness (está pronto para tráfego?), liveness (ainda está vivo?). Em uma probe **tcpSocket** você informa só a **porta**. O Kubernetes **não envia HTTP**: só tenta **abrir uma conexão TCP** naquela porta. Se a conexão for aceita = sucesso; se falhar = falha. Resumindo: **tcpSocket = “a porta X está aberta?”**.

- **Por que usamos:** o Config Server é um processo Java que demora para subir (JVM, carregar config, abrir porta 8888). Usamos **startupProbe** com tcpSocket na porta 8888 para o Kubernetes esperar a porta abrir antes de considerar o container “iniciado”. Usamos **readinessProbe** com tcpSocket na 8888 para marcar o pod como Ready quando a porta estiver ouvindo, sem depender de path HTTP.

### 7.3 Por que usar tcpSocket na readiness e não depender do path HTTP?

Se usássemos **httpGet** em um path (ex.: `/` ou `/actuator/health`): o Kubernetes só marcaria o pod como Ready com resposta 2xx. Problemas: (1) o path exato varia por versão/imagem do Config Server; (2) a imagem que usamos é antiga e não sabemos quais paths expõe; (3) para o Service encaminhar tráfego basta a porta estar aberta — o cliente (portal-demo) é que fará as chamadas HTTP corretas. Com **tcpSocket** na porta 8888 não dependemos de path; o pod fica Ready assim que a porta estiver ouvindo.

**Resumo conceitos:**

| Tópico | Resposta curta |
|--------|----------------|
| **De onde vem springcloud/configserver?** | Docker Hub; imagem pronta do Spring Cloud Config Server. Usamos para não buildar o Config Server no lab. |
| **O que é tcpSocket?** | Probe do Kubernetes que só testa se uma **porta TCP** está aberta, sem HTTP. |
| **Por que tcpSocket aqui?** | Dar tempo ao Java subir (startupProbe) e marcar Ready quando a porta 8888 estiver ouvindo (readinessProbe), sem depender de path HTTP. |
| **Por que não path HTTP na readiness?** | Paths variam por versão/imagem; para o Service encaminhar tráfego basta a porta estar aberta. |

---

## 8. De onde veio este modelo

### 8.1 Fluxo de referência (típico em ambiente enterprise)

Um deploy com Config Server costuma envolver **vários repositórios** e **infraestrutura em nuvem**:

| Camada | No fluxo de referência | Papel |
|--------|------------------------|-------|
| **Config (fonte)** | Repo Git de properties | Arquivos `{app}.properties` e `{app}-{perfil}.properties`. |
| **Config (servidor)** | App Config Server (deploy no cluster) | Spring Cloud Config Server; lê o repo via Git. |
| **Código da aplicação** | Repo da aplicação (Maven) | Build → JAR; Dockerfile (JAR + certs, agent, etc.). |
| **Manifestos K8s** | Repo separado | deployment, service, hpa, ingress por ambiente. |
| **Artefatos de build** | Repo ou storage | Certs, agent; pipeline baixa e coloca no contexto do Docker build. |
| **Registry** | Registry na nuvem (OCIR, ECR, GCR) | Pipeline faz push; cluster faz pull com imagePullSecrets. |
| **Cluster** | Cluster gerenciado (OKE, EKS, GKE) | Runners com acesso; pipeline usa CLI do provedor. |
| **Pipeline** | Workflows no repo da aplicação | Checkout → build → Docker → push registry → download manifests → kubectl apply + rollout. |

Ordem típica: 1º properties → 2º Config Server → 3º cluster + registry → 4º manifestos → 5º artefatos → 6º credenciais → 7º cluster pronto → 8º código e pipeline.

### 8.2 Mapeamento: fluxo de referência → lab

| No fluxo de referência | No lab (03 - nvt-cnt) | Motivo |
|------------------------|------------------------|--------|
| Repo Git de properties | `config-properties/` + **ConfigMap** | Não exigir repo Git separado. |
| App Config Server própria | Imagem **springcloud/configserver** (Docker Hub), modo **native** | Evitar build de um segundo app Java. |
| App completa (muitos módulos) | **mock-connect** (portal-demo: Config Client + /portal/ping) | Replicar só o comportamento relevante. |
| Manifestos em repo separado | **manifests/** dentro do lab | Tudo em um repo. |
| Certs / agent no build | **Não usado** no mock | Mock só JAR. |
| Registry na nuvem | **kind load docker-image** | Ver 8.3 abaixo. |
| Cluster gerenciado | **Kind** (cluster local) | Cluster na sua máquina; pipeline self-hosted. |
| Runners com acesso ao cluster | **Self-hosted runner** | Job roda onde estão `kubectl` e `kind`. |
| Manifests baixados via API | **kubectl apply -f** nos arquivos do repo | YAML versionados no repo. |
| imagePullSecrets | **Nenhum**; `imagePullPolicy: IfNotPresent` | Imagem carregada com kind load. |
| Múltiplos ambientes | **Um ambiente**: namespace `lab-portal`, perfil `kind` | Foco em um fluxo fim a fim. |
| Vários deployments, HPA, Ingress | **Só portal-demo** (1 réplica), **ClusterIP**, **port-forward** | Reduzir complexidade. |
| Rollout com nova tag | **kubectl rollout restart** após apply | Tag fixa `portal-demo:local`; sem restart os pods antigos continuariam (RollingUpdate, zero downtime). |

### 8.3 Kind load (sem registry na nuvem)

**No fluxo de referência (com registry na nuvem):** o pipeline builda a imagem, faz login no registry (ex.: OCIR, ECR), dá push da imagem. O cluster faz pull do registry (usando imagePullSecrets). A imagem vai: máquina do pipeline → registry → nodes do cluster.

**No lab (sem registry na nuvem):** não usamos registry; o cluster é o **Kind**, na sua máquina. Depois do `docker build`, a imagem fica no **daemon Docker local**. O comando **`kind load docker-image <nome>:<tag>`** carrega essa imagem **direto para dentro dos nodes do Kind**. O cluster passa a “enxergar” a imagem e sobe os pods com ela. **Resumo:** a imagem vai **da sua máquina para o Kind** via `kind load`, sem passar por registry na nuvem.

### 8.4 Decisões de desenho (resumo)

- **Manter igual ao fluxo de referência:** ordem config → Config Server → build → imagem → deploy com CLOUD_PROFILE; Spring Cloud Config Client; estrutura deployment/service; “properties base + perfil”.
- **Simplificar para o lab:** um repo em vez de vários; imagem pública para o Config Server; mock em vez de app completa; Kind + kind load em vez de registry/cluster na nuvem; manifestos locais; rollout restart explícito para mesma tag.
- **Omitir no lab:** Nexus, certificados, OpenTelemetry, múltiplos deployments (async), HPA, Ingress (opcional depois), múltiplos ambientes.

### 8.5 Referência rápida de arquivos

| No lab | Onde |
|--------|------|
| Properties da aplicação | `config-properties/portal-demo*.properties` e ConfigMap em `manifests/config-server/configmap.yaml` |
| Config Server | Imagem springcloud/configserver + ConfigMap em `manifests/config-server/` |
| Pipeline | `labs/.github/workflows/pipeline-connect-kind.yml` |
| Deployment/Service portal-demo | `manifests/connect/` |
| Bootstrap da aplicação | `mock-connect/src/main/resources/bootstrap.properties` |

---

## 9. Resumo

- **Fluxo replicado:** Properties → Config Server → Build (portal-demo) → Imagem → Kind → Deploy com CLOUD_PROFILE.
- **Diferenças:** Sem registry na nuvem (imagem via `kind load`), manifestos e config no próprio repo, Config Server em modo native com ConfigMap.

---

## 10. Alterações recentes

| Data       | Alteração |
|-----------|-----------|
| 2026-02-08 | **Pipeline:** após `kubectl apply`, passou a rodar `kubectl rollout restart deployment/portal-demo` para que novos builds (mesma tag `portal-demo:local`) gerem novos pods. Deploy sem queda (RollingUpdate). |
