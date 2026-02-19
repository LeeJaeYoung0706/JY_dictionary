스윙으로 만드는 사전 배포 대상 PC는 인터넷 없음 / Java 설치 없음 / Windows 환경을 기준 프로젝트 구조 요약 입력(원본 엑셀) src/main/resources/source/data.xlsx

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

A. 배포 태스크를 실행해야 생성됩니다.

.\gradlew assemblePortable

참고 (배포 태스크 이름/산출물 경로) 배포 태스크: assemblePortable

배포 폴더 경로: build/dist/dictionary/
