# =============================================================================
# Dockerfile - Imagem da aplicação Connect (mock) para o lab
# =============================================================================
# Replica a ideia do portal/Dockerfile do navita-connect: JAR + ENV CLOUD_PROFILE
# + java -Dspring.cloud.config.profile=$CLOUD_PROFILE -jar. A imagem é taggeada
# como connect:local e carregada no Kind com "kind load docker-image". No K8s
# o CLOUD_PROFILE é sobrescrito pelo Deployment (value: "kind").
# =============================================================================
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

ARG VERSION=1.0.0
COPY target/mock-connect-${VERSION}.jar app.jar

ENV CLOUD_PROFILE=kind
ENV JVM_OPTIONS="-Xmx256m"

EXPOSE 9090
ENTRYPOINT ["sh", "-c", "java $JVM_OPTIONS -Dspring.cloud.config.profile=$CLOUD_PROFILE -jar app.jar"]
