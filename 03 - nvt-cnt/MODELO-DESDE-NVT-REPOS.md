# Como chegamos a este modelo (a partir de nvt-repos)

Este documento explica **de onde veio** o desenho do lab 03 e **por que** cada escolha foi feita em relação ao fluxo real em `nvt-repos` (Navita Connect no OCI/OKE).

---

## 1. O que existe em nvt-repos (fluxo real)

O deploy da Navita Connect no ambiente Navita envolve **vários repositórios** e **infraestrutura OCI**:

| Camada | No nvt-repos | Papel |
|--------|----------------|-------|
| **Config (fonte)** | `navita-config-properties` (repo Git) | Arquivos `connect.properties` e `connect-{perfil}.properties`; fonte única de config por ambiente. |
| **Config (servidor)** | `navita-config-server` (app Java, deploy OKE) | Spring Cloud Config Server; lê o repo acima via Git e serve properties por aplicação/perfil. |
| **Código da aplicação** | `navita-connect` (portal + connect-client + mdmbus) | Build Maven → `portal.jar`; Dockerfile usa esse JAR + certificados `.jks` + opentelemetry. |
| **Manifestos K8s** | `devops-config` (repo separado) | `k8s/{prod,hom,vivo,...}/app-manifest/connect/`: deployment, service, hpa; cluster-manifest: ingress, setup OCI. |
| **Artefatos de build** | `infra-devops` (repo separado) | Certificados `.jks` e `opentelemetry-javaagent.jar`; o pipeline baixa e coloca no contexto do Docker build. |
| **Registry** | OCIR (Oracle Cloud) | Imagens `connect-prod`, `connect-hom`, etc.; pipeline faz push; cluster usa imagePullSecrets. |
| **Cluster** | OKE (Oracle Kubernetes Engine) | Runners GitHub (ex.: `k8s_hom`) com acesso à rede; pipeline usa OCI CLI para kubeconfig e apply. |
| **Pipeline** | `.github/workflows/` em `navita-connect` | Checkout → setup OCI (setup-config, private.key) → download certs de infra-devops → Maven (release ou deploy) → Docker build → push OCIR → download manifests de devops-config (API GitHub) → kubectl apply + set image + rollout. |

Ordem de configuração no procedimento: 1º properties → 2º Config Server → 3º OCI (OKE, OCIR) → 4º manifestos → 5º artefatos → 6º credenciais → 7º cluster pronto → 8º código e pipeline.

---

## 2. Mapeamento: nvt-repos → lab (03 - nvt-cnt)

Cada “peça” do fluxo real foi mapeada para algo que funcione **no seu GitHub + Kind**, sem OCI, sem repositórios internos Navita e sem secrets corporativos.

| No nvt-repos | No lab (03 - nvt-cnt) | Motivo da escolha |
|--------------|------------------------|-------------------|
| **navita-config-properties** (Git) | `config-properties/` (arquivos) + **ConfigMap** no K8s | Não exigir um repo Git separado só para rodar o lab. As properties são poucas e estáticas; o ConfigMap montado em `/config` no Config Server substitui o clone do Git. |
| **navita-config-server** (app Java própria) | Imagem **springcloud/configserver** (Docker Hub) em modo **native** | Evitar build e manutenção de um segundo app Java. A imagem pronta faz o mesmo papel: servir config por application/profile a partir de arquivos (no nosso caso, do ConfigMap). |
| **navita-connect** (código completo) | **mock-connect** (app mínima: Config Client + /portal/ping) | O Connect real depende de Nexus, muitos módulos e configs (DB, Keycloak, RabbitMQ, etc.). O mock replica só o **comportamento** relevante para o lab: bootstrap no Config Server + endpoint usado pelas probes; o fluxo “config → deploy → runtime” fica igual. |
| **devops-config** (manifestos em repo separado) | **manifests/** dentro do próprio lab | Manter tudo em um único repositório (labs). O pipeline não precisa de token para baixar YAML de outro repo; os manifestos são versionados junto com o código do mock. |
| **infra-devops** (certs, agent) | **Não usado** no mock | O mock não usa TLS nem OpenTelemetry. O Dockerfile do lab só copia o JAR; se no futuro você usar a Connect real, aí sim precisará de certs/agent (ou variantes sem TLS para dev). |
| **OCIR** (registry na OCI) | **kind load docker-image** (imagem local no cluster) | Ver seção “Kind load (sem OCI)” abaixo. |
| **OKE** (cluster na OCI) | **Kind** (cluster local) | Você já tem Kind; não é necessário criar cluster na nuvem. O pipeline roda em self-hosted na mesma máquina do Kind. |
| **Runners k8s_hom / k8s_prod** (acesso OKE) | **Self-hosted runner** no seu GitHub | O job precisa de `kubectl` e `kind` na máquina onde o cluster está; por isso o workflow usa `runs-on: self-hosted`. |
| **Manifests baixados via API GitHub** | **kubectl apply -f** nos arquivos do repo | Os YAML estão no próprio repositório; não há passo de “download de outro repo”. |
| **imagePullSecrets (ocirsecret)** | **Nenhum**; `imagePullPolicy: IfNotPresent` | A imagem vem do Kind (já carregada); o cluster não puxa de registry externo. |
| **Múltiplos ambientes (prod, hom, vivo…)** | **Um ambiente**: namespace `nvt-cnt`, perfil `kind` | O lab foca em “um fluxo fim a fim”; múltiplos ambientes podem ser replicados depois copiando a estrutura. |
| **connect-async, HPA, Ingress** | **Só Connect** (1 réplica), **Service ClusterIP**, **port-forward** para acesso | Reduzir complexidade. Ingress pode ser adicionado depois (você já tem ingress-nginx no Kind); HPA e async não são necessários para entender o fluxo. |
| **Rollout “implícito” (nova tag de imagem)** | **kubectl rollout restart** após apply | No real, cada deploy usa uma nova tag (ex.: versão); o K8s vê template diferente e faz rolling update. No lab a tag é sempre `connect:local`; sem restart os pods antigos continuariam rodando; o restart força novos pods a usarem a imagem recém-carregada (sem queda, RollingUpdate). |

---

## 2.1 Kind load (sem OCI) – o que é e por que “sem OCI”

**No fluxo real (nvt-repos):**

1. O pipeline **builda** a imagem Docker (ex.: `connect-hom:1.0.2077`).
2. Faz **login** no **OCIR** (Oracle Cloud Infrastructure Registry) – um registry de imagens na nuvem Oracle.
3. Dá **push** da imagem para o OCIR: `docker push us-ashburn-1.ocir.io/.../connect-hom:1.0.2077`.
4. O **cluster OKE** (também na OCI) precisa rodar essa imagem. Quando o Deployment pede `image: .../connect-hom:1.0.2077`, o kubelet em cada **node** faz **pull** do OCIR (usando imagePullSecrets). Ou seja: a imagem vai da máquina do pipeline → registry (OCIR) → nodes do cluster.

**No lab (sem OCI):**

- Não temos **OCIR** (nem conta Oracle). Não temos **OKE** (o cluster é o **Kind**, na sua máquina).
- O Kind não “fala” com nenhum registry na nuvem; os nodes do Kind são containers Docker na sua máquina.
- Depois do `docker build`, a imagem fica só no **daemon Docker local**. O Kind, por dentro, usa esse mesmo daemon para criar os nodes. Então, em vez de “push para registry → pull no cluster”, usamos o comando **`kind load docker-image <nome>:<tag>`**.
- O que ele faz: **carrega** a imagem do Docker da sua máquina **direto para “dentro” dos nodes do Kind**, como se tivesse sido puxada de um registry. Assim o cluster passa a “enxergar” essa imagem e consegue subir os pods com ela.

**Resumo:** “**kind load (sem OCI)**” = não usamos registry na nuvem (OCIR); a imagem vai **da sua máquina para o Kind** via `kind load`, sem passar por OCI.

---

## 3. Decisões de desenho (resumo)

- **Manter igual ao real:** ordem “config → Config Server → build → imagem → deploy com CLOUD_PROFILE”; uso de Spring Cloud Config Client no app; estrutura de deployment/service; ideia de “properties base + perfil”.
- **Simplificar para o lab:** um repo (labs) em vez de vários; uma imagem pública para o Config Server em vez de app própria; mock em vez da Connect completa; Kind + kind load em vez de OCI/OCIR; manifestos locais em vez de download de devops-config; rollout restart explícito para mesma tag.
- **Omitir no lab (por não ser essencial ao fluxo):** Nexus, certificados, OpenTelemetry, connect-async, HPA, Ingress (opcional depois), múltiplos ambientes, secrets OCI.

Com isso, o lab replica o **fluxo fim a fim** (properties → Config Server → build → deploy → pods que buscam config no startup) sem depender de infraestrutura ou repositórios Navita, e você consegue rodar tudo no seu GitHub e no seu Kind.

---

## 4. Referência rápida de arquivos

| Você quer ver no nvt-repos… | Para entender o equivalente no lab… |
|-----------------------------|--------------------------------------|
| Ordem e fluxo geral | `nvt-repos/PROCEDIMENTO-TECNICO-DEPLOY-NAVITA-CONNECT.md` |
| Properties da Connect | `nvt-repos/navita-config-properties/connect*.properties` → `03 - nvt-cnt/config-properties/` e ConfigMap |
| Config Server (Git, native) | `nvt-repos/navita-config-server` → imagem springcloud/configserver + ConfigMap no lab |
| Pipeline hom (build + deploy) | `nvt-repos/navita-connect/.github/workflows/pipeline-connect-hom-oke.yml` → `labs/.github/workflows/pipeline-connect-kind.yml` |
| Deployment/Service Connect | `nvt-repos/devops-config/k8s/hom/app-manifest/connect/` → `03 - nvt-cnt/manifests/connect/` |
| Bootstrap da aplicação | `nvt-repos/navita-connect/portal/src/main/resources/bootstrap.properties` → `mock-connect/src/main/resources/bootstrap.properties` |

Este modelo foi definido a partir da leitura do PROCEDIMENTO e dos repositórios listados em nvt-repos, priorizando um fluxo completo e didático sem dependências corporativas.
