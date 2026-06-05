FROM openjdk:17.0.2-jdk

LABEL project="spring-petclinic"
LABEL author="Rhabby"

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

# Active le profil Spring "mysql"
ENV SPRING_PROFILES_ACTIVE=mysql

# Expose le port de l'application
EXPOSE 8080

# Commande pour démarrer l'application
CMD ["java", "-jar", "app.jar"]
