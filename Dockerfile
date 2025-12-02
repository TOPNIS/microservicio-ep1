# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Copiar el JAR compilado desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Cambiar ownership del archivo
RUN chown appuser:appgroup app.jar

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 8081

# Configuración de JVM optimizada
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]