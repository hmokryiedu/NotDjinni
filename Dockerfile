FROM gradle:8.10-jdk21 AS builder

WORKDIR /app
COPY . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /opt/notdjinni
COPY --from=builder /app/build/install/NotDjinni ./NotDjinni

EXPOSE 8080
ENTRYPOINT ["/opt/notdjinni/NotDjinni/bin/NotDjinni"]
