FROM openjdk:17-oracle

WORKDIR /app

COPY build/libs/tms-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
