# Use lightweight Java 21 image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and config
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Download dependencies (makes future builds faster)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Package the app (skip tests to make build faster)
RUN ./mvnw clean package -DskipTests

# Start the app (❗update the JAR name if it's different)
CMD ["java", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]
