# FlowForge Deployment Checklist

Use this runbook on deploy day. Replace placeholders with your real values.

**Target stack:** MongoDB Atlas + AWS ECS Fargate (API + worker) + Vercel (frontend)

| Placeholder | Your value |
|-------------|------------|
| `API_DOMAIN` | e.g. `api.yourdomain.com` |
| `FRONTEND_DOMAIN` | e.g. `flowforge.vercel.app` |
| `AWS_ACCOUNT_ID` | 12-digit AWS account ID |
| `AWS_REGION` | e.g. `us-east-1` |

---

## Before you start

- [ ] AWS CLI installed and authenticated (`aws sts get-caller-identity`)
- [ ] Docker installed locally
- [ ] Vercel account connected to GitHub repo `ShayaanSadiq/FlowForge`
- [ ] Domain or Vercel URL decided for frontend
- [ ] Generate a strong JWT secret (32+ chars): `openssl rand -base64 32`

---

## Step 1 — MongoDB Atlas (~15 min)

- [ ] Create free **M0** cluster at [mongodb.com/atlas](https://www.mongodb.com/atlas)
- [ ] Create database user (read/write on `flowforge` database)
- [ ] Network access: add your IP for testing; add `0.0.0.0/0` temporarily for ECS (tighten to VPC later)
- [ ] Copy connection string:

```
mongodb+srv://<user>:<password>@cluster0.xxxxx.mongodb.net/flowforge?retryWrites=true&w=majority
```

---

## Step 2 — AWS secrets (~10 min)

Store production secrets in **AWS Secrets Manager** (region `AWS_REGION`):

```bash
aws secretsmanager create-secret \
  --name flowforge/mongodb-uri \
  --secret-string "mongodb+srv://USER:PASS@cluster0.xxxxx.mongodb.net/flowforge?retryWrites=true&w=majority"

aws secretsmanager create-secret \
  --name flowforge/jwt-secret \
  --secret-string "YOUR_32_PLUS_CHAR_SECRET"
```

Note the full secret ARNs — update `ACCOUNT_ID` in:
- `.aws/flowforge-api-task.json`
- `.aws/flowforge-worker-task.json`

---

## Step 3 — ECR repositories (~5 min)

```bash
aws ecr create-repository --repository-name flowforge-api --region $AWS_REGION
aws ecr create-repository --repository-name flowforge-worker --region $AWS_REGION
```

Create CloudWatch log groups:

```bash
aws logs create-log-group --log-group-name /ecs/flowforge-api --region $AWS_REGION
aws logs create-log-group --log-group-name /ecs/flowforge-worker --region $AWS_REGION
```

---

## Step 4 — Build and push images (~10 min)

Replace `ACCOUNT_ID` in both task definition JSON files, then:

```bash
export AWS_ACCOUNT_ID=YOUR_ACCOUNT_ID
export AWS_REGION=us-east-1
chmod +x scripts/push-ecr.sh
./scripts/push-ecr.sh
```

---

## Step 5 — ECS cluster and services (~30 min)

### API service (with load balancer)

1. Create ECS cluster: `flowforge-cluster`
2. Register API task definition:

```bash
# Replace ACCOUNT_ID in .aws/flowforge-api-task.json first
aws ecs register-task-definition --cli-input-json file://.aws/flowforge-api-task.json
```

3. Create Fargate service for API behind an **Application Load Balancer**
   - Container port: **8080**
   - Health check path: `/actuator/health`
   - HTTPS listener (443) with ACM certificate
   - Point `API_DOMAIN` DNS to the ALB

### Worker service (no load balancer)

1. Register worker task definition:

```bash
# Replace ACCOUNT_ID in .aws/flowforge-worker-task.json first
aws ecs register-task-definition --cli-input-json file://.aws/flowforge-worker-task.json
```

2. Create Fargate service for worker in the **same VPC/subnets** as API
   - No public load balancer required
   - Desired count: **1**
   - Worker polls MongoDB directly — ensure security groups allow outbound to Atlas

---

## Step 6 — Vercel frontend (~10 min)

1. Import repo at [vercel.com/new](https://vercel.com/new)
2. **Root Directory:** `frontend`
3. Framework: **Vite** (auto-detected via `vercel.json`)
4. Environment variable:

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://API_DOMAIN` |

5. Deploy

### Custom domain CORS (if not using `*.vercel.app`)

Set on the API ECS task:

```
FLOWFORGE_CORS_ORIGINS=https://FRONTEND_DOMAIN
```

---

## Step 7 — Verify (~5 min)

```bash
export API_URL=https://API_DOMAIN
export FRONTEND_URL=https://FRONTEND_DOMAIN
chmod +x scripts/verify-deployment.sh
./scripts/verify-deployment.sh
```

Manual checks:
- [ ] Register a new account on the live frontend
- [ ] Submit a **Hash Generate** job → reaches **SUCCEEDED**
- [ ] Submit a **Python Script** job → reaches **SUCCEEDED** (worker has Python)
- [ ] Cancel a pending/scheduled job → status **CANCELLED**
- [ ] Swagger UI loads at `https://API_DOMAIN/swagger-ui.html`

---

## GitHub secrets (optional — for future CI deploy)

Only needed if you re-add a GitHub Actions deploy workflow later:

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM user with ECR + ECS permissions |
| `AWS_SECRET_ACCESS_KEY` | Matching secret key |

Current repo uses **manual deploy** via `scripts/push-ecr.sh` — no deploy workflow is configured.

---

## Cost-saving tips

- Stop ECS services when not demoing (saves Fargate hours)
- Atlas M0 and Vercel Hobby are free tiers
- Tear down unused ECR images periodically

---

## Troubleshooting

| Symptom | Check |
|---------|-------|
| Frontend can't reach API | `VITE_API_BASE_URL`, CORS origins, HTTPS on both sides |
| Jobs stay PENDING | Worker service running? Same MongoDB URI? Atlas IP allowlist |
| API unhealthy | CloudWatch logs `/ecs/flowforge-api`, secrets ARNs |
| Python jobs fail | Worker image includes Python (`worker` Dockerfile target) |

See also [DEPLOYMENT.md](DEPLOYMENT.md) for detailed reference.
