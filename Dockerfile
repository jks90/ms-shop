FROM amazoncorretto:21

ARG APP_NAME="ms-cards"

LABEL maintainer="makingCleanCode"

WORKDIR /app

COPY /target/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]