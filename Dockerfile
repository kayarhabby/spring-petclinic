FROM 192.168.0.99:5000/eclipse-temurin:17-jre

LABEL project="spring-petclinic"
LABEL author="Rhabby"

WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=mysql

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
