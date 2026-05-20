# --- 第一階段：用 Gradle 編譯 ---
FROM gradle:jdk21-alpine AS builder
WORKDIR /app

# 先複製設定檔，利用 Docker layer cache 加速重複 build
COPY build.gradle ./
COPY src ./src

# 編譯（跳過測試以節省時間）
RUN gradle bootJar -x test --no-daemon

# --- 第二階段：執行 JAR ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
