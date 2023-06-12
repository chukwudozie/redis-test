FROM openjdk:17-alpine3.14
EXPOSE 8081
WORKDIR /app
COPY target/redis_test.jar .
ENTRYPOINT ["java","-jar","/app/redis_test.jar"]