# No Ads Flashlight / 광고 없는 플래시라이트

<div align="center">

**🔦 광고 없는 심플한 플래시라이트 앱 🔦**

간단하고 깔끔한 플래시라이트 앱으로, 광고 없이 무료로 사용할 수 있습니다.

[English](#english) | [한국어](#korean)

</div>

---

## <a name="korean"></a>한국어

### 📱 프로젝트 소개

**No Ads Flashlight**는 광고가 전혀 없는 무료 플래시라이트 앱입니다. 
플레이 스토어의 많은 플래시라이트 앱들이 광고로 가득 차 있고 사용이 불편해서, 
부모님과 모든 사람들이 편하게 사용할 수 있도록 만들었습니다.

### ✨ 주요 기능

- **🔦 플래시 ON/OFF**: 큰 버튼으로 쉽게 플래시를 켜고 끌 수 있습니다
- **🎚️ 밝기 조절**: 슬라이더로 밝기를 조절할 수 있습니다 (일부 기기에서 지원)
- **📱 안드로이드 홈 화면 위젯**: 앱을 열지 않고도 바로 플래시를 제어할 수 있습니다
  - 위젯 크기는 정사각형 비율로 조절 가능 (1x1, 2x2, 3x3 등)
  - 플래시 상태에 따라 아이콘과 색상이 변경됩니다
- **🎨 모던한 디자인**: Material Design 3를 적용한 깔끔한 UI
- **🌓 다크 모드**: 시스템 설정에 따라 자동으로 라이트/다크 테마 전환
- **🚫 광고 없음**: 완전히 광고가 없는 무료 앱

### 📸 스크린샷

_추후 추가 예정_

### 🛠️ 기술 스택

- **Framework**: Flutter 3.35.6
- **언어**: Dart 3.9.2
- **플랫폼**: Android (최소 API 21 / Android 5.0 이상)
- **주요 패키지**:
  - `torch_light`: 플래시 제어
  - `home_widget`: 안드로이드 홈 화면 위젯
  - `shared_preferences`: 상태 저장
  - `permission_handler`: 권한 관리

### 📦 설치 방법

#### 1. Flutter 개발 환경 설정

Flutter가 설치되어 있지 않다면 [Flutter 공식 문서](https://docs.flutter.dev/get-started/install)를 참고하여 설치하세요.

```bash
flutter doctor
```

#### 2. 프로젝트 클론

```bash
git clone https://github.com/yourusername/noadsflashlight.git
cd noadsflashlight
```

#### 3. 의존성 설치

```bash
flutter pub get
```

#### 4. 앱 실행

```bash
flutter run
```

### 🏗️ 빌드 방법

#### APK 빌드 (디버그)

```bash
flutter build apk --debug
```

#### APK 빌드 (릴리즈)

```bash
flutter build apk --release
```

#### AAB 빌드 (Play Store 배포용)

```bash
flutter build appbundle --release
```

빌드된 파일은 `build/app/outputs/` 디렉토리에서 찾을 수 있습니다.

### 📂 프로젝트 구조

```
lib/
├── main.dart                           # 앱 진입점
├── screens/
│   └── home_screen.dart                # 메인 화면
├── services/
│   └── flashlight_service.dart         # 플래시 제어 로직
android/
├── app/src/main/
│   ├── kotlin/com/chojiwoong/noadsflashlight/
│   │   ├── MainActivity.kt             # 메인 액티비티
│   │   └── FlashlightWidget.kt         # 홈 화면 위젯
│   └── res/
│       ├── layout/
│       │   └── flashlight_widget.xml   # 위젯 레이아웃
│       └── xml/
│           └── flashlight_widget_info.xml  # 위젯 정보
```

### 🎯 사용 방법

#### 앱 사용

1. 앱을 실행합니다
2. 처음 실행 시 카메라 권한을 허용합니다
3. 화면 중앙의 큰 버튼을 눌러 플래시를 켜거나 끕니다
4. 슬라이더를 움직여 밝기를 조절합니다 (일부 기기에서만 지원)

#### 위젯 사용

1. 홈 화면을 길게 눌러 위젯 추가 모드로 진입합니다
2. "No Ads Flashlight" 위젯을 찾습니다
3. 위젯을 홈 화면에 배치합니다
4. 위젯 크기를 정사각형 비율로 조절할 수 있습니다
5. 위젯을 탭하여 플래시를 켜거나 끕니다

### ⚠️ 알려진 제한사항

- **밝기 조절**: 대부분의 안드로이드 기기에서 플래시 밝기 조절은 하드웨어적으로 지원되지 않습니다. 
  일부 삼성 갤럭시 기기 등 특정 기기에서만 밝기 조절이 가능할 수 있습니다.

### 🤝 기여하기

기여는 언제나 환영합니다! 다음과 같은 방법으로 참여하실 수 있습니다:

1. 이 저장소를 Fork 합니다
2. 새로운 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 Push 합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성합니다

### 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

### 👨‍💻 개발자

- **조지웅** - [chojiwoong.com](https://chojiwoong.com)
- Email: chojiwoong.dev@gmail.com

### 🔒 개인정보처리방침

- [https://jiwoong26.github.io/no_add_flashlight/privacy-policy](https://jiwoong26.github.io/no_add_flashlight/privacy-policy)

### 💬 문의 및 버그 제보

버그를 발견하거나 기능 제안이 있으시면 [Issues](https://github.com/yourusername/noadsflashlight/issues) 페이지에서 알려주세요.

---

## <a name="english"></a>English

### 📱 Project Overview

**No Ads Flashlight** is a completely ad-free flashlight app. 
Many flashlight apps on the Play Store are cluttered with ads and difficult to use, 
so I created this app for my parents and everyone to use comfortably.

### ✨ Key Features

- **🔦 Flashlight ON/OFF**: Easily turn the flashlight on and off with a large button
- **🎚️ Brightness Control**: Adjust brightness with a slider (supported on some devices)
- **📱 Android Home Screen Widget**: Control the flashlight without opening the app
  - Widget size can be adjusted in square ratios (1x1, 2x2, 3x3, etc.)
  - Icon and color change based on flashlight state
- **🎨 Modern Design**: Clean UI with Material Design 3
- **🌓 Dark Mode**: Automatic light/dark theme switching based on system settings
- **🚫 No Ads**: Completely free app with no advertisements

### 📸 Screenshots

_To be added_

### 🛠️ Tech Stack

- **Framework**: Flutter 3.35.6
- **Language**: Dart 3.9.2
- **Platform**: Android (Minimum API 21 / Android 5.0+)
- **Main Packages**:
  - `torch_light`: Flashlight control
  - `home_widget`: Android home screen widget
  - `shared_preferences`: State persistence
  - `permission_handler`: Permission management

### 📦 Installation

#### 1. Set up Flutter Development Environment

If Flutter is not installed, please refer to the [official Flutter documentation](https://docs.flutter.dev/get-started/install).

```bash
flutter doctor
```

#### 2. Clone the Project

```bash
git clone https://github.com/yourusername/noadsflashlight.git
cd noadsflashlight
```

#### 3. Install Dependencies

```bash
flutter pub get
```

#### 4. Run the App

```bash
flutter run
```

### 🏗️ Build Instructions

#### Build APK (Debug)

```bash
flutter build apk --debug
```

#### Build APK (Release)

```bash
flutter build apk --release
```

#### Build AAB (for Play Store)

```bash
flutter build appbundle --release
```

Built files can be found in the `build/app/outputs/` directory.

### 📂 Project Structure

```
lib/
├── main.dart                           # App entry point
├── screens/
│   └── home_screen.dart                # Main screen
├── services/
│   └── flashlight_service.dart         # Flashlight control logic
android/
├── app/src/main/
│   ├── kotlin/com/chojiwoong/noadsflashlight/
│   │   ├── MainActivity.kt             # Main activity
│   │   └── FlashlightWidget.kt         # Home screen widget
│   └── res/
│       ├── layout/
│       │   └── flashlight_widget.xml   # Widget layout
│       └── xml/
│           └── flashlight_widget_info.xml  # Widget info
```

### 🎯 How to Use

#### Using the App

1. Launch the app
2. Allow camera permission on first run
3. Tap the large button in the center to turn the flashlight on or off
4. Move the slider to adjust brightness (supported on some devices only)

#### Using the Widget

1. Long press on the home screen to enter widget mode
2. Find the "No Ads Flashlight" widget
3. Place the widget on your home screen
4. You can resize the widget in square ratios
5. Tap the widget to turn the flashlight on or off

### ⚠️ Known Limitations

- **Brightness Control**: Flashlight brightness adjustment is not supported by hardware on most Android devices. 
  Only certain devices, such as some Samsung Galaxy phones, may support brightness control.

### 🤝 Contributing

Contributions are always welcome! You can participate in the following ways:

1. Fork this repository
2. Create a new branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

### 📝 License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for more details.

### 👨‍💻 Developer

- **Jiwoong Cho** - [chojiwoong.com](https://chojiwoong.com)
- Email: chojiwoong.dev@gmail.com

### 🔒 Privacy Policy

- [https://jiwoong26.github.io/no_add_flashlight/privacy-policy](https://jiwoong26.github.io/no_add_flashlight/privacy-policy)

### 💬 Contact & Bug Reports

If you find bugs or have feature suggestions, please let us know on the [Issues](https://github.com/yourusername/noadsflashlight/issues) page.

---

<div align="center">

Made with ❤️ by Jiwoong Cho

</div>
