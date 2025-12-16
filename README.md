# microservice-template

This project is a small Quarkus (3.x) Java microservice template.

It currently exposes **one public HTTP API endpoint** (`GET /hello`) and includes both unit and packaged-mode integration tests.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Public API documentation

### Base URL

- **Dev / local**: `http://localhost:8080`

### `GET /hello`

- **Summary**: Returns a plain-text greeting (health-check-style endpoint).
- **Method**: `GET`
- **Path**: `/hello`
- **Produces**: `text/plain`
- **Success response**:
  - **Status**: `200 OK`
  - **Body**: `Hello from Quarkus REST`

#### Example (curl)

```bash
curl -i http://localhost:8080/hello
```

Expected output (example):

```text
HTTP/1.1 200 OK
content-type: text/plain;charset=UTF-8

Hello from Quarkus REST
```

#### Example (Java client)

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HelloClient {
  public static void main(String[] args) throws Exception {
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/hello"))
        .GET()
        .build();

    HttpResponse<String> res = HttpClient.newHttpClient()
        .send(req, HttpResponse.BodyHandlers.ofString());

    System.out.println(res.statusCode());
    System.out.println(res.body());
  }
}
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```bash
./mvnw compile quarkus:dev
```

Then call the API:

```bash
curl http://localhost:8080/hello
```

> **_NOTE:_** Quarkus ships with a Dev UI (dev mode only) at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```bash
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Tests

This project includes:

- **Unit/in-dev tests** via `@QuarkusTest` (fast JVM tests)
- **Packaged-mode integration tests** via `@QuarkusIntegrationTest` (runs the app as a packaged artifact)

Run unit tests:

```bash
./mvnw test
```

Run unit + integration tests:

```bash
./mvnw verify
```

## Creating a native executable

You can create a native executable using:

```bash
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/microservice-template-1.0-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Configuration

Runtime configuration is read from `src/main/resources/application.properties`.

This template currently does not define custom properties, but common examples include:

```properties
# Change the HTTP port
quarkus.http.port=8080

# Example datasource settings (if/when you add persistence)
# quarkus.datasource.db-kind=postgresql
# quarkus.datasource.username=...
# quarkus.datasource.password=...
# quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/...
```

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
