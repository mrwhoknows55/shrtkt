# ShrtKt

Simple URL Shortener API written in Ktor 🚀

## How to run the server

Run this command and server should be up, on port 8080

```shell
  ./gradlew clean run
```

Or with docker compose:

```shell
  docker compose up -d
```

This should return `OK`

```shell
  curl 0.0.0.0:8080/status
```

## How to run tests

```shell
  ./gradlew clean test
```

Output

```text
> Task :test

ApplicationTest > testRoot PASSED

ApplicationTest > testShortenAndRedirect PASSED
```

## API Performance

### Test Configuration

- Endpoints: `/shorten` and `/redirect` APIs
- Environment: Docker Compose with resource constraints: 2 CPU cores and 4GB RAM


To run the benchmark:
```shell 
  k6 run benchmark.js
```

### Latency:

| Database  | Concurrent users | Iterations | p(50) | p(90)  | p(95)  | p(99)   |
|-----------|------------------|------------|-------|--------|--------|---------|
| In-memory | 10               | 10         | 6ms   | 9.01ms | 9.93ms | 10.51ms |

