# ChronoAge - Java Spring Boot Age Calculator SPA & Huawei Cloud DevSecOps

A modern, production-ready Single Page Application (SPA) for high-precision age calculation and life analytics, built with **Java 21**, **Spring Boot 3**, and packaged with enterprise DevSecOps standards for **Huawei Cloud CodeArts** and **SWR (Software Repository for Container)**.

---

## 📁 Repository Tree Structure

```text
agecalculator java/
├── .dockerignore                            # Excludes unnecessary files from container context
├── .gitignore                               # Git ignored files & build artifacts
├── .gitleaks.toml                           # Gitleaks security rules for CI/CD secret scanning
├── Dockerfile                               # Multi-stage, non-root, cached Docker build
├── huaweicloud-codearts-pipeline.md         # Step-by-step Huawei Cloud CodeArts GUI guide
├── pom.xml                                  # Maven project descriptor (Java 21, Spring Boot 3.3.4)
├── README.md                                # Project documentation
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── example/
    │   │           └── agecalculator/
    │   │               ├── AgeCalculatorApplication.java     # Application entrypoint
    │   │               ├── controller/
    │   │               │   └── AgeApiController.java         # REST endpoints (/api/calculate, /api/health)
    │   │               ├── model/
    │   │               │   ├── AgeCalculationRequest.java    # Request payload model
    │   │               │   └── AgeCalculationResponse.java   # Response payload model
    │   │               └── service/
    │   │                   └── AgeCalculatorService.java     # Age & astrological calculation engine
    │   └── resources/
    │       ├── application.yml              # Server & static resource configuration
    │       └── static/
    │           └── index.html               # SPA frontend (Embedded CSS/JS, Dark/Light Mode)
    └── test/
        └── java/
            └── com/
                └── example/
                    └── agecalculator/
                        └── AgeCalculatorApplicationTests.java # MockMvc & Service unit tests
```

---

## 🚀 Key Highlights & Architectural Features

### 1. Technology Stack
- **Backend:** Java 21 LTS with Spring Boot 3.3.4 (`spring-boot-starter-web`).
- **Frontend:** Single `index.html` static file featuring:
  - Glassmorphic modern dark/light UI.
  - Live seconds/minutes precision ticker.
  - Exact Years, Months, and Days calculation.
  - Astrological Zodiac and Chinese Zodiac profiling.
  - Next birthday countdown and yearly progress milestone bar.
  - Cumulative analytics (Total Days, Hours, Minutes, Heartbeat estimates).
- **REST Endpoints:**
  - `GET /api/calculate?birthDate=YYYY-MM-DD[&targetDate=YYYY-MM-DD]`
  - `POST /api/calculate` with JSON body `{"birthDate": "YYYY-MM-DD", "targetDate": "YYYY-MM-DD"}`
  - `GET /api/health`

### 2. Multi-Stage Dockerfile Security
- **Stage 1 (Builder):** `maven:3.9.9-eclipse-temurin-21-alpine` caches dependencies via `mvn dependency:go-offline` before copying application code.
- **Stage 2 (Runtime):** `eclipse-temurin:21-jre-alpine` delivers an ultra-slim runtime surface.
- **Non-Root Execution:** Dedicated system user `appuser` (UID: 10001) and group `appgroup` (GID: 10001).
- **Container Tuned JVM:** Configured with `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`.
- **Healthcheck Probe:** Built-in container health checking on `/api/health`.

### 3. DevSecOps for Huawei Cloud CodeArts & SWR
- Comprehensive step-by-step GUI guide in [`huaweicloud-codearts-pipeline.md`](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/huaweicloud-codearts-pipeline.md).
- Covers **CodeArts Repo**, **CodeArts Check** (SAST), **Gitleaks** secret scanning, **CodeArts Build** Maven packaging, and native image push to **Huawei Cloud SWR**.

---

## 💻 Local Development & Build

### Prerequisites
- JDK 21 (or 17)
- Maven 3.8+ (or run via Docker)

### Run with Maven
```bash
# Compile and run locally
mvn spring-boot:run
```
Visit `http://localhost:8080` in your web browser.

### Run Unit Tests
```bash
mvn clean test
```

---

## 🐳 Docker Build & Execution

### Build Container Image
```bash
docker build -t age-calculator:1.0.0 .
```

### Run Container as Non-Root User
```bash
docker run -d --name age-calc -p 8080:8080 age-calculator:1.0.0
```

Verify non-root user execution:
```bash
docker exec -it age-calc id
# Output: uid=10001(appuser) gid=10001(appgroup)
```

---

## ☁️ Huawei Cloud CodeArts Pipeline

Refer to the full deployment documentation in [huaweicloud-codearts-pipeline.md](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/huaweicloud-codearts-pipeline.md).
