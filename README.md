Notification System (two services)


Pradeep Sankonatti is Software Engineering


Services:
- notification-service (port 8081)
- notification-batch-service (port 8082)

Quick start (requires Java 17 + Maven):
1. Start notification-service:
   cd notification-service
   mvn spring-boot:run

2. Start notification-batch-service:
   cd ../notification-batch-service
   mvn spring-boot:run

Demo flow:
- POST /api/notifications to notification-service to create a notification with payload keys like title and message.
- The batch runs every notification.batch.interval ms (default 30000) and calls notification-service /api/notifications/ready to fetch ready notifications.
- Batch reserves, fetches payload, resolves placeholders, selects provider and "sends" (Email provider prints to console), then updates status to SENT or RETRY.

See each project's README for more details.
