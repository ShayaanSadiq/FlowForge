# FlowForge

[![CI](https://github.com/ShayaanSadiq/FlowForge/actions/workflows/ci.yml/badge.svg)](https://github.com/ShayaanSadiq/FlowForge/actions/workflows/ci.yml)

FlowForge is a background job processing platform built with Spring Boot, MongoDB, and React. You submit a job through the REST API or the web dashboard, and a separate worker service runs it, retries it if it fails, and records its logs and result.

## Why I built it

Most web apps finish their work inside a single request. I wanted to learn how systems handle work that doesn't fit in one: work that is slow, can fail partway through, or needs to run later. CI runners, data pipelines, and ML experiment platforms like MLflow and Airflow all solve this problem, so I built a smaller version from scratch to understand the pieces a managed queue usually hides: job states, retries, scheduling, and running scripts within time and output limits.

## Features

- Submit jobs through a REST API or a React dashboard
- Run jobs immediately, after a delay, or at a set time up to 7 days ahead
- Five job types: Python scripts, JSON formatting, CSV analysis, SHA-256/SHA-512 hashing, and Base64 encoding and decoding
- Up to 3 attempts per job, then a dead-letter state with manual retry
- Execution logs, results, timing, and an audit trail for every job
- JWT authentication, per-user data isolation, and rate limiting on job submission
- Swagger API docs, health checks, and structured JSON logs in production

## How it works

The backend is split into two Spring Boot services that share a common `core` module and communicate only through MongoDB:

- **API** (`backend/api`) handles authentication and job requests. A new job is saved to MongoDB as `PENDING`, and the API responds right away without running it.
- **Worker** (`backend/worker`) checks MongoDB every 2 seconds for pending jobs that are due and runs up to 10 of them, one after another.

When the worker picks up a job, it marks it `RUNNING` and runs the handler for its type. Python scripts run as a separate `python3` process with a 30-second timeout. If the job succeeds, it is marked `SUCCEEDED` and its output is saved. If it fails, it goes back to `PENDING` to try again, and after the third failure it moves to `DEAD_LETTER` until the user retries it. Pending jobs, including scheduled ones, can be cancelled before they start.

Because the two services only share the database, the worker can be restarted without losing work. Jobs submitted in the meantime wait in MongoDB until it is back.

## Getting started

Requires Docker.

```bash
git clone https://github.com/ShayaanSadiq/FlowForge.git
cd FlowForge
docker compose up --build
```

- Dashboard: http://localhost:3000
- API: http://localhost:8080
- API docs (Swagger): http://localhost:8080/swagger-ui.html

## Usage

**Dashboard:** open http://localhost:3000, register, click **New Job**, choose a job type, and submit. Open a job to see its status, logs, and result.

**API:** register to get a token, then use it to submit jobs:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "ada@example.com", "password": "flowforge123", "displayName": "Ada"}'

curl -X POST http://localhost:8080/api/jobs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"type": "PYTHON_SCRIPT", "payload": "print(sum(range(10)))", "delaySeconds": 5}'
```

Replace `<token>` with the `token` from the register response. Then call `GET /api/jobs/{id}` to see the job's status, logs, and output.

## API endpoints

All endpoints except register and login need an `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Log in and get a token |
| POST | `/api/jobs` | Submit a job |
| GET | `/api/jobs` | List your jobs, filtered by status or type |
| GET | `/api/jobs/{id}` | Job details, logs, and result |
| GET | `/api/jobs/{id}/events` | Job audit trail |
| POST | `/api/jobs/{id}/retry` | Retry a dead-lettered job |
| POST | `/api/jobs/{id}/cancel` | Cancel a pending job |
| GET | `/api/stats` | Job counts by status |

## Testing

Run `mvn verify` in `backend/` for the JUnit tests and `npm test` in `frontend/` for the Vitest tests. GitHub Actions runs both on every push and pull request to `main`.

## Deployment

The project is set up to deploy the API and worker as separate containers on AWS ECS Fargate, the dashboard on Vercel, and the database on MongoDB Atlas. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for setup steps.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, Spring Data MongoDB
- **Frontend:** React 18, Vite, Material UI
- **Database:** MongoDB 7
- **DevOps:** Docker, GitHub Actions, AWS ECS, Vercel

## License

MIT. See [LICENSE](LICENSE).
