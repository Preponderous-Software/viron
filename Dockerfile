# BUILD STAGE -----------------
FROM ubuntu:20.04 AS build

# Install Java 21 and Maven
RUN apt-get update
RUN apt-get install -y openjdk-21-jdk
RUN apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy src and pom.xml to the container
COPY src /app/src
COPY pom.xml /app

# Build the project
RUN mvn clean package -DskipTests


# RUNTIME STAGE -----------------
FROM ubuntu:20.04 AS runtime

# Install Java 21 runtime
RUN apt-get update
RUN apt-get install -y openjdk-21-jre

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/viron-0.7.0-SNAPSHOT-8-8-2026.jar /app/viron-0.7.0-SNAPSHOT-8-8-2026.jar

# Run the jar file
CMD ["java", "-jar", "viron-0.7.0-SNAPSHOT-8-8-2026.jar"]