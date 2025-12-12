# Stage 1: Build ứng dụng
# Sử dụng Maven và OpenJDK 21 để build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy các file cấu hình maven trước để tận dụng cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
# Cấp quyền thực thi cho mvnw
RUN chmod +x mvnw

# Copy source code
COPY src src

# Build ra file JAR (bỏ qua test để build nhanh hơn)
RUN ./mvnw clean package -DskipTests

# Stage 2: Chạy ứng dụng
# Dùng bản JRE nhẹ để chạy
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar từ Stage 1
COPY --from=build /app/target/*.jar app.jar

# Tạo thư mục uploads để lưu ảnh (dựa trên UploadFileController của bạn)
RUN mkdir -p /app/uploads

# Expose port 8080
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]