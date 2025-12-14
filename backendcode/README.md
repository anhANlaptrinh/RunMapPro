# Running App Backend

Spring Boot 3 backend providing authentication, chat, and social features for the mobile running app.

## Tech Stack

- Java 17
- Spring Boot 3 (Web, Security, Validation)
- Spring Data MongoDB
- JWT (jjwt)
- Lombok

## Getting Started

1. **Configure environment**
   - A ready-to-use `.env` file is included with sample values for `MONGO_URI`, `JWT_SECRET`, and `JWT_EXPIRATION_MILLIS`; adjust them per environment.
   - Spring Boot automatically imports `.env` (thanks to `spring.config.import`), or you can export the same variables in your shell before running the service.
   - `MONGO_URI` **must** point to your MongoDB Atlas cluster. By default the app now targets the provided cluster (`mongodb+srv://dohuynhan2408_db_user:qnBGPJ9R9Qo9Zm46@running-app-cluster.4hcp7lk.mongodb.net/running_app_db?appName=running-app-cluster`), but you can override it by setting `MONGO_URI`.
   - `JWT_SECRET` **must** be provided (there is no fallback). The included `.env` already contains a strong placeholder, but you should replace it with your own 32+ character random string per environment.

2. **Install dependencies & run tests**

```bash
./mvnw clean test
```

3. **Run the service**

```bash
./mvnw spring-boot:run
```

The service exposes REST endpoints under `/api/auth`, `/api/chat`, `/api/posts`, and `/api/groups` covering registration/login, password reset, direct/group chat, posts, likes/comments/shares, and group management.

## Media uploads via MongoDB GridFS

- All images/videos are now stored directly inside MongoDB using GridFS. Attachments are referenced by their `mediaId` instead of external URLs.
- Use `POST /api/media` (multipart form) with field `file` to upload a post attachment. Optional `category` defaults to `POST_ATTACHMENT`. Each file is validated to be ≤10MB.
- The API responds with `{ mediaId, contentType, length, downloadUrl }`. Use the returned `mediaId` inside `CreatePostRequest.mediaIds`.
- Any authenticated client can download a stored file over `GET /api/media/{mediaId}`; the response streams the original binary with the stored content type.
- User avatars follow the same flow via `POST /api/users/me/avatar` (multipart). The backend stores the file under category `AVATAR`, deletes the old avatar if present, and returns the updated profile containing both `avatarMediaId` and a ready-to-use download URL.
