#!/usr/bin/env bash
set -euo pipefail

# Build and push FlowForge API + worker images to Amazon ECR.
#
# Required environment variables:
#   AWS_ACCOUNT_ID  - 12-digit AWS account ID
#
# Optional:
#   AWS_REGION      - defaults to us-east-1
#   IMAGE_TAG       - defaults to git short SHA or "latest"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
AWS_REGION="${AWS_REGION:-us-east-1}"
IMAGE_TAG="${IMAGE_TAG:-$(git -C "$ROOT" rev-parse --short HEAD 2>/dev/null || echo latest)}"

if [[ -z "${AWS_ACCOUNT_ID:-}" ]]; then
  echo "Error: set AWS_ACCOUNT_ID before running this script." >&2
  exit 1
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
API_REPO="${ECR_REGISTRY}/flowforge-api"
WORKER_REPO="${ECR_REGISTRY}/flowforge-worker"

echo "Logging in to ECR (${AWS_REGION})..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "Building API image..."
docker build --target api -t flowforge-api:"$IMAGE_TAG" "$ROOT/backend"
docker tag flowforge-api:"$IMAGE_TAG" "$API_REPO:$IMAGE_TAG"
docker tag flowforge-api:"$IMAGE_TAG" "$API_REPO:latest"

echo "Building worker image..."
docker build --target worker -t flowforge-worker:"$IMAGE_TAG" "$ROOT/backend"
docker tag flowforge-worker:"$IMAGE_TAG" "$WORKER_REPO:$IMAGE_TAG"
docker tag flowforge-worker:"$IMAGE_TAG" "$WORKER_REPO:latest"

echo "Pushing API image..."
docker push "$API_REPO:$IMAGE_TAG"
docker push "$API_REPO:latest"

echo "Pushing worker image..."
docker push "$WORKER_REPO:$IMAGE_TAG"
docker push "$WORKER_REPO:latest"

echo "Done."
echo "  API:    $API_REPO:$IMAGE_TAG"
echo "  Worker: $WORKER_REPO:$IMAGE_TAG"
