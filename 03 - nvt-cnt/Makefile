# Lab 03 – Kind + NGINX Ingress + MetalLB
# Uso: make rebuild (destrói, recria cluster, instala Ingress e MetalLB)
# Depois: namespace, config-server, build+load+deploy portal-demo, aplicar Ingress (manifests/connect/)
#
CLUSTER_NAME ?= kind
KIND_CONFIG  ?= scripts/kind-cluster.yaml
K8S_VERSION  ?= 1.34.3
KIND_NODE_IMAGE ?= kindest/node:v$(K8S_VERSION)

.PHONY: up ingress metallb destroy rebuild

up:
	@echo "[INFO] Criando cluster Kind..."
	kind create cluster --config $(KIND_CONFIG) --name $(CLUSTER_NAME) --image $(KIND_NODE_IMAGE)

ingress:
	bash scripts/deploy-nginx-ingress.sh

metallb:
	bash scripts/deploy-metallb.sh

destroy:
	@echo "[INFO] Removendo cluster $(CLUSTER_NAME)..."
	kind delete cluster --name $(CLUSTER_NAME)

rebuild: destroy up ingress metallb
	@echo "[INFO] Cluster pronto. Próximos passos: namespace, config-server, build+deploy portal-demo, aplicar manifests/connect/ (inclui Ingress). Ver README."
