FROM maven:3.8.7-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

# Примечание: данный Dockerfile предполагает, что Maven соберёт
# файл jar с именем backend-0.0.1-SNAPSHOT.jar.  При изменении имени
# артефакта поправьте путь в инструкции COPY.