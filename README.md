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

## Sample API Keys
Use the following test API keys via the `x-api-key` header:

- Alice: `sk_test_alice`
- Bob: `sk_test_bob`

## API Performance

### Test Configuration

- Endpoints: `/shorten` and `/redirect` APIs
- Environment: Docker Compose with resource constraints: 2 CPU cores and 4GB RAM

To run the benchmark:

```shell 
  k6 run benchmark.js -u 400 -i 400 # concurrent users/iterations count
```

### Latency:

| Database  | Concurrent users | p(50)    | p(90)    | p(95)   | p(99)    | failure % |
|-----------|------------------|----------|----------|---------|----------|-----------|  
| In-memory | 10               | 6ms      | 9.01ms   | 9.93ms  | 10.51ms  | 0%        |
| In-memory | 50               | 15.78ms  | 28.39ms  | 29.61ms | 36.99ms  | 0%        |
| In-memory | 100              | 18.84ms  | 39.96ms  | 40.37ms | 40.89ms  | 0%        |
| In-memory | 200              | 20ms     | 46.45ms  | 47.6ms  | 53.47ms  | 0%        |
| In-memory | 400              | 32.79ms  | 65.05ms  | 67.37ms | 76.23ms  | 0%        |
| In-memory | 800              | 44.84ms  | 79.65ms  | 84.73ms | 94.47ms  | 0%        |
| In-memory | 1600             | 59.73ms  | 94.25ms  | 120.6ms | 128.97ms | 0%        |
| In-memory | 3200             | 299.1ms  | 880.01ms | 1.03s   | 1.05s    | 0%        |
| In-memory | 6400             | 450.01ms | 1.09s    | 1.14s   | 2.92s    | 0%        |
| In-memory | 12800            | 798.58ms | 2.42s    | 3.13s   | 4.33s    | 0%        |
| In-memory | 13000            | 602.45ms | 2.43s    | 5.11s   | 5.44s    | 0.05%     |
