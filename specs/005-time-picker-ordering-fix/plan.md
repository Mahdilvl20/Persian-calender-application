# Implementation Plan: Time Picker Hour/Minute Ordering Fix

**Branch**: `005-time-picker-ordering-fix` | **Date**: 2026-09-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-time-picker-ordering-fix/spec.md`

## Summary

Fix the Time Picker so that Hour → Separator → Minute ordering is
always stable from left to right, regardless of the surrounding RTL
layout direction. The fix isolates the time-control Row by wrapping it
in `CompositionLocalProvider(LocalLayoutDirection provides
LayoutDirection.Ltr)`, forcing LTR on that specific section while the
rest of the dialog retains the calendar-type-driven direction.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Jetpack Compose

**Primary Dependencies**: Jetpack Compose (Material3), Compose
LayoutDirection, existing GlassComponents design system

**Storage**: SharedPreferences (NotificationPreferences for calendar type)

**Testing**: Manual visual verification on device/emulator

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose)

**Performance Goals**: No performance impact — layout direction is a
Compose rendering property, not a calculation

**Constraints**: Only the time-control Row should be forced to LTR.
The rest of the dialog (header, labels, buttons) must remain RTL in
Persian mode. No string reversal or manual RTL hacks.

**Scale/Scope**: Single file modification (LiquidGlassTimePickerDialog.kt)
plus one caller update (AddEditEventSheet.kt). ~10 lines changed.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | No date logic changes |
| II. Calendar Conversion Correctness | PASS | No conversion changes |
| III. Liquid Glass Design Language | PASS | No visual changes to surfaces |
| IV. RTL-Aware Localization | PASS | FR-002 enforces isolation via LayoutDirection |
| V. Offline-First Reliability | PASS | Fully offline |
| VI. Single-ViewModel Simplicity | PASS | No ViewModel changes |
| VII. Testable Date Logic | PASS | No date logic changes |

No constitution violations.

## Project Structure

### Documentation (this feature)

```text
specs/005-time-picker-ordering-fix/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/
└── LiquidGlassTimePickerDialog.kt   # PRIMARY: wrap time Row in LTR

app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/
└── AddEditEventSheet.kt             # MAYBE: update caller parameter
```

**Structure Decision**: Single-file surgical fix. The time-control
Row gets a `CompositionLocalProvider` wrapper forcing
`LayoutDirection.Ltr`. No other files need changes for the core fix.

## Complexity Tracking

No constitution violations requiring justification.
