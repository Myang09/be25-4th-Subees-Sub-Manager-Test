FROM eclipse-temurin:21-jre-alpine-3.22

LABEL maintainer="subees"
LABEL version="1.0"

WORKDIR /app

COPY ./target/*.jar app.jar

ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]