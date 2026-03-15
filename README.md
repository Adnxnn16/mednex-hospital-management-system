# MedNex Enterprise | Modern Hospital Management System

MedNex is an enterprise-grade, multi-tenant Hospital Management System (HMS) built with a focus on high-performance clinical logistics, robust security, and a premium Glassmorphism user experience.

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

- Docker & Docker Compose
- JDK 21+
- Node.js 20+

### Setup

1. **Infrastructure**:
   ```bash
   docker-compose up -d
   ```
2. **Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

---

_Created for the MedNex Enterprise Development Review Session._
