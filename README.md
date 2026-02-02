# Labs – GitHub Actions + Kind (cluster já pronto)

Repositório de labs para praticar **GitHub Actions** integrado com seu **Kind + MetalLB** que já está rodando localmente.

## Como funciona

Os workflows rodam num **self-hosted runner** na sua máquina (onde está o Kind). Assim o pipeline usa `kubectl` contra o cluster que você já tem — sem criar Kind dentro do job.

| Pré-requisito | Descrição |
|---------------|-----------|
| Kind + MetalLB | Já rodando localmente (configurado por você). |
| Self-hosted runner | Instalado na mesma máquina do Kind; ver **Lab 1** (setup). |

---

## Estrutura dos labs

```
labs/
├── 01-self-hosted-runner/     # Configurar runner + primeiro workflow (validar cluster)
├── 02-deploy-app-kind/        # Pipeline de deploy no seu Kind local
└── README.md
```

### Lab 1 – Self-hosted runner + validar cluster
- Configurar o **self-hosted runner** do GitHub na máquina do Kind.
- Primeiro workflow: roda no runner local e valida o cluster (`kubectl get nodes`, etc.).

### Lab 2 – Deploy via pipeline
- Pipeline que faz deploy de aplicação no seu Kind (manifestos, Helm ou Kustomize).
- Build de imagem (opcional) + `kind load docker-image` ou registry local.

---

## Pré-requisitos

- Kind + MetalLB já rodando.
- Conta no GitHub e este repositório.
- Máquina com o Kind acessível para instalar o runner (normalmente a mesma onde o Kind roda).

Comece pelo **Lab 1** (configurar o runner e rodar o primeiro workflow).
