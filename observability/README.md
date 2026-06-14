# KT4 observability

The Docker Compose stack provisions:

- Grafana with Prometheus, Loki, and Jaeger datasources
- Prometheus scraping Node Exporter and cAdvisor
- Promtail collecting Docker logs through the Docker socket
- OpenTelemetry tracing from ApiGateway to StakeholdersService through Jaeger

Start everything from the repository root:

```powershell
docker compose up --build
```

Useful URLs:

- Application gateway: http://localhost:5000
- Grafana: http://localhost:3000 (`admin` / `admin`)
- Prometheus: http://localhost:9090
- Jaeger: http://localhost:16686
- Loki readiness: http://localhost:3100/ready
- cAdvisor: http://localhost:8081

Generate a distributed trace:

```powershell
Invoke-RestMethod http://localhost:5000/api/users
```

In Jaeger, select `api-gateway`, click **Find Traces**, and open the `GET
/api/users` trace. It should contain spans from both `api-gateway` and
`stakeholders-service`.

In Grafana Explore, select Loki and query `{service="api-gateway"}` or
`{service="stakeholders-service"}`. The provisioned `SOA KT4` folder contains
the host and container metrics dashboards.

On Docker Desktop for Windows, Node Exporter reports the Linux VM that hosts
the containers, not the physical Windows host operating system.
