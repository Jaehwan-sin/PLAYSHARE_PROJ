# 프로젝트 이름

## 목차
- [소개](#소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 아키텍처](#프로젝트-아키텍처)
- [프로젝트 성과](#프로젝트-성과)

## 소개
각자의 개성이 담긴 플레이리스트를 공유하는 플랫폼으로서, Spotify API를 이용해 프로젝트를 진행했습니다.

**프로젝트 개발기간** <br>
개발 인원 : 1인 <br>
개발 기간 : 2024년 5월 ~ 2024년 7월

## 주요 기능
- Restful API 설계
- Spring Security를 이용한 OAuth2 로그인 기능 및 **JWT 토큰 부여로 보안 강화**
- Websocket을 이용한 **실시간 알림 기능 구현**
- **Github CI/CD 파이프라인**을 통해 AWS의 Docker 환경에 자동 빌드 및 배포
- Redis 캐싱 전략 적용하여 **조회 성능 65%** 개선
- Redis 분산 락 적용하여 **동시성 이슈 문제 해결**
- TDD를 활용한 **테스트 코드 작성 및 코드 품질 개선**

## 기술 스택
![java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-131F37?style=for-the-badge&logo=JPA&logoColor=white) <br>
![MARIADB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![REDIS](https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white) <br>
![AWS](https://img.shields.io/badge/Amazon_AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) <br>
![Github Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

## 프로젝트 아키텍처


## 프로젝트 성과
### <span style="color:#E67E22">1. JPA와 DB 연관관계</span>
- **객체지향적 설계**: JPA로 객체 지향적 설계 극대화, 코드 가독성 및 유지보수성 향상
- **연관관계 관리**: 엔티티 간 관계 설정으로 데이터 무결성 유지 및 성능 최적화

### <span style="color:#E67E22">2. Spring Security와 JWT 보안</span>
- **JWT 토큰**: Spring Security와 OAuth2로 사용자 인증 및 권한 부여 강화

### <span style="color:#E67E22">3. Redis 캐싱과 NoSQL</span>
- **캐싱 전략**: Redis로 조회 성능 65% 개선, 데이터베이스 부하 감소
- **NoSQL 이해**: 인메모리 기반 NoSQL 데이터베이스 활용

### <span style="color:#E67E22">4. AWS와 Docker 배포</span>
- **AWS**: EC2를 통한 클라우드 배포 및 인프라 관리
- **Docker**: 컨테이너화 및 CI/CD 파이프라인 구축

### <span style="color:#E67E22">5. 예외 처리 및 테스트 코드</span>
- **예외 처리**: 다양한 상황 고려한 오류 및 예외 처리
- **테스트 코드**: 예외 상황 검증으로 코드 신뢰성 향상

### <span style="color:#E67E22">6. 데이터베이스 성능 최적화</span>
- **쿼리 최적화**: 데이터베이스 과부하 감소 및 쿼리 호출 최소화
- **성능 모니터링**: 지속적인 쿼리 성능 모니터링 및 최적화 작업
