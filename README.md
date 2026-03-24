# JAYNES MAX TV — Native Android Source Code

## Muundo wa Project
```
JaynesMaxTV/
├── app/
│   ├── google-services.json          ✅ Firebase config
│   ├── build.gradle                  ✅ Dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml       ✅
│       ├── java/com/jaynes/maxtv/
│       │   ├── JaynesApp.java        ✅ OneSignal init
│       │   ├── SplashActivity.java   ✅ App init + update check
│       │   ├── LoginActivity.java    ✅ Login/Register → /api/auth
│       │   ├── MainActivity.java     ✅ Channels grid
│       │   ├── PlayerActivity.java   ✅ ExoPlayer + ClearKey DRM
│       │   ├── ChannelAdapter.java   ✅ RecyclerView adapter
│       │   ├── Channel.java          ✅ Data model
│       │   ├── ApiClient.java        ✅ HTTP → API server
│       │   ├── SessionManager.java   ✅ SharedPreferences auth
│       │   └── JaynesFCMService.java ✅ Push notifications
│       └── res/
│           ├── layout/               ✅ 4 layouts
│           ├── values/               ✅ colors, strings, themes
│           ├── drawable/             ✅ icons, placeholder
│           ├── mipmap-*/             ✅ adaptive icons
│           └── xml/network_security_config.xml ✅
├── server/
│   ├── server.js                     ✅ + /api/auth endpoint mpya
│   └── package.json
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Hatua za Setup

### 1. Server — Ongeza ENV variables kwenye Render.com
```
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_ANON_KEY=your_anon_key
SUPABASE_SERVICE_KEY=your_service_role_key
ADMIN_KEY=your_admin_key
ONESIGNAL_APP_ID=your_onesignal_app_id
ONESIGNAL_API_KEY=your_onesignal_rest_key
```

### 2. Android — Badilisha placeholders
- `JaynesApp.java` line 15: `YOUR_ONESIGNAL_APP_ID` → OneSignal App ID yako
- `ApiClient.java` line 20: URL imeshasettwa → `https://jaynes-max-tv-api.onrender.com`

### 3. Icons (optional — sasa ni vector icons)
Badilisha na PNG halisi:
- `mipmap-mdpi/ic_launcher.png`     → 48×48
- `mipmap-hdpi/ic_launcher.png`     → 72×72
- `mipmap-xhdpi/ic_launcher.png`    → 96×96
- `mipmap-xxhdpi/ic_launcher.png`   → 144×144
- `mipmap-xxxhdpi/ic_launcher.png`  → 192×192

### 4. Build APK
```bash
# Debug APK (test)
./gradlew assembleDebug

# Release APK (playstore)
./gradlew assembleRelease
```
APK itapatikana: `app/build/outputs/apk/`

## API Endpoints Zinazotumika
| Endpoint | Matumizi |
|---|---|
| GET /api/app/init | Splash — config + update check |
| POST /api/auth | Login / Register / Me / Logout |
| GET /api/channels/all | Channels zote na clearkeys |
| GET /api/update/status | Update check |

## Auth Flow
```
SplashActivity → GET /api/app/init
     ↓ (token ipo)        ↓ (token haipo)
MainActivity         LoginActivity
                          ↓
                    POST /api/auth { action:"login" }
                          ↓
                    SessionManager.saveSession()
                          ↓
                    MainActivity
```

## DRM Flow (ClearKey)
```
Channel.hasDrm = true
Channel.key = "kid_hex:key_hex"
     ↓
PlayerActivity.buildDrmPlayer()
     ↓
hexToBase64Url(kid) + hexToBase64Url(key)
     ↓
ClearKey JSON license → LocalMediaDrmCallback
     ↓
ExoPlayer DASH playback ✅
```
