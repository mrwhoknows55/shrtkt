FROM gradle:8.14.3-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV DB_HOST=localhost \
    DB_PORT=5432 \
    DB_NAME=shrtkt \
    DB_USER=postgres \
    DB_PASSWORD=password

COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java \
  -Ddb.host=$DB_HOST \
  -Ddb.port=$DB_PORT \
  -Ddb.name=$DB_NAME \
  -Ddb.user=$DB_USER \
  -Ddb.password=$DB_PASSWORD \
  -jar app.jar"]
