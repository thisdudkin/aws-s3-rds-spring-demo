# Image Storage Demo

An educational Spring Boot application that uploads images to S3-compatible
object storage and records their metadata in PostgreSQL. It supports a fully
local setup with LocalStack and PostgreSQL, plus an `aws` profile for S3 and
Amazon RDS for PostgreSQL.

This project is intentionally small and is **not a production-ready file
storage system**.

## Architecture

```text
Client -> Spring REST API -> S3 / LocalStack
                         -> PostgreSQL / Amazon RDS
```

The API validates a multipart upload, stores the object first, then stores its
metadata. If database persistence fails, it makes a best-effort attempt to
remove the uploaded object.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- `curl`

## Local setup

1. Copy `.env.example` to `.env` and choose a local database password.

   PowerShell:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Start PostgreSQL 18 and LocalStack:

   ```shell
   docker compose up -d
   docker compose ps
   ```

   LocalStack automatically creates the bucket named by `S3_BUCKET`. To verify
   it manually:

   ```shell
   docker compose exec localstack awslocal s3api head-bucket --bucket image-uploads
   docker compose exec localstack awslocal s3 ls
   ```

3. Export the values used by the Spring process. Docker Compose reads `.env`,
   but Maven does not.

   PowerShell:

   ```powershell
   $env:POSTGRES_DB="image_storage"
   $env:POSTGRES_USER="image_app"
   $env:POSTGRES_PASSWORD="change-me"
   $env:AWS_ACCESS_KEY_ID="test"
   $env:AWS_SECRET_ACCESS_KEY="test"
   $env:AWS_REGION="us-east-1"
   $env:S3_BUCKET="image-uploads"
   ```

4. Run the application:

   ```shell
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

## API examples

Upload an image:

```shell
curl -i -F "file=@C:/path/to/photo.png" http://localhost:8080/images
```

List metadata:

```shell
curl http://localhost:8080/images
```

Get one metadata record:

```shell
curl http://localhost:8080/images/{id}
```

Inspect objects stored in LocalStack:

```shell
docker compose exec localstack awslocal s3 ls s3://image-uploads/images/
```

JPEG, PNG, GIF, and WebP uploads are accepted. The declared content type and
file signature must match. The maximum image size is 5 MB. Objects are private;
the API returns metadata rather than public object URLs.

## Environment variables

| Variable | Profile | Purpose |
| --- | --- | --- |
| `POSTGRES_DB` | local | Local database name |
| `POSTGRES_USER` | local | Local database user |
| `POSTGRES_PASSWORD` | local | Local database password |
| `AWS_ACCESS_KEY_ID` | local/aws | SDK credential from the environment |
| `AWS_SECRET_ACCESS_KEY` | local/aws | SDK credential from the environment |
| `AWS_REGION` | local/aws | S3 region |
| `S3_BUCKET` | local/aws | Existing private bucket |
| `S3_ENDPOINT` | local | LocalStack endpoint; defaults to `http://localhost:4566` |
| `DB_URL` | aws | JDBC URL for Amazon RDS PostgreSQL |
| `DB_USERNAME` | aws | RDS database user |
| `DB_PASSWORD` | aws | RDS database password |

For AWS, prefer an IAM role or another standard AWS SDK credential provider
instead of long-lived access keys. Never store credentials in source control.

## AWS Free Tier notes

1. Create a private S3 bucket. Do not enable public access.
2. Create an RDS for PostgreSQL instance and database within the applicable
   Free Tier limits.
3. Allow the application host to reach RDS on port 5432 without exposing the
   database broadly to the internet.
4. Grant the application identity only the S3 permissions it needs for the
   selected bucket.
5. Set the `aws` profile environment variables, then run:

   ```shell
   mvn spring-boot:run -Dspring-boot.run.profiles=aws
   ```

AWS Free Tier eligibility and pricing can change. Review current AWS pricing
before creating resources, monitor usage, and delete the S3 objects, bucket,
and RDS instance after testing to avoid unexpected charges.

## Known limitations

- Image validation checks common file signatures, but it does not fully decode
  images or scan them for malicious content.
- Database schema changes use Hibernate `ddl-auto=update`; production systems
  should use a migration tool.
- There is no authentication, download endpoint, deletion endpoint, malware
  scanning, image processing, pagination, or retry queue.
- A failed database write triggers best-effort S3 cleanup, so rare orphaned
  objects may still require manual cleanup.

Stop local infrastructure with:

```shell
docker compose down
```

Use `docker compose down -v` only when you also want to delete local database
and LocalStack data.
