# Replicando ideia do portal/Dockerfile (navita-connect) - apenas JAR
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

ARG VERSION=1.0.0
COPY target/mock-connect-${VERSION}.jar app.jar

ENV CLOUD_PROFILE=kind
ENV JVM_OPTIONS="-Xmx256m"

EXPOSE 9090
ENTRYPOINT ["sh", "-c", "java $JVM_OPTIONS -Dspring.cloud.config.profile=$CLOUD_PROFILE -jar app.jar"]
