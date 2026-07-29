# Deployment Guide

This guide covers deploying FlowForge across all three target platforms: Docker (local), Vercel (frontend), and AWS (backend + worker).

## Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for local frontend dev or Vercel)
- Java 21+ and Maven 3.9+ (for local backend dev)
- AWS account (for production backend)
- MongoDB Atlas account (free M0 tier)
- Vercel account (free tier)
- GitHub account (for CI/CD)

---

## 1. Local Development with Docker Compose

The fastest way to run the full stack:

```bash
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

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATA_MONGODB_URI` | `mongodb://mongo:27017/flowforge` | MongoDB connection |
| `FLOWFORGE_JWT_SECRET` | dev secret | JWT signing key (change in prod) |

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

Set as `SPRING_DATA_MONGODB_URI` in AWS Secrets Manager or environment variables.

---

## 3. Frontend on Vercel

### Option A: Vercel Dashboard

1. Push this repo to GitHub
2. Import the project in [vercel.com/new](https://vercel.com/new)
3. Set **Root Directory** to `frontend`
4. Framework Preset: **Vite**
5. Add environment variable:

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://your-api-domain.com` |

6. Deploy

### Option B: Vercel CLI

```bash
cd frontend
npm install -g vercel
vercel --prod
```

### Important: CORS and HTTPS

- Vercel serves the frontend over HTTPS
- Your AWS backend must also use HTTPS (via ALB + ACM certificate)
- Update CORS in `SecurityConfig.java` to include your Vercel domain:

```java
config.setAllowedOriginPatterns(List.of(
    "https://your-app.vercel.app"
));
```

---

## 4. Backend on AWS ECS Fargate

### Step 1: Create ECR Repositories

```bash
aws ecr create-repository --repository-name flowforge-api
aws ecr create-repository --repository-name flowforge-worker
```

### Step 2: Build and Push Docker Images

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

docker build --target api -t flowforge-api ./backend
docker tag flowforge-api:latest ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/flowforge-api:latest
docker push ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/flowforge-api:latest

docker build --target worker -t flowforge-worker ./backend
docker tag flowforge-worker:latest ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/flowforge-worker:latest
docker push ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/flowforge-worker:latest
```

### Step 3: Store Secrets in AWS Secrets Manager

```bash
aws secretsmanager create-secret --name flowforge/mongodb-uri --secret-string "mongodb+srv://..."
aws secretsmanager create-secret --name flowforge/jwt-secret --secret-string "your-256-bit-secret-key"
```

### Step 4: Create ECS Cluster and Services

1. Create an ECS cluster named `flowforge-cluster`
2. Update `.aws/flowforge-api-task.json` with your account ID and ECR image URI
3. Register the task definition:

```bash
aws ecs register-task-definition --cli-input-json file://.aws/flowforge-api-task.json
```

4. Create a Fargate service with an Application Load Balancer
5. Configure ALB listener on port 443 with an ACM certificate
6. Point your API domain (e.g. `api.yourdomain.com`) to the ALB

### Step 5: GitHub Actions CI/CD

Add these secrets to your GitHub repository:

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM user with ECR + ECS permissions |
| `AWS_SECRET_ACCESS_KEY` | IAM secret key |

On push to `main`, `.github/workflows/deploy-backend.yml` builds, pushes to ECR, and deploys to ECS.

---

## 5. Environment Profiles

Spring profiles control runtime behavior:

| Profile | Usage | Logging |
|---------|-------|---------|
| `dev` | Local development | Console, DEBUG level |
| `prod` | AWS production | JSON structured logs |

Activate with `SPRING_PROFILES_ACTIVE=prod`.

---

## 6. Health Checks

Verify deployments:

```bash
# API health
curl https://api.yourdomain.com/actuator/health

# Submit a test job (after login)
curl -X POST https://api.yourdomain.com/api/jobs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"type":"SIMULATION","payload":"hello world"}'
```

---

## Cost Management

Stay on free tiers during development:

- **MongoDB Atlas M0** — free forever (512 MB)
- **Vercel Hobby** — free for personal projects
- **AWS** — ECR free tier, ECS Fargate free tier for 12 months (750 hours/month)
- **Tip**: Stop ECS services when not demoing to avoid charges
