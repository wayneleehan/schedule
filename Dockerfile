# --- 第一階段：使用 Gradle 編譯程式碼 ---
    FROM gradle:jdk21-alpine AS builder
    WORKDIR /app
    
    # 1. 先複製設定檔 (利用 Docker 快取層，加速 build)
    COPY build.gradle settings.gradle ./
    COPY src src
    
    # 2. 執行 Gradle Build (跳過測試，節省時間)
    RUN gradle bootJar -x test --no-daemon
    
    # --- 第二階段：執行 Java 程式 ---
    FROM eclipse-temurin:21-jre-alpine
    WORKDIR /app
    
    # 從第一階段複製 build 好的 jar 檔
    COPY --from=builder /app/build/libs/*.jar app.jar
    
    # 暴露 Port
    EXPOSE 8080
    
    # 啟動
    ENTRYPOINT ["java", "-jar", "app.jar"]