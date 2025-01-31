FROM eclipse-temurin:21.0.6_7-jre-noble
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
