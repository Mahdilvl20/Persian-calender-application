# Implementation Plan: Daily Notification Unified Surface

**Branch**: `006-notification-unified-surface` | **Date**: 2026-09-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-notification-unified-surface/spec.md`

## Summary

Investigate and fix the remaining "rectangle inside rectangle"
appearance in the daily notification. The previous fix (002) removed
`notification_glass_bg` from root layouts, but the Android system
notification container from `DecoratedCustomViewStyle` still adds
visible padding and surface around the custom RemoteViews. The
investigation-first approach determines exactly what Android renders
vs. what the app renders, then redesigns accordingly.

## Technical Context

**Language/Version**: Kotlin 2.2.10, XML RemoteViews

**Primary Dependencies**: Android NotificationCompat, RemoteViews,
DecoratedCustomViewStyle

**Storage**: Room (CalendarEvent), SharedPreferences

**Testing**: Manual visual verification on device/emulator

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose, notification
is XML RemoteViews)

**Performance Goals**: Notification update within 2 seconds; no
main thread blocking

**Constraints**: Must work within Android's notification template
system. Cannot remove system header or system container. Must
preserve ID 1001, ongoing, persistent behavior.

**Scale/Scope**: ~3-5 files affected: 2 XML layouts, 1 Kotlin
manager, 1-2 drawable resources

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | Date rendering reuses DateUtils |
| II. Calendar Conversion Correctness | PASS | JDN pathway preserved |
| III. Liquid Glass Design Language | PASS | Design tokens reused |
| IV. RTL-Aware Localization | PASS | FR-011 enforces via LocalizationManager |
| V. Offline-First Reliability | PASS | Notification is fully offline |
| VI. Single-ViewModel Simplicity | PASS | No ViewModel changes |
| VII. Testable Date Logic | PASS | Date logic unchanged |

No constitution violations.

## Project Structure

### Documentation (this feature)

```text
specs/006-notification-unified-surface/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to investigate and modify)

```text
app/src/main/res/layout/
├── notification_luma_calendar.xml          # PRIMARY: collapsed layout
└── notification_luma_calendar_expanded.xml # PRIMARY: expanded layout

app/src/main/res/drawable/
├── notification_glass_bg.xml              # INVESTIGATE: still exists?
├── notification_calendar_tile_bg.xml      # SIMPLIFY: tile background
├── notification_action_btn_bg.xml         # SIMPLIFY: action backgrounds
└── notification_action_btn_primary_bg.xml # SIMPLIFY: action backgrounds

app/src/main/java/com/aistudio/lumacalendar/vtxk/notification/
├── LumaNotificationManager.kt            # INVESTIGATE: builder config
└── LumaNotificationIconGenerator.kt      # REUSE: icon generation

app/src/main/java/com/aistudio/lumacalendar/vtxk/util/
├── DateUtils.kt                          # REUSE: device time
├── CalendarConverter.kt                  # REUSE: JDN conversions
└── LocalizationManager.kt                # REUSE: RTL/LTR
```

**Structure Decision**: The investigation phase determines which
files actually need changes. The primary targets are the 2 XML
layouts and the builder configuration in LumaNotificationManager.

## Complexity Tracking

No constitution violations requiring justification.
