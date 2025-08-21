# GitHub Finder API

A Spring Boot application that integrates with the **GitHub REST API** to search for repositories, store them in a local database, and expose RESTful APIs to query stored repositories with filters.  

This project also ensures that repository records are **updated instead of duplicated** when they already exist in the database.

---

## 🚀 Features

1. **Search GitHub Repositories**
   - Endpoint: `POST /api/github/search`
   - Fetches repositories from GitHub API based on query parameters.
   - Stores results in the local database.
   - Updates existing repositories if already present (no duplicates).
   - Returns a JSON response with repositories and metadata.

2. **Retrieve Stored Repositories**
   - Endpoint: `GET /api/github/repositories`
   - Retrieve stored results with optional filters:
     - `language` → filter by programming language.
     - `minStars` → filter by minimum star count.
     - `sort` → sort by `stars`, `forks`, or `updated`.
   - Returns repositories in JSON format with count and status messages.

3. **Error Handling**
   - Handles GitHub API failures gracefully.
   - Responds with custom error messages without crashing the API.

---

## ⚙️ Tech Stack

- **Java 17+**
- **Spring Boot 3+**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL / H2 (for local development)**
- **JUnit 5 + Mockito (for testing)**

---

## 🛠️ Setup Instructions

### 1. Clone the repository
```bash
git clone https://github.com/ArnabDh/Github-Finder.git
cd Github-Finder
```

### 2. Configure Database
Update `application.properties` (or `application.yml`) with your DB credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/github_finder
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

For quick testing, you can also use **H2 Database**:
```properties
spring.datasource.url=jdbc:h2:mem:githubdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
```

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## 📡 API Endpoints

### 🔎 1. Search GitHub Repositories
**POST** `/api/github/search`

#### Request Body:
```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

#### Response:
```json
{
  "status": "success",
  "message": "Repositories fetched successfully",
  "count": 2,
  "repositories": [
    {
      "id": 12345,
      "name": "spring-boot",
      "owner": "spring-projects",
      "description": "Spring Boot makes it easy to create stand-alone applications",
      "language": "Java",
      "stars": 60000,
      "forks": 30000,
      "lastUpdated": "2025-08-21T10:00:00Z"
    }
  ]
}
```

---

### 📂 2. Retrieve Stored Repositories
**GET** `/api/github/repositories?language=Java&minStars=100&sort=stars`

#### Response:
```json
{
  "status": "success",
  "message": "Repositories retrieved successfully",
  "count": 1,
  "repositories": [
    {
      "id": 12345,
      "name": "spring-boot",
      "owner": "spring-projects",
      "language": "Java",
      "stars": 60000,
      "forks": 30000,
      "lastUpdated": "2025-08-21T10:00:00Z"
    }
  ]
}
```

---

## 🧪 Testing

The project uses **JUnit 5 + Mockito** for testing.

### Running All Tests
```bash
mvn test
```

### Example Test Cases

#### `RepositoryServiceTest`
- Should fetch repositories from GitHub API.
- Should handle errors gracefully when GitHub API fails.
- Should update existing repository instead of duplicating.

#### `RepositoryControllerTest`
- Should return successful response on `/search` with repositories.
- Should return filtered repositories on `/repositories`.
- Should handle missing parameters with proper validation.

---

## 📌 Workflow Summary
1. User hits `/search` API with query parameters.
2. Backend calls GitHub API.
3. Results are parsed into `RepositoryEntity`.
4. Repositories are saved or updated in DB.
5. API returns a structured JSON response with status + message.
6. User hits `/repositories` API with optional filters.
7. Data is retrieved from local DB.
8. Filters applied → results sorted & returned as JSON.
9. Error handling ensures that even if GitHub API fails, the API responds with a graceful error message.
