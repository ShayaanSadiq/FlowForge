# ML Use Case: Experiment Batch Orchestration

FlowForge mirrors core concepts from ML experiment orchestration platforms (MLflow, Kubeflow, Airflow) at a smaller, portfolio-friendly scale.

## Parallel to ML Infrastructure

| FlowForge Concept | ML Platform Equivalent |
|-------------------|------------------------|
| Job submission API | Experiment run trigger |
| Worker polling queue | Task scheduler / executor |
| Job state machine | Run lifecycle management |
| Retry + dead letter | Fault-tolerant batch processing |
| Job logs + metrics | Experiment tracking |
| `PYTHON_SCRIPT` job type | Model training / inference script |

## How It Applies to CS/ML Research

When running ML experiments, researchers need to:

1. **Submit** long-running jobs (training, hyperparameter sweeps, data preprocessing)
2. **Track** status, logs, and metrics across many runs
3. **Retry** failed runs without losing experiment metadata
4. **Scale** workers to process multiple experiments concurrently

FlowForge demonstrates the backend infrastructure layer that supports these workflows — the same systems thinking required for ML experiment platforms, but implemented with production Java/Spring Boot patterns.

## Example Workflow

```
Researcher submits PYTHON_SCRIPT job
        │
        ▼
API stores job as PENDING in MongoDB
        │
        ▼
Worker picks up job, executes (simulated in MVP)
        │
        ▼
Logs + metrics stored (durationMs, attempt count)
        │
        ▼
Dashboard shows run status and output
```

## Python Script Jobs

The worker runs real Python 3 scripts submitted as the job payload:

- Script is written to a temp file, executed with `python3`, then cleaned up
- stdout is captured as job output; stderr is logged line-by-line
- 30 second timeout, max script/output size limits
- Requires Python 3 in the worker environment (included in the worker Docker image)

Example payload:

```python
print("Hello from FlowForge")
for i in range(3):
    print(f"step {i}")
```

## Future Extensions

- Per-job Docker sandbox for stronger isolation
- Metrics charts (latency distribution, success rate over time)
- Job dependencies (run B after A succeeds)
- Integration with object storage (S3) for model artifacts

These extensions map directly to Kubeflow Pipelines and MLflow's experiment tracking model.

## Statement of Intent Snippet

> "Built FlowForge, a cloud-native async job processing platform with retry semantics and structured observability. The architecture mirrors ML experiment batch runners — job submission, worker orchestration, and run tracking — demonstrating backend systems engineering applicable to ML infrastructure and production API development."
