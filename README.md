# Workspace Management SaaS Backend

A multi-tenant Workspace Management SaaS Backend built with Spring Boot that enables organizations to manage users, projects, tasks, and team workflows securely.

The system provides role-based access control, tenant isolation, project tracking, dashboard analytics, Redis caching, email notifications, Docker support, and AI-powered project risk analysis.

---

## Features

* JWT Authentication & Authorization
* Role-Based Access Control (RBAC)
* Multi-Tenant Architecture
* Organization Management
* User Management
* Project Management
* Task Management
* Dashboard Analytics
* Redis Caching
* Email Notifications
* AI Project Risk Analysis
* Swagger/OpenAPI Documentation
* Docker Support
* Unit Testing with JUnit & Mockito

---

## Tech Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Security
* Spring Data MongoDB
* Spring Cache
* Spring Mail

### Database & Cache

* MongoDB
* Redis

### Authentication

* JWT (JSON Web Token)

### Documentation

* Swagger / OpenAPI

### DevOps

* Docker
* Docker Compose
* Maven

### Testing

* JUnit 5
* Mockito

---

## Architecture

### Role Hierarchy

```text
SUPER_ADMIN
 └── ORG_ADMIN
      └── MANAGER
           └── EMPLOYEE
```

### Multi-Tenant Design

* Each organization is assigned a unique Tenant ID.
* Data access is restricted based on tenant boundaries.
* Role-based authorization is enforced throughout the application.
* Project and task operations are validated against organization ownership.

---

## Modules

### Authentication & Authorization

* User Login
* JWT Token Generation
* Role-Based Access Control

### Organization Management

* Create Organization
* Activate / Deactivate Organization
* Organization Isolation

### User Management

* Create Users
* User Activation / Deactivation
* Organization Membership Management

### Project Management

* Create Projects
* Update Projects
* Cancel Projects
* Project Progress Tracking

### Task Management

* Create Tasks
* Assign Tasks
* Reassign Tasks
* Task Status Management
* Priority Management

### Dashboard & Analytics

* Dashboard Summary
* Project Progress Overview
* Organization Metrics

### AI Risk Analysis

* Project Risk Evaluation
* Progress Analysis
* Overdue Task Detection
* Risk Recommendations

### Notifications

* Task Assignment Emails
* Task Reassignment Emails
* Task Completion Notifications

---

## Security Features

* JWT-based Authentication
* Role-Based Authorization
* Tenant Isolation
* Organization Boundary Validation
* Project Ownership Validation
* Active User Verification
* Environment Variable Based Secret Management

---

## Running Locally

### Clone Repository

```bash
git clone https://github.com/Palak153/workspace-management-.git
cd workspace-management
```

### Configure Environment Variables

Create a file named:

```text
secrets.env
```

Example:

```env
MONGODB_URI=
REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=
MAIL_USERNAME=
MAIL_PASSWORD=
JWT_SECRET=
API_KEY=
```

### Run Application

```bash
mvn spring-boot:run
```

---

## Docker

### Build Docker Image

```bash
docker build -t workspace-app .
```

### Run Docker Container

```bash
docker run --env-file secrets.env -p 8080:8080 workspace-app
```

### Docker Compose

```bash
docker-compose up -d
```

---

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI Docs:

```text
http://localhost:8080/v3/api-docs
```

---

## Testing

Run all tests:

```bash
mvn test
```

### Tested Services

* UserService
* ProjectService
* TaskService

### Testing Tools

* JUnit 5
* Mockito

---

## Deployment

Live URL: Coming Soon

Swagger URL: Coming Soon

---

## Future Enhancements

* Slack / Microsoft Teams Notifications
* GitHub Actions CI/CD Pipeline
* Kubernetes Deployment
* AI-Based Task Prioritization
* AI Project Forecasting
* Advanced Reporting & Insights

---

## Author

**Palak Jain**

B.Tech Computer Science Engineering

Spring Boot | Java | MongoDB | Redis | Docker
