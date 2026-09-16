# Implementation Plan: Widget Visual Refinement

**Branch**: `003-widget-visual-refinement` | **Date**: 2026-09-16 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-widget-visual-refinement/spec.md`

## Summary

Refine the home screen widget's visual layout to improve spacing,
hierarchy, and typography. The widget functionality is already working.
Changes are limited to the widget XML layout (spacing, text sizes,
font weights) and potentially minor Kotlin adjustments if the
WidgetProvider needs to populate restructured view IDs.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Java 11

**Primary Dependencies**: Android RemoteViews (widget XML),
AppWidgetProvider

**Storage**: SharedPreferences (NotificationPreferences for calendar type)

**Testing**: Manual visual verification on device/emulator

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose, widget is XML)

**Performance Goals**: Widget update within 1 second of date change;
lightweight RemoteViews rendering

**Constraints**: RemoteViews supports only a subset of Android views.
All widget UI must be XML-based. Widget sizes vary by user placement
(home screen grid).

**Scale/Scope**: Single XML layout modification, minor Kotlin
adjustments. ~1-3 files affected.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | Widget uses DateUtils.getRealDeviceLocalDate() |
| II. Calendar Conversion Correctness | PASS | Widget reuses CalendarConverter JDN pathway |
| III. Liquid Glass Design Language | PASS | Widget preserves existing dark surface style |
| IV. RTL-Aware Localization | PASS | Persian month name uses RTL via LocalizationManager |
| V. Offline-First Reliability | PASS | Widget is fully offline |
| VI. Single-ViewModel Simplicity | PASS | No ViewModel changes; widget uses WidgetProvider |
| VII. Testable Date Logic | PASS | Date rendering reuses existing pure functions |

No constitution violations.

## Project Structure

### Documentation (this feature)

```text
specs/003-widget-visual-refinement/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/res/layout/
└── widget_luma_calendar.xml           # PRIMARY: spacing, text sizes, weights

app/src/main/res/xml/
└── luma_calendar_widget_info.xml      # MAYBE: widget size constraints

app/src/main/java/com/aistudio/lumacalendar/vtxk/widget/
└── LumaCalendarWidgetProvider.kt      # MAYBE: date population for new layout

app/src/main/java/com/aistudio/lumacalendar/vtxk/util/
├── DateUtils.kt                       # REUSE: device time functions
├── CalendarConverter.kt               # REUSE: JDN conversions
└── LocalizationManager.kt             # REUSE: RTL + month names
```

**Structure Decision**: Primary change is the widget XML layout.
Kotlin changes are minimal (only if WidgetProvider needs to populate
new or restructured view IDs).

## Complexity Tracking

No constitution violations requiring justification.
