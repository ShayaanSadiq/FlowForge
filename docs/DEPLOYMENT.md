# Deployment Guide

This guide covers deploying FlowForge across all three target platforms: Docker (local), Vercel (frontend), and AWS (backend + worker).

**Deploy day runbook:** see [DEPLOY-CHECKLIST.md](DEPLOY-CHECKLIST.md) for a step-by-step checklist.

## Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for local frontend dev or Vercel)
- Java 21+ and Maven 3.9+ (for local backend dev)
- AWS account (for production backend)
- MongoDB Atlas account (free M0 tier)
- Vercel account (free tier)
- GitHub account (for CI)

---

## 1. Local Development with Docker Compose

The fastest way to run the full stack:

```bash
git clone git@github.com:ShayaanSadiq/FlowForge.git
cd FlowForge
docker compose up --build
```

This starts:
- **mongo** — MongoDB 7 on port 27017
- **backend** — Spring Boot API on port 8080
- **worker** — Job processor (polls MongoDB every 2s)
- **frontend** — Nginx serving React build on port 3000

To stop and remove volumes:

```bash
docker compose down -v
```

### Environment Variables (Docker Compose)

Copy `.env.example` to `.env` for local overrides.

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATA_MONGODB_URI` | `mongodb://mongo:27017/flowforge` | MongoDB connection |
| `FLOWFORGE_JWT_SECRET` | dev secret | JWT signing key (change in prod) |
| `FLOWFORGE_CORS_ORIGINS` | _(empty)_ | Extra CORS origin patterns, comma-separated |

---

## 2. MongoDB Atlas (Production Database)

1. Create a free M0 cluster at [mongodb.com/atlas](https://www.mongodb.com/atlas)
2. Create a database user with read/write access
3. Add network access rules:
   - Your local IP (for testing)
   - `0.0.0.0/0` (for AWS ECS — restrict to VPC in production)
4. Copy the connection string:

```
mongodb+srv://<user>:<password>@cluster0.xxxxx.mongodb.net/flowforge?retryWrites=true&w=majority
```

Set as `SPRING_DATA_MONGODB_URI` in AWS Secrets Manager.

---

## 3. Frontend on Vercel

### Option A: Vercel Dashboard

1. Push this repo to GitHub
2. Import the project in [vercel.com/new](https://vercel.com/new)
3. Set **Root Directory** to `frontend`
4. Framework Preset: **Vite** (configured in `frontend/vercel.json`)
5. Add environment variable:

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://api.yourdomain.com` |

6. Deploy

See `frontend/.env.example` for local dev equivalents.

### Option B: Vercel CLI

```bash
cd frontend
npm install -g vercel
vercel --prod
```

### Important: CORS and HTTPS

- Vercel serves the frontend over HTTPS
- Your AWS backend must also use HTTPS (via ALB + ACM certificate)
- Default CORS allows `http://localhost:*` and `https://*.vercel.app`
- For a custom domain, set on the API container:

```
FLOWFORGE_CORS_ORIGINS=https://your-frontend-domain.com
```

Multiple origins: comma-separated patterns.

---

## 4. Backend on AWS ECS Fargate

Deployment is **manual** via scripts (no GitHub Actions deploy workflow).

### Step 1: Create ECR Repositories

```bash
aws ecr create-repository --repository-name flowforge-api
aws ecr create-repository --repository-name flowforge-worker
```

### Step 2: Build and Push Docker Images

Replace `ACCOUNT_ID` in `.aws/flowforge-api-task.json` and `.aws/flowforge-worker-task.json`, then:

```bash
export AWS_ACCOUNT_ID=YOUR_ACCOUNT_ID
export AWS_REGION=us-east-1
chmod +x scripts/push-ecr.sh
./scripts/push-ecr.sh
```

This builds and pushes both **API** (port 8080) and **worker** (port 8081, includes Python 3) images.

### Step 3: Store Secrets in AWS Secrets Manager

```bash
aws secretsmanager create-secret --name flowforge/mongodb-uri --secret-string "mongodb+srv://..."
aws secretsmanager create-secret --name flowforge/jwt-secret --secret-string "your-256-bit-secret-key"
```

Update secret ARNs in both task definition files if your region/account differs.

### Step 4: Create ECS Cluster and Services

1. Create an ECS cluster named `flowforge-cluster`
2. Create CloudWatch log groups: `/ecs/flowforge-api`, `/ecs/flowforge-worker`
3. Register task definitions:

```bash
aws ecs register-task-definition --cli-input-json file://.aws/flowforge-api-task.json
aws ecs register-task-definition --cli-input-json file://.aws/flowforge-worker-task.json
```

4. **API service:** Fargate + Application Load Balancer on port 443 (HTTPS)
5. **Worker service:** Fargate, no load balancer, desired count 1
6. Point your API domain (e.g. `api.yourdomain.com`) to the ALB

Both services need outbound access to MongoDB Atlas.

---

## 5. Environment Profiles

Spring profiles control runtime behavior:

| Profile | Usage | Logging |
|---------|-------|---------|
| `dev` | Local development | Console, DEBUG level |
| `prod` | AWS production | JSON structured logs |

Activate with `SPRING_PROFILES_ACTIVE=prod` (set in ECS task definitions).

---

## 6. Health Checks and Verification

```bash
export API_URL=https://api.yourdomain.com
export FRONTEND_URL=https://your-app.vercel.app
./scripts/verify-deployment.sh
```

Manual API check:

```bash
curl https://api.yourdomain.com/actuator/health
```

### API Endpoints (production)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Get JWT token |
| POST | `/api/jobs` | Submit a job |
| GET | `/api/jobs` | List jobs (filter by status/type) |
| GET | `/api/jobs/{id}` | Job detail + logs |
| POST | `/api/jobs/{id}/retry` | Retry failed job |
| POST | `/api/jobs/{id}/cancel` | Cancel pending job |
| GET | `/api/stats` | Job counts by status |

---

## Cost Management

Stay on free tiers during development:

- **MongoDB Atlas M0** — free forever (512 MB)
- **Vercel Hobby** — free for personal projects
- **AWS** — ECR free tier, ECS Fargate free tier for 12 months (750 hours/month)
- **Tip**: Stop ECS services when not demoing to avoid charges
