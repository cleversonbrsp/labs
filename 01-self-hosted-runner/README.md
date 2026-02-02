# Lab 1 – Self-hosted runner + validar cluster

Objetivo: configurar o **self-hosted runner** na máquina do Kind e rodar um workflow que só valida o cluster (sem criar Kind).

## Pré-requisito

Kind (e MetalLB, se usar) já rodando localmente.

## 1. Configurar o self-hosted runner

1. No GitHub: **Settings** do repositório → **Actions** → **Runners** → **New self-hosted runner**.
2. Escolha **Linux** e siga os comandos (baixar, configurar, rodar como serviço).
3. Execute tudo na **mesma máquina** onde está o Kind; o runner usará o `kubeconfig` local (`~/.kube/config`).

## 2. O que o workflow faz

O workflow em `.github/workflows/kind-ci.yml`:

- Roda no runner local (`runs-on: self-hosted`).
- Faz checkout do repo.
- Usa o `kubectl` já disponível no runner e valida o cluster: `kubectl get nodes`, `kubectl get ns`.

Não instala Kind nem sobe cluster — só conversa com o que já está aí.

## 3. Como usar

1. Com o runner instalado e “Idle” no GitHub.
2. **Actions** → **Kind CI** → **Run workflow** (ou faça push em `main`).
3. O job será enviado ao seu runner e executará na sua máquina contra o Kind local.

## Próximo passo

No **Lab 2** você monta um pipeline de deploy de aplicação no seu Kind.
