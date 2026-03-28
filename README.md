# Player4Home

An Android IPTV media player inspired by [player.ps](https://player.ps/en), built with Jetpack Compose and Material3.

**No desktop or Mac required** — add and manage playlists entirely from your Android device.

---

## Screenshots

> Coming soon — build and run the app to see it in action.

---

## Features

| Feature | Details |
|---|---|
| **Playlist upload (URL)** | Paste any M3U/M3U8 URL — parsed and stored locally |
| **Playlist upload (File)** | Pick M3U files directly from device storage using Android's native file picker (SAF) — no PC needed |
| **Playlist upload (Xtream)** | Enter host, username, password — live channels + VOD imported automatically |
| **Live TV** | HLS/RTSP/TS stream playback via Media3/ExoPlayer |
| **VOD & Series** | Separate categories with group filtering |
| **Background playback** | Media3 `MediaSessionService` keeps audio/video playing when app is minimized |
| **Notification controls** | Play/pause/next from the notification drawer |
| **PIN protection** | Lock individual playlists with a 4-digit PIN |
| **Category filtering** | Filter channels by Live / VOD / Series or search by name |
| **Playlist management** | Edit, delete, reorder playlists |
| **EPG** | Electronic Program Guide support (tvg-id based) |
| **Material3 dark theme** | Deep Navy + Electric Teal + Warm Amber palette |

---

## Color Palette

| Role | Color |
|---|---|
| Background | `#06101E` Deep Navy |
| Surface | `#0A1628` |
| Card | `#1A2744` |
| Primary | `#00BFA5` Electric Teal |
| Secondary | `#FFB300` Warm Amber |

---

## Architecture

```
app/
├── data/
│   ├── db/          — Room database (Playlist, Channel, EpgEntry DAOs)
│   ├── model/       — Data entities
│   └── repository/  — PlaylistRepository (single source of truth)
├── domain/          — Use case layer (future expansion)
├── ui/
│   ├── theme/       — Material3 color scheme, typography
│   ├── navigation/  — NavGraph, Screen sealed class
│   ├── screens/     — Home, Playlists, PlaylistDetail, Upload, Player, Settings
│   └── components/  — BottomNavBar, PlaylistCard, ChannelRow, DeleteConfirmDialog
├── service/         — PlaybackService (MediaSessionService)
└── util/            — M3uParser, XtreamApi
```

**Stack:** Kotlin · Jetpack Compose · Material3 · MVVM · Room · Media3/ExoPlayer · Hilt · Coroutines/Flow · Coil · OkHttp

---

## How to Build

### Prerequisites
- Android Studio Iguana (or later)
- Android SDK 35
- JDK 11+

### Steps

```bash
git clone https://github.com/inenadic/player4home.git
cd player4home
```

Open the project in Android Studio. It will auto-sync and download dependencies.

Or from the command line (requires Android SDK):

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## How to Use the In-App Upload Feature

Player4Home lets you add playlists **entirely from your Android device** — no PC, no ADB, no desktop tools required.

### Method 1 — URL
1. Open the app → tap **Upload** in the bottom nav
2. Enter a playlist name
3. Paste your M3U/M3U8 URL
4. Tap **Save Playlist**

### Method 2 — File (M3U from device storage)
1. Tap **Upload** → select the **File** tab
2. Enter a playlist name
3. Tap **Browse Files** — Android's native file picker opens
4. Navigate to your Downloads (or anywhere) and select your `.m3u` or `.m3u8` file
5. Tap **Save Playlist**

> No storage permission is required — Android's Storage Access Framework (SAF) gives you a scoped read without needing `READ_EXTERNAL_STORAGE`.

### Method 3 — Xtream Codes
1. Tap **Upload** → select the **Xtream** tab
2. Enter playlist name, host URL, username, and password
3. Tap **Save Playlist** — live channels and VOD are imported automatically

---

## Supported Formats

- M3U / M3U8 (URL and local file)
- Xtream Codes API (JSON)
- `tvg-id`, `tvg-name`, `tvg-logo`, `group-title` tags parsed automatically

---

## License

MIT — see [LICENSE](LICENSE)

---

## Disclaimer

This application does not provide any content. You are responsible for the playlists and content sources you use. The app is designed for use with your own or legally obtained playlists.
