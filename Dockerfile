FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY ./server-0.0.1-SNAPSHOT.jar ./server.jar
EXPOSE 8080
CMD java -jar server.jar