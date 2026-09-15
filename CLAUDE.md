# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Luma Calendar is a Persian (Jalali) and Gregorian calendar Android app built with Kotlin and Jetpack Compose. It features dynamic home screen icons showing the current day, Firebase AI integration, and localized UI for Persian/English users.

## Build & Run Commands

```bash
# Build debug APK (requires Gradle wrapper - generate with: gradle wrapper)
./gradlew assembleDebug

# Build release APK (requires signing config in .env or environment variables)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run a single test class
./gradlew testDebugUnitTest --tests "com.aistudio.lumacalendar.vtxk.CalendarAndHolidayTest"

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Generate dynamic day icons (Python script)
python scripts/generate_day_icons.py
```

## Environment Setup

1. Copy `.env.example` to `.env` and configure secrets
2. Required: `GEMINI_API_KEY` for Firebase AI features (uncomment in .env)
3. For release builds: set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` environment variables
4. Place `google-services.json` in `app/` directory for Firebase

## Architecture

### Core Structure
- **Single Activity**: `MainActivity` hosts all Compose UI via `LumaApp` composable
- **MVVM Pattern**: `LumaViewModel` (single ViewModel) manages all app state
- **Room Database**: `LumaDatabase` with `EventDao` for local event storage
- **Repository Pattern**: `EventRepository` for data access abstraction

### Key Packages
```
com.aistudio.lumacalendar.vtxk/
├── data/              # Database, API clients, repositories
│   ├── api/          # Persian calendar API (Retrofit)
│   ├── holiday/      # Holiday providers (Iran, US Federal)
│   ├── model/        # Data classes (PersianCalendarDay)
│   └── repository/   # PersianCalendarRepository
├── notification/     # Notification system
│   ├── EventNotificationScheduler    # Per-event reminders
│   └── LumaNotificationManager      # Daily calendar notification
├── ui/
│   ├── components/   # Reusable composables (GlassComponents, sheets)
│   ├── screens/      # Calendar, Search, Settings screens
│   ├── theme/        # Material3 theme, colors, accent presets
│   └── viewmodel/    # LumaViewModel + state classes
├── util/             # CalendarConverter, DateUtils, LocalizationManager
└── widget/           # Home screen calendar widget
```

### State Management
- All state flows through `LumaViewModel` using Kotlin `StateFlow`
- Calendar state: `selectedYear`, `selectedMonth`, `selectedDate`
- UI state: `currentTab`, `calendarViewMode`, `calendarType` (Jalali/Gregorian)
- Event state: `allEvents`, `selectedDateEvents`, search results

### Calendar System
- **CalendarConverter**: Bidirectional conversion between Gregorian/Jalali/Hijri via JDN (Julian Day Number)
- **CalendarType enum**: `JALALI`, `GREGORIAN`, `HIJRI` - controls date display
- **LocalizationManager**: Provides RTL layout direction and localized strings per calendar type

### Notification Architecture
- **EventNotificationScheduler**: Individual event reminders with exact alarms
- **LumaNotificationManager**: Persistent daily notification showing today's calendar
- **MidnightUpdateReceiver**: Refreshes notification at midnight
- **ReminderRescheduleReceiver**: Reschedules alarms on boot/time changes

### Dynamic Icons
- 31 launcher icon variants (`ic_launcher_fg_day_1.xml` through `ic_launcher_fg_day_31.xml`)
- `DynamicIconManager` swaps icons based on current day of month
- Icon generation script: `scripts/generate_day_icons.py`

## Dependencies

- **Compose BOM**: UI toolkit (Material3, Navigation not currently used)
- **Room**: Local database with KSP annotation processing
- **Retrofit + Moshi**: API client for Persian calendar data
- **Firebase AI**: Gemini integration for AI features
- **Firebase App Check**: Security with reCAPTCHA/debug providers
- **Robolectric + Roborazzi**: Unit testing with screenshot capture

## Testing

- Unit tests in `app/src/test/` using JUnit 4 + Robolectric
- Screenshot tests with Roborazzi (captureGolden assertions)
- No instrumented tests currently (androidTest directory minimal)

## Common Patterns

- **Liquid Glass UI**: Custom `GlassComponents.kt` for translucent glass-morphism effects
- **Accent System**: `AccentPresets` list with primary/secondary color pairs
- **Category System**: Predefined categories (Work, Personal, Health, etc.) with colors
- **Date Formatting**: Persian digits via `CalendarConverter.toPersianDigits()`

## Notes

- Gradle wrapper (`gradlew`/`gradlew.bat`) not in repo - generate with `gradle wrapper`
- ProGuard disabled (`isMinifyEnabled = false`) for release builds
- KSP used for Room compiler and Moshi codegen
- Configuration cache and parallel builds enabled in `gradle.properties`
