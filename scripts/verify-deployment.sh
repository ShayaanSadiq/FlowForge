#!/usr/bin/env bash
set -euo pipefail

# Quick deployment smoke checks for FlowForge.
#
# Usage:
#   API_URL=https://api.example.com ./scripts/verify-deployment.sh
#   API_URL=... FRONTEND_URL=https://app.example.com ./scripts/verify-deployment.sh
#
# Optional auth smoke test (requires existing account):
#   API_URL=... TEST_EMAIL=you@example.com TEST_PASSWORD=secret ./scripts/verify-deployment.sh

API_URL="${API_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-}"

pass() { echo "PASS: $1"; }
fail() { echo "FAIL: $1" >&2; exit 1; }

echo "Checking API health at ${API_URL}/actuator/health ..."
health="$(curl -fsS "${API_URL}/actuator/health" || fail "API health check unreachable")"
echo "$health" | grep -q '"status":"UP"' && pass "API health is UP" || fail "API health not UP"

if [[ -n "$FRONTEND_URL" ]]; then
  echo "Checking frontend at ${FRONTEND_URL} ..."
  curl -fsS -o /dev/null "$FRONTEND_URL" && pass "Frontend responds" || fail "Frontend unreachable"
fi

if [[ -n "${TEST_EMAIL:-}" && -n "${TEST_PASSWORD:-}" ]]; then
  echo "Running authenticated smoke test ..."
  token="$(curl -fsS -X POST "${API_URL}/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"${TEST_EMAIL}\",\"password\":\"${TEST_PASSWORD}\"}" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')"

  curl -fsS "${API_URL}/api/stats" -H "Authorization: Bearer ${token}" >/dev/null \
    && pass "Authenticated stats request" \
    || fail "Authenticated stats request failed"

  echo "Manual follow-up:"
  echo "  1. Submit a HASH_GENERATE job from the dashboard"
  echo "  2. Confirm it reaches SUCCEEDED within ~30 seconds"
else
  echo
  echo "Optional authenticated smoke test:"
  echo "  TEST_EMAIL=you@example.com TEST_PASSWORD=secret API_URL=${API_URL} $0"
fi

echo
echo "All automated checks passed."
