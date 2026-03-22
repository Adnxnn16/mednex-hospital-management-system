# MedNex Enterprise | Modern Hospital Management System

MedNex is an enterprise-grade, multi-tenant Hospital Management System (HMS) built with a focus on high-performance clinical logistics, robust security, and a premium Glassmorphism user experience.

---

## 🖼️ Product UI Showcase

A comprehensive visual guide to the MedNex Enterprise ecosystem.

### 1. Unified Access & Authentication

The entry point for all clinical staff. Secure, role-aware, and multi-tenant.

|          **Main Login Portal**          |          **Admin Access Portals**          |           **Doctor Access Portal**           |           **Nurse Access Portal**           |
| :-------------------------------------: | :----------------------------------------: | :------------------------------------------: | :-----------------------------------------: |
| ![Welcome Back](screenshots/logins/login_page.png) | ![Admin Sign-In](screenshots/logins/admin_signin.png) | ![Doctor Sign-In](screenshots/logins/doctor_signin.png) | ![Nurse Sign-In](screenshots/logins/nurse_signin.png) |

> [!NOTE]
> Integrated role-based authentication ensures that each user lands in their specific context immediately after sign-in.

---

### 2. Operational Dashboards

Domain-specific consoles providing real-time clinical logistics and analytics.

|                    **Admin Dashboard**                     |                     **Doctor Dashboard**                     |                    **Nurse Dashboard**                     |
| :--------------------------------------------------------: | :----------------------------------------------------------: | :--------------------------------------------------------: |
| ![Admin Dashboard](screenshots/dashboards/admin_dashboard.png) | ![Doctor Dashboard](screenshots/dashboards/doctor_dashboard.png) | ![Nurse Dashboard](screenshots/dashboards/nurse_dashboard.png) |

---

### 3. Enterprise Security (Keycloak)

MedNex uses Keycloak for robust identity and access management (IAM).

|                     **Security Gate**                      |                    **Enterprise Console**                     |
| :--------------------------------------------------------: | :-----------------------------------------------------------: |
| ![Keycloak Shield](screenshots/keycloak/keycloak_signin.png) | ![Keycloak Admin](screenshots/keycloak/keycloak_dashboard.png) |

---

## 🚀 Project Overview (Week 1 - Week 4)

This project has evolved from a basic multi-tenant foundation to a sophisticated clinical operational platform.

### Week 1: Enterprise Foundations

- **Multi-Tenant Architecture**: Design for isolated data storage per hospital unit.
- **Tech Stack**: Spring Boot (Java 21), Angular 17.
- **Infrastructure**: Containerized development with Docker Compose (Postgres, Redis, Keycloak).

### Week 2: Security & Clinical Records

- **Auth Hub**: Integration with **Keycloak** for OIDC/JWT based SSO.
- **Patient Registry**: Core demographic storage with data integrity constraints.
- **Multi-Tenant Isolation**: Row-level filtering and tenant-context headers.

### Week 3: Operational Logistics

- **Bed Management**: Real-time ward tracking with AVAILABLE/OCCUPIED/CLEANING states.
- **Scheduling**: Unified Appointment system with conflict detection.
- **Compliance**: Tamper-proof **Audit Log Terminal** for system-wide transparency.

### Week 4: Premium UX & Advanced Intake

- **Glassmorphism UI**: Complete visual overhaul using Tailwind CSS.
- **Advanced Admission Terminal**: High-fidelity 50-field intake form with structured medical history.
- **Domain-Specific Dashboards**: Custom interfaces for **Admin**, **Doctor**, and **Nurse** roles.

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.2, Hibernate (JSONB support), PostgreSQL.
- **Frontend**: Angular 17, Tailwind CSS, FullCalendar, Chart.js.
- **Security**: Keycloak (OIDC), Role-Based Access Control (RBAC).

## 🚦 Getting Started

### Prerequisites

> [!IMPORTANT]
> **Ensure your Docker Desktop / Docker Engine is running** before executing any commands below.

- **Docker Desktop** (Required for Database and Keycloak)
- **JDK 21** (Required for Backend)
- **Node.js 20+** (Required for Frontend)

### 1) Install Frontend Dependencies

```powershell
cd frontend
npm install
```

### 2) Build Frontend (Production Check)

```powershell
npx ng build --configuration production
```

Expected output includes:

```text
Application bundle generation complete.
```

### 3) Run Backend Locally (Optional, outside Docker)

Open a new terminal:

```powershell
cd backend
.\mvnw.cmd clean spring-boot:run "-Dspring-boot.run.profiles=local"
```

Health check (another terminal):

```powershell
curl.exe -s http://localhost:8081/actuator/health/readiness
```

Expected:

```json
{ "status": "UP" }
```

Stop local backend with `Ctrl + C` before starting Docker backend.

### 4) Run Full Stack with Docker

From project root:

```powershell
docker-compose down -v
docker-compose up --build -d
docker-compose ps
```

Expected services:

- `mednex-postgres-1` healthy
- `mednex-redis-1` healthy
- `mednex-keycloak-1` healthy
- `mednex-backend-1` healthy
- `mednex-frontend-1` healthy

### 5) Run Backend Tests

```powershell
cd backend
.\mvnw.cmd test "-Dspring.profiles.active=test"
```

Expected output includes:

```text
BUILD SUCCESS
```

### Step-by-Step Execution

1.  **Start Infrastructure:** `docker-compose up -d postgres keycloak` (Start Docker Engine first!)
2.  **Start Backend:** `cd backend` then `./mvnw spring-boot:run`
3.  **Start Frontend:** `cd frontend` then `npm install` and `npm start`

---

## 🔐 Role-Specific Credentials

Use these credentials at the unified login portal to access role-specific dashboards:

| Role       | Username  | Password    | Dashboard Features                                     |
| :--------- | :-------- | :---------- | :----------------------------------------------------- |
| **Admin**  | `admin1`  | `admin123`  | Hospital analytics, inventory, and bed occupancy.      |
| **Doctor** | `doctor1` | `doctor123` | Patient records, clinical schedule, and consultations. |
| **Nurse**  | `nurse1`  | `nurse123`  | Ward logs, medication tracking, and vitals.            |

### Keycloak System Access

- **Admin Console**: [http://localhost:8080/admin](http://localhost:8080/admin)
- **Login**: `admin` / `admin`

---

_Created for the MedNex Enterprise Final Review Session._

```

```
