# Huawei Cloud CodeArts DevSecOps CI/CD Pipeline Guide

This guide provides end-to-end instructions for deploying the **Age Calculator** Single Page Application using **Huawei Cloud CodeArts** and **Huawei Cloud SWR (Software Repository for Container)**.

---

## 1. Architecture & Pipeline Workflow

```mermaid
flowchart LR
    A[CodeArts Repo\nGit Push] --> B[CodeArts Check & Gitleaks\nSAST + Secret Scan]
    B --> C[CodeArts Build\nMaven clean package & Test]
    C --> D[CodeArts Build Native SWR Task\nDocker Multi-stage Build]
    D --> E[Huawei Cloud SWR\nPrivate Container Registry]
    E -.-> F[Huawei Cloud CCE / CCI\nKubernetes Deployment]
```

---

## 2. Prerequisites on Huawei Cloud

1. **Huawei Cloud Account & IAM:**
   - Ensure your IAM user has permissions for **CodeArts** (Project Manager or Developer role) and **SWR** (Admin or Editor).
2. **Create SWR Organization:**
   - Navigate to the **Huawei Cloud Management Console** &rarr; Search for **Software Repository for Container (SWR)**.
   - In the left sidebar, click **Organization Management** &rarr; **Create Organization**.
   - Name your organization (e.g., `devsecops-org`).
   - Note your regional SWR endpoint:
     - *AP-Singapore:* `swr.ap-southeast-3.myhuaweicloud.com`
     - *CN-Hong Kong:* `swr.ap-southeast-1.myhuaweicloud.com`
     - *CN-North-Beijing4:* `swr.cn-north-4.myhuaweicloud.com`
     - *EU-Paris / Johannesburg / Santiago:* Check your active region endpoint.

---

## 3. Step-by-Step CodeArts GUI Configuration

### Step 1: Push Code to CodeArts Repo
1. Open **CodeArts** console &rarr; Select or create a project (e.g., `AgeCalculator-Project`).
2. Go to **Code** &rarr; **CodeArts Repo**.
3. Click **Create Repository** (or **Import Repository** from GitHub/GitLab).
4. Push this repository codebase into the `main` branch:
   ```bash
   git init
   git add .
   git commit -m "feat: initial commit with Spring Boot SPA and Dockerfile"
   git remote add origin <YOUR_CODEARTS_REPO_HTTPS_URL>
   git push -u origin main
   ```

---

### Step 2: Configure CodeArts Check (SAST & Secret Detection)
Huawei Cloud provides native **CodeArts Check** for deep code quality, security rulesets, and vulnerability detection.

1. In your CodeArts project, go to **Code** &rarr; **CodeArts Check**.
2. Click **Create Task** &rarr; Select your repository (`age-calculator`) and branch (`main`).
3. Under **Rule Sets**:
   - Check **Java Standard Ruleset** and **OWASP Top 10 Security Ruleset**.
4. To integrate **Gitleaks** (Secrets Scanning) and custom **Semgrep** rules:
   - In CodeArts Build (described below), you can also execute a security script task before compilation.

---

### Step 3: Configure CodeArts Build Task
Navigate to **CI/CD** &rarr; **CodeArts Build** &rarr; Click **Create Task**.
Select **Custom Template** or **Maven Template**.

#### Substep 3.1: Code Checkout
- The checkout step is automatically added by CodeArts Build when bound to the repository and branch `main`.

#### Substep 3.2: Security & Secret Scanning (Shell Task)
Add an action: **Execute Shell Command**:
```bash
echo "=== Step 2.1: Running Gitleaks Secrets Scanning ==="
# Download and execute gitleaks binary
wget -q https://github.com/gitleaks/gitleaks/releases/download/v8.18.2/gitleaks_8.18.2_linux_x64.tar.gz
tar -xzf gitleaks_8.18.2_linux_x64.tar.gz
./gitleaks detect --source=. --config=.gitleaks.toml --verbose --redact

echo "=== Step 2.2: Running SAST Check ==="
echo "CodeArts Check executes natively via CodeArts Check task, or run Semgrep via CLI"
```

#### Substep 3.3: Maven Compilation & Unit Testing
Add the native action: **Build with Maven**:
- **Tool Version:** `Maven 3.9` & `OpenJDK 21` (or `OpenJDK 17`).
- **Command:**
  ```bash
  mvn clean package -DskipTests=false
  ```
- **Settings:** Use the default or enterprise internal mirror settings.
- **Unit Test Coverage:** Toggle **Enable Test Results Parsing** (pattern: `**/surefire-reports/*.xml`).

---

### Step 4: Docker Build & Push to Huawei Cloud SWR
In CodeArts Build, add the official native task: **"Build an Image and Push It to SWR"** (or **"Docker Build and Push"**).

Fill in the task GUI fields:
1. **Docker Connection / Authentication:**
   - Select **Current Account / Integrated Service Endpoint** (CodeArts automatically authenticates to your SWR in the same region without hardcoding passwords).
2. **Organization Name:**
   - Select the organization you created in SWR (e.g., `devsecops-org`).
3. **Image Name:**
   - Set to `age-calculator`.
4. **Image Tag:**
   - Set to `${BUILD_NUMBER}` (or `${BUILD_TAG}`).
   - Check **Also push as `latest` tag**.
5. **Context Path:**
   - Set to `.` (workspace root).
6. **Dockerfile Path:**
   - Set to `./Dockerfile`.

---

## 4. Complete CodeArts Build Configuration (`build.yml`)

For teams using Configuration-as-Code in Huawei Cloud CodeArts, you can save this configuration as the pipeline build definition:

```yaml
version: "2.0"
steps:
  - name: Code Checkout
    action: checkout
    inputs:
      scm: codearts_repo
      branch: main

  - name: Secret Scan (Gitleaks)
    action: shell
    inputs:
      commands:
        - wget -q https://github.com/gitleaks/gitleaks/releases/download/v8.18.2/gitleaks_8.18.2_linux_x64.tar.gz
        - tar -xzf gitleaks_8.18.2_linux_x64.tar.gz
        - ./gitleaks detect --source=. --config=.gitleaks.toml --verbose

  - name: Maven Build & Unit Test
    action: maven
    inputs:
      jdk_version: "21"
      maven_version: "3.9"
      commands:
        - mvn clean package -DskipTests=false

  - name: Build and Push Docker Image to SWR
    action: swr_push
    inputs:
      swr_organization: "devsecops-org"
      image_name: "age-calculator"
      image_tag: "${BUILD_NUMBER}"
      dockerfile_path: "./Dockerfile"
      context_path: "."
      push_latest: true
```

---

## 5. Setting up the Full CodeArts Pipeline (Visual Orchestration)

To tie **CodeArts Check**, **CodeArts Build**, and **CodeArts Deploy to CCE** together:

1. In CodeArts, navigate to **CI/CD** &rarr; **CodeArts Pipeline**.
2. Click **Create Pipeline** &rarr; **Blank Template**.
3. Define the stages:
   - **Stage 1 (Code Quality & Security):** Add task &rarr; **CodeArts Check**. Set quality gate to block pipeline on Critical issues.
   - **Stage 2 (CI Build & Registry Push):** Add task &rarr; Link the **CodeArts Build Task** created in Step 3.
   - **Stage 3 (Deploy to CCE):** Add task &rarr; Link the **CodeArts Deploy Task** (described below) targeting your Huawei Cloud CCE cluster using the SWR image tag `${BUILD_NUMBER}`.
4. Set **Triggers**:
   - Check **Code Commit Trigger** on branch `main` to achieve true continuous integration and continuous deployment.

---

## 6. Configuring Huawei Cloud CCE & CodeArts Deploy

### Step 6.1: Create CCE Service Connection in CodeArts
1. In your CodeArts project, go to **Settings** &rarr; **General** &rarr; **Service Endpoints** (or **External Connections**).
2. Click **Create Service Endpoint** &rarr; Select **Kubernetes (CCE)**.
3. Fill in:
   - **Endpoint Name:** e.g., `cce-prod-connection`.
   - **Authentication Mode:** Select **Huawei Cloud Account Token** or **KubeConfig**.
   - **Cluster:** Select your Huawei Cloud CCE Cluster (e.g. `cce-prod-cluster`).
4. Click **Authorize and Save**.

### Step 6.2: Create CodeArts Deploy Task
1. Navigate to **CI/CD** &rarr; **CodeArts Deploy** &rarr; Click **Create Task**.
2. Select **Kubernetes / CCE Deployment Template** (or Custom Blank).
3. Add Step: **Deploy Kubernetes Manifests**:
   - **Service Connection:** Select `cce-prod-connection`.
   - **Manifest Source:** Workspace repository (`k8s/` directory).
   - **Namespace:** `age-calculator`.
   - **Image Replacement:**
     ```
     swr.ap-southeast-3.myhuaweicloud.com/devsecops-org/age-calculator:latest -> swr.${SWR_REGION}.myhuaweicloud.com/${SWR_ORG}/age-calculator:${BUILD_NUMBER}
     ```
4. Alternatively, use **Execute Shell Task**:
   ```bash
   export SWR_REGION="ap-southeast-3"
   export SWR_ORG="devsecops-org"
   export IMAGE_TAG="${BUILD_NUMBER}"
   chmod +x ./k8s/deploy.sh
   ./k8s/deploy.sh
   ```

---

## 7. CCE Kubernetes Manifests Overview (`k8s/`)

The repository includes complete production-grade manifests in the `k8s/` folder:

| Manifest | Purpose & Features |
| :--- | :--- |
| [00-namespace.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/00-namespace.yaml) | Isolated `age-calculator` namespace with standard labels |
| [01-configmap.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/01-configmap.yaml) | Container JVM flags (`-XX:+UseContainerSupport`, `MaxRAMPercentage`), profile & port configs |
| [02-deployment.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/02-deployment.yaml) | 2 replicas, RollingUpdate, Non-root security context (`10001:10001`), Startup/Liveness/Readiness probes, TopologySpread across AZs, SWR `default-secret` |
| [03-service.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/03-service.yaml) | Internal `ClusterIP` Service exposing port 80 &rarr; 8080 |
| [03-service-elb.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/03-service-elb.yaml) | Huawei Cloud ELB `LoadBalancer` Service with auto-provisioning annotations (`kubernetes.io/elb.class: union`) |
| [04-ingress.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/04-ingress.yaml) | Huawei Cloud CCE Ingress resource (`kubernetes.io/ingress.class: cce`) routing HTTP traffic |
| [05-hpa.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/05-hpa.yaml) | Horizontal Pod Autoscaler (HPA v2) scaling 2 &rarr; 10 pods on CPU (75%) & Memory (80%) |
| [06-pdb.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/06-pdb.yaml) | PodDisruptionBudget ensuring high availability during node maintenance / draining |
| [kustomization.yaml](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/kustomization.yaml) | Kustomize resource index for seamless image tag injection in CI/CD |
| [deploy.sh](file:///c:/Users/SystemBus/Desktop/agecalculator%20java/k8s/deploy.sh) | Automated bash deployment script for CodeArts Deploy / CLI with rollout verification |

---

## 8. Verification & Accessing Your Service

1. **Check Deployment and Pods:**
   ```bash
   kubectl get pods -n age-calculator
   kubectl get deployment age-calculator-deployment -n age-calculator
   ```
2. **Check ELB Public IP:**
   ```bash
   kubectl get svc age-calculator-elb-service -n age-calculator
   ```
   Look at the `EXTERNAL-IP` column. Once provisioned by Huawei Cloud ELB, access:
   `http://<EXTERNAL-IP>/`
3. **Verify Health Endpoint:**
   ```bash
   curl http://<EXTERNAL-IP>/api/health
   # Expected response: {"service":"age-calculator","status":"UP"}
   ```
