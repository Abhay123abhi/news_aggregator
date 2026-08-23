# News Aggregator

Full-stack news search application that aggregates The Guardian and New York Times APIs, caches successful searches in Redis, and serves a React user interface.

## Architecture

- **Backend:** Java 21, Spring Boot, OpenFeign, Redis, Actuator and OpenAPI
- **Frontend:** React static site with same-origin API routing
- **Deployment:** Render static site, Docker-based Spring Boot web service, and Redis-compatible Render Key Value
- **CI:** Optional Jenkins pipeline validates backend tests and frontend production builds

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
npm start
```

The API is available at `http://localhost:8080/api/news`. The frontend always calls the relative `/api/news` path: React's development proxy forwards requests to `localhost:8080`, while Render rewrites production requests to the public backend service. No frontend API environment variable is required. Swagger is disabled by default; set `SWAGGER_ENABLED=true` only in a trusted development environment.

## Frontend structure

```text
frontend/src/
├── App.js                 # application shell and page state
├── App.css                # global theme and layout tokens
└── features/news/
    ├── api/               # backend client
    └── components/        # search, result and UI states
```

The UI stores the selected light/dark theme locally, uses accessible labels and focus states, provides loading skeletons, retry and empty states, and remains responsive down to mobile widths.

## Render deployment

The root `render.yaml` Blueprint defines three services:

1. `abhay123abhi-news-web`: a free React static site.
2. `abhay123abhi-news-api`: a free Java 21 Docker web service.
3. `abhay123abhi-news-cache`: a free Redis-compatible Key Value instance.

In the Render Dashboard, create a new Blueprint, connect this repository, and enter `GUARDIAN_API_KEY` and `NYT_API_KEY` when prompted. Render creates the cache connection automatically and deploys new commits after the pull request is merged.

Free backend services sleep after inactivity and can take around one minute to wake. Free Key Value instances are in-memory only, which is acceptable because news results are a disposable cache.

## Operational notes

- Each upstream provider is isolated and times out after five seconds. A failed provider does not fail the full search.
- Redis cache entries expire after six hours; a cached response is used if all providers are unavailable.
- The backend exposes `health` and `info`; Render checks `/actuator/health`.
- Set `CORS_ALLOWED_ORIGINS` explicitly for every deployed frontend origin.
