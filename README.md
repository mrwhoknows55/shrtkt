# ShrtKt

Simple URL Shortener API written in Ktor 🚀

## How to run the server

Run this command and server should be up on port 8080 
```bash
./gradlew clean run
```

This should return `OK`
```bash
curl 0.0.0.0:8080/status
```


## How to run tests

```bash
./gradlew clean test
```
Output
```
> Task :test

ApplicationTest > testRoot PASSED

ApplicationTest > testShortenAndRedirect PASSED
```
## Latency

`/shorten` and `/redirect` api latencies for 10 concurrent users for 10 iterations

| Storage   | p(50)  | p(90)  | p(95)  | p(99)  |
|-----------|--------|--------|--------|--------|
| In-Memory | 3.26ms | 5.51ms | 5.65ms | 6.45ms |
