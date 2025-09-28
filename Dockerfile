FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/quarkus-app/lib/ /app/lib/
COPY target/quarkus-app/app/ /app/app/
COPY target/quarkus-app/quarkus/ /app/quarkus/
COPY target/quarkus-app/quarkus-run.jar /app/quarkus-run.jar
CMD ["java", "-jar", "quarkus-run.jar"]
