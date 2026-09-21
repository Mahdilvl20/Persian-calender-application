# Implementation Plan: Luma Notification Small Icon — Dynamic Persian Day Number

**Branch**: `008-notification-day-icon` | **Date**: 2026-09-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/008-notification-day-icon/spec.md`

## Summary

Replace the bell (`ic_notification_luma`) small icon on the persistent Daily Notification (ID 1001, channel `luma_calendar_daily`) with a compact Luma-branded calendar icon carrying the current Jalali day number. The day number is derived from the real device-local date via the existing device-time authority (`DateUtils.getRealDeviceLocalDate()` → `CalendarConverter.gregorianToJalali()`), formatted through `CalendarConverter.toPersianDigits()` when the active calendar type is JALALI (Latin digits otherwise), and rendered by a dedicated small-icon renderer at Android small-icon scale (24dp, monochrome-safe white-on-transparent) so it remains a valid small icon under system tinting. The icon refreshes on the existing lifecycle (midnight alarm, `ReminderRescheduleReceiver` for boot/time/timezone/package-update, and every Daily Notification refresh) by re-posting notification 1001 with the new icon.

## Technical Context

**Language/Version**: Kotlin 2.0.21, Android Gradle Plugin 8.7.3, compileSdk 35, minSdk 24 (per project constitution technical constraints)

**Primary Dependencies**: Jetpack Compose BOM (UI), AndroidX Core (NotificationCompat), AndroidX AppCompat, Room (existing, untouched), Retrofit+Moshi (existing, untouched), Firebase AI (existing, untouched). **No new dependencies** (constitution: new third-party dependencies require justification; none needed).

**Storage**: N/A — feature adds no persistence; reads only existing `NotificationPreferences` and the device-local date. (Event lookup path in `LumaNotificationManager.updateNotification()` is untouched.)

**Testing**: JUnit 4 + Robolectric (`app/src/test/`), Roborazzi for screenshot capture where applicable. Existing suites: `LumaNotificationTest`, `CalendarAndHolidayTest`.

**Target Platform**: Android 7.0 (API 24) — Android 15 (API 35); phone form factors.

**Project Type**: Mobile app (single-module `app/`, single Activity, MVVM)

**Performance Goals**: Notification refresh path performs one small bitmap render (24–48dp-equivalent pixels) per refresh; no perceptible change to notification post latency versus the current implementation.

**Constraints**:
- Notification small icon MUST be a drawable that renders correctly when tinted by the system (white-on-transparent monochrome-safe); not a full-color launcher icon.
- Notification identity unchanged: ID 1001, channel `luma_calendar_daily`, single post per refresh (no duplicate notifications).
- No launcher alias reuse/creation; no MainActivity relaunch; no UI code outside the notification small icon.
- Day number MUST come from `DateUtils.getRealDeviceLocalDate()` (device-time authority) — never UTC, selected/viewed date, event date, or ±1 day workarounds.
- Persian digits via `CalendarConverter.toPersianDigits()`; no hardcoded per-digit strings.
- Offline-capable (Principle V): icon logic uses no network.

**Scale/Scope**: 1 notification surface (Daily Notification), 1 dedicated small-icon artifact + small-icon rendering support, touched files limited to: `LumaNotificationManager.kt`, `LumaNotificationIconGenerator.kt` (small-icon rendering support), `ic_notification_luma.xml` (replaced), plus tests. Event reminders untouched (FR-015).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principle | Status | Evidence / Plan |
|---|-----------|--------|-----------------|
| I | Device-Time Authority (NON-NEGOTIABLE) | ✅ PASS | Day number from `DateUtils.getRealDeviceLocalDate()` (app/src/main/java/com/aistudio/lumacalendar/vtxk/util/DateUtils.kt:51) → `CalendarConverter.gregorianToJalali()`; no UTC, no bare `Calendar.getInstance()` for user-facing date logic; no ±1 day workarounds (FR-003, FR-016). |
| II | Calendar Conversion Correctness (NON-NEGOTIABLE) | ✅ PASS | Day number conversion goes through `CalendarConverter.gregorianToJalali()` (JDN pipeline) from the ISO device-local date; no ad-hoc date arithmetic anywhere in the feature. |
| III | Liquid Glass Design Language | ✅ PASS (not applicable to status-bar icon) | The RemoteViews body (glass tile, actions) is untouched; the small icon adopts the Luma brand silhouette without introducing new translucency tokens. |
| IV | RTL-Aware Localization | ✅ PASS | Day digits produced by `CalendarConverter.toPersianDigits()` under JALALI localization, Latin digits under GREGORIAN (FR-007); no hardcoded user-facing strings added (digit rendering, not strings). |
| V | Offline-First Reliability | ✅ PASS | Icon derivation uses device time + `CalendarConverter` only; no network on the path. |
| VI | Single-ViewModel Simplicity | ✅ PASS | No ViewModel changes; `LumaNotificationManager` (existing notification object) is the touch point. |
| VII | Testable Date Logic | ✅ PASS | Day-number derivation (device-local date → Jalali day → localized digits) is a pure function of its inputs; unit tests planned in `app/src/test/` (Robolectric for bitmap/notification assertions). |

**Gate result**: PASS — no violations to justify in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/008-notification-day-icon/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/
├── notification/
│   ├── LumaNotificationManager.kt        # MODIFIED: build day-number text, render small-icon bitmap, setSmallIcon(IconCompat.createWithBitmap(...)), post 1001 (unchanged ID)
│   ├── LumaNotificationIconGenerator.kt  # MODIFIED: add generateSmallIcon() — monochrome alpha-mask (white-on-transparent) day-number renderer
│   └── LumaNotificationTest.kt (test)    # EXTENDED: day-number derivation + small-icon validity tests
```

**Structure Decision**: Single-module Android app; the feature touches only
the existing notification package. **`ic_notification_luma.xml` is NOT
replaced or deleted** — the grep in research.md shows event reminders
(`EventNotifications.kt:300`) still reference it, and FR-015 requires them
to stay untouched. The daily notification switches to a runtime
`IconCompat.createWithBitmap(...)` small icon instead. No new modules,
packages, or activities (FR-011).

> **Note**: Android renders notification small icons as alpha masks and
> tints them. The generated small-icon bitmap MUST be white-on-transparent
> (monochrome), NOT the full-color glass gradient used by the in-body icon
> — see research.md Decision 1.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*Empty — no constitution violations.*
