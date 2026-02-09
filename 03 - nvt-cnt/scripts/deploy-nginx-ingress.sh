#!/bin/bash
set -e
echo "[INFO] Adicionando repo Helm ingress-nginx (se necessário)..."
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx 2>/dev/null || true
helm repo update

echo "[INFO] Instalando ingress-nginx no Kind (admission webhooks desabilitados)..."
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.admissionWebhooks.enabled=false \
  --set controller.ingressClassResource.default=true \
  --set controller.ingressClassResource.name=nginx \
  --set controller.service.type=LoadBalancer

for node in $(kubectl get nodes -o name); do
  kubectl label "$node" ingress-ready=true --overwrite 2>/dev/null || true
done

echo "[INFO] Aguardando Ingress Controller ficar pronto..."
kubectl wait --namespace ingress-nginx \
  --for=condition=Ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=180s
