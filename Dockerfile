# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 复制构建好的 jar
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]