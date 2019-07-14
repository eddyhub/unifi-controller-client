ARG ARCH=amd64
FROM ${ARCH}/openjdk:8-jre-slim
COPY unifi-tools.jar /opt

ENTRYPOINT ["java", "-jar", "/opt/unifi-tools.jar"]
