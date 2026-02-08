# Conceitos do Lab 03 – Navita Connect no Kind

Este documento explica, de forma didática, três pontos usados no lab: a imagem do Config Server, o `tcpSocket` nas probes e a escolha de não depender de path HTTP na readiness.

---

## 1. De onde vem o `springcloud/configserver` e por quê?

### De onde vem

A imagem **`springcloud/configserver`** vem do **Docker Hub** (registry público de imagens Docker):

- **Repositório:** [hub.docker.com/r/springcloud/configserver](https://hub.docker.com/r/springcloud/configserver)
- **Publicada por:** organização **springcloud** (ecossistema Spring).
- **Conteúdo:** uma aplicação Java pré-buildada que é um **Spring Cloud Config Server** — o servidor que entrega configuração (arquivos `.properties` ou `.yml`) para outras aplicações via HTTP.

Quando o Kubernetes faz `image: springcloud/configserver`, o kubelet (no nó) faz o equivalente a um `docker pull springcloud/configserver`: baixa a imagem do Docker Hub se ainda não estiver no node.

### Por que usamos essa imagem

No fluxo real da Navita, o **Config Server** é buildado a partir do repositório `navita-config-server` (código próprio) e deployado no OKE. Aqui, para replicar o fluxo sem manter um código Java próprio do Config Server, usamos uma **imagem pronta** que já implementa o mesmo papel:

- Servir configuração por **application name** e **profile** (ex.: `connect` + `kind`).
- Suportar o perfil **native**, que lê arquivos do sistema de arquivos (nosso ConfigMap montado em `/config`) em vez de um repositório Git.

Assim, o lab foca em “config server + connect” sem precisar buildar e manter o código do Config Server.

---

## 2. O que é `tcpSocket` e por que usamos aqui?

### O que é

**`tcpSocket`** é um tipo de **probe** (sonda) do Kubernetes. Probes são verificações que o Kubernetes faz no container para decidir:

- **startupProbe:** o container já “subiu” o suficiente para começar a ser verificado pelas outras?
- **readinessProbe:** o container está pronto para receber tráfego (incluído no Service)?
- **livenessProbe:** o processo ainda está vivo ou devemos reiniciar o container?

Em uma probe do tipo **tcpSocket** você informa apenas **porta** (e opcionalmente o host). O Kubernetes **não envia HTTP**: ele só tenta **abrir uma conexão TCP** naquela porta. Se a conexão for aceita, a probe é considerada **sucesso**; se falhar (porta fechada ou recusada), é **falha**.

Resumindo: **tcpSocket = “a porta X está aberta e aceitando conexões?”**.

### Por que usamos aqui

O Config Server é um processo Java. Ele demora vários segundos para:

1. Inicializar a JVM.
2. Carregar as configurações (env vars, arquivos em `/config`).
3. Subir o servidor HTTP na porta 8888.

Enquanto isso, a porta 8888 ainda não está aberta. Usamos:

- **startupProbe com tcpSocket na porta 8888:** o Kubernetes espera até a porta abrir antes de considerar que o container “iniciou”. Assim evitamos que o kubelet mate o container por “não ter respondido” durante a subida lenta.
- **readinessProbe com tcpSocket na porta 8888:** consideramos o pod “Ready” quando a porta já está aceitando conexões. Não precisamos saber se uma URL HTTP específica retorna 200.

Ou seja: usamos **tcpSocket** para dizer ao Kubernetes “está pronto quando a porta 8888 estiver ouvindo”, o que é simples e adequado para um serviço que sobe devagar e cujo contrato é “responder na porta 8888”.

---

## 3. Por que usar tcpSocket na readiness e não depender do path HTTP?

### O que aconteceria com httpGet em um path

Se usássemos **readinessProbe** com **httpGet** em um path (ex.: `path: /` ou `path: /connect/kind`):

- O Kubernetes faria uma requisição **HTTP GET** nesse path.
- Só marcaria o pod como Ready se recebesse uma resposta com **código de sucesso** (ex.: 2xx).

Problemas com isso aqui:

1. **Path exato varia:** diferentes versões ou imagens do Config Server podem expor `/`, `/actuator/health`, `/application/default`, etc. Se o path não existir (404), o pod **nunca** fica Ready, mesmo com o serviço funcionando.
2. **Imagem antiga:** a imagem `springcloud/configserver` é antiga; não sabemos exatamente quais paths ela expõe. Apostar em um path específico aumenta o risco de falha sem necessidade.
3. **Objetivo da readiness:** para o **Service** encaminhar tráfego, basta saber que **algo** está ouvindo na porta 8888. O cliente (mock-connect) que depois fará as chamadas HTTP corretas (`/connect/kind`). A readiness não precisa validar o conteúdo da resposta.

### Por que tcpSocket resolve

Com **readinessProbe** em **tcpSocket** na porta 8888:

- Não dependemos de **nenhum path HTTP**.
- Qualquer imagem de Config Server que suba e abra a porta 8888 passará na probe.
- O pod fica Ready assim que o processo estiver escutando na porta, e o Service pode começar a enviar tráfego.

Ou seja: usamos tcpSocket na readiness para **não depender do path HTTP**, manter o lab estável com imagens/versões diferentes e ainda assim garantir que o pod só receba tráfego quando a porta estiver realmente aberta.

---

## Resumo

| Tópico | Resposta curta |
|--------|----------------|
| **De onde vem springcloud/configserver?** | Do Docker Hub; é uma imagem pronta do Spring Cloud Config Server. Usamos para não precisar buildar o Config Server no lab. |
| **O que é tcpSocket?** | Tipo de probe do Kubernetes que só testa se uma **porta TCP** está aberta, sem fazer HTTP. |
| **Por que tcpSocket aqui?** | Para dar tempo ao Java subir (startupProbe) e marcar o pod como Ready quando a porta 8888 estiver ouvindo (readinessProbe), sem depender de path HTTP. |
| **Por que não depender de path HTTP na readiness?** | Paths variam por versão/imagem; a imagem que usamos é antiga; para o Service encaminhar tráfego basta a porta estar aberta. |
