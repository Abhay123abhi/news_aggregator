# News Aggregator

Full-stack news search application that aggregates The Guardian and New York Times APIs, caches successful searches in Redis, and serves a React user interface.

## Architecture

- **Backend:** Java 21, Spring Boot, OpenFeign, Redis, Actuator and OpenAPI
- **Frontend:** React served by NGINX; NGINX proxies API and WebSocket traffic to the backend
- **Deployment:** Docker images deployed to Kubernetes through Jenkins

## Local development

Prerequisites: Java 21, Node 20+, Redis, and API keys for both providers.

```bash
export GUARDIAN_API_KEY=your_guardian_key
export NYT_API_KEY=your_nyt_key
cd backend && ./gradlew bootRun
```

In a second terminal:

```bash
cd frontend
npm ci
REACT_APP_API_URL=http://localhost:8080 npm start
```

The API is available at `http://localhost:8080/api/news`. Swagger is disabled by default; set `SWAGGER_ENABLED=true` only in a trusted development environment.

## Kubernetes deployment

Create API secrets outside source control, then apply the manifests:

```bash
kubectl create secret generic api-secrets \\
  --from-literal=GUARDIAN_API_KEY=... \\
  --from-literal=NYT_API_KEY=...
kubectl apply -f k8s/
```

The Jenkins pipeline builds immutable, build-number-tagged images, deploys those exact tags, and waits for the rollout. Do not commit provider keys, kubeconfig files, or generated build directories.

## Operational notes

- Each upstream provider is isolated and times out after five seconds. A failed provider does not fail the full search.
- Redis cache entries expire after six hours; a cached response is used if all providers are unavailable.
- The backend exposes `health` and `info`; Kubernetes uses Actuator readiness and liveness probes.
- Set `CORS_ALLOWED_ORIGINS` explicitly for every deployed frontend origin.
