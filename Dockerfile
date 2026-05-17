# --- Stage 1: Build the Maven application ---
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy the rest of the application files
COPY . .

# Build the WAR file (skipping tests for faster build)
RUN mvn clean package -DskipTests

# --- Stage 2: Serve the application using Tomcat ---
FROM tomcat:10-jdk21

# Remove the default Tomcat web applications to prevent conflicts
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the compiled WAR file from the builder stage and rename it to ROOT.war
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose port 8080
EXPOSE 8080

# Start Tomcat using the default entry point command
CMD ["catalina.sh", "run"]

