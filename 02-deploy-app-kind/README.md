# Lab 2 – Deploy via pipeline no Kind local

Objetivo: pipeline que faz **deploy de aplicação** no seu Kind (já pronto), rodando no self-hosted runner.

## O que este lab faz

1. Roda no runner local (mesmo do Lab 1).
2. Aplica manifestos ou faz deploy (ex.: nginx de exemplo) no seu Kind.
3. Valida rollout e lista recursos.

Não sobe Kind — usa o cluster que já está rodando.

## Como usar

O workflow está em `.github/workflows/kind-deploy.yml`. Ele:

- Usa `runs-on: self-hosted`.
- Faz deploy (ex.: Deployment + Service nginx).
- Roda `kubectl rollout status` e `kubectl get pods/svc`.

## Próximas variações (para praticar)

- Trocar ou estender os manifestos em `02-deploy-app-kind/manifests/`.
- Usar **Helm** ou **Kustomize** no workflow.
- Build de imagem local + `kind load docker-image` (no runner que tem acesso ao Kind).
- Testes após deploy (curl no Service ou LoadBalancer com MetalLB).
