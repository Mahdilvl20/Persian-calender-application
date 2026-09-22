# Implementation Plan: Fix UI Audit Issues

**Branch**: `009-fix-ui-audit-issues` | **Date**: 2026-09-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/009-fix-ui-audit-issues/spec.md`

## Summary

Fix six confirmed UI defects: (1) wire category-visibility toggles into a
reactive filtered events flow feeding the calendar views and persist them;
(2) persist accent/first-day/week-numbers/theme across restarts; (3) resolve the
unused Persian loading parameter; (4) add confirmation dialogs to the two
destructive Settings actions; (5) block blank/whitespace event titles with
localized validation in Add and Edit; (6) align the snooze description with the
selectable options. All persistence reuses the existing
`NotificationPreferences` SharedPreferences singleton. No new persistence
mechanism, no notification/CI/signing/ABI changes.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00), Material 3

**Primary Dependencies**: Compose, Room 2.7.0 (KSP), existing
`NotificationPreferences` (SharedPreferences, file `notification_preferences`)

**Storage**: SharedPreferences via `NotificationPreferences` — existing keys
(calendar type, snooze) plus new keys for appearance + category visibility.
Room `CalendarEvent` unchanged (schema untouched).

**Testing**: JUnit 4 + Robolectric (`app/src/test/`)

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose, single `LumaViewModel`)

**Performance Goals**: Category filtering reactive via existing StateFlow
`combine`; no measurable UI latency change.

**Constraints**: Preserve RTL/LTR, Jalali/Gregorian/Hijri behavior, device-local
date/timezone, Today-vs-selected distinction; no second persistence mechanism;
notification architecture, signing, CI, ABI, applicationId unchanged.

**Scale/Scope**: ~5 files touched: `EventNotifications.kt`
(`NotificationPreferences` new keys/getters/setters), `LumaViewModel.kt`
(read prefs in init, persist in setters, add filtered events flow),
`MainActivity.kt` (pass filtered events + visibility to CalendarScreen wiring),
`SettingsScreen.kt` (confirmation dialogs, snooze description),
`AddEditEventSheet.kt` (title validation) + AppStrings for new labels + tests.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | No date-source changes; Today vs selected preserved (FR-013) |
| II. Calendar Conversion Correctness | PASS | No conversion changes |
| III. Liquid Glass Design Language | PASS | Dialogs reuse existing glass pattern (FR-008) |
| IV. RTL-Aware Localization | PASS | Validation feedback + dialog labels via AppStrings (FR-009, VB-002) |
| V. Offline-First Reliability | PASS | All changes local (SharedPreferences + Room) |
| VI. Single-ViewModel Simplicity | PASS | All state stays in LumaViewModel; no new ViewModel |
| VII. Testable Date Logic | PASS | New tests for filtering, persistence, validation (TR-001..004) |

No violations.

## Project Structure

### Documentation (this feature)

```text
specs/009-fix-ui-audit-issues/
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
│   └── EventNotifications.kt          # NotificationPreferences: add keys +
│                                      #   get/set for accentColorIndex,
│                                      #   firstDayMonday, showWeekNumbers,
│                                      #   themeName, category visibility x3
├── ui/viewmodel/
│   └── LumaViewModel.kt               # init reads prefs; setters persist;
│                                      #   new visibleEvents filtered flow
├── MainActivity.kt                    # pass visibleEvents (not allEvents) +
│                                      #   visibility flags to CalendarScreen
├── ui/screens/
│   └── SettingsScreen.kt              # confirm dialogs; snooze description
├── ui/components/
│   └── AddEditEventSheet.kt           # blank-title validation
└── util/
    └── Localization.kt                # AppStrings: validation + dialog labels

app/src/test/java/com/aistudio/lumacalendar/vtxk/
└── (new/updated tests: category filtering, prefs persistence, title validation)
```

**Structure Decision**: Single-module app. Category filtering is a new
`combine` flow in `LumaViewModel` over `allEvents` + the three visibility flags,
exposed as `visibleEvents` and passed to `CalendarScreen` in place of the raw
`allEvents`. Persistence extends the existing `NotificationPreferences`
singleton — the established pattern (getSnoozeMinutes/setSnoozeMinutes). Search
continues to use the unfiltered `allEvents` (FR-002a).

## Complexity Tracking

No constitution violations requiring justification.
