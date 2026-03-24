# 📺 JAYNES MAX TV — Flutter Native Android App

Tanzania Live TV Streaming — Native APK (ExoPlayer, sio WebView)

---

## 🚀 Quick Start (Local Build)

### 1. Mahitaji (Requirements)
- Flutter SDK 3.22+ → [flutter.dev](https://flutter.dev)
- Android Studio / VS Code
- Java 17+
- Git

### 2. Setup
```bash
git clone https://github.com/WEWE/jaynes_max_tv.git
cd jaynes_max_tv
chmod +x setup.sh
./setup.sh
```

Script itafanya hivi kiotomatiki:
- Download fonts (BebasNeue + Rajdhani)
- `flutter pub get`
- Generate launcher icons

### 3. Build APK
```bash
# Debug
flutter build apk --debug

# Release
flutter build apk --release
```

APK iko: `build/app/outputs/flutter-apk/app-release.apk`

---

## ☁️ Codemagic CI/CD

### Hatua za Codemagic:

1. **Push project GitHub**
   ```bash
   git init
   git add .
   git commit -m "JAYNES MAX TV v1.0.0"
   git remote add origin https://github.com/WEWE/jaynes_max_tv.git
   git push -u origin main
   ```

2. **Codemagic Dashboard** → [codemagic.io](https://codemagic.io)
   - Add application → Flutter App
   - Link GitHub repo
   - Workflow: `android-release` (tayari iko `codemagic.yaml`)

3. **Weka Environment Variables** (Settings → Environment variables):
   | Variable | Value |
   |----------|-------|
   | `CM_KEYSTORE` | Base64 ya keystore yako |
   | `CM_KEYSTORE_PASSWORD` | Password ya keystore |
   | `CM_KEY_ALIAS` | Alias ya key |
   | `CM_KEY_PASSWORD` | Password ya key |

4. **Start Build** → APK itapatikana moja kwa moja!

---

## 🔧 Muundo wa Project

```
jaynes_max_tv/
├── lib/
│   ├── main.dart                    # Entry point
│   ├── utils/app_theme.dart         # Colors, fonts, constants
│   ├── services/
│   │   ├── api_service.dart         # Backend API client
│   │   ├── channel_provider.dart    # Channels state
│   │   └── auth_provider.dart       # Subscription state
│   └── screens/
│       ├── splash_screen.dart       # Animated splash
│       ├── main_screen.dart         # Bottom navigation
│       ├── home_screen.dart         # Home page
│       ├── channels_screen.dart     # Channels list + search
│       ├── player_screen.dart       # Native ExoPlayer
│       ├── subscribe_screen.dart    # Subscription plans
│       └── account_screen.dart      # Account
├── android/                         # Android native files
├── assets/
│   ├── fonts/                       # BebasNeue, Rajdhani (downloaded by setup.sh)
│   └── images/                      # App icon
├── codemagic.yaml                   # CI/CD config
├── pubspec.yaml                     # Dependencies
└── setup.sh                         # Setup script
```

---

## 🌐 Backend API

Backend iko: `https://jaynes-max-tv-api.onrender.com`

Deploy backend (Node.js) kwenye Render.com:
1. Push `server.js` + `package.json` GitHub
2. Render → New Web Service → link repo
3. Build command: `npm install`
4. Start command: `node server.js`

---

## 📦 Dependencies Muhimu

| Package | Kazi |
|---------|------|
| `better_player` | Native ExoPlayer HLS+DASH+DRM |
| `dio` | HTTP client kwa API |
| `provider` | State management |
| `onesignal_flutter` | Push notifications |
| `cached_network_image` | Channel logos |
| `shared_preferences` | Local storage |

---

## 🎨 Design System

| Color | Hex | Matumizi |
|-------|-----|---------|
| Primary | `#E8001D` | Buttons, badges, accents |
| Accent | `#FF6B00` | Gradient, premium icons |
| Background | `#0A0A0F` | App background |
| Surface | `#111118` | Cards, nav bar |
| Text | `#F0F0F8` | Main text |
| Muted | `#888899` | Subtitles, labels |

Fonts: **Bebas Neue** (headings) + **Rajdhani** (body)

---

## 💳 Subscription Plans

| Plan | Siku | Bei (TZS) |
|------|------|-----------|
| Wiki | 7 | 3,000 |
| Mwezi | 30 | 5,000 |
| Miezi 3 | 90 | 12,000 |
| Miezi 6 | 180 | 20,000 |
| Mwaka | 365 | 35,000 |

---

## 📞 Msaada

Maswali: WhatsApp / Email ya JAYNES MAX TV
