# Khatabook Application

A digital ledger management system built with Java, Hibernate, and Jersey REST API.

## Features

- Organization Management
- Contact Management
- Expense Categories
- Transaction Tracking (Expenses and Give/Take)
- Comprehensive Reporting
- Firebase Authentication
- Modern UI with Tailwind CSS

## Project Structure

```
khatabook-app/
├── khatabook-parent/
│   ├── khatabook-core/       # Core business logic and data access
│   │   ├── src/main/java/
│   │   │   └── com/khatabook/core/
│   │   │       ├── config/   # Configuration classes
│   │   │       ├── model/    # Domain models
│   │   │       ├── repository/ # Data access layer
│   │   │       └── service/  # Business logic
│   │   └── src/main/resources/
│   │       ├── database.properties
│   │       ├── logback.xml
│   │       └── firebase-config-template.json
│   └── khatabook-web/        # Web layer and REST API
│       ├── src/main/java/
│       │   └── com/khatabook/web/
│       │       ├── config/   # Web configuration
│       │       ├── filter/   # Authentication filters
│       │       ├── resource/ # REST endpoints
│       │       └── exception/ # Exception handlers
│       └── src/main/webapp/  # Web resources
```

## Prerequisites

- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Firebase account for authentication

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/khatabook-app.git
   cd khatabook-app
   ```

2. Configure database connection:
   - Copy `database.properties.template` to `database.properties`
   - Update database connection details

3. Configure Firebase:
   - Create a Firebase project
   - Generate service account credentials
   - Copy `firebase-config-template.json` to `firebase-config.json`
   - Update with your Firebase credentials

4. Build the project:
   ```bash
   mvn clean install
   ```

5. Deploy to a web container (e.g., Tomcat)

## API Documentation

### Authentication

- POST `/api/auth/verify-token` - Verify Firebase ID token
- POST `/api/auth/refresh-token` - Refresh authentication token
- GET `/api/auth/status` - Check authentication status

### Organizations

- POST `/api/organizations` - Create organization
- GET `/api/organizations/{id}` - Get organization
- PUT `/api/organizations/{id}` - Update organization
- DELETE `/api/organizations/{id}` - Delete organization

### Contacts

- POST `/api/organizations/{orgId}/contacts` - Create contact
- GET `/api/organizations/{orgId}/contacts/{id}` - Get contact
- PUT `/api/organizations/{orgId}/contacts/{id}` - Update contact
- DELETE `/api/organizations/{orgId}/contacts/{id}` - Delete contact

### Expense Categories

- POST `/api/organizations/{orgId}/expense-categories` - Create category
- GET `/api/organizations/{orgId}/expense-categories/{id}` - Get category
- PUT `/api/organizations/{orgId}/expense-categories/{id}` - Update category
- DELETE `/api/organizations/{orgId}/expense-categories/{id}` - Delete category

### Transactions

- POST `/api/organizations/{orgId}/transactions/expenses` - Create expense
- POST `/api/organizations/{orgId}/transactions/give-take` - Create give/take
- GET `/api/organizations/{orgId}/transactions/{id}` - Get transaction
- DELETE `/api/organizations/{orgId}/transactions/{id}` - Delete transaction

### Reports

- GET `/api/organizations/{orgId}/reports/contact-balance-summary` - Get balance summary
- GET `/api/organizations/{orgId}/reports/contact-statement/{contactId}` - Get contact statement
- GET `/api/organizations/{orgId}/reports/expense-summary` - Get expense summary
- GET `/api/organizations/{orgId}/reports/period-wise-expense-summary` - Get period-wise summary

## Configuration

### Database Properties

Configure database connection in `database.properties`:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/khatabook
db.username=your_username
db.password=your_password
```

### Logging

Configure logging levels in `logback.xml`:
- Console logging for development
- File logging for production
- Separate error log file
- SQL query logging for debugging

## Security

- Firebase Authentication
- HTTPS enforcement
- CORS configuration
- Session management
- Input validation
- Error handling

## Development

1. Run in development mode:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=dev
   ```

2. Access the application:
   - Web UI: `http://localhost:8080`
   - API: `http://localhost:8080/api`

## Production Deployment

1. Build for production:
   ```bash
   mvn clean package -P prod
   ```

2. Deploy WAR file:
   - Copy `khatabook-web/target/khatabook-web.war` to your web container
   - Configure HTTPS
   - Set up proper database credentials
   - Configure logging paths

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
