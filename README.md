News Aggregator Microservice

A **production-ready, full-stack News Aggregator application** that fetches, normalizes, and displays the latest news from multiple public APIs — **The Guardian** and **The New York Times** — based on user search keywords.

The application demonstrates:
- Aggregation and normalization from multiple APIs  
- Duplicate elimination and unified pagination  
- Offline caching (no DB, only JSON cache)  
- SOLID, 12-Factor, and HATEOAS design principles  
- CI/CD automation with Jenkins  
- Containerization with Docker  
- OpenAPI documentation via Swagger  

---

# Architecture Overview ( High Level Flow )

1. **Frontend (React / HTML Client)**  
   - Displays content cards with title, description, URL, and publication date.  
   - Supports pagination and "Read More" links.  
   - Supports prev and next page along with total pages and serach keyword.

2. **Backend (Spring Boot Service)**  
   - Exposes a unified `/news/search?query={keyword}&page={page}` endpoint.  
   - Internally aggregates results from:
     - *New York Times API*
     - *The Guardian API*
   - Merges, normalizes, and removes duplicates based on URL and title similarity.
   - Returns combined, paginated results to frontend.

3. **Cache Service (Offline Mode)**  
   - Saves API responses as JSON files on disk for offline availability.  
   - Automatically deletes stale cache entries older than 1 hour.  
   - Used when external APIs are unavailable.

4. **CI/CD Pipeline (Jenkins)**  
   - Automatically clones the GitHub repository.  
   - Builds the project using Maven.  
   - Builds and pushes Docker images to a container registry.  
   - Deploys the container locally.

5. **Docker Container**  
   - Runs the Spring Boot backend (port `8080`).  
   - Can be executed on any machine with Docker installed.  

---

## Sequence / Architecture Diagram

<img width="1183" height="538" alt="image" src="https://github.com/user-attachments/assets/0b8e7c28-5578-4064-aacc-1f9820240d8c" />

## Design Patterns 
   - Aggregator Pattern : Combines multiple API responses into a unified structure.
   - Strategy Pattern : Each API client (NYT, Guardian) implements a common strategy interface for fetching news. 
   - Decorator Pattern : Adds caching layer (CacheService) without modifying core aggregation logic. 
   - Singleton Pattern : Used for managing the in-memory cache manager and thread pool (`ExecutorService`).

## Key Features

  - **Keyword-based search**  
  - **Pagination support** (default: 10 results per page)
  - **Parallel API calls** via `ExecutorService`
  - **Duplicate elimination**
  - **Offline mode with JSON caching**
  - **API keys encrypted using application properties**
  - **Swagger UI for API documentation**
  - **Dockerized build and deploy**
  - **Jenkins and Git for CI/CD**

## Setup & Run Instructions

   git clone https://github.com/Abhay123abhi/news-aggregator.git
   cd news-aggregator

   **Configure API Keys**
   guardian.api.key=YOUR_GUARDIAN_KEY
   nyt.api.key=YOUR_NYT_KEY

   **Build and Run Locally**
   mvn clean install
   mvn spring-boot:run

<img width="1883" height="865" alt="image" src="https://github.com/user-attachments/assets/66db8156-5d27-49dc-88d2-209f4a4d6a3b" />


   Service will be available at:
   http://localhost:8080

   **Docker Instructions**
   docker build -t news-aggregator:latest.
   docker run -d -p 8080:8080 --name news-aggregator news-aggregator:latest
