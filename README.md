# Luma Calendar

Luma Calendar is a modern Android calendar application focused on a clean,
premium user experience. It supports Persian (Jalali), Gregorian, and Hijri
calendars with a dark, glassmorphism-inspired interface and full right-to-left
support for Persian.

## Project Status

> ⚠️ **Development Status: Active development — Known bugs and unfinished features remain.**

- The project is currently **under active development**.
- It is **not** considered production-ready yet.
- The project still contains known bugs and unfinished parts.
- Features and UI may change during development.
- Some functionality may not work perfectly on all devices.
- Contributions, testing, and bug reports are welcome.

## Features

Implemented:

- Persian (Jalali) calendar
- Gregorian calendar
- Hijri calendar
- Calendar type switching applied across the whole app
- Event management (create, edit, delete) stored locally
- Event search
- Event reminder notifications
- Persistent daily calendar notification
- Home screen widget showing the current day
- Dynamic launcher shortcut reflecting the current device day
- RTL layout for Persian/Jalali/Hijri, LTR for Gregorian
- Modern Liquid Glass / Glassmorphism UI
- Offline-first operation: the calendar, events, and reminders work
  without a network connection (events are stored in a local database)

Notes:

- Persian calendar day data can be enriched from a remote API when available,
  but the app falls back to local calendar conversion and cached data offline.

## Design

The visual direction is a premium, modern UI built around:

- Liquid Glass / Glassmorphism with frosted translucent surfaces
- Dark / OLED-friendly palette
- Subtle blue/violet ambient glow effects
- Rounded surfaces and generous spacing
- Vazirmatn typography for Persian text
- Native Android RTL/LTR layout handling driven by the active calendar type

## Calendar & Date Handling

Luma Calendar supports three calendar systems — **Jalali (Persian)**,
**Gregorian**, and **Hijri**. The active calendar type determines the primary
displayed dates and the layout direction (RTL for Jalali/Hijri, LTR for
Gregorian).

Date and time behavior is based on the device's **real local date and
timezone**. Calendar conversions go through a shared converter that uses the
Julian Day Number (JDN) as the canonical intermediate representation, so all
three calendar systems refer to the same real-world day.

## Notifications

Luma Calendar uses two **separate** notification systems:

- **Daily / persistent calendar notification** — a single ongoing notification
  (notification ID `1001`, channel `luma_calendar_daily`) showing today's date.
  It is updated in place on day changes, reboot, and timezone changes.
- **Event reminder notifications** — per-event reminders on their own channel
  (`event_reminders`) with event-specific notification IDs, scheduled via
  `AlarmManager`.

The two systems are independent: an event reminder never replaces or cancels
the daily notification, and vice versa.

## Tech Stack

- Kotlin `2.2.10`
- Jetpack Compose (Compose BOM `2024.09.00`) with Material 3
- Android Gradle Plugin `9.1.1`, Gradle wrapper `9.3.1`
- `minSdk` 24, `targetSdk` 36
- Room `2.7.0` (local database) with KSP `2.3.5`
- Retrofit `2.12.0` + Moshi `1.15.2` + OkHttp `4.10.0` (Persian calendar API)
- Robolectric `4.16.1` for unit tests
- Firebase (BOM `34.17.0`): Firebase AI and App Check dependencies are declared
  in the build; they are not central to the current feature set.

## Architecture

Single-module Android app (`:app`), single-Activity (`MainActivity`) with a
Jetpack Compose UI. The code is organized under
`com.aistudio.lumacalendar.vtxk`:

- `data/` — Room database, DAO, repositories, Persian calendar API + DTOs,
  holiday providers, and models
- `notification/` — daily notification manager, event reminder scheduler, and
  broadcast receivers
- `ui/` — Compose `screens/` (Calendar, Search, Settings), reusable
  `components/`, `theme/`, and a single `LumaViewModel` holding app state
- `util/` — `CalendarConverter`, `DateUtils`, `LocalizationManager`,
  validators, and `DynamicIconManager`
- `widget/` — home screen widget provider

State flows through the single `LumaViewModel` using Kotlin `StateFlow`.

## Build

The project uses the Gradle wrapper.

Debug build:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Release build (requires signing configuration — see below):

```bash
./gradlew clean assembleRelease
```

### Release build with ABI splits

ABI splits are gated behind a Gradle property so normal debug/IDE builds stay a
single universal APK. To produce per-ABI release APKs:

```bash
./gradlew clean assembleRelease -PabiSplits
```

This generates the following APKs under `app/build/outputs/apk/release/`:

- `LumaCalendar-universal-release.apk`
- `LumaCalendar-arm64-v8a-release.apk`
- `LumaCalendar-armeabi-v7a-release.apk`
- `LumaCalendar-x86_64-release.apk`
- `LumaCalendar-x86-release.apk`

Release signing reads `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`
from environment variables. A release build will not complete without a valid
keystore.

## GitHub Actions

Two workflows live in `.github/workflows/`:

- **`android-ci.yml`** — runs on every push and pull request. Sets up JDK 21,
  uses the Gradle wrapper with Gradle caching, runs unit tests, builds the
  normal release and the ABI-split release, and uploads the five APKs as
  build artifacts. This workflow does **not** create GitHub Releases.
- **`android-release.yml`** — runs only for version tags matching `v*.*.*`.
  It runs the tests, builds all ABI-split APKs, creates a GitHub Release, and
  attaches the five APK files directly as release assets.

Release builds use signing values supplied through GitHub repository secrets
(`KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`). The keystore is decoded
into a temporary file, used only for signing, and removed after the build — it
is never committed or exposed. Secret values are not printed in logs.

## Installation

1. Open the **Releases** page of this repository.
2. Download the APK matching your device:
   - Most modern phones: `LumaCalendar-arm64-v8a-release.apk`
   - Older ARM devices: `LumaCalendar-armeabi-v7a-release.apk`
   - x86_64 devices/emulators: `LumaCalendar-x86_64-release.apk`
   - If unsure: `LumaCalendar-universal-release.apk` (works everywhere, larger)
3. Install the APK on your device (you may need to allow installs from unknown
   sources).

> Releases are **development-stage builds** while the project is under active
> development. Expect bugs and changes between versions.

## Known Limitations

- Known issues may still exist.
- Some UI and device-specific behaviors are still being refined.
- Some features are still under development.
- Stability and compatibility testing are ongoing.

## Roadmap

- [x] Jalali, Gregorian, and Hijri calendar support
- [x] Event management with local storage
- [x] Event reminder notifications
- [x] Persistent daily calendar notification
- [x] Home screen widget
- [x] Dynamic launcher shortcut with current day
- [x] RTL/LTR handling per calendar type
- [x] ABI-split release builds and GitHub Actions CI/release workflows
- [ ] Broader device and stability testing
- [ ] Additional UI/UX refinement
- [ ] Expanded automated test coverage

## Contributing

Contributions are welcome, especially:

- Bug reports (please include device model and Android version)
- Testing on different Android devices and screen sizes
- UI/UX feedback
- Pull requests

Open an issue or pull request on the repository to get started.

## License

This project currently has **no explicit open-source license**. Until a license
is added, default copyright applies and reuse rights are not granted.

## Disclaimer

Luma Calendar is an actively developed project. Users should expect bugs,
changes, and incomplete functionality. Use it at your own discretion while
development continues.
