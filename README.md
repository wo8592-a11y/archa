# WO8592 Android WebView App 📱

`https://wo8592-a11y.github.io/wo8592/` 웹사이트를 전체화면으로 쾌적하게 이용할 수 있도록 제작된 안드로이드 웹뷰(WebView) 전용 앱 프로젝트입니다.

다양한 일상 글(뻘글)부터 유용한 정보까지 활발하게 교류할 수 있는 커뮤니티 성격에 맞추어 **밝고 세련된 모던 아이콘**과 편리한 기능들이 탑재되어 있습니다.

---

## ✨ 주요 기능 및 특징

1. **전체화면(Edge-to-Edge) 몰입형 UI**
   - 상단 상태바 및 하단 내비게이션 영역까지 매끄럽게 연결되는 모던 전체화면 디자인
   - 게시판 글 작성 시 키보드가 입력 필드를 가리지 않도록 자동 리사이징(`adjustResize`) 적용

2. **게시판 필수 기능 완벽 지원**
   - **사진 및 파일 첨부 지원 (`WebChromeClient.onShowFileChooser`)**: 안드로이드 갤러리/파일 탐색기 연동
   - **JavaScript & DOM Storage 활성화**: 로컬 스토리지, 세션 스토리지 완벽 호환
   - **자바스크립트 대화상자 지원**: 웹 `alert()`, `confirm()`을 안드로이드 Material3 다이얼로그로 표시

3. **향상된 사용자 경험 (UX)**
   - **당겨서 새로고침 (Pull to Refresh)**: 화면을 아래로 당겨 언제든 최신 게시글 갱신
   - **로딩 프로그레스 바**: 페이지 로딩 상태를 상단에 슬림하게 표시
   - **스마트 뒤로가기**: 웹 페이지 방문 기록이 있으면 뒤로가기, 첫 페이지에서 연속 2번 누르면 안전 종료
   - **외부 링크 분기**: 유튜브, 카카오톡 등 외부 링크나 타 도메인은 기기 외부 브라우저/앱으로 자동 연결
   - **오프라인/에러 안내 화면**: 네트워크 연결이 끊겼을 때 감성적인 안내와 함께 1클릭 '다시 시도' 버튼 제공

4. **밝고 세련된 아이콘 디자인**
   - 활기찬 소통(뻘글)과 유익한 인사이트(꿀팁)를 상징하는 선셋-바이올렛 그라디언트 + 겹쳐진 말풍선 & 반짝이는 별빛(Sparkle) 디자인
   - 최신 안드로이드 Adaptive Icon(Vector) 및 전 해상도(MDPI~XXXHDPI, 512px 웹용) PNG 완비

5. **GitHub Actions 100% 무설치 자동 빌드**
   - 로컬에 **Android Studio나 SDK가 없어도** GitHub에 push하는 즉시 자동으로 APK 파일 빌드 및 다운로드 제공!

---

## 🚀 GitHub Actions로 APK 빌드 및 다운로드 방법

컴퓨터에 아무런 개발 도구가 없어도 아래 순서대로만 하면 3~5분 안에 APK가 생성됩니다.

### 1단계: GitHub 저장소(Repository) 생성 및 코드 업로드
1. [GitHub](https://github.com)에 로그인 후 새 저장소(New Repository)를 생성합니다. (Public 또는 Private 상관없음)
2. 현재 프로젝트 폴더의 터미널(PowerShell 또는 Git Bash)에서 아래 명령어를 차례대로 입력합니다:

```bash
git init
git add .
git commit -m "Initial commit: WO8592 Android WebView App with GitHub Actions"
git branch -M main
git remote add origin https://github.com/사용자아이디/저장소이름.git
git push -u origin main
```

### 2단계: 자동 빌드 확인 및 APK 다운로드
1. GitHub 저장소 페이지 상단의 **[Actions]** 탭을 클릭합니다.
2. `Android Build & APK Generator` 워크플로우가 자동으로 실행 중인 것을 확인할 수 있습니다.
   *(필요 시 우측의 `Run workflow` 버튼을 눌러 언제든지 수동으로 빌드할 수도 있습니다)*
3. 빌드가 완료되면(약 2~4분 소요, 초록색 체크표시 ✅), 해당 빌드 제목을 클릭하여 상세 페이지로 이동합니다.
4. 페이지 하단의 **Artifacts** 섹션에 있는 **`wo8592-webview-debug-apk`** 를 클릭하여 다운로드합니다.
5. 다운로드된 ZIP 파일의 압축을 풀면 바로 설치 가능한 **`app-debug.apk`** 파일이 들어있습니다.

### 3단계: 스마트폰에 설치
- 스마트폰에 APK 파일을 옮긴 후 실행하여 설치합니다.
- (알 수 없는 출처의 앱 설치 허용 팝업이 뜰 경우 '허용'을 선택하시면 됩니다.)

---

## 🛠️ 주요 프로젝트 구조

```
├── .github/workflows/
│   └── android-build.yml       # GitHub Actions 자동 빌드 설정
├── app/
│   ├── build.gradle.kts        # 앱 모듈 빌드 설정
│   └── src/main/
│       ├── AndroidManifest.xml # 권한 및 앱 선언
│       ├── java/com/wo8592/app/
│       │   └── MainActivity.kt # 웹뷰 및 제어 로직 (Kotlin)
│       └── res/
│           ├── layout/activity_main.xml # 전체화면 + 웹뷰 + 에러 레이아웃
│           ├── drawable/       # Adaptive Icon 및 벡터 그래픽
│           ├── mipmap-*/       # 각 해상도별 앱 아이콘
│           └── values/         # 색상, 테마, 문자열 리소스
├── generate_icons.ps1          # 아이콘 자동 생성 스크립트
└── ic_launcher-web.png         # 512x512 고해상도 앱 아이콘
```

---

## ⚙️ 설정 커스터마이징 안내

- **웹사이트 주소 변경**: `app/src/main/java/com/wo8592/app/MainActivity.kt` 파일의 `targetUrl` 및 `targetHost` 변수 수정
- **앱 이름 변경**: `app/src/main/res/values/strings.xml` 파일의 `app_name` 값 수정
- **테마 색상 변경**: `app/src/main/res/values/colors.xml` 파일의 색상 코드 수정
