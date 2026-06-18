## Project goal

You are - Senior Staff Level Engineer, Solution Architect, Tech Lead.
Build a small educational Spring Boot application that uploads image files to AWS S3-compatible object storage and records image metadata in PostgreSQL.

The app should support two runtime modes:

1. AWS Free Tier:

    * S3 bucket for image storage.
    * PostgreSQL on AWS RDS.

2. Local development:

    * LocalStack for S3-compatible storage.
    * Local PostgreSQL via Docker Compose.

Prefer simple, explicit implementation over production-grade complexity.

## Technology stack

Use:

* Java 21
* Spring Boot 4.x
* Spring Web
* Spring Data JPA
* PostgreSQL 18
* AWS SDK for Java v2
* Maven
* Docker Compose for local infrastructure

Do not use:

* Tests
* Kotlin
* Gradle
* Reactive WebFlux
* Microservices
* Kubernetes
* Terraform
* Advanced AWS services beyond S3 and RDS/PostgreSQL

## Application requirements

Implement a simple REST API for image upload and metadata retrieval.

Minimum API:

* `POST /images`

    * Accepts multipart image upload.
    * Validates that the uploaded file is an image.
    * Uploads the image to S3 or LocalStack.
    * Stores metadata in PostgreSQL.
    * Returns saved metadata.

* `GET /images`

    * Returns a list of uploaded image metadata.

* `GET /images/{id}`

    * Returns metadata for one uploaded image.

* Optional:

    * `DELETE /images/{id}`
    * Deletes metadata and removes the object from S3.

## Metadata model

Store at least:

* id
* original file name
* content type
* file size
* S3 bucket name
* S3 object key
* upload timestamp

Use a simple JPA entity. Avoid unnecessary abstraction.

## Configuration requirements

Use Spring profiles:

* `local`

    * Uses LocalStack S3 endpoint.
    * Uses local PostgreSQL from Docker Compose.

* `aws`

    * Uses real AWS S3.
    * Uses AWS RDS PostgreSQL.

Configuration should be externalized through environment variables.

Never hardcode:

* AWS access keys
* AWS secret keys
* AWS account IDs
* database passwords
* real bucket names
* real hostnames

Provide `.env.example`, but never commit `.env`.

## Local development

Create a `docker-compose.yml` that starts:

* PostgreSQL
* LocalStack with S3 enabled

The local setup should allow a developer to run the full application without using real AWS.

Include clear README instructions for:

* starting Docker Compose
* creating or initializing the LocalStack S3 bucket
* running the Spring Boot app with the `local` profile
* uploading an image with `curl`
* checking stored metadata

## Code style

Keep the codebase small and readable.

Use a conventional layered structure:

* controller
* service
* repository
* entity
* dto
* config

Prefer constructor injection.

Do not introduce unnecessary patterns such as:

* CQRS
* event sourcing
* custom framework abstractions
* over-generalized storage interfaces unless useful for S3/LocalStack compatibility

## Error handling

Add simple, consistent error handling.

Handle at least:

* missing file
* empty file
* unsupported content type
* image too large
* missing metadata record
* S3 upload failure
* database persistence failure

Use appropriate HTTP status codes.

## Testing expectations

Add tests where practical.

Prioritize:

* service-level tests
* controller tests for upload validation
* repository tests if database behavior is non-trivial

For AWS/S3 behavior, prefer LocalStack-compatible tests if reasonable. Do not require real AWS credentials for automated tests.

## Documentation requirements

Create or update `README.md` with:

* project purpose
* architecture overview
* prerequisites
* local setup
* AWS Free Tier setup notes
* environment variables
* API examples
* known limitations

The README should clearly state that this is an educational project, not a production-ready file storage system.

## Security and cost constraints

This project is for learning.

Avoid features that may create unexpected AWS costs.

Do not create public S3 buckets.

Do not recommend storing credentials in source code.

Mention that real AWS resources should be deleted after testing to avoid charges.

## Implementation discipline

Before changing code:

1. Inspect the existing project structure.
2. Reuse existing conventions.
3. Make the smallest coherent change.
4. Avoid unrelated refactoring.

After changing code:

1. Run relevant tests.
2. Run Maven build if possible.
3. Summarize changed files.
4. Mention any commands that could not be run.

## Definition of done

The project is complete when:

* The app can upload an image.
* The image object is stored in S3 or LocalStack.
* Metadata is stored in PostgreSQL.
* Metadata can be retrieved through REST endpoints.
* Local development works through Docker Compose.
* README contains runnable setup instructions.
