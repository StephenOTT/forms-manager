FROM adoptopenjdk/openjdk13-openj9:jdk-13.0.2_8_openj9-0.18.0-alpine-slim
#COPY build/libs/forms-manager-*-all.jar forms-manager.jar
ADD https://github.com/StephenOTT/forms-manager/releases/download/v1.0/forms-manager-all.jar forms-manager-all.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-XX:+IdleTuningGcOnIdle", "-Xtune:virtualized", "-jar", "forms-manager.jar"]
