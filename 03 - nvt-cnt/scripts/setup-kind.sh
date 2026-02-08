#!/usr/bin/env bash
# Lab 03 - Cria cluster Kind e namespace nvt-cnt (se não existirem)

set -e

CLUSTER_NAME="${KIND_CLUSTER_NAME:-kind}"
NAMESPACE="nvt-cnt"

if ! command -v kind &>/dev/null; then
  echo "kind não encontrado. Instale: https://kind.sigs.k8s.io/docs/user/quick-start/#installation"
  exit 1
fi

if ! kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
  echo "Criando cluster Kind: ${CLUSTER_NAME}"
  kind create cluster --name "${CLUSTER_NAME}"
else
  echo "Cluster Kind '${CLUSTER_NAME}' já existe."
fi

# Kind já configura o kubeconfig em ~/.kube/config por padrão
echo "Kubectl apontando para: $(kubectl config current-context)"

if ! kubectl get namespace "$NAMESPACE" &>/dev/null; then
  echo "Criando namespace: ${NAMESPACE}"
  kubectl create namespace "$NAMESPACE"
else
  echo "Namespace '${NAMESPACE}' já existe."
fi

echo "Pronto. Use: kubectl get pods -n ${NAMESPACE}"
