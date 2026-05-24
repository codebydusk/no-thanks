# No, Thanks!

A minimal Android home screen widget that serves you random excuses to gracefully say "no" — powered by the [No as a Service](https://github.com/hotheadhacker/no-as-a-service) API.

## Features

### Widget
- **Switchable 4×2 / 4×1 layout** — resizes automatically based on the space you give it
- **Flat two-color design** — foreground and background only, no visual noise
- **Scrollable text** in 4×2 mode for longer excuses
- **Refresh button** (↻) to fetch a new excuse from the API
- **Previous button** (←) to browse through your last 10 excuses
- **Tap to copy** or **dedicated copy button** — configurable in settings
- **"Copied!" feedback** shown for 2 seconds after copying
- **Sarcastic fallback messages** when the API is unreachable

### Settings App
- **Appearance** — System / Light / Dark mode
- **Widget Theme** — Material, Nothing OS, or Samsung One UI style
- **Corner Style** — Round or Square
- **Copy Mechanism** — Tap text or show a copy button
- **Navigation** — Toggle the previous (←) button on/off

## Screenshots

*Coming soon*

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI Framework | Jetpack Compose + Material 3 |
| Widget Framework | Jetpack Glance 1.1.1 |
| Networking | Retrofit 2.11 + Gson |
| Persistence | DataStore Preferences |
| Build System | Gradle (AGP 9.2.1) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

## Project Structure

```
app/src/main/java/com/github/codebydusk/nothanks/
├── MainActivity.kt              # Settings screen (Jetpack Compose)
├── data/
│   ├── ExcuseApi.kt             # Retrofit API interface
│   └── ExcuseRepository.kt      # Data layer — API calls, history, settings
├── widget/
│   ├── NoThanksWidget.kt        # Glance widget UI & theming
│   ├── NoThanksWidgetReceiver.kt# GlanceAppWidgetReceiver
│   └── WidgetActions.kt         # ActionCallbacks — refresh, history, copy
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## API

Uses [No as a Service](https://naas.isalman.dev/) — a free API that returns random excuses.

```
GET https://naas.isalman.dev/no
```

Response:
```json
{
  "reason": "I signed up for a 'Just Say No' workshop and I'm committed to practicing."
}
```

## Building & Running

### Prerequisites
- Android Studio (latest stable)
- JDK 11+
- An Android device or emulator (API 26+)

### Build
```bash
./gradlew assembleDebug
```

### Install on a connected device
```bash
./gradlew installDebug
```

### Adding the Widget
1. Open the **No, Thanks!** app once to initialize settings
2. Long-press on your home screen → **Widgets**
3. Find **No, Thanks!** and drag it to your home screen
4. Resize to 4×1 (compact) or 4×2 (expanded with scrollable text)

## Widget Themes

| Theme | Dark | Light |
|---|---|---|
| **Nothing OS** | Black bg, Nothing red (#D71921) text, bold font | White bg, black text, bold font |
| **Samsung One UI** | Warm dark (#1A1A1A), warm light text | Warm light (#F7F7F7), dark text |
| **Material** | Standard surface (#1C1B1F) | Standard surface (#FFFBFE) |

## Credits

- **[No as a Service](https://github.com/hotheadhacker/no-as-a-service)** by [@hotheadhacker](https://github.com/hotheadhacker) — for the brilliant idea and the API that powers this widget
- Built with [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) and [Jetpack Compose](https://developer.android.com/jetpack/compose)

## License

This project is open source and available under the [MIT License](LICENSE).