FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw

RUN apk add --no-cache curl
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN addgroup -S habittracker && adduser -S habittracker -G habittracker

COPY --from=builder /app/target/habittracker-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/data && chown -R habittracker:habittracker /app

USER habittracker

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]