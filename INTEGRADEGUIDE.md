# Notification Integration Guide

This document explains how to run, integrate, and consume the notification system built as two Spring Boot services:

- `notification-service` — stores notification requests, payloads, user status, and exposes APIs for creating and checking notifications.
- `notification-batch-service` — polls ready notifications, resolves placeholders, picks the matching provider, and sends notifications.

---

## 1. Project overview

### Services

| Service | Default port | Responsibility |
| --- | ---: | --- |
| `notification-service` | `8000` | Notification CRUD, scheduling, ready-state queries, status updates |
| `notification-batch-service` | `8082` | Polling job, provider dispatch, retry logic |

### Main flow

1. External system sends a notification request to `notification-service`.
2. The service saves the record and payload details in MySQL.
3. The batch service periodically calls the ready endpoint.
4. It reserves the notification, resolves payload templates, selects a provider, and sends it.
5. The service updates the final status as `SENT`, `RETRY`, or `FAILED`.

---

## 2. Prerequisites

Before integrating, make sure the following are installed:

- Java 17+
- Maven 3.8+
- MySQL 8+
- Optional: Postman / curl / Swagger / frontend client

Also create a database, for example:

```sql
CREATE DATABASE notificationdb;
```

If you use a different database name or credentials, update the environment variables in the application properties files.

---

## 3. Configuration

### 3.1 notification-service

File: `notification-service/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:notificationdb}?createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
server.port=8000
```

Recommended environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=notificationdb
export DB_USERNAME=root
export DB_PASSWORD=root
```

### 3.2 notification-batch-service

File: `notification-batch-service/src/main/resources/application.properties`

```properties
server.port=8082
notification.batch.interval=30000
notification.service.url=http://localhost:8000
```

The batch service also contains SMTP config for email delivery:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

> If Gmail is used, generate an App Password instead of using the normal account password.

---

## 4. Run the services

### Start notification-service

```bash
cd notification-service
mvn spring-boot:run
```

### Start notification-batch-service

```bash
cd notification-batch-service
mvn spring-boot:run
```

After startup, verify the services are listening:

- `http://localhost:8000` for notification-service
- `http://localhost:8082` for batch-service

---

## 5. Notification API contract

Base URL:

```text
http://localhost:8000/api/notifications
```

### 5.1 Create notification

Endpoint:

```http
POST /api/notifications
```

Request body example:

```json
{
  "source": "USER_APP",
  "eventType": "PASSWORD_RESET",
  "userId": 101,
  "recipient": "user@example.com",
  "notificationMode": "EMAIL",
  "scheduledDate": "2026-08-17T12:00:00",
  "priority": "HIGH",
  "maxRetryCount": 3,
  "payload": {
    "title": "Welcome {{firstName}}",
    "message": "Hello {{firstName}}, your password was reset successfully.",
    "firstName": "Amit",
    "recipient": "user@example.com"
  }
}
```

Important notes:

- `notificationMode` should be something supported by the provider factory (for example `EMAIL`).
- `payload` is stored dynamically and read later by the batch job.
- If `notificationMode` is `EMAIL` and `recipient` is missing, the system tries to read a fallback value from payload keys such as `recipient`, `recipientEmail`, `email`, or `to`.

### 5.2 Get single notification

```http
GET /api/notifications/{id}
```

### 5.3 Get notifications by user

```http
GET /api/notifications/user/{userId}
```

### 5.4 Get unread notifications

```http
GET /api/notifications/user/{userId}/unread
```

### 5.5 Mark as read

```http
PUT /api/notifications/{id}/read
```

### 5.6 Cancel notification

```http
PUT /api/notifications/{id}/cancel
```

### 5.7 Ready notifications for batch processing

```http
GET /api/notifications/ready
```

This is used internally by the batch service.

### 5.8 Reserve notification for processing

```http
PUT /api/notifications/{id}/reserve
```

The batch service reserves the notification so multiple workers do not process the same item concurrently.

### 5.9 Get payload details

```http
GET /api/notifications/{id}/payload
```

### 5.10 Update processing status

```http
PUT /api/notifications/{id}/status
```

Example body:

```json
{
  "status": "SENT",
  "retryIncrement": 1
}
```

---

## 6. Placeholder resolution

The batch service resolves values inside payload templates using a placeholder engine.

Example:

```json
{
  "title": "Hello {{firstName}}",
  "message": "Your order {{orderId}} has been shipped.",
  "firstName": "Riya",
  "orderId": "ORD-1045"
}
```

This results in:

```text
Hello Riya
Your order ORD-1045 has been shipped.
```

The system supports dynamic substitution for notification content before sending.

---

## 7. Batch job behavior

The scheduler runs on a fixed delay configured by:

```properties
notification.batch.interval=30000
```

This means the batch will check for ready notifications every 30 seconds by default.

Flow:

1. Fetch ready notifications from `GET /api/notifications/ready`
2. Try to reserve each one with `PUT /api/notifications/{id}/reserve`
3. Fetch payload details from `GET /api/notifications/{id}/payload`
4. Resolve placeholders in `title` and `message`
5. Detect provider from `notificationMode`
6. Send message using selected provider
7. Update status as `SENT` or `RETRY`

---

## 8. Supported provider model

The provider pattern is implemented by a factory. The batch service checks the value of `notificationMode` and selects the correct provider.

The current project includes provider-based dispatch for notifications, typically with email sending in the batch layer.

### Example provider selection

```java
String mode = n.getNotificationMode();
NotificationProvider provider = providerFactory.get(mode);
```

If no provider is found, the system sets status to `FAILED`.

---

## 9. Example integration requests

### cURL example

```bash
curl -X POST http://localhost:8000/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "source": "CRM",
    "eventType": "ORDER_CONFIRMATION",
    "userId": 42,
    "recipient": "customer@example.com",
    "notificationMode": "EMAIL",
    "scheduledDate": "2026-08-17T18:30:00",
    "priority": "NORMAL",
    "maxRetryCount": 3,
    "payload": {
      "title": "Order Confirmed",
      "message": "Hi {{firstName}}, your order {{orderId}} is confirmed.",
      "firstName": "Neha",
      "orderId": "ORD-9001"
    }
  }'
```

### Check status

```bash
curl http://localhost:8000/api/notifications/1
```

### Fetch ready notifications

```bash
curl http://localhost:8000/api/notifications/ready
```

---

## 10. Common integration patterns

### Pattern A: Send a notification from another service

Use the `notification-service` REST API as the source of truth and send notification requests from your app or gateway.

### Pattern B: Queue or event-driven trigger

Instead of direct REST calls, you can publish an event to Kafka or RabbitMQ, then have a small adapter service call the notification API.

### Pattern C: Use batch + provider selection

This project is designed for asynchronous processing. You create the notification and let the batch service handle actual sending.

---

## 11. Status lifecycle

The notification usually follows a flow similar to this:

```text
PENDING -> PROCESSING -> SENT
      \-> RETRY -> PROCESSING
      \-> FAILED
```

The exact status values depend on the flow implemented by the batch service and provider responses.

---

## 12. Troubleshooting

### Service fails to start

- Check Java version: `java -version`
- Check MySQL is running and credentials are valid
- Check if the selected database exists
- Review logs in the terminal for missing dependencies or configuration errors

### Email not sent

- Verify SMTP credentials and app password
- Ensure the correct Gmail settings are used
- Check `spring.mail.properties.mail.smtp.*` values

### Notification never processes

- Check if the notification status is `PENDING` or `RETRY`
- Confirm the batch service is running on `8082`
- Confirm `notification.service.url` points to the correct service URL
- Check the ready endpoint result manually

### Batch fails to reserve an item

- Another worker may already be processing the same record
- The record status may not match `PENDING` or `RETRY`
- Use the ready endpoint and inspect the item status manually

---

## 13. Security and production recommendations

Before using this in production, consider the following:

- Move credentials to environment variables or secrets manager
- Use TLS/HTTPS for external service communication
- Add rate limiting and throttling for notification APIs
- Add audit logs around notification creation and sending
- Add retry backoff and dead-letter handling for failed sends
- Validate payload data before processing
- Restrict direct database access and configure least-privilege users

---

## 14. Useful commands

```bash
# Build notification service
cd notification-service
mvn clean package

# Build batch service
cd ../notification-batch-service
mvn clean package

# Run service
mvn spring-boot:run
```

---

## 15. Summary

This system is designed to let other services integrate through a lightweight notification API and let the batch worker handle actual dispatch. The main integration point is the `POST /api/notifications` endpoint, with status monitoring and payload details available through the same service.

For most external integrations, you only need to:

1. Create a notification request with `source`, `eventType`, `userId`, `recipient`, `notificationMode`, and `payload`.
2. Let the batch service process it.
3. Poll status or read the record by ID when needed.

If you are integrating a new backend, keep the payload keys and the `notificationMode` values consistent with the provider implementation to avoid dispatch failures.
