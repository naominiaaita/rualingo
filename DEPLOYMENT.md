# Rualingo deployment

## Spring Boot hosting

Deploy this directory as a Maven service:

```text
Build command: ./mvnw clean package -DskipTests
Start command: java -jar target/rualingo-0.0.1-SNAPSHOT.jar
Health check: /actuator/health
```

Configure these environment variables in the hosting provider. Do not commit their values:

```text
RUALINGO_DB_URL=jdbc:mysql://<host>:3306/<database>?sslMode=REQUIRED
RUALINGO_DB_USERNAME=<database-user>
RUALINGO_DB_PASSWORD=<database-password>
JWT_SECRET=<at-least-32-byte-random-secret>
```

`JWT_SECRET` must contain at least 32 bytes because the application signs tokens with a JWT HMAC key. Generate a strong value locally instead of using a short human-readable password:

```bash
openssl rand -base64 32
```

Set it on Fly.io without committing it to the repository. Replace `<generated-secret>` with the output from the command above:

```bash
fly secrets set JWT_SECRET="<generated-secret>" -a rualingo
fly deploy -a rualingo
```

If your Fly.io app has a different name, replace `rualingo` in both commands with that app name. Existing tokens become invalid after changing the secret, so users may need to sign in again.

The application uses the provider's `PORT` value automatically. `OPENAI_API_KEY` is optional.

## Android production build

From `Frontend`, build the APK with the HTTPS URL supplied by the backend host:

```text
gradlew.bat :app:assembleStudentRelease -PapiBaseUrl=https://your-backend-host.example.com/
```

The URL must end with `/`. The APK is created at `Frontend/app/build/outputs/apk/student/release/app-student-release.apk`.

## GitHub Release and QR code

Create a GitHub Release and upload the APK as a release asset. Use the asset's browser download URL as the QR code target:

```text
https://github.com/<owner>/<repo>/releases/download/<tag>/app-student-release.apk
```
