#!/usr/bin/env bash
# ==============================================================================
# Huawei Cloud CCE Deployment Script for CodeArts Pipeline / Deploy
# ==============================================================================
set -euo pipefail

# Configurable environment variables with defaults
SWR_REGION="${SWR_REGION:-ap-southeast-3}"
SWR_ORG="${SWR_ORG:-devsecops-org}"
IMAGE_NAME="${IMAGE_NAME:-age-calculator}"
IMAGE_TAG="${IMAGE_TAG:-${BUILD_NUMBER:-latest}}"
NAMESPACE="${NAMESPACE:-age-calculator}"
MANIFESTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

FULL_IMAGE="swr.${SWR_REGION}.myhuaweicloud.com/${SWR_ORG}/${IMAGE_NAME}:${IMAGE_TAG}"

echo "=========================================================="
echo " Starting Huawei Cloud CCE Deployment via CodeArts"
echo " Target Image:     ${FULL_IMAGE}"
echo " Target Namespace: ${NAMESPACE}"
echo " Manifests Dir:    ${MANIFESTS_DIR}"
echo "=========================================================="

# 1. Ensure Namespace exists
echo "[1/4] Applying Namespace..."
kubectl apply -f "${MANIFESTS_DIR}/00-namespace.yaml"

# 2. Apply ConfigMap
echo "[2/4] Applying ConfigMap..."
kubectl apply -f "${MANIFESTS_DIR}/01-configmap.yaml"

# 3. Apply Services, Ingress, HPA, and PDB
echo "[3/4] Applying Services, HPA, and PDB..."
kubectl apply -f "${MANIFESTS_DIR}/03-service.yaml"
kubectl apply -f "${MANIFESTS_DIR}/03-service-elb.yaml"
kubectl apply -f "${MANIFESTS_DIR}/04-ingress.yaml"
kubectl apply -f "${MANIFESTS_DIR}/05-hpa.yaml"
kubectl apply -f "${MANIFESTS_DIR}/06-pdb.yaml"

# 4. Deploy Application with dynamic image tag
echo "[4/4] Deploying Application Deployment..."
# If kustomize is available, use it; otherwise use sed replacement on deployment
if command -v kustomize &> /dev/null; then
  echo "Using kustomize to set image..."
  (cd "${MANIFESTS_DIR}" && kustomize edit set image "swr.ap-southeast-3.myhuaweicloud.com/devsecops-org/age-calculator=${FULL_IMAGE}")
  kubectl apply -k "${MANIFESTS_DIR}"
else
  echo "Applying deployment with image: ${FULL_IMAGE}..."
  sed "s|image: .*|image: ${FULL_IMAGE}|g" "${MANIFESTS_DIR}/02-deployment.yaml" | kubectl apply -f -
fi

# 5. Wait for rollout status
echo "Waiting for Deployment rollout to complete..."
kubectl rollout status deployment/age-calculator-deployment -n "${NAMESPACE}" --timeout=180s

echo "=========================================================="
echo " Deployment Successfully Completed to Huawei Cloud CCE!"
echo "=========================================================="
kubectl get pods -n "${NAMESPACE}" -l app.kubernetes.io/name=age-calculator
echo ""
kubectl get svc -n "${NAMESPACE}"
