#
# Build stage
#
FROM gradle:8.1.1-jdk17 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/app/
WORKDIR $APP_HOME

COPY build.gradle.kts settings.gradle.kts $APP_HOME
COPY gradle $APP_HOME/gradle
COPY src $APP_HOME/src

RUN gradle bootJar
#
# Run stage
#
FROM openjdk:17-jdk-slim-buster
ENV ARTIFACT_NAME=library-helper-1.0.0.jar
ENV APP_HOME=/app/

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

EXPOSE 8080
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}