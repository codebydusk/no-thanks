# No, thanks!

A minimal Android home screen widget that serves you random excuses to gracefully say "no" — powered by the [No as a Service](https://github.com/hotheadhacker/no-as-a-service) API.

## Features

### Widget

- **Switchable 4×2 / 4×1 layout** — resizes automatically based on the space you give it
- **"No, thanks!" prefix** automatically prepended to every excuse
- **Scrollable text** in 4×2 mode for longer excuses; up to 3 lines in 4×1 mode
- **Refresh button** (↻) to fetch a new excuse from the API
- **Previous button** (←) to browse through your last 10 excuses (toggle-able in settings)
- **Tap to copy** or **dedicated copy button** — configurable in settings
- **Hilarious copy confirmation** shown for 2 seconds after copying (randomly chosen from 10 messages)
- **Theme-matched loading messages** — funny quips while the API is being called (no boring spinner)
- **Sarcastic fallback messages** when the API is unreachable

### Settings App

- **Appearance** — System / Light / Dark mode
- **Widget Theme** — Blueprint, Material, Nothing OS, Samsung One UI, OnePlus
- **Corner Style** — Pill / Rounded / Sharp
- **Copy Mechanism** — Tap text or show a dedicated copy button
- **Navigation** — Toggle the previous (←) button on/off

## Screenshots

*Coming soon*

## Tech Stack

| Component | Technology |
| --- | --- |
| Language | Kotlin 2.2 |
| UI Framework | Jetpack Compose + Material 3 |
| Widget Framework | Jetpack Glance 1.1.1 |
| Networking | Retrofit 2.11 + Gson |
| Persistence | DataStore Preferences |
| Build System | Gradle (AGP 9.2.1) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

## Project Structure

```text
app/src/main/java/com/github/codebydusk/nothanks/
├── MainActivity.kt                  # Settings screen (Jetpack Compose)
├── data/
│   ├── ExcuseApi.kt                 # Retrofit API interface
│   └── ExcuseRepository.kt          # Data layer — API calls, history, settings, message banks
├── widget/
│   ├── NoThanksWidget.kt            # Glance widget UI & theming
│   ├── NoThanksWidgetReceiver.kt    # GlanceAppWidgetReceiver
│   └── WidgetActions.kt             # ActionCallbacks — refresh, history, copy
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## API

Uses [No as a Service](https://naas.isalman.dev/) — a free API that returns random excuses.

```text
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

1. Open the **No, thanks!** app once to initialize settings
2. Long-press on your home screen → **Widgets**
3. Find **No, thanks!** and drag it to your home screen
4. Resize to 4×1 (compact) or 4×2 (expanded with scrollable text)

## Widget Themes

| Theme | Style | Dark Mode | Light Mode |
| --- | --- | --- | --- |
| **Blueprint** *(default)* | Digital Blue palette | Near-black (#000E24) bg, sky-blue (#CCE0FF) text, vivid blue (#3385FF) accent | Light blue (#E5F0FF) bg, deep navy (#002966) text, brand blue (#0052CC) accent |
| **Material** | MD3 standard | Dark surface (#1C1B1F), light text | Light surface (#FFFBFE), dark text |
| **Nothing OS** | Monospace, dot-matrix | Pure black, white text, **red** (#D71921) refresh | Pure white, black text, red refresh |
| **Samsung One UI** | Rounded sans-serif | Warm dark (#1A1A1A), warm light text, blue accent | Warm light (#F7F7F7), dark text, Samsung blue accent |
| **OnePlus** | Clean OxygenOS | Near-black (#0F0F0F), light text, **red** (#F6000D) refresh | Near-white (#FAFAFA), dark text, red refresh |

## Corner Styles

| Style | Radius | Look |
| --- | --- | --- |
| **Pill** | 50dp | Fully rounded pill shape |
| **Rounded** | 8dp | Gentle Samsung-style corners |
| **Sharp** | 0dp | True square, no rounding |

## Credits

- **[No as a Service](https://github.com/hotheadhacker/no-as-a-service)** by [@hotheadhacker](https://github.com/hotheadhacker) — for the brilliant idea and the API that powers this widget
- Built with [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) and [Jetpack Compose](https://developer.android.com/jetpack/compose)

## License

This project is open source and available under the [MIT License](LICENSE).
