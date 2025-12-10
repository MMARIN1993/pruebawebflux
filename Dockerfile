# syntax=docker/dockerfile:1

# Etapa de build: usa Gradle wrapper para compilar todo el proyecto
FROM gradle:8.5-jdk17 AS build
WORKDIR /workspace

# Copiar todo el repo
COPY . .

# Construir los módulos y empaquetar el fat jar del servicio web (módulo 'application')
RUN gradle --no-daemon clean build

# Etapa de runtime: imagen ligera con JRE 17
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Copiar el jar resultante del módulo 'application' desde la etapa de build
COPY --from=build /workspace/application/build/libs/application.jar /app/app.jar

EXPOSE 8080

# Variables de entorno por defecto (se sobreescriben en docker-compose)
ENV SPRING_R2DBC_URL=r2dbc:mysql://mysql:3306/prueba_db \
    SPRING_R2DBC_USERNAME=root \
    SPRING_R2DBC_PASSWORD=root \
    SERVER_PORT=8080

# Ejecutar
ENTRYPOINT ["java","-jar","/app/app.jar"]
