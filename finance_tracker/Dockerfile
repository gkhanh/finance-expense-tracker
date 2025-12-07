# Stage 1: Build the application
# Using Oracle Linux 9 with OpenJDK 25 (GA)
FROM openjdk:25-ea-25-jdk-oraclelinux9 AS build-java25
WORKDIR /app

# The minimal Oracle Linux image is missing 'xargs' which gradlew needs.
# We install 'findutils' package to get it.
RUN microdnf install findutils -y

COPY . .

# Run the build using the Gradle Wrapper included in the project
RUN ./gradlew build --no-daemon -x test

# Stage 2: Run the application
FROM openjdk:25-jdk-oraclelinux9
WORKDIR /app
COPY --from=build-java25 /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
