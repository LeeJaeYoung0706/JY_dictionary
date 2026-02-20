스윙으로 만드는 사전 배포 대상 PC는 인터넷 없음 / Java 설치 없음 / Windows 환경을 기준 프로젝트 구조 요약 입력(원본 엑셀) src/main/resources/source/data.xlsx

데이터는 쳇지피티를 활용한 더미 데이터 입니다. 

### 설명

용어 사전으로 사용하는 애플리케이션

<img width="1820" height="788" alt="image" src="https://github.com/user-attachments/assets/46341b92-4ad0-44f0-abf1-86528482400c" />

셀렉트박스 자동완성 기능 처리 
<img width="1386" height="184" alt="image" src="https://github.com/user-attachments/assets/545de3a4-c5f1-4819-9a64-254af0b99408" />

검색 히스토리 버튼 클릭하여 이력 확인 가능

<img width="1158" height="502" alt="image" src="https://github.com/user-attachments/assets/6ec66086-87f8-4e8d-8abf-ed84a930c683" />

선택 후 다시검색 클릭

<img width="1155" height="507" alt="image" src="https://github.com/user-attachments/assets/e7da70f3-9394-413c-abc2-8bda52a74fc3" />

해당 이력으로 재검색 실행

<img width="1821" height="783" alt="image" src="https://github.com/user-attachments/assets/1c318d28-9896-4b97-b94f-919a24eebffb" />

더블클릭 시 상세보기 제공

<img width="1815" height="781" alt="image" src="https://github.com/user-attachments/assets/0a663904-177b-4cac-8524-d07d85e437aa" />

<img width="889" height="634" alt="image" src="https://github.com/user-attachments/assets/2e2e7862-398f-4add-8e41-ee34c838bc9b" />

상세보기 이력 제공

<img width="1160" height="508" alt="image" src="https://github.com/user-attachments/assets/bdf2aba6-6ac2-4184-8d2e-23875b133326" />

더블 클릭시 이력 확인 가능

<img width="884" height="633" alt="image" src="https://github.com/user-attachments/assets/1dac766c-669a-4364-9cd9-d75ebaf6036b" />


















### 빌드

빌드 시 생성되는 JSON

build/generated-resources/data.json

배포 폴더(최종 산출물)

build/dist/dictionary/

배포 폴더 안에는 다음이 포함됩니다.

dictionary/ dictionary.exe jre/ (미니 Java 런타임) data/data.json (엑셀 파싱 결과)

운영(사용자 PC)에서는 data/data.json만 읽도록 구성합니다.

엑셀 포맷 규칙 1행: 참고 글귀 확인하지 않음.

2행: key (Customer / Dept / Term 등 “내부 키”)

3행: view (고객사 / 부서 / 용어 등 “화면 표시 한글”)

4행부터: 데이터

배포 방식 (Java 없이 실행) 핵심 개념 사용자 PC에 Java를 설치하지 않습니다.

대신, 배포 폴더 안에 미니 Java(jre 폴더) 를 같이 넣습니다.

EXE는 배포 폴더의 jre\bin\java.exe 를 사용합니다.

빌드 준비물(배포하는 개발자 PC) 배포(빌드)하는 PC에는 다음이 필요합니다.

JDK 17 설치

예시 경로: C:\Program Files\Java\jdk-17\

JDK 안에 아래 파일이 존재해야 함

C:\Program Files\Java\jdk-17\bin\jlink.exe

⚠️ 사용자 PC에는 JDK/JRE 필요 없음 (개발자 PC에서 미니 jre를 만들어서 같이 배포하기 때문)

미니 Java(jre) 만들기 (처음 1회 또는 JDK 변경 시) 프로젝트 루트(= build.gradle 있는 폴더)에서 PowerShell 실행 후 아래 명령을 입력합니다.

⚠️ 이미 jre 폴더가 있으면, 먼저 이름을 jre_old로 바꾸고 진행하세요.

& "C:\Program Files\Java\jdk-17\bin\jlink.exe" --add-modules java.base,java.desktop,java.logging,java.datatransfer,java.prefs,java.xml --strip-debug --no-header-files --no-man-pages --compress=2 --output .\jre

생성 확인

아래 2개 파일이 있어야 정상입니다.

dir .\jre\bin\java.exe dir .\jre\lib\jvm.cfg

배포 빌드(매 버전 업그레이드 때 수행) 엑셀(glossary.xlsx) 수정 src/main/resources/source/glossary.xlsx 수정

배포 폴더 생성 PowerShell에서 실행:

.\gradlew clean assemblePortable

산출물 위치 아래 폴더가 최종 배포본입니다.

build/dist/dictionary/

이 폴더를 그대로 압축해서 전달하면 됩니다.

사용자(최종 배포 대상) 실행 방법 사용자는 아래만 하면 됩니다.

압축 풀기

dictionary.exe 더블클릭

인터넷/Java 설치 필요 없습니다.

자주 발생하는 문제 Q1. 실행했더니 jre\lib\jvm.cfg 를 못 찾는다고 나와요 A. jre 폴더가 “정상 런타임”이 아닙니다. jlink로 생성한 jre를 사용해야 하며, 다음 파일이 존재해야 합니다.

jre\bin\java.exe

jre\lib\jvm.cfg

Q2. dist 폴더가 안 생겨요





배포하는법

프로젝트 를 받고.

배포 태스크를 실행해서 생성

.\gradlew assemblePortable

참고 (배포 태스크 이름/산출물 경로) 배포 태스크: assemblePortable

배포 폴더 경로: build/dist/dictionary/











