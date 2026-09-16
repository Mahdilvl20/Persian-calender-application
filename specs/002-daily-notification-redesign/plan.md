# Implementation Plan: Daily Notification Redesign

**Branch**: `002-daily-notification-redesign` | **Date**: 2026-09-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-daily-notification-redesign/spec.md`

## Summary

Redesign the daily persistent notification to eliminate the "rectangle
inside rectangle" visual problem. The current notification uses an inner
RemoteViews card inside the Android notification container, creating a
nested appearance. The goal is one unified premium notification surface
using the existing Liquid Glass design language, with integrated date
display, mini calendar tile, daily message, and action buttons. The
snooze duration becomes user-configurable in Settings.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Java 11

**Primary Dependencies**: Android RemoteViews (XML layouts), Jetpack
Compose (app UI only, NOT notification), Room 2.7.0, Vazirmatn font

**Storage**: Room database (CalendarEvent), SharedPreferences
(NotificationPreferences), shared prefs for new snooze duration setting

**Testing**: JUnit 4 + Robolectric, Roborazzi (screenshot golden)

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose)

**Performance Goals**: Notification update within 2 seconds; no main
thread blocking; lightweight RemoteViews rendering

**Constraints**: RemoteViews supports only a subset of Android views
(no Compose, no custom drawing, limited view types). All notification
UI must be XML-based RemoteViews compatible.

**Scale/Scope**: Single notification redesign — affects ~4 source files
and 2-3 XML layout files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | FR-005 enforces device time for dates |
| II. Calendar Conversion Correctness | PASS | FR-006 enforces JDN pathway |
| III. Liquid Glass Design Language | PASS | FR-015 enforces design tokens |
| IV. RTL-Aware Localization | PASS | FR-013 enforces RTL/LTR via LocalizationManager |
| V. Offline-First Reliability | PASS | Notification is fully offline |
| VI. Single-ViewModel Simplicity | PASS | No new ViewModels; snooze pref added to existing |
| VII. Testable Date Logic | PASS | Date rendering reuses existing pure functions |

No constitution violations. All requirements align with existing principles.

## Project Structure

### Documentation (this feature)

```text
specs/002-daily-notification-redesign/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/
├── notification/
│   ├── LumaNotificationManager.kt      # Build unified notification, add snooze
│   ├── LumaNotificationIconGenerator.kt # Existing icon generation (reuse)
│   └── EventNotifications.kt           # NOT modified (event reminders untouched)
├── ui/screens/
│   └── SettingsScreen.kt               # Add snooze duration setting row
├── ui/viewmodel/
│   └── LumaViewModel.kt                # Add snooze duration state, read/write pref
├── notification/
│   └── NotificationPreferences.kt      # Add snooze duration preference

app/src/main/res/layout/
├── notification_luma_calendar.xml          # REDESIGN: collapsed layout
├── notification_luma_calendar_expanded.xml # REDESIGN: expanded layout
└── notification_action_button.xml          # NEW: reusable action button item

app/src/main/res/drawable/
├── (existing glass backgrounds - reuse)
└── (may need notification-specific shapes)

app/src/main/res/values/
└── strings.xml                            # Add snooze duration labels
```

**Structure Decision**: Android single-module. Notification layouts are
XML RemoteViews (not Compose). Kotlin files modify existing notification
and settings infrastructure. No new files beyond the action button
layout and snooze duration preference.

## Complexity Tracking

No constitution violations requiring justification.
