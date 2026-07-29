# Portfolio Guide for UofT MASc Application

Use this guide when adding FlowForge to your CV, statement of intent, and GitHub profile.

## One-Line Summary

**FlowForge** — Cloud-native async job processing platform with Spring Boot, React, MongoDB, Docker, AWS ECS, and Vercel deployment.

## CV Entry

```
FlowForge — Async Job Processing Platform                    [Month Year – Present]
• Designed and built a full-stack job orchestration platform with Spring Boot REST API,
  background worker, and React dashboard
• Implemented job state machine (PENDING → RUNNING → SUCCEEDED/FAILED → DEAD_LETTER)
  with automatic retries and audit logging
• Deployed via Docker Compose (local), AWS ECS Fargate (backend), and Vercel (frontend)
• CI/CD pipeline with GitHub Actions: test → build → ECR push → ECS deploy
Tech: Java 21, Spring Boot 3, MongoDB, React, Docker, AWS, GitHub Actions
GitHub: [your-link]  |  Live Demo: [your-vercel-url]
```

## Statement of Intent Talking Points

Pick 2–3 of these for your letter:

1. **Systems design**: "I designed an async job processing pipeline with explicit state transitions, retry policies, and dead-letter handling — patterns essential for reliable distributed systems."

2. **DevOps maturity**: "The project includes containerized deployment, CI/CD from commit to production, structured logging with correlation IDs, and health checks — demonstrating I can build and operate production software."

3. **ML infrastructure relevance**: "FlowForge's architecture mirrors ML experiment batch runners (job submission, worker orchestration, run tracking), connecting my backend engineering skills to CS/ML research infrastructure."

4. **Full-stack ownership**: "I owned the entire stack from database schema design to frontend dashboard to cloud deployment, documenting architecture decisions and deployment procedures."

## GitHub README Checklist

Before sharing your repo with admissions reviewers:

- [ ] README with architecture diagram and quick start
- [ ] Live demo URL (Vercel frontend)
- [ ] API health check URL (AWS backend)
- [ ] `docs/DEPLOYMENT.md` with step-by-step deploy guide
- [ ] OpenAPI docs accessible at `/swagger-ui.html`
- [ ] Clean commit history with meaningful messages
- [ ] 2–3 minute video walkthrough (Loom/YouTube) linked in README

## Demo Script (2 minutes)

1. **Show architecture** (30s): "React on Vercel talks to Spring Boot on AWS ECS, which stores jobs in MongoDB Atlas. A separate worker process polls for pending jobs."

2. **Submit a job** (30s): Register, submit a Simulation job, show it transition PENDING → RUNNING → SUCCEEDED.

3. **Show logs** (30s): Open job detail, point out timestamped execution logs and result.

4. **Show DevOps** (30s): Briefly show Docker Compose, GitHub Actions workflow, and ECS task definition.

## Skills Demonstrated

| Skill | Evidence in Project |
|-------|---------------------|
| Backend API design | REST endpoints, JWT auth, validation, error handling |
| Database design | MongoDB document model for jobs, users, audit events |
| Async processing | Worker with polling, state machine, retries |
| Containerization | Multi-stage Dockerfile, Docker Compose |
| Cloud deployment | AWS ECS Fargate, ECR, Secrets Manager |
| CI/CD | GitHub Actions for test + deploy |
| Frontend | React dashboard with real-time job monitoring |
| Documentation | README, deployment guide, architecture docs |
| Testing | JUnit tests for job state machine |

## What Makes This Stand Out

Compared to typical student portfolios (todo apps, e-commerce clones):

- **Systems thinking** — state machine, retries, dead-letter queue
- **Production patterns** — health checks, rate limiting, structured logging, CORS
- **Multi-platform deployment** — Docker + AWS + Vercel with documented rationale
- **Research relevance** — connects to ML experiment orchestration infrastructure
