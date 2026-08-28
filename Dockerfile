# ---------- Stage 1: build ----------
# Maven 3.9 + JDK 17 (java.version=17 no pom.xml)
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Baixa as dependencias primeiro para aproveitar o cache de layers
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Usuario sem privilegios
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /build/target/task_manager-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
