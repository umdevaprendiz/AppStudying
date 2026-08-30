# ---- Stage 1: build do frontend (React/Vite) ----
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: build do backend, embutindo o frontend já compilado ----
# Usa o Maven já instalado na imagem em vez do wrapper (mvnw): o jar do
# wrapper (.mvn/wrapper/maven-wrapper.jar) fica fora do git (.gitignore),
# então não estaria disponível no contexto de build.
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

# ---- Stage 3: imagem final, só o JRE + o jar ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend-build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
