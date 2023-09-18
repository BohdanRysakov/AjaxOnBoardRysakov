
# This will ensure you can build your Kotlin/Spring Boot project.
FROM gradle:8.2.1-jdk17 AS build

# Set the working directory inside the container.
WORKDIR /app

# Copy your whole project inside the container.
COPY . .

# Use Gradle to build the project inside the container.
# This will create a JAR file in build/libs directory.
RUN gradle bootJar

# Start a new stage with only JDK 17 (without Gradle) for the runtime environment.
FROM openjdk:17-slim

# Set the working directory in the container.
WORKDIR /app

# Copy only the built JAR from the previous stage to keep the final image lean.
COPY --from=build /app/build/libs/*.jar app.jar

# Inform Docker that the container will bind to port 8080.
EXPOSE 8080

# Specify the command to run when the container starts.
CMD ["java", "-jar", "app.jar"]
