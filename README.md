<div align="center">

![No, thanks! banner](assets/banner.png)

# No, thanks!

*The excuse widget you never knew you needed.*

</div>

A minimal Android home screen widget that serves you random excuses to gracefully say "no" — powered by the [No as a Service](https://github.com/hotheadhacker/no-as-a-service) API.


## Download

<div align="center">

[![Latest Release](https://img.shields.io/github/v/release/codebydusk/no-thanks?style=for-the-badge&logo=android&label=Latest+APK&color=FFCB47)](https://github.com/codebydusk/no-thanks/releases/latest)
&nbsp;
[![Total Downloads](https://img.shields.io/github/downloads/codebydusk/no-thanks/total?style=for-the-badge&color=1E1E24&label=Total+Downloads)](https://github.com/codebydusk/no-thanks/releases)

**[⬇ Download latest APK](https://github.com/codebydusk/no-thanks/releases/latest)** &nbsp;·&nbsp; [View all releases →](https://github.com/codebydusk/no-thanks/releases)

</div>

> **Note:** The APK is unsigned (debug-signed by the CI runner). Android may show an "unknown source" prompt on first install — this is expected. Enable *Install from unknown sources* for your browser or file manager.

### Release History

| Version | Date | Notes |
| --- | --- | --- |
| [v1.0.1](https://github.com/codebydusk/no-thanks/releases/tag/v1.0.1) | May 31, 2026 | Added text size adjustment (Small/Normal/Large) · Added emojis to loading and copy confirmation messages · Updated Copy Prefix description to clarify on/off behavior · "No, thanks!" prefix now respects Copy Prefix setting in widget display |
| [v1.0.0](https://github.com/codebydusk/no-thanks/releases/tag/v1.0) | May 30, 2026 | Initial release |

> Older releases are always available on the [GitHub Releases page](https://github.com/codebydusk/no-thanks/releases).


## Features

### Widget

- **Switchable 4×2 / 4×1 layout** — resizes automatically based on the space you give it
- **"No, thanks!" prefix** automatically prepended to every excuse
- **Scrollable text** in 4×2 mode for longer excuses; up to 3 lines in 4×1 mode
- **Refresh button** (↻) to fetch a new excuse from the API
- **Previous button** (←) to browse through your last 10 excuses (toggle-able in settings)
- **Tap to copy** or **dedicated copy button** — configurable in settings
- **Copy with or without prefix** — toggle whether "No, thanks!" is included in copied text
- **Hilarious copy confirmation with emojis** shown for 2 seconds after copying (randomly chosen from 10 messages)
- **Themed loading messages with emojis** — fun quips while the API is being called, no boring spinner
- **Sarcastic fallback messages** when the API is unreachable

### Settings App

- **Appearance** — System default / Light / Dark
- **Widget Theme** — 3 themes: Nothing OS, Golden Silence, System
- **Corner Style** — Pill / Rounded / Sharp
- **Text Size** — Small / Normal / Large
- **Copy Mechanism** — Tap text to copy, or show a dedicated copy button
- **Copy Prefix** — Toggle whether "No, thanks!" is included when copying
- **Navigation** — Toggle the previous (←) button on/off

## Widget Themes

| Theme | Font | Light Mode | Dark Mode |
| --- | --- | --- | --- |
| **Nothing OS** *(default)* | Space Grotesk | White (`#fdfbff`) bg · dark (`#1b1b1d`) text · grey (`#5e5e62`) nav · **red** (`#d71921`) refresh | Near-black (`#1b1b1d`) bg · white (`#fdfbff`) text · grey (`#5e5e62`) nav · **red** (`#d71921`) refresh |
| **Golden Silence** | Metamorphous | Gold (`#FFCB47`) bg · charcoal (`#1E1E24`) text · all controls charcoal | Charcoal (`#1E1E24`) bg · gold (`#FFCB47`) text · all controls gold |
| **System** | System default | Uses your device's **Material You** wallpaper-derived colors (Android 12+) | Uses your device's **Material You** wallpaper-derived colors (Android 12+) |

> Each theme automatically adapts to your device's system dark/light preference unless you override it in settings.
>
> **System** theme reads your device's dynamic accent color on Android 12+. On older devices, it falls back to neutral Material3 defaults.

### 🎨 Want to add your phone's theme?

I designed the Nothing OS theme because that's the phone I carry every day — but this project is built for everyone. If you rock a Samsung, OnePlus, Pixel, or any other device, I'd love to see your phone's aesthetic represented here.

Here's how you can contribute a new theme:

1. **Fork** this repo
2. Pick your colors — background, text, navigation, and accent for both light and dark modes
3. Add your theme constant in `ExcuseRepository.kt`, colors in `NoThanksWidget.kt`, and the option in `MainActivity.kt`
4. Open a **Pull Request** — I'll review it personally and merge it in

Every contribution makes this app feel more like home for someone. Don't hesitate — even if it's your first PR ever, you're welcome here. 💛

## Corner Styles

| Style | Corner Radius | Description |
| --- | --- | --- |
| **Pill** | 50 dp | Fully rounded, pill/capsule shape |
| **Rounded** | 8 dp | Gentle, Samsung-style corner rounding |
| **Sharp*** | 0 dp | True square with zero rounding |

> **Sharp** corners may appear rounded on some launchers due to system-level widget rounding that cannot be overridden by the app.
>
> | Launcher / OS | Sharp corners respect? | Notes |
> | --- | --- | --- |
> | **Pixel Launcher** (stock Android) | ❌ Rounded | Google enforces ~28 dp rounding on all widgets since Android 12 |
> | **Samsung One UI Home** | ❌ Rounded | One UI applies its own minimum corner radius to all widgets |
> | **Nothing Launcher** (Nothing OS) | ❌ Rounded | System-level rounding may override Sharp on most devices |
> | **OxygenOS Launcher** (OnePlus) | ✅ Works | Generally respects 0 dp corners on stock launcher |
> | **Nova Launcher** (third-party) | ✅ Works | Custom launchers usually pass widget radii through unchanged |
> | **Lawnchair** (third-party) | ✅ Works | No additional rounding applied |
> | **MIUI / HyperOS** (Xiaomi) | ⚠️ Partial | Some devices override, some don't — varies by ROM version |
> | **EMUI** (Huawei) | ⚠️ Partial | Behaviour depends on Android version and EMUI skin |
>
> *Disclaimer: Launcher behaviour can change with OS or launcher updates. The table above reflects observations at time of writing and may not hold across all versions.*

## Text Size

| Size | Scale | Description |
| --- | --- | --- |
| **Small** | 85% | Compact text size for fitting more content |
| **Normal** *(default)* | 100% | Standard readable text size |
| **Large** | 125% | Enlarged text for better readability |

> Text size scales proportionally across all themes while maintaining visual consistency.

## Appearance Modes

| Mode | Behaviour |
| --- | --- |
| **System** | Follows the device's light/dark setting automatically |
| **Light** | Always uses the light variant of the selected theme |
| **Dark** | Always uses the dark variant of the selected theme |

## Copy Mechanism

| Option | Behaviour |
| --- | --- |
| **Tap text** | Tap anywhere on the excuse text to copy it to the clipboard |
| **Copy button** | Shows a dedicated copy icon (📋) next to the refresh button |

> By default, only the raw excuse text is copied. Use the **Copy Prefix** toggle in settings to include "No, thanks!" in the copied text.

## Copy Confirmations

After copying, one of these emoji-filled messages is shown at random for 2 seconds:

| | |
| --- | --- |
| That's a copy, Houston. 📋 | Snagged! Use it wisely. 🏯 |
| Ctrl+C executed. Godspeed. ⚡ | In your clipboard. No refunds. 📱 |
| Excuse extracted with prejudice. 🔪 | Pasted into your soul. 🌀 |
| That excuse is now legally yours. ⚖️ | Copy secured. Mission complete. ✅ |
| Yours now. Don't abuse it. 🤐 | Clipboard hijacked. You're welcome. 😎 |

## Theme-Specific Loading Messages

While fetching excuses, theme-appropriate loading messages with emojis appear:

### Nothing OS 🎯
- 📡 signal: searching...
- 🔊 beep. boop. thinking.
- // excuse.render() 💻
- ⏳ nothing to show yet...
- ✨ glyphs assembling...

### Golden Silence ⚜️
- ⚜️ summoning ancient wisdom...
- 🏰 the scroll is unrolling...
- 📜 consulting the archives...
- 🗡️ forging your refusal...
- 👑 the royal decree approaches...

### System ⏳
- ⏳ fetching your excuse...
- 🔄 loading something clever...
- 💭 thinking of a good one...
- 📡 connecting to excuse server...
- ✨ generating brilliance...

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
| Fonts | Metamorphous, Space Grotesk (via Google Fonts) |
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
    ├── Color.kt                     # Golden Silence palette constants
    ├── Theme.kt                     # GoldenSilenceTheme composable
    └── Type.kt                      # Metamorphous font loading + typography
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

### Build debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Install directly on a connected device

```bash
./gradlew installDebug
```

### Adding the Widget

1. Open the **No, thanks!** app once to initialize settings
2. Long-press on your home screen → **Widgets**
3. Find **No, thanks!** and drag it to your home screen
4. Resize to **4×1** (compact, up to 3 lines) or **4×2** (expanded, scrollable)
5. Open the app to change theme, corner style, appearance, and copy behaviour

## Credits

- **[No as a Service](https://github.com/hotheadhacker/no-as-a-service)** by [@hotheadhacker](https://github.com/hotheadhacker) — for the brilliant idea and the API that powers this widget
- Built with [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) and [Jetpack Compose](https://developer.android.com/jetpack/compose)

## License

This project is open source and available under the [MIT License](LICENSE).
