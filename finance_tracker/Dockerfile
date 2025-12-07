# Stage 1: Build the application
# We use the Oracle Linux 9 base image for Java 25 EA support
FROM openjdk:25-ea-jdk-oraclelinux9 AS build-java25
WORKDIR /app

# The minimal Oracle Linux image is missing 'xargs' which gradlew needs.
# We install 'findutils' package to get it.
RUN microdnf install findutils -y

COPY . .

# Run the build using the Gradle Wrapper included in the project
RUN ./gradlew build --no-daemon -x test

# Stage 2: Run the application
FROM openjdk:25-ea-jdk-oraclelinux9
WORKDIR /app
COPY --from=build-java25 /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
