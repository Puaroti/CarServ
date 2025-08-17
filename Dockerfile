# ====== Build stage ======
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

# Сначала зависимости (кэшируется)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Затем исходники и сборка
COPY src ./src
RUN mvn -q -DskipTests package

# ====== Runtime stage ======
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем собранный jar
# Если необходимо — можно заменить *.jar на конкретное имя артефакта
COPY --from=builder /app/target/*.jar /app/app.jar

# Опциональные Java флаги можно прокинуть через переменную JAVA_OPTS
ENV JAVA_OPTS=""

EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]