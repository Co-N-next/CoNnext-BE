# Co:N-next

  
<img width="859" height="483" alt="Image" src="https://github.com/user-attachments/assets/8783f474-00a1-42bb-b1d6-6f055d10428b" />
<br><br/>
오늘의 좋은 공연은, 다음을 기대되게 합니다.

Co:N-next는
공연장 내·외부 길 찾기 및 메이트 위치 공유 기능을 제공하는
공연 관객 경험 향상 서비스입니다.

관객과 아티스트, 그리고 공간을 하나로 연결합니다.
<br>
<br/>

## Technology Stack (기술 스택)

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
<br>
<br/>

## System Architecture (시스템 아키텍처)

<img width="2745" height="1476" alt="connext" src="https://github.com/user-attachments/assets/c88c27ce-bd3d-473c-9149-c5a3bb7ca527" />

<br>
<br/>

## Key Features (주요 기술)

- **회원가입/로그인**:
  - 자체 회원가입 및 로그인 기능을 제공합니다.
  - 네이버, 구글, 카카오 소셜 로그인을 지원합니다.
  - 토큰 재발급 및 로그아웃이 가능합니다.

- **약관**:
  - 약관 목록 조회가 가능합니다.
  - 동의한 optional 약관 조회 및 수정이 가능합니다.

- **공연**:
  - 최신 공연, 오늘의 공연, 다가오는 공연을 조회할 수 있습니다.
  - 조회순/시간순 정렬을 지원합니다.
  - 공연 검색이 가능합니다.
  - 공연 상세 정보 및 아티스트, 공연장, 공지사항 정보를 제공합니다.

- **공연장**:
  - 인기 공연장 및 인기 검색 공연장을 조회할 수 있습니다.
  - 근처 공연장 조회 및 검색 기능을 제공합니다.
  - 공연장 즐겨찾기 등록/삭제 및 목록 조회가 가능합니다.
  - 공연장 내부 지도 및 시설 위치를 확인할 수 있습니다.
  - 내부 길찾기 기능을 제공합니다.

- **예매**:
  - 예매 내역 추가, 조회, 수정, 삭제가 가능합니다.

- **알림**:
  - 내소식 및 공지사항 조회가 가능합니다.
  - 위치 공유 요청 및 메이트 요청 수락이 가능합니다.
  - 알림 읽음 처리 및 관리자 공지 발송 기능을 제공합니다.
 
- **약관**:
  - 최근 검색어 저장, 조회, 삭제 및 전체 삭제가 가능합니다.

<br>
<br/>

## Team Members (팀원 및 팀 소개)
| 이윤지 | 이주영 | 조예슬 | 천희정 |
|:------:|:------:|:------:|:------:|
| <img src="https://github.com/user-attachments/assets/c1c2b1e3-656d-4712-98ab-a15e91efa2da" alt="이윤지" width="150"> | <img src="https://github.com/user-attachments/assets/3ba69cfe-5e6f-40b4-a19c-91199faf5ad9" alt="이주영" width="150"/> | <img src="https://github.com/user-attachments/assets/78ce1062-80a0-4edb-bf6b-5efac9dd992e" alt="조예슬" width="150"> | <img src="https://github.com/user-attachments/assets/beea8c64-19de-4d91-955f-ed24b813a638" alt="천희정" width="150"> |
| Leader | Member | Member | Member |
| [GitHub](https://github.com/yooniicode) | [GitHub](https://github.com/LeeJuYoung12) | [GitHub](https://github.com/joyrii) | [GitHub](https://github.com/heejung72) |

<br>
<br/>


## Tasks & Responsibilities (작업 및 역할 분담)
|  |  |  |
|-----------------|-----------------|-----------------|
| 이윤지    |  <img src="https://github.com/user-attachments/assets/c1c2b1e3-656d-4712-98ab-a15e91efa2da" alt="이윤지" width="100"> | <ul><li>공연 목록 기능 구현</li><li>공연 검색 기능 구현</li><li>내부 지도 기능 구현</li></ul>     |
| 이주영   |  <img src="https://github.com/user-attachments/assets/3ba69cfe-5e6f-40b4-a19c-91199faf5ad9" alt="이주영" width="100">| <ul><li>자체 로그인 기능 구현</li><li>소셜 로그인 기능 구현</li><li>공연 및 좌석 공개범위 설정 기능 구현</li></ul> |
| 조예슬   |  <img src="https://github.com/user-attachments/assets/78ce1062-80a0-4edb-bf6b-5efac9dd992e" alt="조예슬" width="100">    |<ul><li>예매 내역 기능 구현</li><li>즐겨찾기 기능 구현</li><li>공연장 검색 기능 구현</li></ul>  |
| 천희정    |  <img src="https://github.com/user-attachments/assets/beea8c64-19de-4d91-955f-ed24b813a638" alt="천희정" width="100">    | <ul><li>메이트 기능 구현</li><li>검색어 기능 구현</li></ul>    |


<br>
<br/>
