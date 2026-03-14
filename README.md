# Remote Executor

A simple service that executes shell commands on a remote executor (Docker container). Clients can submit commands with CPU requirements, track execution status, and retrieve output.

## Requirements

- Java 21+
- Maven
- Docker Desktop (or Docker Engine) with the daemon exposed on `tcp://localhost:2375` without TLS
  - Docker Desktop → Settings → General → Enable "Expose daemon on tcp://localhost:2375 without TLS"

## Run

**Option 1 — IntelliJ IDEA:**
Open the project and run `RemoteExecutorApplication.java`

**Option 2 — Maven wrapper:**

```bash
./mvnw spring-boot:run
```

**Option 3 — Maven:**

```bash
mvn spring-boot:run
```

Run from the project root in the host OS native shell (not WSL).

## API

### Submit a command

```
POST http://localhost:8080/execute
```

```json
{
  "command": "echo hello world",
  "cpuCount": 1
}
```

### Get job status

```
GET http://localhost:8080/execute/{id}/status
```

Returns: `QUEUED` / `IN_PROGRESS` / `FINISHED` / `FAILED`

### Get full job details

```
GET http://localhost:8080/execute/{id}
```

Returns the full job including command, cpuCount, status, and output.

## How it works

1. Client submits a command — service saves it as `QUEUED` and returns the job ID immediately
2. A background thread starts a Docker container with the specified CPU limit
3. The command runs inside the container — status updates to `IN_PROGRESS`
4. Output is captured from stdout and stderr
5. Container is removed after completion — status updates to `FINISHED`

## Notes

- Uses local Docker as the remote executor
- To use a remote Docker daemon, update `DOCKER_HOST` in the config to point to any remote machine running Docker (e.g. `tcp://your-server-ip:2375`)
- CPU count maps to Docker's `--cpus` flag
