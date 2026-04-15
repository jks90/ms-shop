# Dockerfile de ms-shop
# Build previo requerido: mvn clean package -DskipTests (en /home/keromanix/dev/ms-shop)
# El JAR debe existir en target/ antes de hacer docker build

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR ya compilado
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
