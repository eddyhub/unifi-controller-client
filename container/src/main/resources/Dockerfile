ARG ARCH=amd64
FROM ${ARCH}/adoptopenjdk:11-jre-hotspot-focal
COPY unifi-tools.jar /opt

ENTRYPOINT ["java", "-jar", "/opt/unifi-tools.jar"]
