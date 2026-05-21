# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.3/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.3/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.3/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.3/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

## Chatbot API

### Endpoints
- `POST /api/chat/ask` (anonymous allowed)
  - Request JSON: `{ "userQuery": "..." }` (optional `userId` is ignored by the backend for security)
  - Response JSON: `{ "userQuery": "...", "response": "..." }`
- `GET /api/chat/analytics` (requires authentication)
  - Response JSON: `UserAnalyticsDTO` with totals + last activity/chat timestamps.

### Supported commands (examples)
- `languages`
- `courses` / `courses language 3`
- `lessons` / `lessons course 2`
- `my activity` (requires login)
- `analytics` / `my stats` (requires login)
- `help`

## Testing

- Run: `./mvnw test`

## UI / Accessibility notes (frontend)

The backend returns plain text responses. For an accessible UI, ensure:
- Large touch targets and readable font sizes.
- High contrast for chatbot text and buttons.
- Screen-reader labels for input + send button.
- Avoid relying on color-only meaning for correctness/feedback.
