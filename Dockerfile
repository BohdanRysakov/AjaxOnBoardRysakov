FROM openjdk:17-slim
EXPOSE 8080
WORKDIR /usr/src/java-app
ENTRYPOINT ["java", "-jar", "app.jar"]
