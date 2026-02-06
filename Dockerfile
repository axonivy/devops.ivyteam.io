FROM eclipse-temurin:25.0.2_10-jre-noble
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
