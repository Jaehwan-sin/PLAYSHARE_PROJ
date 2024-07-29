# Base 이미지 정의
FROM openjdk:17-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
# COPY target/spotify-0.0.1-SNAPSHOT.jar /app/spotify.jar
COPY build/libs/spotify-0.0.1-SNAPSHOT.jar /app/spotify.jar

# 포트 노출
EXPOSE 8081

# 애플리케이션 실행 명령어
CMD ["java", "-jar", "spotify.jar"]