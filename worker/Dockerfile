FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build.gradle settings.gradle gradlew /app/

COPY src /app/src

COPY gradle /app/gradle

RUN chmod +x gradlew

RUN ./gradlew build -x test --no-daemon

EXPOSE 8080

CMD ["./gradlew", "bootRun"]
