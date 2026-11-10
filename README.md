# Rualingo

Rualingo is a language-learning platform for Papua New Guinean languages. It combines a Spring Boot API with an Android application for learners and administrators.

## Features

- Language, course, lesson, exercise, and vocabulary management
- Student registration, login, Google sign-in, and JWT-based authentication
- Course enrollment and lesson progress tracking
- Exercise submissions, accuracy metrics, activity history, and reports
- Vocabulary translation and personal vocabulary management
- Optional AI tutor chat powered by OpenAI
- Separate Android student and admin application flavors

## Project structure

```text
.
├── src/                  # Spring Boot backend
├── Frontend/             # Android application
├── content/              # Curriculum and project content
├── Dockerfile            # Container build for the backend
├── fly.toml              # Fly.io deployment configuration
├── master_curriculum.json
└── pom.xml
```

## Technology stack

- **Backend:** Java 17, Spring Boot, Spring Web, Spring Security, Spring Data JPA
- **Data:** MySQL, Redis
- **Authentication:** JWT and Google Sign-In
- **Mobile:** Android, Kotlin Gradle DSL, Java 21 toolchain, Retrofit, Room, Material Components
- **Deployment:** Docker and Fly.io

## Backend setup

### Prerequisites

- Java 17
- Maven 3.9+ or the included Maven Wrapper
- MySQL
- Redis

### Configuration

The backend reads its runtime configuration from environment variables:

| Variable | Required | Description |
| --- | --- | --- |
| `RUALINGO_DB_URL` | Yes | JDBC connection URL |
| `RUALINGO_DB_USERNAME` | Yes | Database username |
| `RUALINGO_DB_PASSWORD` | Yes | Database password |
| `REDIS_URL` | Yes | Redis connection URL |
| `JWT_SECRET` | Yes | Secret used to sign JWTs |
| `OPENAI_ENABLED` | No | Enable the optional AI tutor (`false` by default) |
| `OPENAI_API_KEY` | No | OpenAI API key when the AI tutor is enabled |
| `OPENAI_MODEL` | No | OpenAI model name (`gpt-4` by default) |
| `PORT` | No | HTTP port (`8080` by default) |

Example:

```bash
export RUALINGO_DB_URL='jdbc:mysql://localhost:3306/rualingo'
export RUALINGO_DB_USERNAME='rualingo'
export RUALINGO_DB_PASSWORD='your-password'
export REDIS_URL='redis://localhost:6379'
export JWT_SECRET='replace-with-a-long-random-secret'

./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Health checks are available at [`/actuator/health`](http://localhost:8080/actuator/health).

### Build and test

```bash
./mvnw clean test
```

## Android application

Open `Frontend/` in Android Studio with a Java 21 toolchain. The app provides two product flavors:

- `student` — learner application
- `admin` — administration application

The API base URL defaults to `https://rualingo.fly.dev/`. Override it with either a Gradle property or environment variable:

```bash
cd Frontend
RUALINGO_API_BASE_URL='http://10.0.2.2:8080/' ./gradlew assembleStudentDebug
```

The same pattern can be used with `assembleAdminDebug` for the admin application.

## API areas

The backend exposes REST endpoints under these primary route groups:

- `/api/auth` — registration, login, and Google authentication
- `/api/account` — profile, settings, progress, enrollment, and notifications
- `/api/languages` — language management and related content
- `/api/courses` and `/api/lessons` — curriculum management
- `/api/exercises` and `/api/user_responses` — exercises and learner submissions
- `/api/vocabulary` — vocabulary and translation
- `/api/chat` — tutor chat and chat analytics
- `/api/reports` — reporting and analytics

## Docker

Build and run the backend container:

```bash
docker build -t rualingo .
docker run --rm -p 8080:8080 \
  -e RUALINGO_DB_URL \
  -e RUALINGO_DB_USERNAME \
  -e RUALINGO_DB_PASSWORD \
  -e REDIS_URL \
  -e JWT_SECRET \
  rualingo
```

## Deployment

The repository includes:

- GitHub Actions Maven checks for pushes and pull requests targeting `backend`
- A multi-stage `Dockerfile` targeting Java 17
- `fly.toml` configured for Fly.io with an `/actuator/health` health check

Keep production secrets in the deployment platform’s secret manager; do not commit them to the repository.

## Security

To report a vulnerability privately, use the repository’s [security page](https://github.com/naominiaaita/rualingo/security).

## License

No license has been specified yet.
