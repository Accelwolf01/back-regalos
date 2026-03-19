# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine AS build

# Set the working directory in the container
WORKDIR /app

# Copy the gradle executable and wrapper
COPY gradlew .
COPY gradle gradle

# Copy the build configuration files
COPY build.gradle .
COPY settings.gradle .

# Ensure the gradle wrapper is executable
RUN chmod +x gradlew

# Download dependencies (this will be cached unless build.gradle/settings.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the application source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Final stage: Use a smaller JRE image for runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar file from the build stage
# Gradle usually places it in build/libs/ and we only want the fat jar (not the -plain one)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Set the default port (Render will override this via the PORT environment variable)
ENV PORT=8080
EXPOSE ${PORT}

# Run the jar file with the dynamically assigned port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
