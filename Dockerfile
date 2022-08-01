FROM openjdk:11-jre-slim

WORKDIR /root

COPY ./server-0.0.1-SNAPSHOT.jar .

CMD java -jar -Dspring.profiles.active=${active} server-0.0.1-SNAPSHOT.jar