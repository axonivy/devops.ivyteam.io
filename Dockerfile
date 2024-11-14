FROM eclipse-temurin:21.0.5_11-jre-noble
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
