# StreamNator — IPTV Player for Android TV

**StreamNator** is a native Android TV application for watching IPTV streams. It supports M3U playlist import, Xtream Codes API login, EPG program guides, VOD libraries, and D-pad optimized navigation — all designed for the big screen.

---

## Features

| Feature | Description |
|---|---|
| **M3U Playlist Import** | Load `.m3u` or `.m3u8` files from URLs or local storage |
| **Xtream Codes API** | Login with server URL, username, and password |
| **Live TV Browser** | Browse channels grouped by category with a Netflix-style dashboard |
| **Channel Dashboard** | Continue Watching, Recently Watched, and Favorites carousels |
| **EPG Program Guide** | Full grid-based Electronic Program Guide with XMLTV support |
| **VOD / Series** | Browse Video-on-Demand movies and TV series by category |
| **Favorites** | Add/remove channels with a single click via D-pad navigable star buttons |
| **Search** | Full-text search across all channels in your playlists |
| **Multi-Playlist** | Manage multiple playlists — switch between them |
| **D-Pad Optimized** | Every UI element is focusable and navigable with a remote control |
| **Background EPG Updates** | WorkManager keeps EPG data fresh in the background |
| **Offline-First** | Room database stores channels, favorites, and watch history locally |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 100% |
| **UI** | XML layouts with RecyclerView |
| **Architecture** | MVVM with LiveData + Coroutines |
| **Database** | Room (SQLite) with KSP |
| **Media Player** | AndroidX Media3 (ExoPlayer) with HLS support |
| **Networking** | OkHttp 4 + Retrofit 2 + Gson |
| **Image Loading** | Coil |
| **Background Work** | WorkManager |
| **M3U Parsing** | Custom parser (regex-based, handles `#EXTINF` tags) |
| **XMLTV Parsing** | Custom SAX-based parser for EPG data |
| **Build System** | Gradle Kotlin DSL |
| **Min SDK** | 21 (Android 5.0) |
| **Target SDK** | 34 (Android 14) |

---

## Project Structure

```
iptvnator/
├── app/
│   ├── build.gradle.kts          # App module dependencies
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/streamnator/app/
│       │   ├── StreamNatorApp.kt           # Application class (DI setup)
│       │   ├── data/
│       │   │   ├── db/                      # Room entities, DAOs, database
│       │   │   ├── network/                 # Retrofit + OkHttp + Xtream API
│       │   │   ├── repository/              # Repository layer
│       │   │   └── prefs/                   # SharedPreferences wrapper
│       │   ├── domain/model/                # Domain models
│       │   ├── service/workers/             # WorkManager background tasks
│       │   ├── ui/
│       │   │   ├── home/                    # Home screen + playlist management
│       │   │   ├── channels/                # Channel browser + dashboard
│       │   │   ├── player/                  # Video player with controls
│       │   │   ├── epg/                     # EPG grid guide
│       │   │   ├── vod/                     # VOD / Series browser
│       │   │   ├── search/                  # Global search
│       │   │   ├── settings/                # App settings
│       │   │   └── common/                  # Shared UI components
│       │   └── util/                        # Utility classes + parsers
│       └── res/
│           ├── drawable/                    # Focus borders, icons
│           ├── layout/                      # All XML layouts
│           ├── values/                      # Colors, strings, themes
│           ├── mipmap-*/                    # App launcher icons
│           └── xml/                         # Network security config
├── build.gradle.kts              # Root build file
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
├── gradle/wrapper/               # Gradle wrapper
├── .gitignore
└── README.md
```

---

## Getting Started

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17**
- **Android SDK 34**
- An Android TV device or emulator running Android 5.0+

### Build & Run

```bash
# Clone the repository
git clone https://github.com/Sewer2K/streamnator.git
cd streamnator

# Build the debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or open the project in Android Studio and click **Run**.

### Adding a Playlist

1. **M3U URL**: Home screen → Add Playlist → paste an M3U URL
2. **Xtream Codes**: Home screen → Xtream Codes Login → enter server URL, username, password
3. **Local File**: Place `.m3u` file in device storage and import

---

## Navigation (D-Pad / Remote)

| Action | Result |
|---|---|
| **UP/DOWN** | Scroll through channel list or sidebar |
| **LEFT/RIGHT** | Move between sidebar and content, or to favorite buttons |
| **OK / DPAD_CENTER** | Select channel, play, or toggle favorite |
| **Long-press OK** | Toggle favorite on a channel |
| **Back** | Return to previous screen |

---

## Key Design Decisions

### TextureView for Player Controls
The video player uses `TextureView` (not `SurfaceView`) so the on-screen controls bar renders on top of the video and remains fully interactive via D-pad and touch.

### Sibling Focus Architecture
Each channel row uses a non-focusable wrapper containing two sibling views:
- **`channel_info`** — the channel name/group (focusable, clickable)
- **`btn_fav`** — the favorite star button (focusable, clickable)

This prevents Android's focus system from hijacking navigation and keeps UP/DOWN scrolling natural while RIGHT reaches the favorite button.

### Dashboard vs Channel List
The sidebar's "Dashboard" item shows the Netflix-style carousels (Continue Watching, Recently Watched, Favorites). All other sidebar items (groups, All Channels, etc.) show the standard channel list. Views are toggled via visibility, not fragments.

---

## Dependencies

```kotlin
// Media3 (ExoPlayer)
androidx.media3:media3-exoplayer:1.3.1
androidx.media3:media3-exoplayer-hls:1.3.1
androidx.media3:media3-ui:1.3.1

// AndroidX
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
androidx.recyclerview:recyclerview:1.3.2
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0

// Room
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Networking
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0

// Image Loading
io.coil-kt:coil:2.5.0

// Background Work
androidx.work:work-runtime-ktx:2.9.0
```

---

## License

This project is licensed under the MIT License. See `LICENSE` for details.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

**Built for the big screen. Optimized for the remote.**
