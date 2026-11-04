FROM eclipse-temurin:21-jre@sha256:0b99b3376fbb50dbf9c0af8535fdd5b8eb0d3fdfc8b1e4967f383b6919f6901a
WORKDIR /app
COPY target/order-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
