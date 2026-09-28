# ──────────────────────────────────────────────────────────────────────────────
# Dockerfile — Backend Consultas (Spring Boot 4 + Java 17)
#
# Estágio 1 (build): compila o projeto com Maven
# Estágio 2 (run):   imagem leve só com o JAR gerado
# ──────────────────────────────────────────────────────────────────────────────

# ── Estágio 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copia o Maven Wrapper e o pom.xml primeiro (cache de dependências)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Garante que o mvnw tem permissão de execução
RUN chmod +x mvnw

# Baixa dependências (camada de cache reutilizada quando só o pom.xml muda)
RUN ./mvnw dependency:go-offline -B

# Copia o código-fonte e compila (sem rodar os testes)
COPY src ./src
RUN ./mvnw package -DskipTests

# ── Estágio 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia apenas o JAR gerado pelo estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Porta que o Spring Boot escuta
EXPOSE 8080

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
