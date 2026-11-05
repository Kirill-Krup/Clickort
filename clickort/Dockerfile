FROM gradle:9-jdk25 AS builder
WORKDIR /build
COPY settings.gradle.kts build.gradle.kts ./
RUN gradle --no-daemon dependencies --configuration runtimeClasspath
COPY src ./src
RUN gradle --no-daemon build -x test

FROM eclipse-temurin:25-jre
WORKDIR /clicker
RUN addgroup --system app && adduser --system --ingroup app --no-create-home app
COPY --from=builder --chown=app:app /build/build/libs/*.jar /clicker/clickort.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/clicker/clickort.jar"]
