# Implementation Plan: Time Picker RTL/LTR Fix & UI Refinement

**Branch**: `004-fix-time-picker` | **Date**: 2026-09-17 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-fix-time-picker/spec.md`

## Summary

Fix the RTL layout bug in the Time Picker where hour/minute fields
are incorrectly positioned when Persian/Jalali is active, and refine
the picker's visual design for better spacing, alignment, focus states,
error states, and premium feel. The changes are confined to the
TimePickerDialog composable and related utility functions.

## Technical Context

**Language/Version**: Kotlin 2.2.10, Jetpack Compose

**Primary Dependencies**: Jetpack Compose (Material3), Compose
LayoutDirection, existing GlassComponents design system

**Storage**: SharedPreferences (NotificationPreferences for calendar type)

**Testing**: JUnit 4 + Robolectric (TimeValidator), manual visual
verification on device/emulator

**Target Platform**: Android 7.0+ (API 24), targetSdk 36

**Project Type**: Mobile app (single-activity Compose)

**Performance Goals**: Time Picker opens within 100ms; validation
feedback within 200ms of field losing focus

**Constraints**: Must use existing LayoutDirection system, not manual
string manipulation. Must preserve existing Liquid Glass design
language. Must not modify unrelated calendar, event, or notification
functionality.

**Scale/Scope**: Single composable modification + minor utility
updates. ~2-4 files affected.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Device-Time Authority | PASS | Time Picker uses device time for defaults |
| II. Calendar Conversion Correctness | PASS | No calendar conversion in Time Picker |
| III. Liquid Glass Design Language | PASS | Picker reuses GlassComponents tokens |
| IV. RTL-Aware Localization | PASS | FR-001/FR-002 enforce RTL via LayoutDirection |
| V. Offline-First Reliability | PASS | Time Picker is fully offline |
| VI. Single-ViewModel Simplicity | PASS | Time state managed in existing ViewModel |
| VII. Testable Date Logic | PASS | TimeValidator provides pure validation functions |

No constitution violations.

## Project Structure

### Documentation (this feature)

```text
specs/004-fix-time-picker/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (files to modify)

```text
app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/components/
├── TimePickerDialog.kt          # PRIMARY: RTL fix + visual refinement
└── GlassComponents.kt           # REUSE: glass surface tokens, focus states

app/src/main/java/com/aistudio/lumacalendar/vtxk/util/
└── TimeValidator.kt             # REUSE: time validation logic

app/src/main/java/com/aistudio/lumacalendar/vtxk/ui/viewmodel/
└── LumaViewModel.kt             # MAYBE: time picker state management

app/src/main/java/com/aistudio/lumacalendar/vtxk/util/
└── LocalizationManager.kt       # REUSE: RTL/LTR direction
```

**Structure Decision**: Primary change is TimePickerDialog.kt.
TimeValidator.kt is reused for validation. GlassComponents.kt provides
design tokens. LocalizationManager provides layout direction.

## Complexity Tracking

No constitution violations requiring justification.
