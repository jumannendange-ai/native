# JAYNES MAX TV — Native Android APK
## Jinsi ya Build APK

---

### HATUA 1: Pakua Android Studio
https://developer.android.com/studio

---

### HATUA 2: Fungua Project
1. Android Studio → **Open**
2. Chagua folder hii (`jaynes_native`)
3. Subiri Gradle sync ikamilike (dakika 3-5)

---

### HATUA 3: Weka API URL yako
Fungua: `app/src/main/java/com/jaynes/maxtv/JaynesApp.java`

Badilisha:
```java
public static final String API_BASE = "https://api-jaynestvmax2.onrender.com";
```

---

### HATUA 4: Weka SUPABASE_SERVICE_KEY kwenye Render
Nenda Render → api.jaynestvmax2 → Environment → Ongeza:
```
SUPABASE_SERVICE_KEY = [service key yako kutoka Supabase → Settings → API]
```
Kisha upload `server_UPDATED.js` kama `server.js` mpya kwenye GitHub.

---

### HATUA 5: Build APK
Android Studio → **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**

APK itapatikana:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

### HATUA 6: Install kwenye simu
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
Au transfer APK kwa USB/Bluetooth.

---

## Muundo wa App

```
Splash Screen
    ↓ (API init + update check)
Login / Register  ←→  (Supabase via /api/auth)
    ↓
Main Screen (Channels Grid)
    ↓ (click channel)
Player (ExoPlayer + ClearKey DRM)
```

## API Endpoints Inayotumia
- `POST /api/auth?action=login`
- `POST /api/auth?action=register`
- `POST /api/auth?action=reset_password`
- `GET  /api/app/init`
- `GET  /api/channels/all`
- `GET  /api/health`

## Pricing (TZS)
| Plan     | Bei    |
|----------|--------|
| Wiki 1   | 1,000  |
| Mwezi 1  | 3,000  |
| Miezi 3  | 8,000  |
| Miezi 6  | 15,000 |
| Mwaka 1  | 25,000 |

## Mawasiliano
WhatsApp: 0616393956
