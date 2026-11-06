# Etapa 1: compila o projeto com Maven (nao precisa ter Java no computador)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Etapa 2: roda so o .jar gerado, numa imagem menor
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/unilink-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
