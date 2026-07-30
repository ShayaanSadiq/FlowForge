# FlowForge

Cloud-native async job processing platform built with **Spring Boot**, **React**, and **MongoDB**. Designed as a portfolio project demonstrating backend systems engineering, DevOps practices, and production-ready deployment across **Docker**, **AWS**, and **Vercel**.

## Features

- JWT authentication (register / login)
- Submit async jobs via REST API (`POST /api/jobs`)
- Schedule jobs with a delay (seconds) or a specific run time — queued until due
- Background worker with job state machine: `PENDING → RUNNING → SUCCEEDED | FAILED → DEAD_LETTER`
- Automatic retries (up to 3 attempts) with dead-letter queue
- Job types: Python Script, JSON Format, CSV Analyze, Hash Generate, Base64 Codec
- React dashboard with live job status, logs, and retry controls
- OpenAPI docs at `/swagger-ui.html`
- Health checks at `/actuator/health`
- Rate limiting on job submission (30 req/min per IP)
- Structured JSON logging with correlation IDs in production

## Architecture

```
React (Vercel)  ──HTTPS──▶  Spring Boot API (AWS ECS)  ──▶  MongoDB Atlas
                                    │
                                    ▼
                            Worker (AWS ECS / Docker)
```

Local development uses Docker Compose with MongoDB, API, Worker, and Frontend services.

## Quick Start (Docker)

```bash
git clone github.com/ShayaanSadiq/FlowForge
cd Agile
docker compose up --build
```

| Service   | URL                        |
|-----------|----------------------------|
| Frontend  | http://localhost:3000      |
| API       | http://localhost:8080      |
| Swagger   | http://localhost:8080/swagger-ui.html |
| MongoDB   | localhost:27017            |

1. Open http://localhost:3000
2. Register an account
3. Submit a job and watch it process in real time

## Local Development (without Docker)

**Backend** (requires Java 21+ and MongoDB running locally):

```bash
cd backend
mvn spring-boot:run -pl api
mvn spring-boot:run -pl worker   # separate terminal
```

**Frontend** (requires Node.js 20+):

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server proxies API calls to `http://localhost:8080`.

## Project Structure

```
├── backend/
│   ├── core/       # Domain models, repositories, services, job state machine
│   ├── api/        # REST API, JWT auth, OpenAPI
│   └── worker/     # Scheduled job processor
├── frontend/       # React + Vite + MUI dashboard
├── docker-compose.yml
├── .github/workflows/   # CI/CD pipelines
└── docs/           # Deployment and portfolio guides
```

## API Endpoints

| Method | Path                  | Auth | Description           |
|--------|-----------------------|------|-----------------------|
| POST   | `/api/auth/register`  | No   | Create account        |
| POST   | `/api/auth/login`     | No   | Get JWT token         |
| POST   | `/api/jobs`           | Yes  | Submit a job          |
| GET    | `/api/jobs`           | Yes  | List your jobs        |
| GET    | `/api/jobs/{id}`      | Yes  | Job detail + logs     |
| POST   | `/api/jobs/{id}/retry`| Yes  | Retry failed job      |
| GET    | `/api/stats`          | Yes  | Job counts by status  |

## Deployment

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for full guides on:

- **Docker Compose** — local full stack
- **Vercel** — frontend deployment
- **AWS ECS Fargate** — backend + worker via ECR
- **MongoDB Atlas** — managed database

<!-- ## Portfolio Narrative

See [docs/PORTFOLIO.md](docs/PORTFOLIO.md) for how to present this project in your UofT MASc application. -->

## Tech Stack

- Java 21, Spring Boot 3.3, Spring Security, Spring Data MongoDB
- React 18, Vite, Material UI, React Router
- MongoDB 7
- Docker, Docker Compose, GitHub Actions, AWS ECS/ECR, Vercel

## License

MIT
