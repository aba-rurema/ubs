# Utility Billing System (UBS)

## Overview

The Utility Billing System (UBS) is a Spring Boot-based backend application designed to automate the management of utility services, including water and electricity billing. The system provides secure user management, meter tracking, tariff configuration, bill generation, payment processing, and customer notifications.

This project implements role-based access control using JWT authentication and follows industry-standard backend development practices.

---

## Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Secure password encryption
- First-login password change support
- Automated user onboarding

### User Roles
- **ROLE_ADMIN**
  - Manage users
  - Configure tariffs
  - Manage customers
  - Manage meters
  - Approve bills

- **ROLE_OPERATOR**
  - Capture meter readings
  - View assigned operational data

- **ROLE_FINANCE**
  - Approve bills
  - Process payments
  - View financial reports

- **ROLE_CUSTOMER**
  - View bills
  - View payment history
  - View notifications

---

## System Modules

### Customer Management
- Register customers
- Update customer information
- Activate/Deactivate customers
- Prevent duplicate National IDs

### Meter Management
- Water meters
- Electricity meters
- Meter assignment to customers
- Meter status tracking

### Meter Reading Management
- Monthly reading capture
- Consumption calculation
- Reading validation
- One reading per meter per month

### Tariff Management
- Water tariffs
- Electricity tariffs
- Fixed service charges
- VAT configuration
- Late payment penalties
- Tariff versioning

### Billing Management
- Automated bill generation
- Consumption-based billing
- Due date management
- Bill approval workflow

### Payment Processing
- Partial payments
- Full payments
- Outstanding balance tracking
- Payment history

### Notification System
- Bill notifications
- Payment confirmations
- Role assignment notifications
- Email integration

---

## Technology Stack

### Backend
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate

### Database
- PostgreSQL

### Security
- JWT Authentication
- BCrypt Password Encoding

### Documentation
- Swagger/OpenAPI

### Build Tool
- Maven

---

## Database Entities

- User
- Customer
- Meter
- MeterReading
- Tariff
- Bill
- Payment
- Notification

---

## Main Workflow

Customer Registration
→ Authentication (JWT)
→ Meter Assignment
→ Tariff Configuration
→ Meter Reading Capture
→ Consumption Calculation
→ Bill Generation
→ Bill Approval
→ Customer Notification
→ Payment Processing
→ Balance Update
→ Payment Confirmation

---

## Business Rules

### Customer Rules
- National ID must be unique.
- Inactive customers cannot receive new bills.

### Meter Rules
- Meter number must be unique.
- Meter must be active before readings can be recorded.

### Reading Rules
- Current reading must be greater than previous reading.
- Only one reading per meter per month.

### Billing Rules
- Bills are generated from validated readings.
- Tariffs are applied based on meter type.
- New tariff versions only affect future bills.

### Payment Rules
- Partial payments are allowed.
- Full payment automatically marks bill as PAID.
- Outstanding balance is updated automatically.

---

## API Documentation

Swagger UI is available after starting the application:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Running the Application

### Clone Repository

```bash
git clone https://github.com/aba-rurema/ubs.git
cd ubs
```

### Configure Database

Update:

```properties
application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ubs
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Run Application

```bash
mvn spring-boot:run
```

or

```bash
./mvnw spring-boot:run
```

---

## Future Improvements

- SMS Notifications
- Mobile Application Integration
- Automated Meter Reading Integration
- Reporting Dashboard
- Analytics and Consumption Forecasting

---

## Author

Utility Billing System (UBS)

Spring Boot Backend Project
